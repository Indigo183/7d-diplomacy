package nodomain.seven.dip.orders

import nodomain.seven.dip.utils.ComplexNumber
import nodomain.seven.dip.BoardIndex
import nodomain.seven.dip.provinces.Province
import kotlin.enums.enumEntries

sealed interface Piece {
    val at: Location

    val holds: HoldOrder
        get() = HoldOrder(this)

    infix fun M(to: Location): MoveOrder = MoveOrder(this, Moves(to))
    infix fun M(to: Province): MoveOrder = M(Location(to, at.board))

    infix fun S(supporting: () -> Order): SupportOrder {
        val order = supporting();
        return SupportOrder(this, Supports(if (order.action is Supports) order.piece.holds else order));
    }
}

data class Location(val province: Province, val board: BoardIndex)

fun T(boardIndex: ComplexNumber, timeplane: Int): BoardIndex  = BoardIndex(boardIndex, timeplane)

operator fun BoardIndex.get(province: Province): Location = Location(province, this)

@JvmInline
value class Army(override val at: Location): Piece
infix fun BoardIndex.A(province: Province): Army = Army(Location(province, this))

abstract class Order(val piece: Piece, val symbol: String) {
    abstract val action: Action

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
class HoldOrder(piece: Piece): Order(piece, " ") {
    override val action: Holds = Holds
}

@JvmInline
value class Moves(val to: Location): Action
class MoveOrder(piece: Piece, override val action: Moves, var flare: TimeFlare? = null): Order(piece, " - ") {
    infix fun i(timeFlare: Int): Order {
        flare = enumEntries<TimeFlare>()[timeFlare % 4];
        return this;
    }
}

@JvmInline
value class Supports(val order: Order): Action
class SupportOrder(piece: Piece, override val action: Supports): Order(piece, " S ")
