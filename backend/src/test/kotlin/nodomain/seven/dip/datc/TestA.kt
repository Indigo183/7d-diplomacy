package nodomain.seven.dip.datc

import nodomain.seven.dip.orders.*
import nodomain.seven.dip.provinces.StandardPlayer.*
import nodomain.seven.dip.provinces.StandardCoast.*
import nodomain.seven.dip.utils.c
import kotlin.test.Test

/** ## 6.A. TEST CASES, BASIC CHECKS
 *  Missing tests clearly irrelevant due to not using fleets, coasts and sea regions
 */
object TestA: WithAssertionsDATC {
    val origin = T(0.c, 0)
    @Test
    fun `6_A_1 TEST CASE, MOVING TO AN AREA THAT IS NOT A NEIGHBOUR`() {
        """
        |England: 
        |F North Sea - Picardy
        |""".parse().adjudicateAsDATC(expectAllOrderToBeValid = false).andAssertThatNothingMoved()
    }

    @Test
    fun `6_A_2 TEST CASE, MOVE ARMY TO SEA`() {
        """
        |England: 
        |A Liverpool - Irish Sea
        |""".parse().adjudicateAsDATC(expectAllOrderToBeValid = false).andAssertThatNothingMoved()
    }

    @Test
    fun `6_A_3 TEST CASE, MOVE FLEET TO LAND`() {
        """
        |Germany: 
        |F Kiel - Munich
        |""".parse().adjudicateAsDATC(expectAllOrderToBeValid = false).andAssertThatNothingMoved()
    }

    @Test
    fun `6_A_4 TEST CASE, MOVE TO OWN SECTOR`() {
        """
        |Germany:
        |A Kiel - Kiel
        |""".parse().adjudicateAsDATC(expectAllOrderToBeValid = false).andAssertThatNothingMoved()
    }

    //6.A.5. TEST CASE, MOVE TO OWN SECTOR WITH CONVOY

    @Test
    fun `6_A_6 TEST CASE, ORDERING A UNIT OF ANOTHER COUNTRY`() {
        val franceHasAnArmyInParis: Setup = mapOf(origin A LON to England)

        """
        |Germany:
        |F London - North Sea
        |""".parse().adjudicateAsDATC(setup = {franceHasAnArmyInParis}, expectAllOrderToBeValid = false).andAssertThatNothingMoved()
    }

    //6.A.7. TEST CASE, ONLY ARMIES CAN BE CONVOYED

    @Test
    fun `6_A_8 TEST CASE, SUPPORT TO HOLD YOURSELF IS NOT POSSIBLE`() {
        val result = """
        |Italy:
        |A Venice - Trieste
        |A Tyrolia Supports A Venice - Trieste
        |
        |Austria:
        |F Trieste Supports F Trieste
        |""".parse().adjudicateAsDATC(expectAllOrderToBeValid = false)

        assertThat(result.pieces).containsEntry(TRI, Italy)
    }

    @Test
    fun `6_A_9 TEST CASE, FLEETS MUST FOLLOW COAST IF NOT ON SEA`() {
        """
        |Italy: 
        |F Rome - Venice
        |""".parse().adjudicateAsDATC(expectAllOrderToBeValid = false).andAssertThatNothingMoved()
    }

    @Test
    fun `6_A_10 TEST CASE, SUPPORT ON UNREACHABLE DESTINATION NOT POSSIBLE`() {
        """
        |Austria:
        |A Venice Hold
        |
        |Italy:
        |F Rome Supports A Apulia - Venice
        |A Apulia - Venice
        |""".parse().adjudicateAsDATC(expectAllOrderToBeValid = false).andAssertThatNothingMoved()
    }

    @Test
    fun `6_A_11 TEST CASE, SIMPLE BOUNCE` () {
        """
        |Austria:
        |A Vienna - Tyrolia
        |
        |Italy:
        |A Venice - Tyrolia
        |""".parse().adjudicateAsDATC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_A_12 TEST CASE, BOUNCE OF THREE UNITS`() {
        """
        |Austria: 
        |A Vienna - Tyrolia
        |
        |Germany: 
        |A Munich - Tyrolia
        |
        |Italy: 
        |A Venice - Tyrolia
        |""".parse().adjudicateAsDATC().andAssertThatNothingMoved()
    }
}
