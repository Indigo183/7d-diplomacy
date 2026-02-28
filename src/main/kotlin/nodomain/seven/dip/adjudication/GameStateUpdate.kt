package nodomain.seven.dip.adjudication

import nodomain.seven.dip.game.*
import nodomain.seven.dip.orders.MoveOrder
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.orders.SupportOrder
import nodomain.seven.dip.orders.TemporalFlare
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.utils.Location

fun Game.sortOrders(orders: List<Order>): Pair<Map<TemporalFlare, List<MoveOrder>>, List<SupportOrder>> {
    // For future optimisation
    currentOrders = orders

    for (order in orders) when (order) {
        is MoveOrder -> moves += order
        is SupportOrder -> supports += order
        else -> {}
    }
    return Pair(moves.groupBy { it.flare !! }, supports)
}

fun Game.getAllPieces(player: Player? = null, onlyActive: Boolean = false): Map<Location, Player> {
    val pieces: MutableMap<Location, Player> = mutableMapOf()
    for (t in timeplanes) for (board in t.boards()) if (!onlyActive || board.isActive) for (piece in board.pieces) {
        if (player !== null && piece.value == player) {
            pieces[Location(piece.key, board.boardIndex)] = piece.value
        }
    }

    return pieces
}

// Adjudicate board in a single direction
fun Game.adjudicateBoard(boardIndex: BoardIndex, direction: TemporalFlare, moveResults: List<MoveResult>): Pair<Board, Board>? {
    val board = getBoard(boardIndex)!!
    println("killing board at ${board.boardIndex}")
    kill(board)
    // Do nothing if nothing relevant happened
    if (moveResults.isEmpty()) return null

    val pieces: MutableMap<Province, Player> = board.pieces.toMutableMap()
    val centres: MutableMap<Province, Player> = board.centres.toMutableMap()

    for (move in moveResults) if (move is SuccessfulMove) {
        // Add pieces moving to board
        if (move.moveOrder.action.to.boardIndex == board.boardIndex) {
            pieces[move.moveOrder.action.to.province] =
                getBoard(move.moveOrder.piece.location.boardIndex)?.pieces[move.moveOrder.from.province]
                ?: throw IllegalStateException("order not properly validated")
        }
        // Remove pieces moving from board
        if (move.moveOrder.from.boardIndex == board.boardIndex) {
            pieces.remove(move.moveOrder.from.province)
        }
    }

    // Compare the new board with the last produced child
    val newChild = Board(
        BoardIndex(board.boardIndex.coordinate + direction.direction, board.boardIndex.timeplane),
        board,
        pieces,
        centres,
    )
    val newestChild = board.children.lastOrNull { limboBoard ->
        limboBoard.boardIndex.coordinate - board.boardIndex.coordinate == direction.direction }
    return if (
        newestChild === null ||
        (newChild.pieces == newestChild.pieces && newChild.centres == newestChild.centres)
    ) {
        Pair(board, newChild)
    } else null
}

fun Game.adjudicateMoves() {
    val pieces = getAllPieces()
    val boardIndexes = timeplanes.flatMap { it.keys } // to ensure the list of boards isn't updated
    val parentChildBoardPairs: MutableList<Pair<Board, Board>> = mutableListOf()
    val adjudicators: MutableMap<TemporalFlare, Adjudicator> = mutableMapOf()

    // Generate children
    for (flare in TemporalFlare.entries) {
        println("INFO: adjudicating $flare")
        val adjudicator = Adjudicator(moves.filter { it.flare == flare }, supports, pieces)
        adjudicators[flare] = adjudicator
        println("INFO: with results: ${adjudicator.moveResults}")
        for (index in boardIndexes) {
            println("INFO: adjudicating board at ${index}")
            parentChildBoardPairs.add(adjudicateBoard(index, flare, adjudicator.moveResults.filter {
                it !is SuccessfulMove || // "it is Bounce"
                it.moveOrder.from.boardIndex == index ||
                it.moveOrder.action.to.boardIndex == index
            }) ?: continue)
        }
    }

    // Add children
    for ((boardIndex, pairs) in parentChildBoardPairs.groupBy { (_, child) -> child.boardIndex }) {
        if (pairs.size == 1) addChild(pairs[0].first, pairs[0].second)
        // TODO: "else <check board bounce>"
    }

    //val boardDestinations: List<BoardIndex> = mutableListOf()
    //for ((parent, child) in parentChildBoardPairs)

    advanceState()
    if (retreats.isEmpty()) adjudicateRetreats()
}

fun Game.adjudicateRetreats() {
    retreats.clear()
    advanceState()
    if (timeplanes.flatMap { it.boards().filter { it.isActive && it.boardIndex.coordinate.isEven() } }.isEmpty()) {
        adjudicateBuilds()
    }
}

fun Game.adjudicateBuilds() {
    val boards = timeplanes.flatMap { it.boards().filter { it.isActive && it.boardIndex.coordinate.isEven() } }
    for (board in boards); // TODO: adjudicate builds
    advanceState()
}

fun Game.adjudicate() {
    when (gameState) {
        GameState.MOVES -> adjudicateMoves()
        GameState.RETREATS -> adjudicateRetreats()
        GameState.BUILDS -> adjudicateBuilds()
    }
}
