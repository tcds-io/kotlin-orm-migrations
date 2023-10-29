package io.tcds.orm.migrations.plugin.tasks

import io.tcds.orm.migrations.CreateMigration
import io.tcds.orm.migrations.FileWriter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class CreateMigrationTask : DefaultTask() {
    @TaskAction
    fun run() {
        val migrationDirectory = project.properties["migrations.directory"]?.toString()
            ?: throw Exception("Missing `migrations.directory` property")

        val name = project.properties["migration"]?.toString()
            ?: throw Exception("Missing migration name. Please run the command with `-P migration={name}` argument")

        val writer = FileWriter()
        val creator = CreateMigration(writer)
        creator.create(migrationDirectory, name)
    }
}
