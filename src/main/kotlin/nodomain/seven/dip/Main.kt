package nodomain.seven.dip

import nodomain.seven.dip.utils.ComplexNumber

data class Location(val boardIndex: ComplexNumber, val timeplane: Int = 0)

class Game() {
    private val _timeplanes: MutableList<Timeplane> = mutableListOf(
        mutableMapOf(
            ComplexNumber(0, 0) to
                    Board(Location(ComplexNumber(0, 0)))
        ))
    private val _limbo: MutableSet<Board> = mutableSetOf()
    val timeplanes: List<Timeplane>
        get() = _timeplanes
    val limbo: Set<Board>
        get() = _limbo
}
typealias Timeplane = MutableMap<ComplexNumber, Board>
fun Timeplane.boards() = values
class Board(var location: Location?, val parent: Location? = null) {
    override fun toString(): String {
        return "Board {\n    location: $location\n}" // JSON notation
    }
    var isActive = true
        private set
}

fun main() {
    val game = Game()
    for (timeplane in game.timeplanes) {
        for (board in timeplane.values) {
            testBoard(board)
        }
    }
}

fun testBoard(board: Board) {
    println("This is, in fact, a Board!\n\n$board")
}