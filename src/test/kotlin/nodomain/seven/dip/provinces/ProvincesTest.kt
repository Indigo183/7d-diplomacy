package nodomain.seven.dip.provinces

import nodomain.seven.dip.orders.A
import nodomain.seven.dip.orders.T
import nodomain.seven.dip.provinces.RomanPlayers.*
import nodomain.seven.dip.provinces.Romans.*
import nodomain.seven.dip.utils.c
import org.assertj.core.api.WithAssertions
import kotlin.test.Test

class ProvincesTest: WithAssertions {
    val origin = T(0.c, 0)

    @Test
    fun setupTest() {
        assertThat(setup<RomanPlayers>()).containsExactlyEntriesOf(mapOf(origin A CAT to Cato, origin A POM to Pompey))
    }
}
