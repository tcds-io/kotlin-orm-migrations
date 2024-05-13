package io.tcds.orm.migrations

internal fun Map<String, *>.modules(): Map<String, String> {
    return filter { it.key.startsWith("migrations.directory") }
        .map { Pair(it.key, it.value.toString()) }
        .also { if (it.isEmpty()) throw Exception("Missing `migrations.directory` property") }
        .toMap()
}
