package nodomain.seven.dip.orders

import nodomain.seven.dip.utils.ComplexNumber
import nodomain.seven.dip.Location
import nodomain.seven.dip.provinces.Province
import kotlin.enums.enumEntries

sealed interface Piece {
    val board: Location

    val holds: HoldOrder
        get() = HoldOrder(this)

    infix fun M(to: Space): MoveOrder = MoveOrder(this, Moves(to))
    infix fun M(to: Province): MoveOrder = M(Space(to, board))

    infix fun S(supporting: () -> Order): SupportOrder {
        val order = supporting();
        return SupportOrder(this, Supports(if (order.action is Supports) order.piece.holds else order));
    }
}

data class Space(val province: Province, val board: Location)

fun T(boardIndex: ComplexNumber, timeplane: Int): Location  = Location(boardIndex, timeplane)

operator fun Location.get(province: Province): Space = Space(province, this)

@JvmInline
value class Army(val at: Space): Piece {
    override val board get() = at.board
}
infix fun Location.A(province: Province): Army = Army(Space(province, this))

abstract class Order(val piece: Piece, val action: Action, val symbol: String) {
    override fun toString(): String =  "$piece$symbol$action"
}

enum class TimeFlare(val direction: ComplexNumber) {
    RIGHT(ComplexNumber( 1, 0)),
    UP   (ComplexNumber( 0, 1)),
    LEFT (ComplexNumber(-1, 0)),
    DOWN (ComplexNumber( 0,-1));
}

sealed interface Action {}

data object Holds: Action
class HoldOrder(piece: Piece): Order(piece, Holds, " ")

@JvmInline
value class Moves(val to: Space): Action
class MoveOrder(piece: Piece, action: Moves, var flare: TimeFlare? = null): Order(piece, action, " - ") {
    infix fun i(timeFlare: Int): Order {
        if (action is Moves) flare = enumEntries<TimeFlare>()[timeFlare % 4];
        return this;
    }
}

@JvmInline
value class Supports(val order: Order): Action
class SupportOrder(piece: Piece, action: Supports): Order(piece, action, " S ")
