package io.tcds.orm.migrations

data class Statement(
    val name: String,
    val statement: String,
    val type: Type,
) {
    enum class Type { UP }

    companion object {
        fun up(name: String, statement: String) = Statement(name, statement, Type.UP)
    }
}
