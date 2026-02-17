package nodomain.seven.dip.game

import nodomain.seven.dip.orders.MoveOrder
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.utils.ComplexNumber
import nodomain.seven.dip.utils.ComplexNumber.*

import nodomain.seven.dip.orders.SupportOrder
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.provinces.RomanPlayers.*
import nodomain.seven.dip.provinces.Romans.*
import nodomain.seven.dip.utils.BoardIndex

class Game {
    val supports: MutableList<SupportOrder> = mutableListOf()
    val moves: MutableList<MoveOrder> = mutableListOf()

    // For future optimisation of adjudication
    var currentOrders: List<Order> = listOf()

    // Interior mutability
    private val _timeplanes: MutableList<Timeplane> = mutableListOf( // Stored bottom-up
        mutableMapOf(
            ComplexNumber(0, 0) to
                    Board(BoardIndex(0 * i))
        ))
    private val _limbo: MutableSet<Board> = mutableSetOf() // Set of all boards currently in Limbo

    // Public immutable interface
    val timeplanes: List<Timeplane>
        get() = _timeplanes
    val limbo: Set<Board>
        get() = _limbo

    fun getBoard(boardIndex: BoardIndex): Board? = timeplanes.getOrNull(boardIndex.timeplane)?.get(boardIndex.coordinate)

    fun addChild(board: Board, boardIndex: BoardIndex?) {
        val child = Board(boardIndex, board)
        board.children += child
        if (boardIndex !== null) {
            _timeplanes[boardIndex.timeplane][boardIndex.coordinate] = child
        }
    }
}

typealias Timeplane = MutableMap<ComplexNumber, Board>
fun Timeplane.boards() = values

class Board(
    var boardIndex: BoardIndex?, // null represents a board in Limbo
    val parent: Board? = null, // null represents the origin board
    val pieces: Map<Player, List<Province>> = mapOf(
        Cato to listOf(CAT),
        Pompey to listOf(POM)
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