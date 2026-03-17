package nodomain.seven.dip.game

import nodomain.seven.dip.orders.*
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.provinces.RomanPlayers
import nodomain.seven.dip.provinces.setup
import nodomain.seven.dip.utils.*

enum class GameState {
    MOVES,
    RETREATS,
    BUILDS,
}

class Game(setup: Map<Province, Player> = setup<RomanPlayers>()) {
    // All orders, past and present
    private val orders: MutableMap<Location, Order> = mutableMapOf()
    val moves: List<MoveOrder>
        get() = orders.values.filterIsInstance<MoveOrder>()
    val supports: List<SupportOrder>
        get() = orders.values.filterIsInstance<SupportOrder>()

    // All units requiring retreats
    val retreats: MutableList<Location> = mutableListOf()

    // All inputted retreats/builds
    private val _adjustments: MutableMap<Location, Adjustment> = mutableMapOf() // Stores both retreats and builds
    val adjustments: Collection<Adjustment>
        get() = _adjustments.values

    fun clearAdjustments() = _adjustments.clear()
    fun addAdjustments(newAdjustments: List<Adjustment>) {
        for (item in newAdjustments) _adjustments[item.piece.location]
    }

    // For future optimisation of adjudication
    var currentOrders: List<Order> = listOf()

    // Interior mutability
    private val _timeplanes: MutableList<Timeplane> = mutableListOf( // Stored bottom-up
        mutableMapOf(
            0.c to Board(BoardIndex(0.c), originalPieces = setup.toMutableMap())
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
            timeplanes.getOrNull(boardIndex.timeplane!!)?.get(boardIndex.coordinate)
        } else {
            limbo.singleOrNull { board -> board.boardIndex.coordinate == boardIndex.coordinate }
        }
    }

    // Add new orders to the map of orders
    fun Game.addOrders(newOrders: List<Order>) {
        // TODO: For future optimisation
        currentOrders = newOrders

        // for (order in orders) when (order) {
        //     is MoveOrder -> moves += order
        //     is SupportOrder -> supports += order
        //     else -> {}
        // }

        for (order in newOrders) {
            val location = order.piece.location
            if (orders[location] !== null) println("WARN: overwriting order in $location")
            orders[location] = order
        }
    }

    // Add child directly
    fun addChild(parent: Board, child: Board) {
        if (child.parent !== parent) throw IllegalArgumentException("`child.parent` is not equal to `parent`")
        parent.children += child
        if (child.boardIndex.timeplane !== null) {
            // Propagate child up
            var iter = child.boardIndex.timeplane!!
            while (getBoard(BoardIndex(child.boardIndex.coordinate, iter)) !== null) iter++
            child.boardIndex.timeplane = iter
            // Create new timeplane if necessary
            while (timeplanes.getOrNull(child.boardIndex.timeplane!!) === null)
                _timeplanes += mutableMapOf()
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

    val originalPieces: Map<Province, Player>,
    val centres: MutableMap<Province, Player> = originalPieces.toMutableMap() // clone pieces
) {
    val pieces: MutableMap<Province, Player> = originalPieces.toMutableMap()
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
            |    parent: ${parent?.boardIndex}
            |    pieces: $pieces
            |    centres: $centres
            |}""".trimMargin() // JSON notation
        //|    parent: ${parent.toString().prependIndent("    ").drop(4)}
    }
}
