package io.tcds.orm.migrations

import fixtures.freezeClock
import io.tcds.orm.OrmException
import io.tcds.orm.connection.SqLiteConnection
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.DriverManager
import java.time.LocalDateTime

class RunMigrationsTest {
    private val connection = SqLiteConnection(
        DriverManager.getConnection("jdbc:sqlite::memory:"),
        null,
    )
    private val runner = RunMigrations(connection)

    @Test
    fun `given a directory then create migration table and run migration files`() = freezeClock {
        Assertions.assertEquals(emptyList<String>(), tables())

        runner.run("src/test/kotlin/io/tcds/orm/migrations/migrations")

        Assertions.assertEquals(listOf("_migrations", "bar", "foo"), tables())
        Assertions.assertEquals(
            listOf(
                mapOf("name" to "2022_12_18_054852_foo", "executed_at" to "2022-12-18T05:48:52"),
                mapOf("name" to "2022_12_18_064852_bar", "executed_at" to "2022-12-18T05:48:52"),
            ).toSet(),
            migrations().toSet(),
        )
    }

    @Test
    fun `given a directory when migrations already exist then do not migrate again`() = freezeClock {
        Assertions.assertEquals(emptyList<String>(), tables())
        runner.run("src/test/kotlin/io/tcds/orm/migrations/migrations")
        Assertions.assertEquals(listOf("_migrations", "bar", "foo"), tables())

        runner.run("src/test/kotlin/io/tcds/orm/migrations/migrations")

        Assertions.assertEquals(listOf("_migrations", "bar", "foo"), tables())
        Assertions.assertEquals(
            listOf(
                mapOf("name" to "2022_12_18_054852_foo", "executed_at" to "2022-12-18T05:48:52"),
                mapOf("name" to "2022_12_18_064852_bar", "executed_at" to "2022-12-18T05:48:52"),
            ).toSet(),
            migrations().toSet(),
        )
    }

    @Test
    fun `given a directory when migration file is invalid then throw exception`() = freezeClock {
        Assertions.assertEquals(emptyList<String>(), tables())
        val directory = "src/test/kotlin/io/tcds/orm/migrations/migrations/yaml"

        val exception = assertThrows<OrmException> {
            runner.run(directory)
        }

        Assertions.assertEquals("Invalid migration file: 2022_12_18_054852_foo.yaml", exception.message)
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
            val datetime = it.value("executed_at", LocalDateTime::class.java)!!

            mapOf("name" to name, "executed_at" to datetime.toString())
        }.toList()
    }
}
