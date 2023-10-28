package io.tcds.orm.migrations

import java.io.File

class FileWriter {
    fun write(directory: String, name: String, content: String) {
        val dir = File(directory)
        if (!dir.exists()) dir.mkdirs()

        File("$directory/$name")
            .bufferedWriter()
            .use { out -> out.write(content) }
    }
}
