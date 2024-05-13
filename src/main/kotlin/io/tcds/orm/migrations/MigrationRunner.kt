package io.tcds.orm.migrations

import io.tcds.orm.OrmException
import io.tcds.orm.connection.Connection
import io.tcds.orm.param.DateTimeParam
import io.tcds.orm.param.StringParam
import java.io.File
import java.time.LocalDateTime

class MigrationRunner(private val connection: Connection) {
    fun run(modules: Map<String, String>, log: (String) -> Unit) {
        val migrations = mutableListOf<Migration>()

        modules.map { module ->
            File(module.value)
                .listFiles()
                ?.filter { it.isFile }
                ?.forEach { migrations.add(Migration(it.name, it.readText())) }
        }

        run(migrations, log)
    }

    fun run(migrations: List<Migration>, log: (String) -> Unit) {
        createMigrationTableIfNeeded()

        log("Running migrations...")
        val executed = migrations.mapNotNull { migration -> migrate(migration) }
        executed.forEach { file -> log(" - migrated ${file.name}") }

        log("Done.")
    }

    private fun createMigrationTableIfNeeded() {
        connection.write(
            """
             CREATE TABLE IF NOT EXISTS `_migrations` (
                 `name`        VARCHAR(255) PRIMARY KEY,
                 `executed_at` DATETIME(6)  NOT NULL
             );
        """.trimIndent(),
        )
    }

    private fun migrate(migration: Migration): Migration? {
        if (!migration.name.endsWith(".sql")) {
            throw OrmException("Invalid migration file: ${migration.name}")
        }

        val name = migration.name.removeSuffix(".sql")

        val result = connection.read(
            "SELECT count(*) as count FROM _migrations WHERE name = ?",
            listOf(StringParam("name", name)),
        ).first()

        val total = result.value("count", Int::class.java)!!

        if (total > 0) {
            return null
        }

        connection.transaction {
            connection.write(migration.content)
            connection.write(
                "INSERT INTO _migrations VALUES (?, ?)",
                listOf(
                    StringParam("name", name),
                    DateTimeParam("executed_at", LocalDateTime.now()),
                ),
            )
        }

        return migration
    }
}
