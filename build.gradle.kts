import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ormVersion: String by project
val mysqlConnectorVersion: String by project

object Publication {
    const val GROUP = "io.tcds.orm"
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
    implementation("com.mysql:mysql-connector-j:$mysqlConnectorVersion")

    testImplementation("org.xerial:sqlite-jdbc:3.43.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("io.mockk:mockk:1.13.2")
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

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}
val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

publishing {
    repositories {
        maven {
            name = "SonaType"
            group = Publication.GROUP
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
                    name.set(Publication.Project.NAME)
                    description.set(Publication.Project.DESCRIPTION)
                    url.set(Publication.Project.REPOSITORY)
                    packaging = "jar"

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("${Publication.Project.REPOSITORY}/blob/main/LICENSE")
                        }
                    }

                    developers {
                        developer {
                            id.set(Publication.Project.ORGANIZATION)
                            name.set(Publication.Project.DEVELOPER)
                        }
                    }
                    scm {
                        connection.set(Publication.Project.SCM)
                        url.set(Publication.Project.REPOSITORY)
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
