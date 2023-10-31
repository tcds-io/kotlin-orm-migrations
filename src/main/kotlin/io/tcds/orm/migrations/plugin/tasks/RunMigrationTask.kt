package io.tcds.orm.migrations.plugin.tasks

import io.tcds.orm.connection.GenericConnection
import io.tcds.orm.migrations.RunMigration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.sql.DriverManager

abstract class RunMigrationTask : DefaultTask() {
    private val jdbcUrl: String
        get() = project.properties["migrations.jdbcUrl"]
            ?.toString()
            ?.substringAfter("\${")
            ?.substringBefore("}")
            ?: throw Exception("Missing `migrations.jdbcUrl` property")

    @TaskAction
    fun run() {
        val jdbcWriteUrl = System.getenv(jdbcUrl)
        val jdbcConnection = DriverManager.getConnection(jdbcWriteUrl)
        val ormConnection = GenericConnection(jdbcConnection, jdbcConnection, null)

        RunMigration(
            connection = ormConnection,
            logger = logger,
            properties = project.properties,
        ).run()
    }
}
