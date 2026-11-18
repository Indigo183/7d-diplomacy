package nodomain.seven.dip.provinces

import nodomain.seven.dip.orders.Piece
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.utils.Spot
import nodomain.seven.dip.utils.c
import nodomain.seven.dip.utils.i
import kotlin.math.abs

fun ProvincialSpot.equalTo(other: ProvincialSpot): Adjacency = Adjacency {equals(other)}

enum class AdjacencyType(val isAdjacentOnForeignBoard: ProvincialSpot.(Piece) -> Boolean) {
    LOOSE({isAdjacentFor(it) || equals(it.spot.provincialSpot)}),
    STRICT({equals(it.spot.provincialSpot)});
}

fun interface Adjacency {
    fun forUnit(piece: Piece?): Boolean
    fun forAnyUnit(): Boolean = forUnit(null)

    operator fun plus(alternative: Adjacency): Adjacency {
        return Adjacency { forUnit(it) || alternative.forUnit(it) }
    }
}

// Checks if boards are adjacent, but not the same
fun BoardIndex.isAdjacentTo(other: BoardIndex): Boolean {
    if (timeplane === null || other.timeplane === null) {
        System.err.println("WARNING: called `BoardIndex.isAdjacentTo()` on a BoardIndex in Limbo")
        return false
    }
    return if (coordinate == other.coordinate) {
        abs(timeplane!! - other.timeplane!!) == 1
    } else if (timeplane!! == other.timeplane!!) when (coordinate - other.coordinate) {
        i, -i, 1.c, (-1).c -> true
        else -> false
    } else false
}

fun Spot.isAdjacentTo(other: Piece, adjacencyType: AdjacencyType = AdjacencyType.LOOSE): Boolean {
    return when {
        boardIndex == other.spot.boardIndex -> // adjacency is local
            provincialSpot isAdjacentFor other
        boardIndex.isAdjacentTo(other.spot.boardIndex) ->
            provincialSpot.(adjacencyType.isAdjacentOnForeignBoard)(other)
        else -> return false
    }
}
