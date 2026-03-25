package nodomain.seven.dip.datc

import nodomain.seven.dip.provinces.StandardPlayer.*
import nodomain.seven.dip.provinces.StandardProvince.*
import kotlin.test.Test

/** ## 6.A. TEST CASES, BASIC CHECKS
 *  Missing tests clearly irrelevant due to not using fleets, coasts and sea regions
 */
object TestA: WithAssertionsDATC {
    @Test
    fun `6_A_1 TEST CASE, MOVING TO AN AREA THAT IS NOT A NEIGHBOUR`() { // Modified due to involving sea regions
        """
        |Turkey:
        |F Smyrna - Bulgaria
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    //6.A.2. TEST CASE, MOVE ARMY TO SEA

    //6.A.3. TEST CASE, MOVE FLEET TO LAND

    @Test
    fun `6_A_4 TEST CASE, MOVE TO OWN SECTOR`() {
        """
        |Germany:
        |A Kiel - Kiel
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    //6.A.5. TEST CASE, MOVE TO OWN SECTOR WITH CONVOY

    @Test
    fun `6_A_6 TEST CASE, ORDERING A UNIT OF ANOTHER COUNTRY`() {// Modified due to involving sea regions
        val franceHasAnArmyInParis: Setup = mapOf(PAR to France)

        """
        |Germany:
        |A Paris - Brest
        |""".parse().adjudicateAsDOTC(setup = franceHasAnArmyInParis).andAssertThatNothingMoved()
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
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces).containsEntry(TRI, Italy)
    }

    //6.A.9. TEST CASE, FLEETS MUST FOLLOW COAST IF NOT ON SEA

    @Test
    fun `6_A_10 TEST CASE, SUPPORT ON UNREACHABLE DESTINATION NOT POSSIBLE`() { // Modified due to involving sea regions
        """
        |Austria:
        |A Venice Hold
        |
        |Italy:
        |A Naples Supports A Apulia - Venice
        |A Apulia - Venice
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_A_11 TEST CASE, SIMPLE BOUNCE` () {
        """
        |Austria:
        |A Vienna - Tyrolia
        |
        |Italy:
        |A Venice - Tyrolia
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
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
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }
}
