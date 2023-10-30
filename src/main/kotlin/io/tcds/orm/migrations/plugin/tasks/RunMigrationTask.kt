package io.tcds.orm.migrations.plugin.tasks

import io.tcds.orm.connection.GenericConnection
import io.tcds.orm.migrations.RunMigration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.sql.DriverManager

abstract class RunMigrationTask : DefaultTask() {
    @TaskAction
    fun run() {
        val jdbcUrlProperty = project.properties["migrations.jdbcUrl"]?.toString()?.substringAfter("\${")?.substringBefore("}")
            ?: throw Exception("Missing `migrations.jdbcUrl` property")

        val directories = project.properties.keys
            .filter {
                it.startsWith("migrations.directory")
            }.map {
                project.properties[it].toString()
            }

        if (directories.isEmpty()) throw Exception("Missing `migrations.directory` property")

        val jdbcWriteUrl = System.getenv(jdbcUrlProperty)
        val jdbcConnection = DriverManager.getConnection(jdbcWriteUrl)
        val ormConnection = GenericConnection(jdbcConnection, jdbcConnection, null)
        val migrator = RunMigration(ormConnection)

        directories.forEach { directory ->
            migrator.run(directory)
        }
    }
}
