package io.tcds.orm.migrations

abstract class BaseProperty(properties: Map<String, *>) {
    val directories: Map<String, String> = properties
        .filter {
            it.key.startsWith("migrations.directory")
        }.map {
            Pair(it.key, it.value.toString())
        }.also {
            if (it.isEmpty()) throw Exception("Missing `migrations.directory` property")
        }.toMap()
}
