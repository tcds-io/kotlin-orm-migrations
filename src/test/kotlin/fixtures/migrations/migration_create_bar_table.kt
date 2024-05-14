package fixtures.migrations

import io.tcds.orm.migrations.Migration

fun migration_2022_12_18_054852_create_bar_table() = Migration(
    up = """
        CREATE TABLE bar
        (
            `id` VARCHAR(36) PRIMARY KEY
        );
    """.trimIndent(),
    down = """
        DROP TABLE bar;
    """.trimIndent(),
)
