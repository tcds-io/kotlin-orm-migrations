import com.mysql.cj.jdbc.Driver
import io.tcds.orm.connection.GenericConnection
import io.tcds.orm.migration.CreateMigration
import io.tcds.orm.migration.FileWriter
import io.tcds.orm.migration.RunMigrations
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.sql.DriverManager

val jUnitVersion: String by project
val mockkVersion: String by project
val ormVersion: String by project

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
        const val email = "thiago@tcds.io"
    }
}

plugins {
    `kotlin-dsl`
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    gradleApi()
    api(kotlin("stdlib"))

    implementation("io.tcds.orm:orm:$ormVersion")
    implementation("com.mysql:mysql-connector-j:8.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter:$jUnitVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

buildscript {
    val ormVersion: String by project

    dependencies {
        classpath("io.tcds.orm:orm:$ormVersion")
        classpath("com.mysql:mysql-connector-j:8.2.0")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        languageVersion = "1.9"
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

tasks.register("migration-create") {
    group = "orm"

    doLast {
        val migrationDirectory = project.properties["migrations.directory"]?.toString()
            ?: throw Exception("Missing `migrations.directory` property")

        val name = project.properties["migration"]?.toString()
            ?: throw Exception("Missing migration name. Please run the command with `-P migration={name}` argument")

        val writer = FileWriter()
        val creator = CreateMigration(writer)
        creator.create(migrationDirectory, name)
    }
}

tasks.register("migration-migrate") {
    group = "orm"

    doLast {
        val migrationDirectory = project.properties["migrations.directory"]?.toString()
            ?: throw Exception("Missing `migrations.directory` property")
        val jdbcUrlProperty = project.properties["migrations.jdbcUrl"]?.toString()?.substringAfter("\${")?.substringBefore("}")
            ?: throw Exception("Missing `migrations.jdbcUrl` property")

        Driver()
        val jdbcWriteUrl = System.getenv(jdbcUrlProperty)
        val jdbc = DriverManager.getConnection(jdbcWriteUrl)
        val connection = GenericConnection(jdbc, jdbc, null)

        val migrator = RunMigrations(connection)
        migrator.run(migrationDirectory)
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

val sourcesJar by tasks.creating(Jar::class) { archiveClassifier.set("sources"); from(sourceSets.main.get().allSource) }
val javadocJar by tasks.creating(Jar::class) { archiveClassifier.set("javadoc"); from(tasks.javadoc) }

publishing {
    repositories {
        maven {
            name = "SonaType"
            group = Publication.group
            version = Publication.buildVersion
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials {
                username = Publication.Sonatype.username
                password = Publication.Sonatype.password
            }
        }
    }

    publications {
        listOf("defaultMavenJava", "pluginMaven").forEach { publication ->
            create<MavenPublication>(publication) {
                artifact(sourcesJar)
                artifact(javadocJar)

                pom {
                    name.set(Publication.Project.name)
                    description.set(Publication.Project.description)
                    url.set(Publication.Project.repository)
                    packaging = "jar"

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("${Publication.Project.repository}/blob/main/LICENSE")
                        }
                    }

                    developers {
                        developer {
                            id.set(Publication.Project.organization)
                            name.set(Publication.Project.developer)
                            email.set(Publication.Project.email)
                        }
                    }
                    scm {
                        connection.set(Publication.Project.scm)
                        url.set(Publication.Project.repository)
                    }
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        Publication.Gpg.signingKeyId,
        Publication.Gpg.signingKey,
        Publication.Gpg.signingPassword,
    )
    sign(publishing.publications["pluginMaven"])
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(Publication.Sonatype.username)
            password.set(Publication.Sonatype.password)
        }
    }
}
