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
        val order = supporting()
        return SupportOrder(this, Supports(if (order.action is Supports) order.piece.holds else order))
    }

    // Creates a new piece of the same type but with the new location
    infix fun moveTo(destination: Location): Piece = when (this) {
        is Army -> Army(destination)
        is Fleet -> Fleet(destination)
    }
}

// Support square bracket notation for creating a `Location` out of its components
operator fun BoardIndex.get(province: Province): Location = Location(province, this)

@JvmInline
value class Army(override val location: Location): Piece
infix fun BoardIndex.A(province: Province): Army = Army(Location(province, this))

@JvmInline
value class Fleet(override val location: Location): Piece
infix fun BoardIndex.F(province: Province): Fleet = Fleet(Location(province, this))