package nodomain.seven.dip

import nodomain.seven.dip.orders.Army
import nodomain.seven.dip.utils.ComplexNumber
import nodomain.seven.dip.utils.ComplexNumber.*

import nodomain.seven.dip.orders.Piece
import nodomain.seven.dip.orders.Location
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.provinces.RomanPlayers.*
import nodomain.seven.dip.provinces.Romans.*

data class BoardIndex(val boardIndex: ComplexNumber, val timeplane: Int = 0) {
    override fun toString(): String = "($boardIndex, T$timeplane)"
}

class Game() {
    private val _timeplanes: MutableList<Timeplane> = mutableListOf(
        mutableMapOf(
            ComplexNumber(0, 0) to
                    Board(BoardIndex(0*i))
        ))
    private val _limbo: MutableSet<Board> = mutableSetOf()
    val timeplanes: List<Timeplane>
        get() = _timeplanes
    val limbo: Set<Board>
        get() = _limbo

    fun getBoard(boardIndex: BoardIndex) = timeplanes[boardIndex.timeplane][boardIndex.boardIndex]!!

    fun addChild(board: Board, boardIndex: BoardIndex?) {
        val child = Board(boardIndex, board)
        board.children += child
        if (boardIndex !== null) {
            _timeplanes[boardIndex.timeplane][boardIndex.boardIndex] = child
        }
    }
}

typealias Timeplane = MutableMap<ComplexNumber, Board>
fun Timeplane.boards() = values

class Board(
    var boardIndex: BoardIndex?,
    val parent: Board? = null,
    val pieces: Map<Player, List<Piece>> = mapOf(
        Cato to listOf(Army(Location(CAT, BoardIndex(0*i)))),
        Pompey to listOf(Army(Location(POM, BoardIndex(0*i))))
    )) {
    val children = mutableListOf<Board>()
    var isActive = true
        private set

    override fun toString(): String {
        return """
            |Board {
            |    isActive: $isActive
            |    boardIndex: $boardIndex
            |    parent: ${parent.toString().prependIndent("    ").drop(4)}
            |    pieces: $pieces
            |}""".trimMargin() // JSON notation
    }
}

fun main() {
    val game = Game()
    game.addChild(game.getBoard(BoardIndex(0*i)), BoardIndex(1 + 0*i))

    for (timeplane in game.timeplanes) {
        for (board in timeplane.values) {
            testBoard(board)
        }
    }
}

fun testBoard(board: Board) {
    println("This is, in fact, a Board!\n$board\n\n")
}