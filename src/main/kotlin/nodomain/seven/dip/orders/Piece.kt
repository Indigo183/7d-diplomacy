package nodomain.seven.dip.orders

import nodomain.seven.dip.BoardIndex
import nodomain.seven.dip.provinces.Province

sealed interface Piece {
    val location: Location

    val holds: HoldOrder
        get() = HoldOrder(this)

    // Accepts both local and non-local moves
    infix fun M(destination: Location): MoveOrder = MoveOrder(this, Moves(destination))
    infix fun M(destination: Province): MoveOrder = M(Location(destination, location.board))

    infix fun S(supporting: () -> Order): SupportOrder {
        val order = supporting();
        return SupportOrder(this, Supports(if (order.action is Supports) order.piece.holds else order));
    }
}

data class Location(val province: Province, val board: BoardIndex)

// TODO: actually understand and properly comment this
operator fun BoardIndex.get(province: Province): Location = Location(province, this)

@JvmInline
value class Army(override val location: Location): Piece
infix fun BoardIndex.A(province: Province): Army = Army(Location(province, this))