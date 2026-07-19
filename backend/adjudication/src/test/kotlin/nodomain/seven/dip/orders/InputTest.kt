package nodomain.seven.dip.orders

import nodomain.seven.dip.game.Game
import nodomain.seven.dip.provinces.A
import nodomain.seven.dip.provinces.StandardPlayer
import nodomain.seven.dip.provinces.StandardCoast.*
import nodomain.seven.dip.provinces.setup
import org.assertj.core.api.WithAssertions
import kotlin.test.Test

class InputTest: WithAssertions {
    @Test
    fun isValidTest() {
        //A Naples Supports A Apulia - Venice
        val testOrder = A[NAP] S { A[APU] M VEN}

        val result = Game(setup<StandardPlayer>()).isValid(testOrder)

        assertThat(result).isFalse
    }

    @Test
    fun inputTest() {
        val game = Game(setup<StandardPlayer>())

        val testOrder = A[NAP] S { A[APU] M VEN}

        game.input(listOf(testOrder))

        assertThat(game.supports).isEmpty()
    }
}
