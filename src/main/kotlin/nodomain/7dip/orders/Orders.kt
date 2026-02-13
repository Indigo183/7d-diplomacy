package org.example.nodomain.`7dip`.orders

import org.example.ComplexNumber
import org.example.Location
import org.example.nodomain.`7dip`.provinces.Province
import kotlin.enums.enumEntries

sealed interface Piece {
    val holds: Order
        get() = Order(this, Holds)

    infix fun M(to: Space): Order = Order(this, Moves(to))

    infix fun S(supporting: () -> Order): Order {
        val order = supporting()
        return Order(this, Supports(if (order.action is Supports) order.piece.holds else order))
    }
}

data class Space(val province: Province, val board: Location);
operator fun Location.get(province: Province): Space = Space(province, this)

@JvmInline
value class Army(val space: Space): Piece
infix fun Location.A(province: Province): Army = Army(Space(province, this))

data class Order(val piece: Piece, val action: Action, val flare: TimeFlare? = null) {
    infix fun i(timeFlare: Int): Order =
        if (action is Moves) Order(piece, action, enumEntries<TimeFlare>()[timeFlare % 4]) else this
}

enum class TimeFlare(val direction: ComplexNumber) {
    RIGHT   (ComplexNumber( 1, 0)),
    UP      (ComplexNumber( 0, 1)),
    LEFT    (ComplexNumber(-1, 0)),
    DOWN    (ComplexNumber( 0,-1));
}

sealed interface Action {}

data object Holds: Action
@JvmInline
value class Moves(val space: Space): Action
@JvmInline
value class Supports(val order: Order): Action
