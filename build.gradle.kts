@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Publication {
    const val group = "io.tcds.orm"
    val buildVersion: String = System.getenv("VERSION") ?: "dev"

    object Sonatype {
        val username: String? = System.getenv("OSS_USER")
        val password: String? = System.getenv("OSS_PASSWORD")
    }

    object Gpg {
        val signingKeyId: String? = System.getenv("GPG_KEY_ID")
        val signingKey: String? = System.getenv("GPG_KEY")
        val signingPassword: String? = System.getenv("GPG_KEY_PASSWORD")
    }

    object Project {
        const val name = "Kotlin Simple ORM Migration Tools"
        const val description = "Migration tool for Kotlin Simple ORM"
        const val repository = "https://github.com/tcds-io/kotlin-orm-migrations"
        const val scm = "scm:git:git://github.com:tcds-io/kotlin-orm-migrations.git"

        const val organization = "tcds-io"
        const val developer = "Thiago Cordeiro"
    }
}

plugins {
    `kotlin-dsl`

    id("com.gradle.plugin-publish") version "1.2.1"
}

group = Publication.group
version = Publication.buildVersion
description = Publication.Project.description

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
    website.set(Publication.Project.repository)
    vcsUrl.set(Publication.Project.repository)

    plugins {
        create("tcdsOrmMigrations") {
            id = "io.tcds.orm.migrations"
            implementationClass = "io.tcds.orm.migrations.plugin.OrmMigrationsPlugin"
            displayName = Publication.Project.name
            description = Publication.Project.description
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
