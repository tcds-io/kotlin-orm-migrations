package io.tcds.orm.migrations.plugin.tasks

import io.tcds.orm.connection.GenericConnection
import io.tcds.orm.migrations.RunMigrations
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.sql.DriverManager

abstract class RunMigrationTask : DefaultTask() {
    @TaskAction
    fun run() {
        doLast {
            val migrationDirectory = project.properties["migrations.directory"]?.toString()
                ?: throw Exception("Missing `migrations.directory` property")
            val jdbcUrlProperty = project.properties["migrations.jdbcUrl"]?.toString()?.substringAfter("\${")?.substringBefore("}")
                ?: throw Exception("Missing `migrations.jdbcUrl` property")

            val jdbcWriteUrl = System.getenv(jdbcUrlProperty)
            val jdbcConnection = DriverManager.getConnection(jdbcWriteUrl)
            val ormConnection = GenericConnection(jdbcConnection, jdbcConnection, null)
            val migrator = RunMigrations(ormConnection)

            migrator.run(migrationDirectory)
        }
    }
}
