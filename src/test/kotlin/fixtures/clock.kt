package fixtures

import io.mockk.*
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

fun freezeClock(block: () -> Unit) {
    val datetime = "2022-12-18T05:48:{{seconds}}Z"
    var second = 10

    mockkStatic(Clock::class)
    every { Clock.systemDefaultZone() } answers {
        val now = datetime.replace("{{seconds}}", "${second++}")
        Clock.fixed(Instant.parse(now), ZoneOffset.UTC)
    }
    block()
    unmockkStatic(Clock::class)
}
