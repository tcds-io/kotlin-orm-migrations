package io.tcds.orm.migrations

import org.intellij.lang.annotations.Language

data class Migration(
    @Language("SQL") val up: String,
    @Language("SQL") val down: String,
)
