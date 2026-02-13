package nodomain.seven.dip.orders

import nodomain.seven.dip.ComplexNumber
import nodomain.seven.dip.Location
import nodomain.seven.dip.provinces.Province
import kotlin.enums.enumEntries

sealed interface Piece {
    val board: Location

    val holds: Order
        get() = Order(this, Holds)

    infix fun M(to: Space): Order = Order(this, Moves(to))
    infix fun M(to: Province): Order = M(Space(to, board))

    infix fun S(supporting: () -> Order): Order {
        val order = supporting();
        return Order(this, Supports(if (order.action is Supports) order.piece.holds else order));
    }
}

data class Space(val province: Province, val board: Location);
operator fun Location.get(province: Province): Space = Space(province, this)

@JvmInline
value class Army(val space: Space): Piece {
    override val board get() = space.board
}
infix fun Location.A(province: Province): Army = Army(Space(province, this))

data class Order(val piece: Piece, val action: Action, var flare: TimeFlare? = null) {
    init {
        if (action !is Moves) flare = null;
    }

    infix fun i(timeFlare: Int): Order {
        if (action is Moves) flare = enumEntries<TimeFlare>()[timeFlare % 4];
        return this;
    }
}

enum class TimeFlare(val direction: ComplexNumber) {
    RIGHT(ComplexNumber( 1, 0)),
    UP   (ComplexNumber( 0, 1)),
    LEFT (ComplexNumber(-1, 0)),
    DOWN (ComplexNumber( 0,-1));
}

sealed interface Action {}

data object Holds: Action
@JvmInline
value class Moves(val space: Space): Action
@JvmInline
value class Supports(val order: Order): Action
