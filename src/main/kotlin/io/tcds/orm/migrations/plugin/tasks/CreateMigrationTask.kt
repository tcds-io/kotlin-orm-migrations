package io.tcds.orm.migrations.plugin.tasks

import io.tcds.orm.migrations.MigrationCreator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class CreateMigrationTask : DefaultTask() {
    @TaskAction
    fun run() {
        MigrationCreator(MigrationCreator.Writer())
            .run(project.properties) { message ->
                logger.lifecycle(message)
            }
    }
}
