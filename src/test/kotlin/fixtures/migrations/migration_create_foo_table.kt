package fixtures.migrations

import io.tcds.orm.migrations.Migration

fun migration_2022_12_18_064852_create_foo_table() = Migration(
    up = """
        CREATE TABLE foo
        (
            `id` VARCHAR(36) PRIMARY KEY
        );
    """.trimIndent(),
    down = """
        DROP TABLE foo;
    """.trimIndent(),
)
