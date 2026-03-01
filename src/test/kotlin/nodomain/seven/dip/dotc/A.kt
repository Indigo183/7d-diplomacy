package nodomain.seven.dip.dotc

import nodomain.seven.dip.provinces.StandardPlayer.*
import nodomain.seven.dip.provinces.StandardProvince.*
import kotlin.test.Test

class A: WithAssertionsDOTC {
    //6.A. TEST CASES, BASIC CHECKS

    @Test
    fun `6_A_1 TEST CASE, MOVING TO AN AREA THAT IS NOT A NEIGHBOUR`() {

    }

    @Test
    fun `6_A_4 TEST CASE, MOVE TO OWN SECTOR`() {
        """
        |Germany:
        |F Kiel - Kiel
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_A_8 TEST CASE, SUPPORT TO HOLD YOURSELF IS NOT POSSIBLE`() {
        val result = """
        |Italy:
        |A Venice - Trieste
        |A Tyrolia Supports A Venice - Trieste
        |
        |Austria:
        |F Trieste Supports F Trieste
        |""".parse().adjudicateAsDOTC()

        assertThat(result!!.pieces).containsEntry(TRI, Italy)
    }
}