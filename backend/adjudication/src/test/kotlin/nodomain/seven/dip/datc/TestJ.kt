package nodomain.seven.dip.datc

import nodomain.seven.dip.game.GameState.*
import nodomain.seven.dip.provinces.StandardCoast.*
import nodomain.seven.dip.provinces.StandardInLand.*
import kotlin.test.Test

//6.I. TEST CASES, CIVIL DISORDER AND DISBANDS
object TestJ: WithAssertionsDATC {
    @Test
    fun `6_J_1 TEST CASE, TOO MANY DISBAND ORDERS`() {
        // Check how program reacts when someone orders too many disbands.
        // France has to disband one and has an army in Paris and Picardy.

        val game = """
        |England:
        |F London - English Channel
        |
        |France:
        |A Brest - Picardy
        |A Paris Holds
        |""".parse().adjudicateAsDATC()
        """
        |England:
        |F English Channel - Brest
        |
        |France:
        |A Picardy Holds
        |A Paris Holds
        |""".parse().adjudicateAsDATC(game = game)

        assertThat(game.gameState).isEqualTo(BUILDS)

        """
        |France:
        |Remove F Gulf of Lyon
        |Remove A Picardy
        |Remove A Paris
        |""".parse(BUILDS).adjudicateAsDATC(expectAllOrderToBeValid = false, game = game)

        // Program should not disband both Paris and Picardy, but should handle it in a different way. See also issue
        // 4.D.6.

        assertThat(game.pieces).hasSize(2).containsKey(PAR).doesNotContainKey(PIC)
    }
}