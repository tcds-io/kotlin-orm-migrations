package io.tcds.orm.migrations.plugin.tasks

import io.tcds.orm.migrations.MigrationCreator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class CreateMigrationTask : DefaultTask() {
    @TaskAction
    fun run() = MigrationCreator(
        writer = MigrationCreator.Writer(),
        properties = project.properties,
        log = { message -> logger.lifecycle(message) },
    ).run()
}
