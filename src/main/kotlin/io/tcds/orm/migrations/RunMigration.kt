package io.tcds.orm.migrations

import io.tcds.orm.OrmException
import io.tcds.orm.connection.Connection
import io.tcds.orm.param.DateTimeParam
import io.tcds.orm.param.StringParam
import java.io.File
import java.time.LocalDateTime

class RunMigration(
    private val connection: Connection,
) {
    fun run(directory: String) {
        val migrations: List<File> = File(directory)
            .listFiles()
            ?.filter { it.isFile }
            ?: return

        createMigrationTableIfNeeded()

        migrations.forEach {
            migrate(it)
        }
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

    private fun migrate(file: File): Boolean {
        if (!file.name.endsWith(".sql")) {
            throw OrmException("Invalid migration file: ${file.name}")
        }

        val name = file.name.removeSuffix(".sql")

        val result = connection.read(
            "SELECT count(*) as count FROM _migrations WHERE name = ?",
            listOf(StringParam("name", name)),
        ).first()

        val total = result.value("count", Int::class.java)!!

        if (total > 0) {
            return false
        }

        connection.transaction {
            write(file.readText())
            write(
                "INSERT INTO _migrations VALUES (?, ?)",
                listOf(
                    StringParam("name", name),
                    DateTimeParam("executed_at", LocalDateTime.now()),
                ),
            )
        }

        return true
    }
}