package io.tcds.orm.migrations

import fixtures.freezeClock
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.logging.Logger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreateMigrationTest {
    private val writer: CreateMigration.Writer = mockk(relaxed = true)
    private val logger: Logger = mockk(relaxed = true)

    @Test
    fun `given the properties when directory is missing then throw an exception`() = freezeClock {
        val props = emptyMap<String, Any>()

        val exception = assertThrows<Exception> { CreateMigration(writer, logger, props) }

        assertEquals("Missing `migrations.directory` property", exception.message)
    }

    @Test
    fun `given the properties when migration is missing then throw an exception`() = freezeClock {
        val props = mapOf<String, Any>("migrations.directory" to "migration/folder")
        val creator = CreateMigration(writer, logger, props)

        val exception = assertThrows<Exception> { creator.run() }

        assertEquals("Missing migration name. Please run the command with `-P migration={name}` argument", exception.message)
    }

    @Test
    fun `given migration directory then run migration`() = freezeClock {
        val props = mapOf<String, Any>(
            "migrations.directory" to "migration/folder",
            "migration" to "FooBar",
        )
        val creator = CreateMigration(writer, logger, props)

        creator.run()

        verify(exactly = 1) {
            writer.write(
                "migration/folder",
                "2022_12_18_054852_foo_bar.sql",
                "# CREATE TABLE ...",
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
        val creator = CreateMigration(writer, logger, props)

        val exception = assertThrows<Exception> { creator.run() }

        assertEquals("Missing migration directory. Please run the command with `-P dir={dir}` with one of `user,sales`", exception.message)
    }

    @Test
    fun `given named migration directories when dir property matches one name then run migration`() = freezeClock {
        val props = mapOf<String, Any>(
            "migrations.directory[user]" to "user/migration/folder",
            "migrations.directory[sales]" to "sales/migration/folder",
            "migration" to "FooBar",
            "dir" to "sales",
        )
        val creator = CreateMigration(writer, logger, props)

        creator.run()

        verify(exactly = 1) {
            writer.write(
                "sales/migration/folder",
                "2022_12_18_054852_foo_bar.sql",
                "# CREATE TABLE ...",
            )
        }
        verify(exactly = 1) { logger.lifecycle("Migration `sales/migration/folder/2022_12_18_054852_foo_bar.sql` created.") }
    }
}
