package nodomain.seven.dip.orders

import nodomain.seven.dip.utils.*
import kotlin.enums.enumEntries

// The "action" being done, without a piece to order it
sealed interface Action

// An action and the piece ordering it
sealed class Order(val piece: Piece, val symbol: String) {
    abstract val action: Action

    override fun equals(other: Any?): Boolean =
        other is Order && other.from == from && other.action == action

    override fun toString(): String =  "$piece$symbol$action"
    override fun hashCode(): Int = piece.hashCode() * 31 + action.hashCode()


    val from: Location = piece.location
}

// The temporal direction in which a move occurs
enum class TemporalFlare(val direction: ComplexNumber) {
    RIGHT(1.c),
    UP   (i),
    LEFT ((-1).c),
    DOWN (-i);
}

data object Holds: Action
class HoldOrder(piece: Piece): Order(piece, " ") {
    override val action: Holds = Holds
}

@JvmInline
value class Moves(val to: Location): Action
class MoveOrder(piece: Piece, override val action: Moves, var flare: TemporalFlare? = null): Adjustment, Order(piece, " - ") {
    override val piece<Adjustment>
    infix fun i(timeFlare: Int): MoveOrder {
        flare = enumEntries<TemporalFlare>()[timeFlare % 4]
        return this
    }
}

@JvmInline
value class Supports(val order: Order): Action
class SupportOrder(piece: Piece, override val action: Supports): Order(piece, " S ")

// Timeplane specifier shorthand
fun T(boardIndex: ComplexNumber, timeplane: Int): BoardIndex = BoardIndex(boardIndex, timeplane)

sealed interface Adjustment {
    val piece: Piece
}

sealed interface BuildOrder: Adjustment
sealed interface RetreatOrder: Adjustment

class Build(override val piece: Piece): BuildOrder
class Disband(override val piece: Piece): BuildOrder, RetreatOrder
