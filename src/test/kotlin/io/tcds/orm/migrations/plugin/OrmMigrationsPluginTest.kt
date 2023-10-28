package io.tcds.orm.migrations.plugin

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class OrmMigrationsPluginTest {
    @TempDir
    private lateinit var projectDir: Path
    private lateinit var project: Project

    @BeforeEach
    internal fun setUp() {
        project = ProjectBuilder.builder().withProjectDir(projectDir.toFile()).build()
    }

    @Test
    fun `given a configured project then load migration create task`() {
        val name = "migration-create"
        initSingleProjectWithDefaultConfiguration()

        val task = project.getTasksByName(name, true).first()

        Assertions.assertEquals(name, task.name)
        Assertions.assertEquals("orm", task.group)
        Assertions.assertEquals("Create a new migration file", task.description)
    }

    @Test
    fun `given a configured project then load migration run task`() {
        val name = "migration-run"
        initSingleProjectWithDefaultConfiguration()

        val task = project.getTasksByName(name, true).first()

        Assertions.assertEquals(name, task.name)
        Assertions.assertEquals("orm", task.group)
        Assertions.assertEquals("Run new migrations", task.description)
    }

    private fun initSingleProjectWithDefaultConfiguration() {
        project.apply(plugin = "java")
        project.apply(plugin = "maven-publish")
        project.apply<OrmMigrationsPlugin>()
    }
}
