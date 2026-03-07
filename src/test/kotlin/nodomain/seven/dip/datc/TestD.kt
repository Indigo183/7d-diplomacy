package nodomain.seven.dip.datc

import nodomain.seven.dip.provinces.StandardPlayer.*
import nodomain.seven.dip.provinces.StandardProvince.*
import kotlin.test.Test

/** ## 6.D. TEST CASES, SUPPORTS AND DISLODGES
 *  Missing tests clearly irrelevant due to not using fleets, coasts and sea regions
 */
object TestD: WithAssertionsDOTC {
    @Test
    fun `6_D_1 TEST CASE, SUPPORTED HOLD CAN PREVENT DISLODGEMENT`() { // Modified due to involving sea regions
        """
        |Austria:
        |A Apulia Supports A Trieste - Venice
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
        |A Apulia Supports A Trieste - Venice
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
    fun `6_D_3 TEST CASE, A MOVE CUTS SUPPORT ON MOVE`() { // Modified due to involving sea regions
        """
        |Austria:
        |A Tuscany Supports A Trieste - Venice
        |A Trieste - Venice
        |
        |Italy:
        |A Venice Hold
        |A Rome - Tuscany
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_D_4 TEST CASE, SUPPORT TO HOLD ON UNIT SUPPORTING A HOLD ALLOWED`() { // Modified due to involving sea regions
        """
        |Germany:
        |A Berlin Supports A Kiel
        |A Kiel Supports A Berlin
        |
        |Russia:
        |A Silesia Supports A Prussia - Berlin
        |A Munich - Berlin
        |The Russian move from Prussia to Berlin fails.
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_D_5 TEST CASE, SUPPORT TO HOLD ON UNIT SUPPORTING A MOVE ALLOWED`() { // Modified due to involving sea regions
        val result = """
        |Germany:
        |A Munich Supports A Berlin - Silesia
        |A Kiel Supports A Munich
        |A Berlin - Silesia
        |
        |Austria:
        |A Bohemia Supports A Tyrolia - Munich
        |A Tyrolia - Munich
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces).containsEntry(MUN, Germany)
    }

    //6.D.6. TEST CASE, SUPPORT TO HOLD ON CONVOYING UNIT ALLOWED

    @Test
    fun `6_D_7 TEST CASE, SUPPORT TO HOLD ON MOVING UNIT NOT ALLOWED`() { // Modified due to involving sea regions
        val result = """
        |Germany:
        |A Warsaw - Ukraine
        |A Prussia Supports A Warsaw
        | 
        |Russia:
        |A Livonia - Warsaw
        |A Moscow Supports A Livonia - Warsaw
        |A Sevastopol - Ukraine
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces)
            .containsEntry(WAR, Russia)
            .doesNotContainEntry(UKR, Germany)
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
        |A Kiel - Berlin
        |A Munich Supports A Kiel - Berlin
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_D_11 TEST CASE, NO SELF DISLODGMENT OF RETURNING UNIT`() {
        """
        |Germany:
        |A Berlin - Prussia
        |A Kiel - Berlin
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
        |A Trieste Hold
        |A Vienna Supports A Venice - Trieste
        |
        |Italy:
        |A Venice - Trieste
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_D_13 TEST CASE, SUPPORTING A FOREIGN UNIT TO DISLODGE A RETURNING OWN UNIT PROHIBITED`() { // Modified due to involving sea regions
        """
        |Austria:
        |A Trieste - Albania
        |A Vienna Supports A Venice - Trieste
        |
        |Italy:
        |A Venice - Trieste
        |A Greece - Albania
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_D_14 TEST CASE, SUPPORTING A FOREIGN UNIT IS NOT ENOUGH TO PREVENT DISLODGEMENT`() { // Modified due to involving sea regions
        val result = """
        |Austria:
        |A Trieste Hold
        |A Vienna Supports A Venice - Trieste
        |
        |Italy:
        |A Venice - Trieste
        |A Tyrolia Supports A Venice - Trieste
        |A Albania Supports A Venice - Trieste
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces)
            .containsEntry(TRI, Italy)
    }

    @Test
    fun `6_D_15 TEST CASE, DEFENDER CANNOT CUT SUPPORT FOR ATTACK ON ITSELF`() { // Modified due to involving sea regions
        val result = """
        |Russia:
        |A Constantinople Supports A Armenia - Ankara
        |A Armenia - Ankara
        |
        |Turkey:
        |A Ankara - Constantinople
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces).containsEntry(ANK, Russia)
    }

    //6.D.16. TEST CASE, CONVOYING A UNIT DISLODGING A UNIT OF SAME POWER IS ALLOWED

    @Test
    fun `6_D_17 TEST CASE, DISLODGEMENT CUTS SUPPORTS`() { // Modified due to involving sea regions
        val result = """
        |Russia:
        |A Ankara Supports A Sevastopol - Armenia
        |A Sevastopol - Armenia
        |
        |Turkey:
        |A Armenia - Ankara
        |A Smyrna Supports A Armenia - Ankara
        |A Syria - Armenia
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces)
            .containsEntry(ANK, Turkey)
            .doesNotContainEntry(ARM, Russia)
    }

    @Test
    fun `6_D_18 TEST CASE, A SURVIVING UNIT WILL SUSTAIN SUPPORT`() { // Modified due to involving sea regions
        val result = """
        |Russia:
        |A Ankara Supports A Sevastopol - Armenia
        |A Black Sea - Ankara
        |A Constantinople Supports A Ankara
        | 
        |Turkey:
        |A Armenia - Ankara
        |A Smyrna Supports F Armenia - Ankara
        |A Syria - Armenia
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces).containsEntry(ARM, Russia)
    }

    @Test
    fun `6_D_19 TEST CASE, EVEN WHEN SURVIVING IS IN ALTERNATIVE WAY`() { // Modified due to involving sea regions
        val result = """
        |Russia:
        |A Constantinople Supports A Armenia - Ankara
        |A Armenia - Ankara
        |A Smyrna Supports A Ankara - Constantinople
        |
        |Turkey:
        |A Ankara - Constantinople
        |The Russian fleet in Constantinople is not dislodged, because one of the supports is of Russian origin. The support from Black Sea to Ankara will sustain and the fleet in Ankara will be dislodged.
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces).containsEntry(ANK, Russia)
    }

    //6.D.20. TEST CASE, UNIT CANNOT CUT SUPPORT OF ITS OWN COUNTRY

    @Test
    fun `6_D_21 TEST CASE, DISLODGING DOES NOT CANCEL A SUPPORT CUT`() {
        val result = """
        |Austria:
        |A Trieste Hold
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

    //6_D_24 TEST CASE, IMPOSSIBLE ARMY MOVE CANNOT BE SUPPORTED

    @Test
    fun `6_D_25 TEST CASE, FAILING HOLD SUPPORT CAN BE SUPPORTED`() {
        """
        |Germany:
        |A Berlin Supports A Prussia
        |A Kiel Supports A Berlin
        |
        |Russia:
        |A Munich Supports A Prussia - Berlin
        |A Prussia - Berlin
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    @Test
    fun `6_D_26 TEST CASE, FAILING MOVE SUPPORT CAN BE SUPPORTED`() {
        """
        |Germany:
        |A Berlin Supports A Prussia - Silesia
        |A Kiel Supports A Berlin
        |
        |Russia:
        |A Munich Supports A Prussia - Berlin
        |A Prussia - Berlin
        |""".parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

    //6.D.27. TEST CASE, FAILING CONVOY CAN BE SUPPORTED

    @Test
    fun `6_D_28 TEST CASE, IMPOSSIBLE MOVE AND SUPPORT`() {
        """
        |Austria:
        |A Budapest Supports A Rumania
        |
        |Russia:
        |A Rumania - Holland
        |
        |Turkey:
        |A Serbia - Rumania
        |A Bulgaria Supports A Serbia - Rumania
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
    fun `6_D_34 TEST CASE, SUPPORT TARGETING OWN AREA NOT ALLOWED`() { // Modified due to involving sea regions
        val result = """
        |Germany:
        |A Berlin - Silesia
        |A Munich Supports A Berlin - Silesia
        |A Prussia Supports A Berlin - Silesia
        |
        |Italy:
        |A Silesia Supports Warsaw - Silesia
        |
        |Russia:
        |A Galicia Supports A Warsaw - Silesia
        |A Warsaw - Silesia
        |""".parse().adjudicateAsDOTC()

        assertThat(result.pieces).containsEntry(SIL, Germany)
    }
}
