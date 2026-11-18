package nodomain.seven.dip.game

import nodomain.seven.dip.orders.*
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.ProvincialSpot
import nodomain.seven.dip.provinces.RomanPlayers
import nodomain.seven.dip.provinces.setup
import nodomain.seven.dip.utils.*
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import nodomain.seven.dip.adjudication.AdjudicationResult
import nodomain.seven.dip.provinces.Owned
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.provinces.having
import java.io.Serializable

fun MutableMap<Piece, Player>.remove(provincialSpot: ProvincialSpot) {
    keys.filter { it.spot.provincialSpot == provincialSpot}.forEach { remove(it) }
}

fun MutableMap<Piece, Player>.remove(spot: Spot) {
    remove(keys.find { it.spot == spot})
}

operator fun Map<Piece, Player>.get(spot: Spot): Player? {
    return get(Army(spot)) ?: get(Fleet(spot))
}

fun Map<Piece, Player>.getEntry(piece: Piece): Pair<Piece, Player>? {
    return piece to (get(piece) ?: return null)
}
fun Map<Piece, Player>.getEntry(spot: Spot) =
    getEntry(Army(spot)) ?: getEntry(Fleet(spot))

data class RequiredRetreat(
    val piece: Piece,
    val temporalFlare: TemporalFlare,
    val player: Player,
    val disallowed: Spot)

enum class GameState {
    MOVES,
    RETREATS,
    BUILDS,
}

class Game(setup: Map<Piece, Player> = setup<RomanPlayers>()): Serializable {
    companion object {
        const val serialVersionUID: Long = 1
    }

    // The current "turn" in terms of move phases only
    var turn = 1
        private set

    // All orders, past and present
    private val orders: MutableMap<Spot, Order> = mutableMapOf()
    val moves: List<MoveOrder>
        get() = orders.values.filterIsInstance<MoveOrder>()
    val supports: List<SupportOrder>
        get() = orders.values.filterIsInstance<SupportOrder>()
    val convoys: List<ConvoyOrder>
        get() = orders.values.filterIsInstance<ConvoyOrder>()

    // Most recent adjudication results
    @JsonIgnore
    val adjudicators: MutableMap<TemporalFlare, AdjudicationResult> = mutableMapOf()

    // All units requiring retreats
    val requiredRetreats: MutableList<RequiredRetreat> = mutableListOf()

    // All inputted retreats/builds
    @JsonIgnore
    val locationsOfAdjustments: MutableMap<Spot, Adjustment> = mutableMapOf() // Stores both retreats and builds
    @get:JsonIgnore
    val adjustments: Collection<Adjustment>
        get() = locationsOfAdjustments.values

    fun clearAdjustments() = locationsOfAdjustments.clear()
    fun addAdjustments(newAdjustments: List<Adjustment>) {
        for (adjustment in newAdjustments) locationsOfAdjustments[adjustment.piece.spot] = adjustment
    }

    // For future optimisation of adjudication
    @JsonIgnore
    var currentOrders: List<Order> = listOf()

    // Interior mutability
    private val _timeplanes: MutableList<Timeplane> = mutableListOf( // Stored bottom-up
        mutableMapOf(
            0.c to Board(BoardIndex(0.c), originalPieces = setup.entries.associate {
                it.key.spot.province to Owned(it.key, it.value)
            }))
        )
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
            val location = order.piece.spot
            if (orders[location] !== null) println("WARN: overwriting order in $location")
            orders[location] = order
        }
    }

    // Add child directly
    fun addChild(parent: Board, child: Board) {
        if (child.parent !== parent.boardIndex) throw IllegalArgumentException(
            "`child.parent.boardIndex` is not equal to `parent`"
        )
        parent.children += child.boardIndex
        if (child.boardIndex.timeplane !== null) {
            // Propagate child up
            var iter = child.boardIndex.timeplane!!
            while (getBoard(BoardIndex(child.boardIndex.coordinate, iter)) !== null) iter++
            child.boardIndex.timeplane = iter
            // Map each piece to the new location of the child board
            child.pieces = child.pieces.mapValues {
                (_, ownedPiece) -> ownedPiece.player.having(ownedPiece.component1() moveTo Spot(ownedPiece.component1().spot.provincialSpot, child.boardIndex))
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
            GameState.BUILDS -> {
                turn++
                GameState.MOVES
            }
        }
    }
}

typealias Timeplane = MutableMap<ComplexNumber, Board>
fun Timeplane.boards() = values

class Board(
    var boardIndex: BoardIndex,
    val parent: BoardIndex? = null, // null represents the origin board

    @JsonIgnore val originalPieces: Map<Province, Owned<Piece>>,
    val centres: MutableMap<Province, Player> = originalPieces.mapValues {
        (_, piece) -> piece.player
    }.toMutableMap()
): Serializable {
    @JsonIgnore var pieces: MutableMap<Province, Owned<Piece>> = originalPieces.toMutableMap()
    val children = mutableListOf<BoardIndex>()
    var isActive = true
        private set

    //Jackson friendly versions of pieces and original pieces
    val piecesForJson: Map<Player, List<Piece>>
        @JsonProperty("pieces") get() = pieces.values.groupBy({ it.player }, { it.component1() })

    val originalPiecesForJson: Map<Player, List<Piece>>
        @JsonProperty("original_pieces") get() = originalPieces.values.groupBy({ it.player }, { it.component1() })

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
        centres.values.filter { it === player }.size - pieces.values.filter { it.player === player }.size

    override fun toString(): String {
        return """
            |Board {
            |    isActive: $isActive
            |    boardIndex: $boardIndex
            |    parent: $parent
            |    pieces: $pieces
            |    originalPieces: $originalPieces
            |    centres: $centres
            |}""".trimMargin() // JSON notation
        //|    parent: ${parent.toString().prependIndent("    ").drop(4)}
    }
}
