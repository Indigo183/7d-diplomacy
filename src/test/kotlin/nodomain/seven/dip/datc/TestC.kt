package nodomain.seven.dip.datc

import nodomain.seven.dip.provinces.StandardPlayer.*
import nodomain.seven.dip.provinces.StandardProvince.*
import kotlin.test.Test

/** ## 6.C. TEST CASES, CIRCULAR MOVEMENT
 *  Missing tests clearly irrelevant due to not using fleets, coasts and sea regions
 */
object TestC: WithAssertionsDOTC {
    @Test
    fun `6_C_1 TEST CASE, THREE ARMY CIRCULAR MOVEMENT`() { // Modified due to involving fleets as separate from armies
        val result = """
        |Turkey: 
        |A Ankara - Constantinople
        |A Constantinople - Smyrna
        |
        |Russia:
        |A Smyrna - Ankara
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces)
            .containsEntry(CON, Turkey)
            .containsEntry(SMY, Turkey)
            .containsEntry(ANK, Russia)
    }

    @Test
    fun `6_C_2 TEST CASE, THREE ARMY CIRCULAR MOVEMENT WITH SUPPORT`() { // Modified due to involving fleets as separate from armies
        val result = """
        |Turkey: 
        |A Ankara - Constantinople
        |A Constantinople - Smyrna
        |A Bulgaria Supports A Ankara - Constantinople 
        |
        |Russia:
        |A Smyrna - Ankara
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces)
            .containsEntry(CON, Turkey)
            .containsEntry(SMY, Turkey)
            .containsEntry(ANK, Russia)
    }

    @Test
    fun `6_C_3 TEST CASE, A DISRUPTED THREE ARMY CIRCULAR MOVEMENT`() { // Modified due to involving fleets as separate from armies
        """
        |Turkey: 
        |A Ankara - Constantinople
        |A Constantinople - Smyrna
        |A Smyrna - Ankara
        |A Bulgaria - Constantinople
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    //6.C.4. TEST CASE, A CIRCULAR MOVEMENT WITH ATTACKED CONVOY

    //6.C.5. TEST CASE, A DISRUPTED CIRCULAR MOVEMENT DUE TO DISLODGED CONVOY

    //6.C.6. TEST CASE, TWO ARMIES WITH TWO CONVOYS

    //6.C.7. TEST CASE, DISRUPTED UNIT SWAP

    @Test
    fun `6_C_8 TEST CASE, NO SELF DISLODGEMENT IN DISRUPTED CIRCULAR MOVEMENT`() { // Modified due to involving sea regions
        """
        |Turkey:
        |A Serbia - Bulgaria
        |A Bulgaria - Rumania
        |A Constantinople Supports A Serbia - Bulgaria
        |
        |Russia:
        |A Rumania - Serbia
        |
        |Austria
        |A Trieste - Serbia
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_C_9 TEST CASE, NO HELP IN DISLODGEMENT OF OWN UNIT IN DISRUPTED CIRCULAR MOVEMENT`() { // Modified due to involving sea regions
        """
        |Turkey:
        |A Bulgaria - Rumania
        |A Constantinople Supports A Serbia - Bulgaria
        |
        |Russia:
        |A Rumania - Serbia
        |
        |Austria
        |A Trieste - Serbia
        |A Serbia - Bulgaria
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }
}
