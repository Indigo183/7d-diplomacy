package nodomain.seven.dip.datc

import nodomain.seven.dip.adjudication.adjudicate
import nodomain.seven.dip.game.GameState.*
import nodomain.seven.dip.orders.A
import nodomain.seven.dip.orders.F
import nodomain.seven.dip.orders.T
import nodomain.seven.dip.provinces.StandardCoast.*
import nodomain.seven.dip.provinces.StandardInLand.*
import nodomain.seven.dip.provinces.StandardPlayer.*
import nodomain.seven.dip.provinces.StandardSea.*
import nodomain.seven.dip.utils.c
import kotlin.test.Test

//6.I. TEST CASES, BUILDING
object TestI: WithAssertionsDATC {
    val origin = T(0.c, 0)

    @Test
    fun `6_I_1 TEST CASE, TOO MANY BUILD ORDERS`() {
        val game = """
        |Germany:
        |A Warsaw - Prussia
        |A Munich - Silesia
        |F Kiel - Helgoland Bight
        |""".parse().adjudicateAsDATC()
        """
        |Germany:
        |A Prussia - Berlin
        |A Silesia Supports A Prussia - Berlin
        |F Helgoland Bight Holds
        |""".parse().adjudicateAsDATC(game = game)

        assertThat(game.gameState).isEqualTo(BUILDS)

        """
        |Germany:
        |Build A Warsaw
        |Build A Kiel
        |Build A Munich
        """.parse(BUILDS).adjudicateAsDATC(expectAllOrderToBeValid = false, game = game)

        assertThat(game.pieces)
            .doesNotContainKey(WAR)
            .containsKey(KIE)
            .doesNotContainKey(MUN)
    }

    @Test
    fun `6_I_2 TEST CASE, FLEETS CANNOT BE BUILD IN LAND AREAS`() {
        val game = """
        |Russia:
        |F St Petersburg - Gulf of Bothnia
        |A Moscow - St Petersburg
        |A Warsaw Holds
        |F Sevastopol Holds
        |""".parse().adjudicateAsDATC()
        """
        |Russia:
        |F Gulf of Bothnia - Sweden
        |A St Petersburg Holds
        |A Warsaw Holds
        |F Sevastopol Holds
        |""".parse().adjudicateAsDATC(game = game)

        assertThat(game.gameState).isEqualTo(BUILDS)

        """
        |Russia:
        |Build F Moscow
        """.parse(BUILDS).adjudicateAsDATC(expectAllOrderToBeValid = false, game = game)

        assertThat(game.pieces).doesNotContainKey(MOS)
    }
}
