package nodomain.seven.dip.orders

import nodomain.seven.dip.utils.Location
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.provinces.Province

sealed interface Piece {
    val location: Location

    val holds: HoldOrder
        get() = HoldOrder(this)

    // Accepts both local and non-local moves
    infix fun M(destination: Location): MoveOrder = MoveOrder(this, Moves(destination))
    infix fun M(destination: Province): MoveOrder = M(Location(destination, location.boardIndex))

    infix fun S(supporting: () -> Order): SupportOrder {
        val order = supporting();
        return SupportOrder(this, Supports(if (order.action is Supports) order.piece.holds else order));
    }
}

// Support square bracket notation for creating a `Location` out of its components
operator fun BoardIndex.get(province: Province): Location = Location(province, this)

@JvmInline
value class Army(override val location: Location): Piece
infix fun BoardIndex.A(province: Province): Army = Army(Location(province, this))