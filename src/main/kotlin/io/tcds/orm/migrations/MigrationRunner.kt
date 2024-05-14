package io.tcds.orm.migrations

import io.tcds.orm.connection.Connection
import io.tcds.orm.param.DateTimeParam
import io.tcds.orm.param.StringParam
import java.time.LocalDateTime
import kotlin.reflect.KFunction

class MigrationRunner(private val connection: Connection) {
    fun runMigrations(migrations: List<KFunction<Migration>>, log: (String) -> Unit) {
        val statements = migrations.map { fn ->
            Statement.up(
                name = fn.name,
                statement = fn.call().up,
            )
        }

        runStatements(statements, log)
    }

    fun runStatements(migrations: List<Statement>, log: (String) -> Unit) {
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
                 `name`        VARCHAR(255),
                 `type`        VARCHAR(10),
                 `executed_at` DATETIME(6)  NOT NULL
             );
        """.trimIndent(),
        )
    }

    private fun migrate(migration: Statement): Statement? {
        val result = connection.read(
            "SELECT count(*) as count FROM _migrations WHERE name = ?",
            listOf(StringParam("name", migration.name)),
        ).first()

        val total = result.value("count", Int::class.java)!!

        if (total > 0) {
            return null
        }

        connection.transaction {
            connection.write(migration.statement)
            connection.write(
                "INSERT INTO _migrations VALUES (?, ?, ?)",
                listOf(
                    StringParam("name", migration.name),
                    StringParam("type", migration.type.name),
                    DateTimeParam("executed_at", LocalDateTime.now()),
                ),
            )
        }

        return migration
    }
}
