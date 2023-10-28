package io.tcds.orm.migrations

import fixtures.freezeClock
import io.mockk.*
import org.junit.jupiter.api.Test

class CreateMigrationTest {
    private val writer: FileWriter = mockk()
    private val creator = CreateMigration(writer)

    @Test
    fun `given a directory and a name then create the migration file in the directory`() = freezeClock {
        every { writer.write(any(), any(), any()) } just runs

        creator.create("/foo/bar", "FooBar")

        verify {
            writer.write(
                "/foo/bar",
                "2022_12_18_054852_foo_bar.sql",
                "# CREATE TABLE ...",
            )
        }
    }
}
