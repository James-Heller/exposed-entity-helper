package pers.jamestang.exposed.entity.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import pers.jamestang.exposed.entity.Column
import pers.jamestang.exposed.entity.Entity
import java.io.OutputStreamWriter

internal class ExposedEntityProcessor(
    environment: SymbolProcessorEnvironment,
) : SymbolProcessor {
    private val codeGenerator: CodeGenerator = environment.codeGenerator
    private val logger: KSPLogger = environment.logger
    private val enhanceTableType = environment.options["exposedEntityHelper.enhanceTableClass"]
        ?: "pers.jamestang.exposed.entity.EnhanceTable"
    private val processedClasses = mutableSetOf<String>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Entity::class.qualifiedName.orEmpty()).toList()
        val invalid = symbols.filterNot { it.validate() }.toList()

        symbols
            .filter { it.validate() }
            .filterIsInstance<KSClassDeclaration>()
            .forEach(::generateTable)

        return invalid
    }

    private fun generateTable(entityClass: KSClassDeclaration) {
        val packageName = entityClass.packageName.asString()
        val className = entityClass.simpleName.asString()
        val qualifiedName = entityClass.qualifiedName?.asString() ?: className

        if (!processedClasses.add(qualifiedName)) {
            return
        }

        val entity = entityClass.annotation(Entity::class.qualifiedName.orEmpty())!!
        val tableName = entity.stringArgument("tableName").ifBlank { className.toSnakeCase() }
        val objectName = entity.stringArgument("objectName").ifBlank { "${className}s" }
        val constructor = entityClass.primaryConstructor

        if (constructor == null) {
            logger.error("@Entity can only be applied to classes with a primary constructor.", entityClass)
            return
        }

        val propertyAnnotations = entityClass.getAllProperties()
            .associateBy { it.simpleName.asString() }
            .mapValues { (_, property) -> property.annotation(Column::class.qualifiedName.orEmpty()) }

        val parameters = constructor.parameters
            .filter { it.name != null }
            .map { parameter ->
                val name = parameter.name!!.asString()
                EntityProperty(
                    name = name,
                    columnName = propertyAnnotations[name].stringArgument("name").ifBlank { name.toSnakeCase() },
                    type = parameter.type.resolve().declaration.qualifiedName?.asString().orEmpty(),
                    nullable = parameter.type.resolve().isMarkedNullable,
                    column = propertyAnnotations[name] ?: parameter.annotation(Column::class.qualifiedName.orEmpty()),
                )
            }

        val ignoredNames = setOf("id", "createTime", "updateTime", "deleteTime", "deleted")
        val columnProperties = parameters
            .filterNot { it.name in ignoredNames }
            .filterNot { it.column.booleanArgument("ignore") }

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = false, entityClass.containingFile!!),
            packageName = packageName,
            fileName = "${objectName}Generated",
            extensionName = "kt",
        )

        OutputStreamWriter(file, Charsets.UTF_8).use { writer ->
            writer.write(buildString {
                appendLine("package $packageName")
                appendLine()
                appendLine("import org.jetbrains.exposed.v1.core.ResultRow")
                appendLine("import org.jetbrains.exposed.v1.r2dbc.insertAndGetId")
                appendLine()
                appendLine("public object $objectName : $enhanceTableType<$className>(\"$tableName\") {")
                columnProperties.forEach { property ->
                    appendLine("    public val ${property.name} = ${property.columnExpression()}")
                }
                appendLine()
                appendLine("    public suspend fun insert(entity: $className): Int {")
                appendLine("        return insertAndGetId { statement ->")
                parameters
                    .filterNot { it.name == "id" || it.column.booleanArgument("ignore") }
                    .forEach { property ->
                        appendLine("            statement[${property.name}] = entity.${property.name}")
                    }
                appendLine("        }.value")
                appendLine("    }")
                appendLine()
                appendLine("    public suspend fun update(entity: $className): Int {")
                appendLine("        val entityId = requireNotNull(entity.id) { \"Cannot update $className without id.\" }")
                appendLine("        return updateById(entityId) { statement ->")
                parameters
                    .filterNot { it.name == "id" || it.name == "createTime" || it.column.booleanArgument("ignore") }
                    .forEach { property ->
                        appendLine("            statement[${property.name}] = entity.${property.name}")
                    }
                appendLine("        }")
                appendLine("    }")
                appendLine()
                appendLine("    override suspend fun toEntity(row: ResultRow): $className {")
                appendLine("        return $className(")
                parameters.forEach { property ->
                    appendLine("            ${property.name} = ${property.rowExpression()},")
                }
                appendLine("        )")
                appendLine("    }")
                appendLine("}")
            })
        }
    }

    private fun EntityProperty.columnExpression(): String {
        val base = when (type) {
            "kotlin.String" -> "varchar(\"$columnName\", ${column.intArgument("length", 255)})"
            "kotlin.Int" -> "integer(\"$columnName\")"
            "kotlin.Long" -> "long(\"$columnName\")"
            "kotlin.Boolean" -> "bool(\"$columnName\")"
            "kotlin.Double" -> "double(\"$columnName\")"
            "kotlin.Float" -> "float(\"$columnName\")"
            else -> {
                logger.error("Unsupported column type '$type' for property '$name'. Add @Column(ignore = true) or extend the processor.")
                "text(\"$columnName\")"
            }
        }

        return buildString {
            append(base)
            if (nullable) append(".nullable()")
            if (column.booleanArgument("unique")) append(".uniqueIndex()")
            if (column.booleanArgument("index")) append(".index()")
        }
    }

    private fun EntityProperty.rowExpression(): String {
        return if (name == "id") {
            "row[id].value"
        } else {
            "row[$name]"
        }
    }

    private fun KSAnnotated.annotation(qualifiedName: String): KSAnnotation? {
        return annotations.firstOrNull { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }
    }

    private fun KSAnnotation?.stringArgument(name: String): String {
        return this?.arguments?.firstOrNull { it.name?.asString() == name }?.value as? String ?: ""
    }

    private fun KSAnnotation?.intArgument(name: String, defaultValue: Int): Int {
        return this?.arguments?.firstOrNull { it.name?.asString() == name }?.value as? Int ?: defaultValue
    }

    private fun KSAnnotation?.booleanArgument(name: String): Boolean {
        return this?.arguments?.firstOrNull { it.name?.asString() == name }?.value as? Boolean ?: false
    }

    private fun String.toSnakeCase(): String {
        return replace(Regex("([a-z0-9])([A-Z])"), "$1_$2").lowercase()
    }
}

private data class EntityProperty(
    val name: String,
    val columnName: String,
    val type: String,
    val nullable: Boolean,
    val column: KSAnnotation?,
)
