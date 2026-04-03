package nodomain.seven.dip.adjudication

import nodomain.seven.dip.orders.*
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.RomanPlayers.*
import nodomain.seven.dip.provinces.Romans.*
import nodomain.seven.dip.utils.*
import org.assertj.core.api.WithAssertions
import kotlin.test.Test

class AdjudicatorTest: WithAssertions {
    @Test
    fun whenTwoUnitsEnterLocation_thenABounceOccursThere() {
        val origin = T(0.c , 0)
        val pieces: Map<Piece, Player> = mapOf(origin A CAT to Cato, origin A POM to Pompey)
        val moves = listOf(
            origin A CAT M BRU i 3,
            origin A POM M BRU i 3
        )

        val results = Adjudicator(moves, listOf(), pieces).moveResults

        assertThat(results).contains(Bounce(origin[BRU]))
    }

    @Test
    fun whenTwoUnitsEnterLocationsUnOpposed_thenTheirMovesAreSuccessful() {
        val origin = T(0.c , 0)
        val pieces: Map<Piece, Player> = mapOf(origin A CAT to Cato, origin A POM to Pompey)
        val moves = listOf(
            origin A CAT M CAE i 3,
            origin A POM M BRU i 3
        )

        val results = Adjudicator(moves, listOf(), pieces).moveResults

        assertThat(results).containsAll(moves.map { SuccessfulMove(it) })
    }

    @Test
    fun whenAUnitEntersALocationWhereAUnitHolds_thenItsMoveIsNotSuccessful() {
        val origin = T(0.c , 0)
        val pieces: Map<Piece, Player> = mapOf(origin A CAT to Cato, origin A BRU to Pompey)
        val moves = listOf(
            origin A CAT M BRU i 3
        )

        val results = Adjudicator(moves, listOf(), pieces).moveResults

        assertThat(results).doesNotContainAnyElementsOf(moves.map { SuccessfulMove(it) })
    }

    @Test
    fun whenTwoUnitsEntersEachOthersLocation_thenTheirMovesAreNotSuccessful() {
        val origin = T(0.c , 0)
        val pieces: Map<Piece, Player> = mapOf(origin A CAE to Cato, origin A BRU to Pompey)
        val moves = listOf(
            origin A CAE M BRU i 3,
            origin A BRU M CAE i 3
        )

        val results = Adjudicator(moves, listOf(), pieces).moveResults

        assertThat(results).doesNotContainAnyElementsOf(moves.map { SuccessfulMove(it) })
    }

    @Test
    fun whenTwoUnitsEntersEachOthersLocationWithEqualSupport_thenTheirMovesAreNotSuccessful() {
        val origin = T(0.c , 0)
        val pieces: Map<Piece, Player> = mapOf(origin A CAE to Cato, origin A CAT to Cato, origin A BRU to Pompey, origin A POM to Pompey)
        val moves = listOf(
            origin A CAE M BRU i 3,
            origin A BRU M CAE i 3
        )
        val supports = listOf(
            origin A CAT S {origin A CAE M BRU i 3},
            origin A POM S {origin A BRU M CAE i 3}
        )

        val results = Adjudicator(moves, supports, pieces).moveResults

        assertThat(results).doesNotContainAnyElementsOf(moves.map { SuccessfulMove(it) })
    }

    @Test
    fun whenThreeUnitsEntersEachOthersLocationInACircle_thenTheirMovesAreSuccessful() {
        val origin = T(0.c , 0)
        val pieces: Map<Piece, Player> = mapOf(origin A CAT to Cato, origin A BRU to Pompey, origin A CAE to Pompey)
        val moves = listOf(
            origin A CAT M BRU i 3,
            origin A BRU M CAE i 3,
            origin A CAE M CAT i 3
        )

        val results = Adjudicator(moves, listOf(), pieces).moveResults

        assertThat(results).containsAll(moves.map { SuccessfulMove(it) })
    }

    @Test
    fun whenThreeUnitsEntersEachOthersLocationInACircleAndOneBounces_thenTheirMovesAllFail() {
        val origin = T(0.c , 0)
        val pieces: Map<Piece, Player> = mapOf(origin A CAT to Cato, origin A BRU to Pompey, origin A CAE to Pompey, origin A POM to Pompey)
        val moves = listOf(
            origin A CAT M BRU i 3,
            origin A BRU M CAE i 3,
            origin A CAE M CAT i 3,
            origin A POM M CAE i 3
        )

        val results = Adjudicator(moves, listOf(), pieces).moveResults

        assertThat(results).doesNotContainAnyElementsOf(moves.map { SuccessfulMove(it) })
    }
}