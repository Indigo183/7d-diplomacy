package nodomain.seven.dip.datc

import nodomain.seven.dip.game.GameState.*
import nodomain.seven.dip.provinces.StandardCoast.*
import nodomain.seven.dip.provinces.StandardInLand.*
import nodomain.seven.dip.provinces.StandardPlayer.*
import nodomain.seven.dip.provinces.StandardSea.*
import kotlin.test.Test

//6.H. TEST CASES, RETREATING
object TestH: WithAssertionsDATC {
    @Test
    fun `6_H_1 TEST CASE, NO SUPPORTS DURING RETREAT`() {
        val game = """
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

        assertThat(game.requiredRetreats.locations).containsExactlyInAnyOrder(*retreatsIn(GRE, TRI))

        """
        |Austria:
        |F Trieste - Albania
        |A Serbia Supports F Trieste - Albania
        |
        |Turkey:
        |F Greece - Albania
        |""".parse(RETREATS).adjudicateAsDATC(expectAllOrderToBeValid = false, game = game)

        assertThat(game.pieces).doesNotContainKey(ALB)
    }

    @Test
    fun `6_H_2 TEST CASE, NO SUPPORTS FROM RETREATING UNIT`() {
        val game = """
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
        |""".parse().adjudicateAsDATC()

        assertThat(game.requiredRetreats.locations).containsExactlyInAnyOrder(*retreatsIn(NWY, EDI, HOL))

        """
        |England:
        |F Norway - North Sea
        |
        |Russia:
        |F Edinburgh - North Sea
        |F Holland Supports F Edinburgh - North Sea
        |""".parse(RETREATS).adjudicateAsDATC(expectAllOrderToBeValid = false, game = game)

        assertThat(game.pieces)
            .doesNotContainKey(NTH)
            .doesNotContainEntry(HOL, Russia)
    }

    //6.H.3. TEST CASE, NO CONVOY DURING RETREAT

    @Test
    fun `6_H_4 TEST CASE, NO OTHER MOVES DURING RETREAT`() {
        val game = """
        |England:
        |F North Sea Hold
        |A Holland Hold
        |
        |Germany:
        |F Kiel Supports A Ruhr - Holland
        |A Ruhr - Holland
        |""".parse().adjudicateAsDATC()

        assertThat(game.requiredRetreats.locations).containsExactlyInAnyOrder(*retreatsIn(HOL))

        """
        |England:
        |A Holland - Belgium
        |F North Sea - Norwegian Sea
        |""".parse(RETREATS).adjudicateAsDATC(expectAllOrderToBeValid = false, game = game)

        assertThat(game.pieces)
            .containsEntry(BEL, England)
            .doesNotContainKey(NWG)
    }

    @Test
    fun `6_H_5 TEST CASE, A UNIT MAY NOT RETREAT TO THE AREA FROM WHICH IT IS ATTACKED`() {
        val game = """
        |Russia:
        |F Constantinople Supports F Black Sea - Ankara
        |F Black Sea - Ankara
        |
        |Turkey:
        |F Ankara Hold
        |""".parse().adjudicateAsDATC()

        assertThat(game.requiredRetreats.locations).containsExactlyInAnyOrder(*retreatsIn(ANK))

        """
        |Turkey:
        |F Ankara - Black Sea
        |""".parse(RETREATS).adjudicateAsDATC(expectAllOrderToBeValid = false, game = game)

        assertThat(game.pieces).doesNotContainKey(BLA)
    }

    @Test
    fun `6_H_6 TEST CASE, UNIT MAY NOT RETREAT TO A CONTESTED AREA`() {
        val game = """
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
        |""".parse().adjudicateAsDATC()

        assertThat(game.requiredRetreats.locations).containsExactlyInAnyOrder(*retreatsIn(VIE))

        """
        |Italy:
        |A Vienna - Bohemia
        |""".parse(RETREATS).adjudicateAsDATC(game = game)

        assertThat(game.pieces).doesNotContainKey(BOH)
    }

    @Test
    fun `6_H_7 TEST CASE, MULTIPLE RETREAT TO SAME AREA WILL DISBAND UNITS`() {
        val game = """
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
        |""".parse().adjudicateAsDATC()

        assertThat(game.requiredRetreats.locations).containsExactlyInAnyOrder(*retreatsIn(VIE, BOH))

        """
        |Italy:
        |A Bohemia - Tyrolia
        |A Vienna - Tyrolia
        |""".parse(RETREATS).adjudicateAsDATC(game = game)

        assertThat(game.pieces).doesNotContainKey(TYR)
    }

    @Test
    fun `6_H_8 TEST CASE, TRIPLE RETREAT TO SAME AREA WILL DISBAND UNITS`() {
        val game = """
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
        |""".parse().adjudicateAsDATC()

        assertThat(game.requiredRetreats.locations).containsExactlyInAnyOrder(*retreatsIn(NWY, EDI, HOL))

        """
        |England:
        |F Norway - North Sea
        |
        |Russia:
        |F Edinburgh - North Sea
        |F Holland - North Sea
        |""".parse(RETREATS).adjudicateAsDATC(game = game)

        assertThat(game.pieces).doesNotContainKey(NTH)
    }

    @Test
    fun `6_H_9 TEST CASE, DISLODGED UNIT WILL NOT MAKE ATTACKERS AREA CONTESTED`() {
        val game = """
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
        |""".parse().adjudicateAsDATC()

        assertThat(game.requiredRetreats.locations).containsExactlyInAnyOrder(*retreatsIn(PRU, KIE))

        """
        |Germany:
        |F Kiel - Berlin
        |
        |Russia:
        |Remove A Prussia
        |""".parse(RETREATS).adjudicateAsDATC(game = game)

        assertThat(game.pieces).containsEntry(BER, Germany)

    }

    @Test
    fun `6_H_10 TEST CASE, NOT RETREATING TO ATTACKER DOES NOT MEAN CONTESTED`() {
        val game = """
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
        |""".parse().adjudicateAsDATC()

        assertThat(game.requiredRetreats.locations).containsExactlyInAnyOrder(*retreatsIn(KIE, PRU))

        """
        |England:
        |A Kiel - Berlin
        |
        |Germany:
        |A Prussia - Berlin
        |""".parse(RETREATS).adjudicateAsDATC(expectAllOrderToBeValid = false, game = game)

        assertThat(game.pieces).containsEntry(BER, Germany)
    }

    //6.H.11. TEST CASE, RETREAT WHEN DISLODGED BY ADJACENT CONVOY

    //6.H.12. TEST CASE, RETREAT WHEN DISLODGED BY ADJACENT CONVOY WHILE TRYING TO DO THE SAME

    //6.H.13. TEST CASE, NO RETREAT WITH CONVOY IN MOVEMENT PHASE

    @Test
    fun `6_H_14 TEST CASE, NO RETREAT WITH SUPPORT IN MOVEMENT PHASE`() {
        val game = """
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
        |""".parse().adjudicateAsDATC()

        assertThat(game.requiredRetreats.locations).containsExactlyInAnyOrder(*retreatsIn(PIC, BUR))

        """
        |England:
        |A Picardy - Belgium
        |
        |France:
        |A Burgundy - Belgium
        |""".parse(RETREATS).adjudicateAsDATC(game = game)

        assertThat(game.pieces).doesNotContainKey(BEL)
    }

    //6.H.15. TEST CASE, NO COASTAL CRAWL IN RETREAT

    //6.H.16. TEST CASE, CONTESTED FOR BOTH COASTS
}
