package nodomain.seven.dip.game

import nodomain.seven.dip.orders.MoveOrder
import nodomain.seven.dip.orders.Order
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

    // Create child from component parts
    fun addChild(parent: Board, boardIndex: BoardIndex, pieces: Map<Province, Player>, centres: Map<Province, Player>) {
        val child = Board(boardIndex, parent, pieces, centres)
        parent.children += child
        if (boardIndex.timeplane !== null) {
            _timeplanes[boardIndex.timeplane][boardIndex.coordinate] = child
        } else {
            _limbo += child
        }
    }

    // Add child directly
    fun addChild(parent: Board, child: Board) {
        if (child.parent !== parent) throw IllegalArgumentException("`child.parent` is not equal to `parent`")
        parent.children += child
        if (child.boardIndex.timeplane !== null) {
            _timeplanes[child.boardIndex.timeplane!!][child.boardIndex.coordinate] = child
        } else {
            _limbo += child
        }
    }

    // TODO: implement actual rigorous logic
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

    // set `isActive` to false
    fun kill() {
        if (isActive) isActive = false else println("WARN: called `Board.kill()` on an already dead board")
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
