@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Publication {
    const val GROUP = "io.tcds.orm"
    val buildVersion: String = System.getenv("VERSION") ?: "0.1.7"

    object Project {
        const val NAME = "Kotlin Simple ORM Migration Tools"
        const val DESCRIPTION = "Migration tool for Kotlin Simple ORM"
        const val REPOSITORY = "https://github.com/tcds-io/kotlin-orm-migrations"
        const val SCM = "scm:git:git://github.com:tcds-io/kotlin-orm-migrations.git"

        const val ORGANIZATION = "tcds-io"
        const val DEVELOPER = "Thiago Cordeiro"
    }
}

plugins {
    `kotlin-dsl`

    id("com.gradle.plugin-publish") version "1.2.1"
}

group = Publication.GROUP
version = Publication.buildVersion
description = Publication.Project.DESCRIPTION

repositories {
    mavenCentral()
}

dependencies {
    gradleApi()
    api(kotlin("stdlib"))

    implementation("io.tcds.orm:orm:0.4.1")
    implementation("com.mysql:mysql-connector-j:8.2.0")

    testImplementation("org.xerial:sqlite-jdbc:3.43.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("io.mockk:mockk:1.13.2")
}

gradlePlugin {
    website.set(Publication.Project.REPOSITORY)
    vcsUrl.set(Publication.Project.REPOSITORY)

    plugins {
        create("tcdsOrmMigrations") {
            id = "${Publication.GROUP}.migrations"
            implementationClass = "io.tcds.orm.migrations.plugin.OrmMigrationsPlugin"
            displayName = Publication.Project.NAME
            description = Publication.Project.DESCRIPTION
            tags.set(listOf("orm", "migrations", "database"))
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        languageVersion = "1.9"
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events("started", "skipped", "passed", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
    }
}
