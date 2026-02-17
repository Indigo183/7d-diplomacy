package nodomain.seven.dip.orders

import nodomain.seven.dip.utils.*
import kotlin.enums.enumEntries

// The "action" being done, without a piece to order it
sealed interface Action

// An action and the piece ordering it
sealed class Order(val piece: Piece, val symbol: String) {
    abstract val action: Action
    override fun toString(): String =  "$piece$symbol$action"
}

// The temporal direction in which a move occurs
enum class TemporalFlare(val direction: ComplexNumber) {
    RIGHT(ComplexNumber( 1, 0)),
    UP   (ComplexNumber( 0, 1)),
    LEFT (ComplexNumber(-1, 0)),
    DOWN (ComplexNumber( 0,-1));
}

data object Holds: Action
class HoldOrder(piece: Piece): Order(piece, " ") {
    override val action: Holds = Holds
}

@JvmInline
value class Moves(val to: Location): Action
class MoveOrder(piece: Piece, override val action: Moves, var flare: TemporalFlare? = null): Order(piece, " - ") {
    infix fun i(timeFlare: Int): Order {
        flare = enumEntries<TemporalFlare>()[timeFlare % 4];
        return this;
    }
}

@JvmInline
value class Supports(val order: Order): Action
class SupportOrder(piece: Piece, override val action: Supports): Order(piece, " S ")

// Timeplane specifier shorthand
fun T(boardIndex: ComplexNumber, timeplane: Int): BoardIndex  = BoardIndex(boardIndex, timeplane)
