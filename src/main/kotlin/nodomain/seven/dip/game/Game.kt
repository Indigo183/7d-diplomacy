package nodomain.seven.dip.game

import nodomain.seven.dip.orders.MoveOrder
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.orders.Piece
import nodomain.seven.dip.utils.*

import nodomain.seven.dip.orders.SupportOrder
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.provinces.RomanPlayers
import nodomain.seven.dip.provinces.setup
import nodomain.seven.dip.utils.BoardIndex

enum class GameState {
    MOVES,
    RETREATS,
    BUILDS,
}

class Game(setup: Map<Province, Player> = setup<RomanPlayers>()) {
    val supports: MutableList<SupportOrder> = mutableListOf()
    val moves: MutableList<MoveOrder> = mutableListOf()
    val retreats: MutableList<Piece> = mutableListOf()

    // For future optimisation of adjudication
    var currentOrders: List<Order> = listOf()

    // Interior mutability
    private val _timeplanes: MutableList<Timeplane> = mutableListOf( // Stored bottom-up
        mutableMapOf(
            0.c to Board(BoardIndex(0.c), pieces = setup)
        ))
    private val _limbo: MutableSet<Board> = mutableSetOf() // Set of all boards currently in Limbo

    // Public immutable interface
    val timeplanes: List<Timeplane>
        get() = _timeplanes
    val limbo: Set<Board>
        get() = _limbo

    var gameState: GameState = GameState.MOVES
        private set

    fun getBoard(boardIndex: BoardIndex): Board? {
        return if (boardIndex.timeplane !== null) {
            timeplanes.getOrNull(boardIndex.timeplane)?.get(boardIndex.coordinate)
        } else {
            limbo.singleOrNull { board -> board.boardIndex.coordinate == boardIndex.coordinate }
        }
    }

    fun kill(board: Board) = getBoard(board.boardIndex)?.kill() ?: println("WARN: attempted to kill non-existent board")

    // Add child directly
    fun addChild(parent: Board, child: Board) {
        if (child.parent !== parent) throw IllegalArgumentException("`child.parent` is not equal to `parent`")
        parent.children += child
        if (child.boardIndex.timeplane !== null) {
            if (timeplanes.getOrNull(child.boardIndex.timeplane!!) === null) // Create new timeplane
                _timeplanes[child.boardIndex.timeplane!!] = mutableMapOf()
            _timeplanes[child.boardIndex.timeplane!!][child.boardIndex.coordinate] = child
        } else {
            _limbo += child
        }
    }

    fun advanceState() {
        gameState = when (gameState) {
            GameState.MOVES -> GameState.RETREATS
            GameState.RETREATS -> GameState.BUILDS
            GameState.BUILDS -> GameState.MOVES
        }
    }
}

typealias Timeplane = MutableMap<ComplexNumber, Board>
fun Timeplane.boards() = values

class Board(
    var boardIndex: BoardIndex,
    val parent: Board? = null, // null represents the origin board

    val pieces: Map<Province, Player>,
    val centres: Map<Province, Player> = pieces
) {
    val children = mutableListOf<Board>()
    var isActive = true
        private set

    // set `isActive` to false (very useful comment)
    fun kill() {
        isActive = false
    }

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
