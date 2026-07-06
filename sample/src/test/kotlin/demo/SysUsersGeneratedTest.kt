package demo

import kotlin.test.Test
import kotlin.test.assertEquals

class SysUsersGeneratedTest {
    @Test
    fun generatedTableInitializes() {
        assertEquals("sys_user", SysUsers.tableName)
        assertEquals("id", SysUsers.id.name)
    }
}
