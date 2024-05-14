package io.tcds.orm.migrations.plugin.tasks

import io.tcds.orm.connection.GenericConnection
import io.tcds.orm.connection.ResilientConnection
import io.tcds.orm.migrations.MigrationRunner
import io.tcds.orm.migrations.Statement
import io.tcds.orm.migrations.modules
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
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
        val jdbcConnection = ResilientConnection.reconnectable { DriverManager.getConnection(jdbcUrl) }
        val ormConnection = GenericConnection(jdbcConnection, jdbcConnection, null)
        val migrations = mutableListOf<Statement>()

        project.properties.modules().map { module ->
            File(module.value)
                .listFiles()
                ?.filter { it.isFile }
                ?.forEach { migrations.add(Statement.up(it.name, it.readText())) }
        }

        MigrationRunner(connection = ormConnection)
            .runStatements(migrations) { message ->
                logger.lifecycle(message)
            }
    }
}
