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

    implementation("io.tcds.orm:orm:0.4.1")
    implementation("com.mysql:mysql-connector-j:8.2.0")

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
