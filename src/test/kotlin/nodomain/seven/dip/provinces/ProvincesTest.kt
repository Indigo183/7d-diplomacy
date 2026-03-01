package nodomain.seven.dip.provinces

import nodomain.seven.dip.provinces.RomanPlayers.*
import nodomain.seven.dip.provinces.Romans.*
import org.assertj.core.api.WithAssertions
import kotlin.test.Test

class ProvincesTest: WithAssertions {
    @Test
    fun setupTest() {
        assertThat(setup<RomanPlayers>()).containsExactlyEntriesOf(mapOf(CAT to Cato, POM to Pompey))
    }
}
