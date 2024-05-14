package io.tcds.orm.migrations

import fixtures.freezeClock
import io.mockk.*
import org.gradle.api.logging.Logger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MigrationCreatorTest {
    private val writer: MigrationCreator.Writer = mockk(relaxed = true)
    private val logger: Logger = mockk(relaxed = true)
    private val log: (String) -> Unit = { message -> logger.lifecycle(message) }

    private val migration = """
        package migrations

        import io.tcds.orm.migrations.Migration

        @Suppress("FunctionName")
        fun migration_2022_12_18_054810_foo_bar() = Migration(
            up = ""${'"'}
                # CREATE TABLE ...
            ""${'"'}.trimIndent(),
            down = ""${'"'}
                # DROP TABLE ...
            ""${'"'}.trimIndent(),
        )
    """.trimIndent()

    @Test
    fun `given the properties when directory is missing then throw an exception`() = freezeClock {
        val props = emptyMap<String, Any>()

        val exception = assertThrows<Exception> { MigrationCreator(writer).run(props, log) }

        assertEquals("Missing `migrations.directory` property", exception.message)
    }

    @Test
    fun `given the properties when migration is missing then throw an exception`() = freezeClock {
        val props = mapOf<String, Any>("migrations.directory" to "migration/folder")
        val creator = MigrationCreator(writer)

        val exception = assertThrows<Exception> { creator.run(props, log) }

        assertEquals("Missing migration name. Please run the command with `-P migration={name}` argument", exception.message)
    }

    @Test
    fun `given migration directory then run migration`() = freezeClock {
        val props = mapOf<String, Any>(
            "migrations.directory" to "migration/folder",
            "migration" to "FooBar",
        )
        val creator = MigrationCreator(writer)

        creator.run(props, log)

        verify(exactly = 1) {
            writer.write(
                "migration/folder",
                "migration_2022_12_18_054810_foo_bar.kt",
                migration
            )
        }
    }

    @Test
    fun `given named migration directories when dir property is missing then throw exception`() = freezeClock {
        val props = mapOf<String, Any>(
            "migrations.directory[user]" to "user/migration/folder",
            "migrations.directory[sales]" to "sales/migration/folder",
            "migration" to "FooBar",
        )
        val creator = MigrationCreator(writer)

        val exception = assertThrows<Exception> { creator.run(props, log) }

        assertEquals(
            "Missing migration directory. Please run the command with `-P module={module}` with one of `user,sales`",
            exception.message,
        )
    }

    @Test
    fun `given named migration directories when dir property matches one name then run migration`() = freezeClock {
        val props = mapOf<String, Any>(
            "migrations.directory[user]" to "user/migration/folder",
            "migrations.directory[sales]" to "sales/migration/folder",
            "migration" to "FooBar",
            "module" to "sales",
        )
        val creator = MigrationCreator(writer)

        creator.run(props, log)

        verify(exactly = 1) {
            writer.write(
                "sales/migration/folder",
                "migration_2022_12_18_054810_foo_bar.kt",
                migration
            )
        }
        verify(exactly = 1) { logger.lifecycle("Migration `sales/migration/folder/migration_2022_12_18_054810_foo_bar.kt` created.") }
    }
}
