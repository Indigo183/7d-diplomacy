package nodomain.seven.dip.datc

import nodomain.seven.dip.provinces.StandardPlayer.*
import nodomain.seven.dip.provinces.StandardCoast.*
import nodomain.seven.dip.provinces.StandardSea.*
import nodomain.seven.dip.provinces.StandardInLand.*
import kotlin.test.Test

/** ## 6.E. TEST CASES, HEAD-TO-HEAD BATTLES AND BELEAGUERED GARRISON
 * Missing tests clearly irrelevant due to not using fleets, coasts and sea regions
 */
object TestE: WithAssertionsDATC {
    @Test
    fun `6_E_1 TEST CASE, DISLODGED UNIT HAS NO EFFECT ON ATTACKER'S AREA`() {
        val result = """
        |Germany:
        |A Berlin - Prussia
        |A Kiel - Berlin
        |A Silesia Supports A Berlin - Prussia
        |
        |Russia:
        |A Prussia - Berlin
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces).containsEntry(BER, Germany)
    }

    @Test
    fun `6_E_2 TEST CASE, NO SELF DISLODGEMENT IN HEAD-TO-HEAD BATTLE`() {
        """
        |Germany:
        |A Berlin - Kiel
        |A Kiel - Berlin
        |A Munich Supports A Berlin - Kiel
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_E_3 TEST CASE, NO HELP IN DISLODGING OWN UNIT`() {
        """
        |Germany:
        |A Berlin - Kiel
        |A Munich Supports A Kiel - Berlin
        |
        |France:
        |A Kiel - Berlin
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_E_4 TEST CASE, NON-DISLODGED LOSER STILL HAS EFFECT`() { // Modified due to involving sea regions
        """
        |Germany:
        |A Berlin - Munich
        |A Silesia Supports A Berlin - Munich
        |A Bohemia Supports A Berlin - Munich
        |
        |Italy:
        |A Munich - Tyrolia
        |A Piedmont Supports A Munich - Tyrolia
        |
        |France:
        |A Burgundy Supports A Ruhr - Munich
        |A Kiel Supports A Ruhr - Munich
        |A Ruhr - Munich
        |
        |Austria:
        |A Vienna Supports A Trieste - Tyrolia
        |A Trieste - Tyrolia
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_E_5 TEST CASE, LOSER DISLODGED BY ANOTHER ARMY STILL HAS EFFECT`() { // Modified due to involving sea regions
        val result = """
        |Germany:
        |A Berlin - Munich
        |A Silesia Supports A Berlin - Munich
        |
        |Italy:
        |A Munich - Tyrolia
        |A Piedmont Supports A Munich - Tyrolia
        |
        |France:
        |A Burgundy Supports A Ruhr - Munich
        |A Kiel Supports A Ruhr - Munich
        |A Ruhr - Munich
        |
        |Austria:
        |A Vienna Supports A Trieste - Tyrolia
        |A Trieste - Tyrolia
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces)
            .containsEntry(TRI, Austria)
            .containsEntry(MUN, France)
    }

    @Test
    fun `6_E_6 TEST CASE, NOT DISLODGE BECAUSE OF OWN SUPPORT STILL HAS EFFECT`() { // Modified due to involving sea regions
        """
        |Germany:
        |A Tyrolia - Munich
        |A Bohemia Supports A Tyrolia - Munich
        |
        |France:
        |A Munich - Tyrolia
        |A Piedmont Supports A Munich - Tyrolia
        |A Burgundy Supports A Tyrolia - Munich
        |
        |Austria:
        |A Vienna Supports A Trieste - Tyrolia
        |A Trieste - Tyrolia
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_E_7 TEST CASE, NO SELF DISLODGEMENT WITH BELEAGUERED GARRISON`() { // Modified due to involving sea regions
        """
        |France:
        |A Burgundy Hold
        |A Picardy Supports A Ruhr - Burgundy
        |
        |Italy:
        |A Marseilles Supports A Gascony - Burgundy
        |A Gascony - Burgundy
        |
        |Germany:
        |A Munich Supports A Ruhr - Burgundy
        |A Ruhr - Burgundy
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_E_8 TEST CASE, NO SELF DISLODGEMENT WITH BELEAGUERED GARRISON AND HEAD-TO-HEAD BATTLE`() { // Modified due to involving sea regions
        """
        |France:
        |A Burgundy - Ruhr
        |A Picardy Supports A Ruhr - Burgundy
        |
        |Italy:
        |A Marseilles Supports A Gascony - Burgundy
        |A Gascony - Burgundy
        |
        |Germany:
        |A Munich Supports A Ruhr - Burgundy
        |A Ruhr - Burgundy
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_E_9 TEST CASE, ALMOST SELF DISLODGEMENT WITH BELEAGUERED GARRISON`() { // Modified due to involving sea regions
        val result = """
        |France:
        |A Burgundy - Belgium
        |A Picardy Supports A Ruhr - Burgundy
        |
        |Italy:
        |A Marseilles Supports A Gascony - Burgundy
        |A Gascony - Burgundy
        |
        |Germany:
        |A Munich Supports A Ruhr - Burgundy
        |A Ruhr - Burgundy
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces)
            .containsEntry(BEL, France)
            .containsEntry(BUR, Germany)
    }

    @Test
    fun `6_E_10 TEST CASE, ALMOST CIRCULAR MOVEMENT WITH NO SELF DISLODGEMENT WITH BELEAGUERED GARRISON`() { // Modified due to involving sea regions
        """
        |France:
        |A Burgundy - Paris
        |A Picardy Supports A Ruhr - Burgundy
        |
        |Italy:
        |A Marseilles Supports A Gascony - Burgundy
        |A Gascony - Burgundy
        |A Paris - Gascony
        |
        |Germany:
        |A Munich Supports A Ruhr - Burgundy
        |A Ruhr - Burgundy
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    //6.E.11. TEST CASE, NO SELF DISLODGEMENT WITH BELEAGUERED GARRISON, UNIT SWAP WITH ADJACENT CONVOYING AND TWO COASTS

    @Test
    fun `6_E_12 TEST CASE, SUPPORT ON ATTACK ON OWN UNIT CAN BE USED FOR OTHER MEANS`() {
        """
        |Austria:
        |A Budapest - Rumania
        |A Serbia Supports A Vienna - Budapest
        |
        |Italy:
        |A Vienna - Budapest
        |
        |Russia:
        |A Galicia - Budapest
        |A Rumania Supports A Galicia - Budapest
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_E_13 TEST CASE, THREE WAY BELEAGUERED GARRISON`() { // Modified due to involving sea regions
        """
        |Austria:
        |A Bohemia Supports A Tyrolia - Munich
        |A Tyrolia - Munich
        |
        |France:
        |A Ruhr - Munich
        |A Burgundy Supports A Ruhr - Munich
        |
        |Germany:
        |A Munich Hold
        |
        |Russia:
        |A Berlin - Munich
        |A Silesia Supports A Berlin - Munich
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_E_14 TEST CASE, ILLEGAL HEAD-TO-HEAD BATTLE CAN STILL DEFEND`() {
        """
        |Turkey:
        |A Albania - Bulgaria
        |
        |Russia:
        |A Serbia - Albania
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_E_15 TEST CASE, THE FRIENDLY HEAD-TO-HEAD BATTLE`() { // Modified due to involving sea regions
        """
        |France:
        |A Gascony Supports A Paris - Burgundy
        |A Paris - Burgundy
        |
        |Italy:
        |A Burgundy - Munich
        |A Tyrolia Supports A Burgundy - Munich
        |A Bohemia Supports A Burgundy - Munich
        |
        |Germany:
        |A Munich - Burgundy
        |A Ruhr Supports A Munich - Burgundy
        |A Belgium Supports A Munich - Burgundy
        |
        |Russia:
        |A Berlin Supports A Silesia - Munich
        |A Silesia - Munich
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }
}