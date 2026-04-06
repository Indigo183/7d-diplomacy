package nodomain.seven.dip.game

import nodomain.seven.dip.adjudication.Adjudicator
import nodomain.seven.dip.orders.*
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.provinces.RomanPlayers
import nodomain.seven.dip.provinces.setup
import nodomain.seven.dip.utils.*

fun MutableMap<Piece, Player>.remove(province: Province) {
    keys.filter { it.location.province == province}.forEach { remove(it) }
}

fun MutableMap<Piece, Player>.remove(location: Location) {
    remove(keys.find { it.location == location})
}

operator fun Map<Piece, Player>.get(location: Location): Player? {
    return get(Army(location)) ?: get(Fleet(location))
}

fun Map<Piece, Player>.getEntry(piece: Piece): Pair<Piece, Player>? {
    return piece to (get(piece) ?: return null)
}
fun Map<Piece, Player>.getEntry(location: Location) =
    getEntry(Army(location)) ?: getEntry(Fleet(location))

data class RequiredRetreat(val piece: Piece, val  temporalFlare: TemporalFlare, val  player: Player)

enum class GameState {
    MOVES,
    RETREATS,
    BUILDS,
}

class Game(setup: Map<Piece, Player> = setup<RomanPlayers>()) {
    // All orders, past and present
    private val orders: MutableMap<Location, Order> = mutableMapOf()
    val moves: List<MoveOrder>
        get() = orders.values.filterIsInstance<MoveOrder>()
    val supports: List<SupportOrder>
        get() = orders.values.filterIsInstance<SupportOrder>()

    // Most recent adjudication results
    val adjudicators: MutableMap<TemporalFlare, Adjudicator> = mutableMapOf()

    // All units requiring retreats
    val requiredRetreats: MutableList<RequiredRetreat> = mutableListOf()

    // All inputted retreats/builds
    val locationsOfAdjustments: MutableMap<Location, Adjustment> = mutableMapOf() // Stores both retreats and builds
    val adjustments: Collection<Adjustment>
        get() = locationsOfAdjustments.values

    fun clearAdjustments() = locationsOfAdjustments.clear()
    fun addAdjustments(newAdjustments: List<Adjustment>) {
        for (adjustment in newAdjustments) locationsOfAdjustments[adjustment.piece.location] = adjustment
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
    fun addOrders(newOrders: List<Order>) {
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
            // Map each piece to the new location of the child board
            child.pieces = child.pieces.mapKeys {
                (piece, _) -> piece moveTo Location(piece.location.province, child.boardIndex)
            }.toMutableMap()
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

    val originalPieces: Map<Piece, Player>,
    val centres: MutableMap<Province, Player> = originalPieces.mapKeys {
        (piece, _) -> piece.location.province
    }.toMutableMap()
) {
    var pieces: MutableMap<Piece, Player> = originalPieces.toMutableMap()
    val children = mutableListOf<Board>()
    var isActive = true
        private set

    // Sets `isActive` to false (very useful comment)
    fun kill() {
        isActive = false
    }

    // Returns whether a board requires builds
    fun requiresBuilds(): Boolean {
        if (!isActive || !boardIndex.coordinate.isEven()) return false
        for (player in RomanPlayers.entries) if (countBuilds(player) != 0) return false
        return true
    }

    // Returns the number of builds a player has on a board
    fun countBuilds(player: Player) =
        centres.values.filter { it === player }.size - pieces.values.filter { it === player }.size

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
