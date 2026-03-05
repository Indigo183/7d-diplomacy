package nodomain.seven.dip.adjudication

import nodomain.seven.dip.game.*
import nodomain.seven.dip.orders.*
import nodomain.seven.dip.orders.TemporalFlare
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.provinces.RomanPlayers
import nodomain.seven.dip.utils.*

fun Game.getAllPieces(player: Player? = null, onlyActive: Boolean = false): Map<Location, Player> {
    return timeplanes.asSequence()
        .flatMap{it.boards()}
        .filter{!onlyActive || it.isActive}
        .flatMap{it.pieces.asSequence()
            .filter{ (_, owner) -> player === null || owner == player }
            .map{ (province, owner) -> Location(province, it.boardIndex) to owner }
        }.toMap()
}

// Adjudicate board in a single direction
fun Game.adjudicateBoard(board: Board, direction: TemporalFlare, moveResults: List<MoveResult>): Board? {
    println("killing board at ${board.boardIndex}")
    board.kill()
    // Do nothing if nothing relevant happened
    if (moveResults.isEmpty()) return null

    val pieces: MutableMap<Province, Player> = board.pieces.toMutableMap()
    val centres: MutableMap<Province, Player> = board.centres.toMutableMap()

    // Remove pieces moving from board
    for (move in moveResults) if (move is SuccessfulMove) if (move.moveOrder.from.boardIndex == board.boardIndex) {
        pieces.remove(move.moveOrder.from.province)
    }
    // Add pieces moving to board
    for (move in moveResults) if (move is SuccessfulMove) if (move.moveOrder.action.to.boardIndex == board.boardIndex) {
        pieces[move.moveOrder.action.to.province] =
            getBoard(move.moveOrder.piece.location.boardIndex)?.pieces[move.moveOrder.from.province]
            ?: throw IllegalStateException("order not properly validated")
    }

    // Propagate child up
    var iter = board.boardIndex.timeplane!!
    while (getBoard(BoardIndex(board.boardIndex.coordinate + direction.direction, iter)) !== null) iter++

    // Compare the new board with the last produced child
    val newChild = Board(
        BoardIndex(board.boardIndex.coordinate + direction.direction, iter),
        board,
        pieces,
        centres,
    )
    val latestChild = board.children.lastOrNull { limboBoard ->
        limboBoard.boardIndex.coordinate - board.boardIndex.coordinate == direction.direction }
    return if (
        (latestChild === null &&
            (newChild.pieces != board.pieces || newChild.centres != board.centres))
        || (latestChild !== null &&
        (newChild.pieces != latestChild.pieces || newChild.centres != latestChild.centres))
    ) newChild else null
}

fun Game.adjudicateMoves() {
    val pieces = getAllPieces()
    val boards = timeplanes.flatMap { it.boards() } // to ensure the list of boards isn't updated
    val children: MutableList<Board> = mutableListOf()
    val adjudicators: MutableMap<TemporalFlare, Adjudicator> = mutableMapOf()

    // Generate children
    for (flare in TemporalFlare.entries) {
        println("INFO: adjudicating $flare")
        val adjudicator = Adjudicator(moves.filter { it.flare == flare }, supports, pieces)
        adjudicators[flare] = adjudicator
        println("INFO: with results: ${adjudicator.moveResults}")
        for (board in boards) {
            println("INFO: adjudicating board at ${board.boardIndex}")
            children.add(adjudicateBoard(board, flare, adjudicator.moveResults.filter {
                it !is SuccessfulMove || // "it is Bounce"
                it.moveOrder.from.boardIndex == board.boardIndex ||
                it.moveOrder.action.to.boardIndex == board.boardIndex
            }) ?: continue)
        }
    }

    // Add children
    for ((_, children) in children.groupBy { it.boardIndex }) {
        // The origin board can never create a board bounce out of its children
        if (children.size == 1) addChild(children[0].parent!!, children[0])
        // TODO: "else <check board bounce>" (done?)
        else {
            val childrenAndStrengths: MutableList<Pair<Board, Int>> = mutableListOf()
            for (child in children) childrenAndStrengths += Pair(
                child,
                moves.filter {
                    it.action.to == child.parent!!.boardIndex &&
                    it.flare!!.direction == child.boardIndex.coordinate - child.parent.boardIndex.coordinate
                }.map { 1 } // TODO: move strength
                .sum()
            )
            childrenAndStrengths.sortBy { it.second }
            childrenAndStrengths.forEach { println("INFO: ${it.second}") }
            while (childrenAndStrengths.last().second !=
                childrenAndStrengths.last { childrenAndStrengths.last() != it }.second)
            { // last != penultimate
                val child = childrenAndStrengths.last().first
                addChild(child.parent!!, child)
            }
        }
    }

    advanceState()
    if (adjustments.isEmpty()) adjudicateRetreats()
}

fun Game.adjudicateRetreats() {
    // TODO: adjudicate retreats
    adjustments.clear()
    advanceState()
    for (board in timeplanes.flatMap { it.boards() }) if (board.isActive) for (piece in board.pieces) {
        // TODO: is null check necessary?
        if (board.centres[piece.key] === null || board.centres[piece.key] != piece.value) if (piece.key.isSupplyCentre)
            board.centres[piece.key] = piece.value
    }
    // TODO: calculate builds
    if (timeplanes.asSequence().flatMap { it.boards() }.none { it.isActive && it.boardIndex.coordinate.isEven() }) {
        adjudicateBuilds()
    }
}

fun Game.adjudicateBuilds() {
    for (order in adjustments) {
        val board = getBoard(order.piece.location.boardIndex)!!
        val province = order.piece.location.province
        // TODO: only allow disbands if they are forced
        when (order) {
            is Build -> board.pieces[province] =
                board.centres[order.piece.location.province]!!
            is Disband -> board.pieces.remove(order.piece.location.province)
        }
    }
    val boards = timeplanes.flatMap { it.boards().filter { it.isActive && it.boardIndex.coordinate.isEven() } }
    for (board in boards) if (board.isActive && board.boardIndex.coordinate.isEven()) {
        for (player in RomanPlayers.entries) {
            if (board.pieces.filter { it.value == player }.size > board.centres.filter { it.value == player }.size)
                // TODO: actually implement forced disbands
                throw IllegalArgumentException("Too many builds given or not enough disbands given")
        }
    }
    adjustments.clear()
    advanceState()
}

fun Game.adjudicate() {
    when (gameState) {
        GameState.MOVES -> adjudicateMoves()
        GameState.RETREATS -> adjudicateRetreats()
        GameState.BUILDS -> adjudicateBuilds()
    }
}
