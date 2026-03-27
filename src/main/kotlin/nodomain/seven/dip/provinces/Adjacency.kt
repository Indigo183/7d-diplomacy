package nodomain.seven.dip.provinces

import nodomain.seven.dip.orders.Piece
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.utils.Location
import nodomain.seven.dip.utils.c
import nodomain.seven.dip.utils.i
import kotlin.math.abs

enum class AdjacencyType(val isAdjacentOnForeignBoard: Province.(Province) -> Adjacency) {
    LOOSE({isAdjacentWith(it) + equals(it)}),
    STRICT({ Adjacency{ unit -> equals(it)} });
}

fun interface Adjacency {
    fun forUnit(piece: Piece?): Boolean
    fun forAnyUnit(): Boolean = forUnit(null)

    operator fun plus(alternative: Boolean): Adjacency {
        return Adjacency { forUnit(it) || alternative }
    }
}

// Checks if boards are adjacent, but not the same
fun BoardIndex.isAdjacentTo(other: BoardIndex, forPiece: Piece? = null): Boolean {
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

fun Location.isAdjacentTo(other: Location, adjacencyType: AdjacencyType = AdjacencyType.LOOSE, forPiece: Piece? = null): Boolean {
    return when {
        boardIndex == other.boardIndex -> // adjacency is local
            province isAdjacentWith other.province
        boardIndex.isAdjacentTo(other.boardIndex) ->
            province.(adjacencyType.isAdjacentOnForeignBoard)(other.province)
        else -> return false
    }.forUnit(forPiece)
}
