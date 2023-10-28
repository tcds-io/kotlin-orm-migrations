package io.tcds.orm.migrations

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CreateMigration(private val writer: FileWriter) {
    fun create(directory: String, name: String) {
        val pattern = "(?<=.)[A-Z]".toRegex()
        val migration: String = name.replace(pattern, "_$0").toLowerCase()
        val now = LocalDateTime.now()
        val format = DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmmss")
        val filename = "${now.format(format)}_$migration"

        val content = "# CREATE TABLE ..."

        writer.write(directory, "$filename.sql", content)
    }
}
