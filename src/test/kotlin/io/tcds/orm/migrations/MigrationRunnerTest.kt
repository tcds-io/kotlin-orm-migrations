package io.tcds.orm.migrations

import fixtures.freezeClock
import fixtures.migrations.migration_2022_12_18_054852_create_bar_table
import fixtures.migrations.migration_2022_12_18_064852_create_foo_table
import io.mockk.*
import io.tcds.orm.connection.ResilientConnection
import io.tcds.orm.connection.SqLiteConnection
import io.tcds.orm.extension.toLocalDateTime
import org.gradle.api.logging.Logger
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.sql.DriverManager
import java.time.Instant

class MigrationRunnerTest {
    private val factory = ResilientConnection.reconnectable { DriverManager.getConnection("jdbc:sqlite::memory:") }
    private val connection = SqLiteConnection(factory, null)
    private val logger: Logger = mockk(relaxed = true)

    private val runner = MigrationRunner(connection = connection)
    private val log: (String) -> Unit = { message -> logger.lifecycle(message) }
    private val migrations = listOf(
        ::migration_2022_12_18_054852_create_bar_table,
        ::migration_2022_12_18_064852_create_foo_table,
    )

    private val migratedState = listOf(
        mapOf(
            "name" to "migration_2022_12_18_054852_create_bar_table",
            "type" to "UP",
            "executed_at" to "2022-12-18T05:48:10",
        ),
        mapOf(
            "name" to "migration_2022_12_18_064852_create_foo_table",
            "type" to "UP",
            "executed_at" to "2022-12-18T05:48:11",
        ),
    )

    @Test
    fun `given migration functions then create migration table and run migration files`() = freezeClock {
        Assertions.assertEquals(emptyList<String>(), tables())

        runner.runMigrations(migrations, log)

        Assertions.assertEquals(listOf("_migrations", "bar", "foo"), tables())
        Assertions.assertEquals(migratedState.toSet(), migrations().toSet())
        verify(exactly = 1) { logger.lifecycle("Running migrations...") }
        verify(exactly = 1) { logger.lifecycle(" - migrated migration_2022_12_18_054852_create_bar_table") }
        verify(exactly = 1) { logger.lifecycle(" - migrated migration_2022_12_18_064852_create_foo_table") }
        verify(exactly = 1) { logger.lifecycle("Done.") }
    }

    @Test
    fun `given migration functions when migrations already exist then do not migrate again`() = freezeClock {
        Assertions.assertEquals(emptyList<String>(), tables())
        runner.runMigrations(migrations, log)
        Assertions.assertEquals(listOf("_migrations", "bar", "foo"), tables())

        runner.runMigrations(migrations, log)

        Assertions.assertEquals(listOf("_migrations", "bar", "foo"), tables())
        Assertions.assertEquals(migratedState.toSet(), migrations().toSet())
        verify(exactly = 2) { logger.lifecycle("Running migrations...") }
        verify(exactly = 1) { logger.lifecycle(" - migrated migration_2022_12_18_054852_create_bar_table") }
        verify(exactly = 1) { logger.lifecycle(" - migrated migration_2022_12_18_064852_create_foo_table") }
        verify(exactly = 2) { logger.lifecycle("Done.") }
    }

    private fun tables(): List<String> {
        val tables = connection.read(
            """
                SELECT name FROM sqlite_schema
                    WHERE type = 'table'
                    AND name NOT LIKE 'sqlite_%'
                ORDER BY name;
            """.trimIndent(),
        )

        return tables.map { it.value("name", String::class.java)!! }.toList()
    }

    private fun migrations(): List<Map<String, Any>> {
        val migrations = connection.read("SELECT * FROM _migrations ORDER BY name")

        return migrations.map {
            val name = it.value("name", String::class.java)!!
            val type = it.value("type", String::class.java)!!
            val datetime = it.value("executed_at", Instant::class.java)!!.toLocalDateTime()

            mapOf("name" to name, "type" to type, "executed_at" to datetime.toString())
        }.toList()
    }
}
