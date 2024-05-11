package io.tcds.orm.migrations.plugin.tasks

import io.tcds.orm.connection.GenericConnection
import io.tcds.orm.migrations.MigrationRunner
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.sql.DriverManager

abstract class RunMigrationTask : DefaultTask() {
    private val jdbcUrl: String
        get() {
            val property = project.properties["migrations.jdbcUrl"]?.toString()
                ?: throw Exception("Missing `migrations.jdbcUrl` property")

            val envs = property.split("\$")
                .filter { it.startsWith("{") }
                .map { it.substringAfter("{").substringBefore("}") }

            return envs.fold(property) { value, env -> value.replace("\${$env}", System.getenv(env)!!) }
        }

    @TaskAction
    fun run() {
        val jdbcConnection = DriverManager.getConnection(jdbcUrl)
        val ormConnection = GenericConnection(jdbcConnection, jdbcConnection, null)

        MigrationRunner(
            connection = ormConnection,
            properties = project.properties,
            log = { message -> logger.lifecycle(message) },
        ).run()
    }
}
