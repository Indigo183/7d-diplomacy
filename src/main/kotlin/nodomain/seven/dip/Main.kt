package nodomain.seven.dip

import nodomain.seven.dip.utils.ComplexNumber
import nodomain.seven.dip.utils.ComplexNumber.*

data class Location(val boardIndex: ComplexNumber, val timeplane: Int = 0)

class Game() {
    private val _timeplanes: MutableList<Timeplane> = mutableListOf(
        mutableMapOf(
            ComplexNumber(0, 0) to
                    Board(Location(0*i))
        ))
    private val _limbo: MutableSet<Board> = mutableSetOf()
    val timeplanes: List<Timeplane>
        get() = _timeplanes
    val limbo: Set<Board>
        get() = _limbo

    fun getBoard(location: Location) = timeplanes[location.timeplane][location.boardIndex]!!

    fun addChild(board: Board, location: Location?) {
        val child = Board(location, board)
        board.children += child
        if (location !== null) {
            _timeplanes[location.timeplane][location.boardIndex] = child
        }
    }
}

typealias Timeplane = MutableMap<ComplexNumber, Board>
fun Timeplane.boards() = values

class Board(var location: Location?, val parent: Board? = null) {
    val children = mutableListOf<Board>()
    var isActive = true
        private set

    override fun toString(): String {
        return """
            |Board {
            |    isActive: $isActive
            |    location: $location
            |    parent: ${parent.toString().prependIndent("    ").drop(4)}
            |}""".trimMargin() // JSON notation
    }
}

fun main() {
    val game = Game()
    game.addChild(game.getBoard(Location(0*i)), Location(1 + 0*i))

    for (timeplane in game.timeplanes) {
        for (board in timeplane.values) {
            testBoard(board)
        }
    }
}

fun testBoard(board: Board) {
    println("This is, in fact, a Board!\n$board\n\n")
}