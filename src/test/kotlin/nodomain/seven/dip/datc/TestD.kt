package nodomain.seven.dip.datc

import nodomain.seven.dip.provinces.StandardPlayer.*
import nodomain.seven.dip.provinces.StandardCoast.*
import nodomain.seven.dip.provinces.StandardSea.*
import nodomain.seven.dip.provinces.StandardInLand.*
import kotlin.test.Test

/** ## 6.D. TEST CASES, SUPPORTS AND DISLODGES
 *  Missing tests clearly irrelevant due to not using fleets, coasts and sea regions
 */
object TestD: WithAssertionsDATC {
    @Test
    fun `6_D_1 TEST CASE, SUPPORTED HOLD CAN PREVENT DISLODGEMENT`() {
        """
        |Austria:
        |F Adriatic Sea Supports A Trieste - Venice
        |A Trieste - Venice
        |
        |Italy:
        |A Venice Hold
        |A Tyrolia Supports A Venice
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_D_2 TEST CASE, A MOVE CUTS SUPPORT ON HOLD`() {
        val result = """
        |Austria:
        |F Adriatic Sea Supports A Trieste - Venice
        |A Trieste - Venice
        |A Vienna - Tyrolia
        |
        |Italy:
        |A Venice Hold
        |A Tyrolia Supports A Venice
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces)
            .containsEntry(VEN, Austria)
            .containsEntry(TYR, Italy)
    }

    @Test
    fun `6_D_3 TEST CASE, A MOVE CUTS SUPPORT ON MOVE`() {
        """
        |Austria:
        |F Adriatic Sea Supports A Trieste - Venice
        |A Trieste - Venice
        |
        |Italy:
        |A Venice Hold
        |F Ionian Sea - Adriatic Sea
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_D_4 TEST CASE, SUPPORT TO HOLD ON UNIT SUPPORTING A HOLD ALLOWED`() {
        """
        |Germany:
        |F Berlin Supports A Kiel
        |A Kiel Supports A Berlin
        |
        |Russia:
        |F Baltic Sea Supports A Prussia - Berlin
        |A Prussia - Berlin
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_D_5 TEST CASE, SUPPORT TO HOLD ON UNIT SUPPORTING A MOVE ALLOWED`() {
        val result = """
        |Germany:
        |A Berlin Supports A Munich - Silesia
        |F Kiel Supports A Berlin
        |A Munich - Silesia
        |
        |Russia: 
        |F Baltic Sea Supports A Prussia - Berlin
        |A Prussia - Berlin
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces).containsEntry(BER, Germany)
    }

    //6.D.6. TEST CASE, SUPPORT TO HOLD ON CONVOYING UNIT ALLOWED

    @Test
    fun `6_D_7 TEST CASE, SUPPORT TO HOLD ON MOVING UNIT NOT ALLOWED`() {
        val result = """
        |Germany:
        |F Baltic Sea - Sweden
        |F Prussia Supports F Baltic Sea
        |
        |Russia:
        |F Livonia - Baltic Sea
        |F Gulf of Bothnia Supports F Livonia - Baltic Sea
        |A Finland - Sweden
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces)
            .containsEntry(BAL, Russia)
            .doesNotContainEntry(SWE, Germany)
    }

    //6_D_8 TEST CASE, FAILED CONVOY CANNOT RECEIVE HOLD SUPPORT

    @Test
    fun `6_D_9 TEST CASE, SUPPORT TO MOVE ON HOLDING UNIT NOT ALLOWED`() {
        val result = """
        |Italy:
        |A Venice - Trieste
        |A Tyrolia Supports A Venice - Trieste
        |
        |Austria:
        |A Albania Supports A Trieste - Serbia
        |A Trieste Hold
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces)
            .containsEntry(TRI, Italy)
            .doesNotContainEntry(SER, Austria)
    }

    @Test
    fun `6_D_10 TEST CASE, SELF DISLODGMENT PROHIBITED`() {
        """
        |Germany:
        |A Berlin Hold
        |F Kiel - Berlin
        |A Munich Supports A Kiel - Berlin
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_D_11 TEST CASE, NO SELF DISLODGMENT OF RETURNING UNIT`() {
        """
        |Germany:
        |A Berlin - Prussia
        |F Kiel - Berlin
        |A Munich Supports A Kiel - Berlin
        |
        |Russia:
        |A Warsaw - Prussia
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_D_12 TEST CASE, SUPPORTING A FOREIGN UNIT TO DISLODGE OWN UNIT PROHIBITED`() {
        """
        |Austria:
        |F Trieste Hold
        |A Vienna Supports A Venice - Trieste
        |
        |Italy:
        |A Venice - Trieste
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_D_13 TEST CASE, SUPPORTING A FOREIGN UNIT TO DISLODGE A RETURNING OWN UNIT PROHIBITED`() {
        """
        |Austria:
        |F Trieste - Adriatic Sea
        |A Vienna Supports A Venice - Trieste
        |
        |Italy:
        |A Venice - Trieste
        |F Apulia - Adriatic Sea
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_D_14 TEST CASE, SUPPORTING A FOREIGN UNIT IS NOT ENOUGH TO PREVENT DISLODGEMENT`() {
        val result = """
        |Austria:
        |F Trieste Hold
        |A Vienna Supports A Venice - Trieste
        |
        |Italy:
        |A Venice - Trieste
        |A Tyrolia Supports A Venice - Trieste
        |F Adriatic Sea Supports A Venice - Trieste
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces)
            .containsEntry(TRI, Italy)
    }

    @Test
    fun `6_D_15 TEST CASE, DEFENDER CANNOT CUT SUPPORT FOR ATTACK ON ITSELF`() {
        val result = """
        |Russia:
        |F Constantinople Supports F Black Sea - Ankara
        |F Black Sea - Ankara
        |
        |Turkey:
        |F Ankara - Constantinople
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces).containsEntry(ANK, Russia)
    }

    //6.D.16. TEST CASE, CONVOYING A UNIT DISLODGING A UNIT OF SAME POWER IS ALLOWED

    @Test
    fun `6_D_17 TEST CASE, DISLODGEMENT CUTS SUPPORTS`() {
        val result = """
        |Russia:
        |F Constantinople Supports F Black Sea - Ankara
        |F Black Sea - Ankara
        |
        |Turkey:
        |F Ankara - Constantinople
        |A Smyrna Supports F Ankara - Constantinople
        |A Armenia - Ankara
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces)
            .containsEntry(CON, Turkey)
            .doesNotContainEntry(ANK, Russia)
    }

    @Test
    fun `6_D_18 TEST CASE, A SURVIVING UNIT WILL SUSTAIN SUPPORT`() {
        val result = """
        |Russia:
        |F Constantinople Supports F Black Sea - Ankara
        |F Black Sea - Ankara
        |A Bulgaria Supports F Constantinople
        |
        |Turkey:
        |F Ankara - Constantinople
        |A Smyrna Supports F Ankara - Constantinople
        |A Armenia - Ankara
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces).containsEntry(ANK, Russia)
    }

    @Test
    fun `6_D_19 TEST CASE, EVEN WHEN SURVIVING IS IN ALTERNATIVE WAY`() {
        val result = """
        |Russia:
        |F Constantinople Supports F Black Sea - Ankara
        |F Black Sea - Ankara
        |A Smyrna Supports F Ankara - Constantinople
        |
        |Turkey:
        |F Ankara - Constantinople
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces).containsEntry(CON, Russia)
    }

    //6.D.20. TEST CASE, UNIT CANNOT CUT SUPPORT OF ITS OWN COUNTRY

    @Test
    fun `6_D_21 TEST CASE, DISLODGING DOES NOT CANCEL A SUPPORT CUT`() {
        val result = """
        |Austria:
        |F Trieste Hold
        |
        |Italy:
        |A Venice - Trieste
        |A Tyrolia Supports A Venice - Trieste
        |
        |Germany:
        |A Munich - Tyrolia
        |
        |Russia:
        |A Silesia - Munich
        |A Berlin Supports A Silesia - Munich
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces)
            .containsEntry(TRI, Austria)
            .containsEntry(MUN, Russia)
    }

    //6.D.22. TEST CASE, IMPOSSIBLE FLEET MOVE CANNOT BE SUPPORTED

    //6.D.23. TEST CASE, IMPOSSIBLE COAST MOVE CANNOT BE SUPPORTED

    @Test
    fun `6_D_24 TEST CASE, IMPOSSIBLE ARMY MOVE CANNOT BE SUPPORTED`() {
        """
        |France:
        |A Marseilles - Gulf of Lyon
        |F Spain(sc) Supports A Marseilles - Gulf of Lyon
        |
        |Italy:
        |F Gulf of Lyon Hold
        |
        |Turkey:
        |F Tyrrhenian Sea Supports F Western Mediterranean - Gulf of Lyon
        |F Western Mediterranean - Gulf of Lyon
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_D_25 TEST CASE, FAILING HOLD SUPPORT CAN BE SUPPORTED`() {
        """
        |Germany:
        |A Berlin Supports A Prussia
        |F Kiel Supports A Berlin
        |
        |Russia:
        |F Baltic Sea Supports A Prussia - Berlin
        |A Prussia - Berlin
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_D_26 TEST CASE, FAILING MOVE SUPPORT CAN BE SUPPORTED`() {
        """
        |Germany:
        |A Berlin Supports A Prussia - Silesia
        |F Kiel Supports A Berlin
        |
        |Russia:
        |F Baltic Sea Supports A Prussia - Berlin
        |A Prussia - Berlin
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    //6.D.27. TEST CASE, FAILING CONVOY CAN BE SUPPORTED

    @Test
    fun `6_D_28 TEST CASE, IMPOSSIBLE MOVE AND SUPPORT`() {
        """
        |Austria:
        |A Budapest Supports F Rumania
        |
        |Russia:
        |F Rumania - Holland
        |
        |Turkey:
        |F Black Sea - Rumania
        |A Bulgaria Supports F Black Sea - Rumania
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    //6.D.29. TEST CASE, MOVE TO IMPOSSIBLE COAST AND SUPPORT

    //6.D.30. TEST CASE, MOVE WITHOUT COAST AND SUPPORT

    //6.D.31. TEST CASE, A TRICKY IMPOSSIBLE SUPPORT

    //6.D.32. TEST CASE, A MISSING FLEET

    @Test
    fun `6_D_33 TEST CASE, UNWANTED SUPPORT ALLOWED`() {
        val result = """
        |Austria:
        |A Serbia - Budapest
        |A Vienna - Budapest
        |
        |Russia:
        |A Galicia Supports A Serbia - Budapest
        |
        |Turkey:
        |A Bulgaria - Serbia
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces)
            .containsEntry(BUD, Austria)
            .containsEntry(SER, Turkey)
    }

    @Test
    fun `6_D_34 TEST CASE, SUPPORT TARGETING OWN AREA NOT ALLOWED`() {
        val result = """
        |Germany:
        |A Berlin - Prussia
        |A Silesia Supports A Berlin - Prussia
        |F Baltic Sea Supports A Berlin - Prussia
        |
        |Italy:
        |A Prussia Supports Livonia - Prussia
        |
        |Russia:
        |A Warsaw Supports A Livonia - Prussia
        |A Livonia - Prussia
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces).containsEntry(PRU, Germany)
    }
}
