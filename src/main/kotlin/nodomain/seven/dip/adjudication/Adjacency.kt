package nodomain.seven.dip.adjudication

import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.utils.Location
import nodomain.seven.dip.utils.c
import nodomain.seven.dip.utils.i

enum class Adjacency(val isAdjacentOnForeignBoard: Province.(Province) -> Boolean) {
    LOOSE({isAdjacentTo(it) || equals(it)}),
    STRICT({equals(it)});
}

// Checks if boards are adjacent, but not the same
fun BoardIndex.isAdjacentTo(other: BoardIndex): Boolean {
    if (timeplane === null || other.timeplane === null) {
        System.err.println("WARNING: called `BoardIndex.isAdjacentTo()` on a BoardIndex in Limbo")
        return false
    }
    return if (coordinate == other.coordinate) {
        timeplane - other.timeplane == 1 || timeplane - other.timeplane == -1
    } else if (timeplane == other.timeplane) when (coordinate - other.coordinate) {
        i, -i, 1.c, (-1).c -> true
        else -> false
    } else false
}

fun Location.isAdjacentTo(other: Location, adjacencyType: Adjacency = Adjacency.LOOSE): Boolean {
    return if (boardIndex == other.boardIndex) { // adjacency is local
        province isAdjacentTo other.province
    } else { // adjacency is non-local
        boardIndex.isAdjacentTo(other.boardIndex) &&
                (province.(adjacencyType.isAdjacentOnForeignBoard)(other.province))
    }
}