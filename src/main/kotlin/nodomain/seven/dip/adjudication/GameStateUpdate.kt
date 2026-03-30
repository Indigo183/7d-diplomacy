package nodomain.seven.dip.adjudication

import nodomain.seven.dip.game.*
import nodomain.seven.dip.orders.*
import nodomain.seven.dip.orders.TemporalFlare
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.provinces.RomanPlayers
import nodomain.seven.dip.utils.*
import kotlin.math.absoluteValue

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
fun Game.adjudicateMovesBoard(board: Board, direction: TemporalFlare, moveResults: List<MoveResult>): Board? {
    println("killing board at ${board.boardIndex}")
    board.kill()
    // Do nothing if nothing relevant happened
    if (moveResults.isEmpty()) return null

    val pieces: MutableMap<Province, Player> = board.pieces.toMutableMap()
    val centres: MutableMap<Province, Player> = board.centres.toMutableMap()

    // Remove pieces moving from board
    for (move in moveResults.asSequence().filterIsInstance<SuccessfulMove>()
        .filter { it.moveOrder.from.boardIndex == board.boardIndex })
        pieces.remove(move.moveOrder.from.province)

    // Add pieces moving to board
    for (move in moveResults.asSequence().filterIsInstance<SuccessfulMove>()
        .filter { it.moveOrder.action.to.boardIndex == board.boardIndex }) {

        if (pieces[move.moveOrder.action.to.province] !== null)
            requiredRetreats += Pair(move.moveOrder.action.to, move.moveOrder.flare!!)
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
        (latestChild === null && newChild.pieces != board.pieces)
        || (latestChild !== null && newChild.pieces != latestChild.originalPieces) // latestChild may have had builds
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
            children.add(adjudicateMovesBoard(board, flare, adjudicator.moveResults.filter {
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
        else {
            val childrenAndStrengths: MutableList<Pair<Board, Int>> = mutableListOf()
            for (child in children) childrenAndStrengths += Pair(
                child,
                moves.asSequence().filter {
                    it.action.to.boardIndex == child.parent!!.boardIndex
                            && it.flare!!.direction == child.boardIndex.coordinate - child.parent.boardIndex.coordinate
                            && adjudicators[it.flare]!!.moveResults.contains(SuccessfulMove(it))
                }.sumOf {
                    adjudicators[it.flare]!!.nonCutSupports.filter { support ->
                        support.action.order == it
                                && !adjudicators[it.flare]!!.dislodgements.any { it.action.to == support.piece.location }
                    }.size + 1
                }
            )
            childrenAndStrengths.sortBy { it.second }
            childrenAndStrengths.forEach { println("INFO: ${it.second}") }
            while (!childrenAndStrengths.isEmpty()
                && (childrenAndStrengths.size == 1
                || childrenAndStrengths.last().second !=
                childrenAndStrengths.last { childrenAndStrengths.last() != it }.second))
            { // last != penultimate
                val child = childrenAndStrengths.removeLast().first
                addChild(child.parent!!, child)
            }
            for ((child, _) in childrenAndStrengths) {
                child.boardIndex.timeplane = null
                addChild(child.parent!!, child)
            }
        }
    }

    advanceState()
    if (requiredRetreats.isEmpty()) adjudicateRetreats()
}

fun Game.adjudicateRetreats() {
    for (retreat in requiredRetreats) println(retreat)
    // TODO: adjudicate retreats
    requiredRetreats.clear()
    clearAdjustments()
    advanceState()
    for (board in timeplanes.flatMap { it.boards() }) if (board.isActive && board.boardIndex.coordinate.isEven())
        for (piece in board.pieces) {
        if (board.centres[piece.key] != piece.value && piece.key.isSupplyCentre) board.centres[piece.key] = piece.value
    }
    // TODO: calculate builds
    if (timeplanes.asSequence().flatMap { it.boards() }.none { it.isActive && it.boardIndex.coordinate.isEven() }) {
        adjudicateBuilds()
    }
}

// Adjudicates builds for a single board
fun Game.adjudicateBuildsBoard(board: Board, adjustments: Map<Player, List<Adjustment>>) {
    for ((player, adjustments) in adjustments) {
        // Required builds/disbands
        val count = board.countBuilds(player)

        var validAdjustments = when {
            count > 0 -> adjustments.filterIsInstance<BuildOrder>().filter {
                it is Build
                        && player == board.centres[it.piece.location.province]
                        && board.pieces[it.piece.location.province] === null
            }

            count < 0 -> adjustments.filterIsInstance<BuildOrder>().filter {
                it is Disband
                        && player == board.pieces[it.piece.location.province]
            }

            else -> continue // no valid adjustments for this player
        }.toMutableList()

        if (count > 0 && validAdjustments.size > count.absoluteValue) // too many builds
            validAdjustments = validAdjustments.take(count).toMutableList()
        // TODO: Civil Disorder (to any OWNED supply centre, not home)
        else if (count < 0 && validAdjustments.size < count.absoluteValue) // too few disbands
            validAdjustments += board.pieces.asSequence()
                .filter { it.value == player }
                .take(count-validAdjustments.size)
                .map { Disband(Army(Location(it.key, board.boardIndex))) }

        for (order in validAdjustments) when (order) {
            is Build -> board.pieces[order.piece.location.province] = board.centres[order.piece.location.province]!!
            is Disband -> board.pieces.remove(order.piece.location.province)
        }
    }
}

fun Game.adjudicateBuilds() {
    val boards = timeplanes.flatMap { it.boards().filter { it.isActive && it.boardIndex.coordinate.isEven() } }
    for (board in boards) {
        adjudicateBuildsBoard(
            board,
            adjustments.filter { it.piece.location.boardIndex == board.boardIndex }.groupBy { when (it) {
                is Build -> board.centres[it.piece.location.province]
                    ?: throw IllegalStateException("centre not owned so cannot be built on")
                is Disband -> board.pieces[it.piece.location.province]
                    ?: throw IllegalStateException("no unit found to disband")
                else -> throw IllegalStateException("adjustments contained wrong type (retreat vs build)")
            } }
        )
    }

    clearAdjustments()
    advanceState()
}

fun Game.adjudicate() {
    when (gameState) {
        GameState.MOVES -> adjudicateMoves()
        GameState.RETREATS -> adjudicateRetreats()
        GameState.BUILDS -> adjudicateBuilds()
    }
}
