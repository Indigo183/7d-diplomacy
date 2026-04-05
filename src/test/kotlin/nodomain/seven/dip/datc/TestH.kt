package nodomain.seven.dip.datc

import nodomain.seven.dip.game.GameState.*
import nodomain.seven.dip.orders.RetreatOrder
import nodomain.seven.dip.provinces.StandardCoast.*
import nodomain.seven.dip.provinces.StandardInLand.*
import nodomain.seven.dip.provinces.StandardPlayer.*
import kotlin.test.Test

//6.H. TEST CASES, RETREATING
object TestH: WithAssertionsDATC {
    @Test
    fun `6_H_1 TEST CASE, NO SUPPORTS DURING RETREAT`() { // Modified due to involving sea regions
        val testGame = """
        |Austria:
        |F Trieste Hold
        |A Serbia Hold
        |
        |Turkey:
        |F Greece Hold
        |
        |Italy:
        |A Venice Supports A Tyrolia - Trieste
        |A Tyrolia - Trieste
        |F Ionian Sea - Greece
        |F Aegean Sea Supports F Ionian Sea - Greece
        |""".parse().adjudicateAsDATC()

        assertThat(testGame.requiredRetreats.locations).containsExactlyInAnyOrder(*retreatsIn(GRE, TRI))

        """
        |Austria:
        |F Trieste - Albania
        |A Serbia Supports F Trieste - Albania
        |
        |Turkey:
        |F Greece - Albania
        |""".parse(RETREATS).adjudicateAsDATC(game = testGame)

        assertThat(testGame.pieces)
            .doesNotContainEntry(ALB, Austria)
            .doesNotContainEntry(ALB, Turkey)
    }

    @Test
    fun `6_H_2 TEST CASE, NO SUPPORTS FROM RETREATING UNIT`() {
        """
        |England:
        |A Liverpool - Edinburgh
        |F Yorkshire Supports A Liverpool - Edinburgh
        |F Norway Hold
        |
        |Germany:
        |A Kiel Supports A Ruhr - Holland
        |A Ruhr - Holland
        |
        |Russia:
        |F Edinburgh Hold
        |A Sweden Supports A Finland - Norway
        |A Finland - Norway
        |F Holland Hold
        |The English fleet in Norway and the Russian fleets in Edinburgh and Holland are dislodged. If the following retreat orders are given:
        |""".parse().adjudicateAsDATC()
        """
        |England:
        |F Norway - North Sea
        |
        |Russia:
        |F Edinburgh - North Sea
        |F Holland Supports F Edinburgh - North Sea
        |Although the fleet in Holland may receive an order, it may not support (it is disbanded). The English fleet in Norway and the Russian fleet in Edinburgh bounce and are disbanded.
        |"""
    }

    //6.H.3. TEST CASE, NO CONVOY DURING RETREAT

    @Test
    fun `6_H_4 TEST CASE, NO OTHER MOVES DURING RETREAT`() {
        """
        |England:
        |F North Sea Hold
        |A Holland Hold
        |
        |Germany:
        |F Kiel Supports A Ruhr - Holland
        |A Ruhr - Holland
        |The English army in Holland is dislodged. If England orders the following in retreat:
        |""".parse().adjudicateAsDATC()
        """
        |England:
        |A Holland - Belgium
        |F North Sea - Norwegian Sea
        |The fleet in the North Sea is not dislodge, so the move is illegal.
        |"""
    }

    @Test
    fun `6_H_5 TEST CASE, A UNIT MAY NOT RETREAT TO THE AREA FROM WHICH IT IS ATTACKED`() {
        """
        |Russia:
        |F Constantinople Supports F Black Sea - Ankara
        |F Black Sea - Ankara
        |
        |Turkey:
        |F Ankara Hold
        |Fleet in Ankara is dislodged and may not retreat to Black Sea.
        |""".parse().adjudicateAsDATC()
    }

    @Test
    fun `6_H_6 TEST CASE, UNIT MAY NOT RETREAT TO A CONTESTED AREA`() {
        """
        |Austria:
        |A Budapest Supports A Trieste - Vienna
        |A Trieste - Vienna
        |
        |Germany:
        |A Munich - Bohemia
        |A Silesia - Bohemia
        |
        |Italy:
        |A Vienna Hold
        |The Italian army in Vienna is dislodged. It may not retreat to Bohemia.
        |""".parse().adjudicateAsDATC()
    }

    @Test
    fun `6_H_7 TEST CASE, MULTIPLE RETREAT TO SAME AREA WILL DISBAND UNITS`() {
        """
        |Austria:
        |A Budapest Supports A Trieste - Vienna
        |A Trieste - Vienna
        |
        |Germany:
        |A Munich Supports A Silesia - Bohemia
        |A Silesia - Bohemia
        |
        |Italy:
        |A Vienna Hold
        |A Bohemia Hold
        |If Italy orders the following for retreat:
        |""".parse().adjudicateAsDATC()
        """
        |Italy:
        |A Bohemia - Tyrolia
        |A Vienna - Tyrolia
        |Both armies will be disbanded.
        |"""
    }

    @Test
    fun `6_H_8 TEST CASE, TRIPLE RETREAT TO SAME AREA WILL DISBAND UNITS`() {
        """
        |England:
        |A Liverpool - Edinburgh
        |F Yorkshire Supports A Liverpool - Edinburgh
        |F Norway Hold
        |
        |Germany:
        |A Kiel Supports A Ruhr - Holland
        |A Ruhr - Holland
        |
        |Russia:
        |F Edinburgh Hold
        |A Sweden Supports A Finland - Norway
        |A Finland - Norway
        |F Holland Hold
        |The fleets in Norway, Edinburgh and Holland are dislodged. If the following retreat orders are given:
        |""".parse().adjudicateAsDATC()
        """
        |England:
        |F Norway - North Sea
        |
        |Russia:
        |F Edinburgh - North Sea
        |F Holland - North Sea
        |All three units are disbanded.
        |"""
    }

    @Test
    fun `6_H_9 TEST CASE, DISLODGED UNIT WILL NOT MAKE ATTACKERS AREA CONTESTED`() {
        """
        |England:
        |F Helgoland Bight - Kiel
        |F Denmark Supports F Helgoland Bight - Kiel
        |
        |Germany:
        |A Berlin - Prussia
        |F Kiel Hold
        |A Silesia Supports A Berlin - Prussia
        |
        |Russia:
        |A Prussia - Berlin
        |The fleet in Kiel can retreat to Berlin.
        |""".parse().adjudicateAsDATC()
    }

    @Test
    fun `6_H_10 TEST CASE, NOT RETREATING TO ATTACKER DOES NOT MEAN CONTESTED`() {
        """
        |England:
        |A Kiel Hold
        |
        |Germany:
        |A Berlin - Kiel
        |A Munich Supports A Berlin - Kiel
        |A Prussia Hold
        |
        |Russia:
        |A Warsaw - Prussia
        |A Silesia Supports A Warsaw - Prussia
        |The armies in Kiel and Prussia are dislodged. The English army in Kiel cannot retreat to Berlin, but the army in Prussia can retreat to Berlin. Suppose the following retreat orders are given:
        |""".parse().adjudicateAsDATC()
        """
        |England:
        |A Kiel - Berlin
        |
        |Germany:
        |A Prussia - Berlin
        |The English retreat to Berlin is illegal and fails (the unit is disbanded). The German retreat to Berlin is successful and does not bounce on the English unit.
        |"""
    }

    //6.H.11. TEST CASE, RETREAT WHEN DISLODGED BY ADJACENT CONVOY

    //6.H.12. TEST CASE, RETREAT WHEN DISLODGED BY ADJACENT CONVOY WHILE TRYING TO DO THE SAME

    //6.H.13. TEST CASE, NO RETREAT WITH CONVOY IN MOVEMENT PHASE

    @Test
    fun `6_H_14 TEST CASE, NO RETREAT WITH SUPPORT IN MOVEMENT PHASE`() {
        """
        |England:
        |A Picardy Hold
        |F English Channel Supports A Picardy - Belgium
        |
        |France:
        |A Paris - Picardy
        |A Brest Supports A Paris - Picardy
        |A Burgundy Hold
        |
        |Germany:
        |A Munich Supports A Marseilles - Burgundy
        |A Marseilles - Burgundy
        |After the movement phase the following retreat orders are given:
        |""".parse().adjudicateAsDATC()
        """
        |England:
        |A Picardy - Belgium
        |
        |France:
        |A Burgundy - Belgium
        |Both the army in Picardy and Burgundy are disbanded.
        |"""
    }

    //6.H.15. TEST CASE, NO COASTAL CRAWL IN RETREAT

    //6.H.16. TEST CASE, CONTESTED FOR BOTH COASTS
}
