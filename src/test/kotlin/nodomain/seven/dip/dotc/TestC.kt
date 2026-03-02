package nodomain.seven.dip.dotc

import kotlin.test.Test

object TestC: WithAssertionsDOTC {
    @Test
    fun `6_C_1 TEST CASE, THREE ARMY CIRCULAR MOVEMENT`() {
        """
        |Turkey: 
        |A Ankara - Constantinople
        |A Constantinople - Smyrna
        |A Smyrna - Ankara
        |""".parse().adjudicateAsDOTC()
    }

    @Test
    fun `6_C_2 TEST CASE, THREE ARMY CIRCULAR MOVEMENT WITH SUPPORT`() {
        """
        |Turkey: 
        |A Ankara - Constantinople
        |A Constantinople - Smyrna
        |A Smyrna - Ankara
        |A Bulgaria Supports A Ankara - Constantinople 
        |""".parse().adjudicateAsDOTC()
    }

    @Test
    fun `6_C_3 TEST CASE, A DISRUPTED THREE ARMY CIRCULAR MOVEMENT`() {
        """
        |Turkey: 
        |A Ankara - Constantinople
        |A Constantinople - Smyrna
        |A Smyrna - Ankara
        |A Bulgaria - Constantinople
        |""".parse().adjudicateAsDOTC()
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
        |""".parse().adjudicateAsDOTC()
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
        |""".parse().adjudicateAsDOTC()
    }
}
