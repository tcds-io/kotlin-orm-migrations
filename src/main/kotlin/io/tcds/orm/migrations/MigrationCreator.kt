package io.tcds.orm.migrations

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MigrationCreator(private val writer: Writer) {
    class Writer {
        fun write(directory: String, name: String, content: String) {
            val dir = File(directory)
            if (!dir.exists()) dir.mkdirs()

            File("$directory/$name")
                .bufferedWriter()
                .use { out -> out.write(content) }
        }
    }

    fun run(properties: Map<String, *>, log: (String) -> Unit) {
        val directories: Map<String, String> = properties.directories()

        val name = properties["migration"]?.toString() ?: run {
            throw Exception("Missing migration name. Please run the command with `-P migration={name}` argument")
        }

        val module = properties["module"]
            ?.toString()
            ?.let { "migrations.directory[$it]" }
            ?: "migrations.directory"

        val directory = directories[module] ?: run {
            val names = directories.keys.joinToString(",") { it.substringAfter("[").substringBefore("]") }

            throw Exception("Missing migration directory. Please run the command with `-P module={module}` with one of `$names`")
        }

        val file = this.create(directory, name)
        log("Migration `$file` created.")
    }

    private fun create(directory: String, name: String): String {
        val pattern = "(?<=.)[A-Z]".toRegex()
        val migration: String = name.replace(pattern, "_$0").toLowerCase()
        val now = LocalDateTime.now()
        val format = DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmmss")
        val filename = "${now.format(format)}_$migration"

        writer.write(directory, "$filename.sql", "# CREATE TABLE ...")

        return "$directory/$filename.sql"
    }
}
