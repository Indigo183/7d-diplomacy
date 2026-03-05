package nodomain.seven.dip.datc

import kotlin.test.Test

//6.D. TEST CASES, SUPPORTS AND DISLODGES
object TestD: WithAssertionsDOTC {
    @Test
    fun `6_D_1 TEST CASE, SUPPORTED HOLD CAN PREVENT DISLODGEMENT`() {
        """
    Austria:
    F Adriatic Sea Supports A Trieste - Venice
    A Trieste - Venice

    Italy:
    A Venice Hold
    A Tyrolia Supports A Venice
    The support of Tyrolia prevents the army in Venice from being dislodged. The army in Trieste will not move.
    """
    }

    @Test
    fun `6_D_2 TEST CASE, A MOVE CUTS SUPPORT ON HOLD`() {
        """
    Austria:
    F Adriatic Sea Supports A Trieste - Venice
    A Trieste - Venice
    A Vienna - Tyrolia

    Italy:
    A Venice Hold
    A Tyrolia Supports A Venice
    The support of Tyrolia is cut by the army in Vienna. That means that the army in Venice is dislodged by the army from Trieste.
    """
    }

    @Test
    fun `6_D_3 TEST CASE, A MOVE CUTS SUPPORT ON MOVE`() {
        """
    Austria:
    F Adriatic Sea Supports A Trieste - Venice
    A Trieste - Venice

    Italy:
    A Venice Hold
    F Ionian Sea - Adriatic Sea
    The support of the fleet in the Adriatic Sea is cut. That means that the army in Venice will not be dislodged and the army in Trieste stays in Trieste.
    """
    }

    @Test
    fun `6_D_4 TEST CASE, SUPPORT TO HOLD ON UNIT SUPPORTING A HOLD ALLOWED`() {
        """
    Germany:
    A Berlin Supports F Kiel
    F Kiel Supports A Berlin

    Russia:
    F Baltic Sea Supports A Prussia - Berlin
    A Prussia - Berlin
    The Russian move from Prussia to Berlin fails.
    """
    }

    @Test
    fun `6_D_5 TEST CASE, SUPPORT TO HOLD ON UNIT SUPPORTING A MOVE ALLOWED`() {
        """
    Germany:
    A Berlin Supports A Munich - Silesia
    F Kiel Supports A Berlin
    A Munich - Silesia

    Russia:
    F Baltic Sea Supports A Prussia - Berlin
    A Prussia - Berlin
    The Russian move from Prussia to Berlin fails.
    """
    }

    //6.D.6. TEST CASE, SUPPORT TO HOLD ON CONVOYING UNIT ALLOWED

    @Test
    fun `6_D_7 TEST CASE, SUPPORT TO HOLD ON MOVING UNIT NOT ALLOWED`() {
        """
    Germany:
    F Baltic Sea - Sweden
    F Prussia Supports F Baltic Sea

    Russia:
    F Livonia - Baltic Sea
    F Gulf of Bothnia Supports F Livonia - Baltic Sea
    A Finland - Sweden
    The support of the fleet in Prussia fails. The fleet in Baltic Sea will bounce on the Russian army in Finland and will be dislodged by the Russian fleet from Livonia when it returns to the Baltic Sea.
    """
    }

    //6_D_8 TEST CASE, FAILED CONVOY CANNOT RECEIVE HOLD SUPPORT

    @Test
    fun `6_D_9 TEST CASE, SUPPORT TO MOVE ON HOLDING UNIT NOT ALLOWED`() {
        """
    Italy:
    A Venice - Trieste
    A Tyrolia Supports A Venice - Trieste

    Austria:
    A Albania Supports A Trieste - Serbia
    A Trieste Hold
    The support of the army in Albania fails and the army in Trieste is dislodged by the army from Venice.
    """
    }

    @Test
    fun `6_D_10 TEST CASE, SELF DISLODGMENT PROHIBITED`() {
        """
    Germany:
    A Berlin Hold
    F Kiel - Berlin
    A Munich Supports F Kiel - Berlin
    Move to Berlin fails.
    """
    }

    @Test
    fun `6_D_11 TEST CASE, NO SELF DISLODGMENT OF RETURNING UNIT`() {
        """
    Germany:
    A Berlin - Prussia
    F Kiel - Berlin
    A Munich Supports F Kiel - Berlin

    Russia:
    A Warsaw - Prussia
    Army in Berlin bounces, but is not dislodged by own unit.
    """
    }

    @Test
    fun `6_D_12 TEST CASE, SUPPORTING A FOREIGN UNIT TO DISLODGE OWN UNIT PROHIBITED`() {
        """
    Austria:
    F Trieste Hold
    A Vienna Supports A Venice - Trieste

    Italy:
    A Venice - Trieste
    No dislodgment of fleet in Trieste.
    """
    }

    @Test
    fun `6_D_13 TEST CASE, SUPPORTING A FOREIGN UNIT TO DISLODGE A RETURNING OWN UNIT PROHIBITED`() {
        """
    Austria:
    F Trieste - Adriatic Sea
    A Vienna Supports A Venice - Trieste

    Italy:
    A Venice - Trieste
    F Apulia - Adriatic Sea
    No dislodgment of fleet in Trieste.
    """
    }

    @Test
    fun `6_D_14 TEST CASE, SUPPORTING A FOREIGN UNIT IS NOT ENOUGH TO PREVENT DISLODGEMENT`() {
        """
    Austria:
    F Trieste Hold
    A Vienna Supports A Venice - Trieste

    Italy:
    A Venice - Trieste
    A Tyrolia Supports A Venice - Trieste
    F Adriatic Sea Supports A Venice - Trieste
    The fleet in Trieste is dislodged.
    """
    }

    @Test
    fun `6_D_15 TEST CASE, DEFENDER CANNOT CUT SUPPORT FOR ATTACK ON ITSELF`() {
        """
    Russia:
    F Constantinople Supports F Black Sea - Ankara
    F Black Sea - Ankara

    Turkey:
    F Ankara - Constantinople
    The support of Constantinople is not cut and the fleet in Ankara is dislodged by the fleet in the Black Sea.
    """
    }

    //6.D.16. TEST CASE, CONVOYING A UNIT DISLODGING A UNIT OF SAME POWER IS ALLOWED

    @Test
    fun `6_D_17 TEST CASE, DISLODGEMENT CUTS SUPPORTS`() {
        """
    Russia:
    F Constantinople Supports F Black Sea - Ankara
    F Black Sea - Ankara

    Turkey:
    F Ankara - Constantinople
    A Smyrna Supports F Ankara - Constantinople
    A Armenia - Ankara
    The Russian fleet in Constantinople is dislodged. This cuts the support to from Black Sea to Ankara. Black Sea will bounce with the army from Armenia.
    """
    }

    @Test
    fun `6_D_18 TEST CASE, A SURVIVING UNIT WILL SUSTAIN SUPPORT`() {
        """
    Russia:
    F Constantinople Supports F Black Sea - Ankara
    F Black Sea - Ankara
    A Bulgaria Supports F Constantinople

    Turkey:
    F Ankara - Constantinople
    A Smyrna Supports F Ankara - Constantinople
    A Armenia - Ankara
    The Russian fleet in the Black Sea will dislodge the Turkish fleet in Ankara.
    """
    }

    @Test
    fun `6_D_19 TEST CASE, EVEN WHEN SURVIVING IS IN ALTERNATIVE WAY`() {
        """
    Russia:
    F Constantinople Supports F Black Sea - Ankara
    F Black Sea - Ankara
    A Smyrna Supports F Ankara - Constantinople

    Turkey:
    F Ankara - Constantinople
    The Russian fleet in Constantinople is not dislodged, because one of the supports is of Russian origin. The support from Black Sea to Ankara will sustain and the fleet in Ankara will be dislodged.
    """
    }

    //6.D.20. TEST CASE, UNIT CANNOT CUT SUPPORT OF ITS OWN COUNTRY

    @Test
    fun `6_D_21 TEST CASE, DISLODGING DOES NOT CANCEL A SUPPORT CUT`() {
        """
    Austria:
    F Trieste Hold

    Italy:
    A Venice - Trieste
    A Tyrolia Supports A Venice - Trieste

    Germany:
    A Munich - Tyrolia

    Russia:
    A Silesia - Munich
    A Berlin Supports A Silesia - Munich
    Although the German army is dislodged, it still cuts the Italian support. That means that the Austrian Fleet is not dislodged.
    """
    }

    //6.D.22. TEST CASE, IMPOSSIBLE FLEET MOVE CANNOT BE SUPPORTED

    //6.D.23. TEST CASE, IMPOSSIBLE COAST MOVE CANNOT BE SUPPORTED

    @Test
    fun `6_D_24 TEST CASE, IMPOSSIBLE ARMY MOVE CANNOT BE SUPPORTED`() {
        """
    France:
    A Marseilles - Gulf of Lyon
    F Spain(sc) Supports A Marseilles - Gulf of Lyon

    Italy:
    F Gulf of Lyon Hold

    Turkey:
    F Tyrrhenian Sea Supports F Western Mediterranean - Gulf of Lyon
    F Western Mediterranean - Gulf of Lyon
    The French move from Marseilles to Gulf of Lyon is illegal (an army cannot go to sea). Therefore, the support from Spain fails and there is no beleaguered garrison. The fleet in the Gulf of Lyon is dislodged by the Turkish fleet in the Western Mediterranean.
    """
    }

    @Test
    fun `6_D_25 TEST CASE, FAILING HOLD SUPPORT CAN BE SUPPORTED`() {
        """
    Germany:
    A Berlin Supports A Prussia
    F Kiel Supports A Berlin

    Russia:
    F Baltic Sea Supports A Prussia - Berlin
    A Prussia - Berlin
    Although the support of Berlin on Prussia fails (because of unmatching orders), the support of Kiel on Berlin is still valid. So, Berlin will not be dislodged.
    """
    }

    @Test
    fun `6_D_26 TEST CASE, FAILING MOVE SUPPORT CAN BE SUPPORTED`() {
        """
    Germany:
    A Berlin Supports A Prussia - Silesia
    F Kiel Supports A Berlin

    Russia:
    F Baltic Sea Supports A Prussia - Berlin
    A Prussia - Berlin
    Again, Berlin will not be dislodged.
    """
    }

    //6.D.27. TEST CASE, FAILING CONVOY CAN BE SUPPORTED

    @Test
    fun `6_D_28 TEST CASE, IMPOSSIBLE MOVE AND SUPPORT`() {
        """
    Austria:
    A Budapest Supports F Rumania

    Russia:
    F Rumania - Holland

    Turkey:
    F Black Sea - Rumania
    A Bulgaria Supports F Black Sea - Rumania
    See issue 4.E.1. Illegal orders are ignored. Without an order, Rumania holds and receives support. The fleet in Rumania is not dislodged.
    """
    }

    //6.D.29. TEST CASE, MOVE TO IMPOSSIBLE COAST AND SUPPORT

    //6.D.30. TEST CASE, MOVE WITHOUT COAST AND SUPPORT

    //6.D.31. TEST CASE, A TRICKY IMPOSSIBLE SUPPORT

    //6.D.32. TEST CASE, A MISSING FLEET

    @Test
    fun `6_D_33 TEST CASE, UNWANTED SUPPORT ALLOWED`() {
        """
    Austria:
    A Serbia - Budapest
    A Vienna - Budapest

    Russia:
    A Galicia Supports A Serbia - Budapest

    Turkey:
    A Bulgaria - Serbia
    Due to the Russian support, the army in Serbia advances to Budapest. This enables Turkey to capture Serbia with the army in Bulgaria.
    """
    }

    @Test
    fun `6_D_34 TEST CASE, SUPPORT TARGETING OWN AREA NOT ALLOWED`() {
        """
    Germany:
    A Berlin - Prussia
    A Silesia Supports A Berlin - Prussia
    F Baltic Sea Supports A Berlin - Prussia

    Italy:
    A Prussia Supports Livonia - Prussia

    Russia:
    A Warsaw Supports A Livonia - Prussia
    A Livonia - Prussia
    Russia and Italy wanted to get rid of the Italian army in Prussia (to build an Italian fleet somewhere else). However, they didn't want a possible German attack on Prussia to succeed. They invented this odd order of Italy. It was intended that the attack of the army in Livonia would have strength three, so it would be capable to prevent the possible German attack to succeed. However, the order of Italy is illegal, because a unit may only support to an area where the unit can go by itself. A unit can't go to the area it is already standing, so the Italian order is illegal and the German move from Berlin succeeds. Even if it would be legal, the German move from Berlin would still succeed, because the support of Prussia is cut by Livonia and Berlin.
    """
    }
}
