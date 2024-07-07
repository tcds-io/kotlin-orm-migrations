package io.tcds.orm.migrations

import io.tcds.orm.connection.Connection
import io.tcds.orm.connection.NestedTransactionConnection
import io.tcds.orm.extension.param
import io.tcds.orm.extension.toInstant
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
        createSchemaIfNeeded()
        createMigrationTableIfNeeded()
        val migrated = loadPreviousMigrations()

        log("Running migrations...")

        migrations
            .filter { migration -> !migrated.contains(migration.name) }
            .map { migration -> migrate(migration) }
            .forEach { file -> log(" - migrated ${file.name}") }

        log("Done.")
    }

    private fun createSchemaIfNeeded() {
        when (connection) {
            is NestedTransactionConnection -> connection.write("CREATE SCHEMA IF NOT EXISTS ${connection.writer.schema}")
        }
    }

    private fun createMigrationTableIfNeeded() {
        connection.write(
            """
             CREATE TABLE IF NOT EXISTS `_migrations` (
                 `name`        VARCHAR(255),
                 `type`        VARCHAR(10),
                 `reverted`    TINYINT NOT NULL DEFAULT 0,
                 `executed_at` DATETIME(6)  NOT NULL
             );
            """.trimIndent(),
        )
    }

    private fun loadPreviousMigrations(): List<String> {
        return connection.read("SELECT name FROM _migrations WHERE reverted = 0")
            .map { result ->
                result.value("name", String::class.java)!!
            }.toList()
    }

    private fun migrate(migration: Statement): Statement {
        connection.write(migration.statement)
        connection.write(
            "INSERT INTO _migrations VALUES (?, ?, ?, ?)",
            listOf(
                param(migration.name),
                param(migration.type.name),
                param(0),
                param(LocalDateTime.now().toInstant()),
            ),
        )

        return migration
    }
}
