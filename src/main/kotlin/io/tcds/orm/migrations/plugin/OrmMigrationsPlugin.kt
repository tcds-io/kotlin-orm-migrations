package io.tcds.orm.migrations.plugin

import io.tcds.orm.migrations.plugin.tasks.CreateMigrationTask
import io.tcds.orm.migrations.plugin.tasks.RunMigrationTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class OrmMigrationsPlugin : Plugin<Project> {
    companion object {
        private const val PLUGIN_GROUP = "orm"
    }

    override fun apply(project: Project) {
        require(project == project.rootProject) {
            "Plugin must be applied to the root project but was applied to ${project.path}"
        }

        project.tasks.register<CreateMigrationTask>("migration-create") {
            group = PLUGIN_GROUP
            description = "Create a new migration file"
        }

        project.tasks.register<RunMigrationTask>("migration-run") {
            group = PLUGIN_GROUP
            description = "Run new migrations"
        }
    }
}
