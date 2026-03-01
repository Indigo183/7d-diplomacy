package nodomain.seven.dip.dotc

import nodomain.seven.dip.provinces.StandardPlayer.*
import nodomain.seven.dip.provinces.StandardProvince.*
import kotlin.test.Test

class A: WithAssertionsDOTC {
    //6.A. TEST CASES, BASIC CHECKS

    @Test
    fun `6_A_1 TEST CASE, MOVING TO AN AREA THAT IS NOT A NEIGHBOUR`() {

    }

    //6.A.2. TEST CASE, MOVE ARMY TO SEA

    //6.A.3. TEST CASE, MOVE FLEET TO LAND

    @Test
    fun `6_A_4 TEST CASE, MOVE TO OWN SECTOR`() {
        """
        |Germany:
        |F Kiel - Kiel
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    //6.A.5. TEST CASE, MOVE TO OWN SECTOR WITH CONVOY

    //6.A.6. TEST CASE, ORDERING A UNIT OF ANOTHER COUNTRY

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

        assertThat(result!!.pieces).containsEntry(TRI, Italy)
    }

    //6.A.9. TEST CASE, FLEETS MUST FOLLOW COAST IF NOT ON SEA

    //6.A.10. TEST CASE, SUPPORT ON UNREACHABLE DESTINATION NOT POSSIBLE

    //6.A.11. TEST CASE, SIMPLE BOUNCE

    //6.A.12. TEST CASE, BOUNCE OF THREE UNITS
}