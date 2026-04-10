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
        |A Berlin - Prussia
        |A Munich - Silesia
        |F Kiel - Helgoland Bight
        |""".parse().adjudicateAsDATC()
        """
        |Germany:
        |A Prussia - Warsaw
        |A Silesia Supports A Prussia - Warsaw
        |F Helgoland Bight Holds
        |""".parse().adjudicateAsDATC(game = game)

        assertThat(game.gameState).isEqualTo(BUILDS)

        """
        |Germany:
        |Build A Warsaw
        |Build A Kiel
        |Build A Munich
        """.parse(BUILDS).adjudicateAsDATC(expectAllOrderToBeValid = false, game = game)

        println(game.pieces)
        assertThat(game.pieces)//.containsKey(KIE)
    }
}