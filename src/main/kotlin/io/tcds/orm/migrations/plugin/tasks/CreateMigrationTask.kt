package io.tcds.orm.migrations.plugin.tasks

import io.tcds.orm.migrations.CreateMigration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class CreateMigrationTask : DefaultTask() {
    @TaskAction
    fun run() = CreateMigration(
        writer = CreateMigration.Writer(),
        logger = logger,
        properties = project.properties,
    ).run()
}
