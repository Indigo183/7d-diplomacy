package nodomain.seven.dip.provinces

import nodomain.seven.dip.orders.A
import nodomain.seven.dip.orders.T
import nodomain.seven.dip.provinces.RomanPlayers.*
import nodomain.seven.dip.provinces.Romans.*
import nodomain.seven.dip.provinces.StandardCoast.*
import nodomain.seven.dip.utils.c
import org.assertj.core.api.WithAssertions
import kotlin.test.Test

class ProvincesTest: WithAssertions {
    val origin = T(0.c, 0)

    @Test
    fun setupTest() {
        assertThat(setup<RomanPlayers>()).containsExactlyEntriesOf(mapOf(origin A CAT to Cato, origin A POM to Pompey))
    }

    @Test
    fun provincesWithInlandBorderAreNonAdjacentForFleets() {
        StandardProvince.entries //Yes, this line does seem pointless. Please, remove it and try to run just this test through intelij.
        assertThat(ROM.isAdjacent(VEN)).isTrue
        assertThat(ROM.hasInlandBorderWith(VEN)).isTrue
        //assertThat(ROM.isAdjacentForFleets(VEN)).isFalse
    }
}
