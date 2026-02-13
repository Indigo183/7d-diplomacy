package org.example.nodomain.`7dip`.orders

import org.example.Location
import org.example.nodomain.`7dip`.provinces.Province

sealed interface Piece {
    val holds: Order
        get() = Order(this, Holds)

    operator fun minus(to: Space): Order = Order(this, Moves(to))

    infix fun S(supporting: () -> Order): Order {
        val order = supporting()
        return Order(this, Supports(if (order.action is Supports) order.piece.holds else order))
    }
}

data class Space(val province: Province, val board: Location);

@JvmInline
value class Army(val space: Space): Piece
infix fun Location.A(province: Province): Army = Army(Space(province, this))


sealed interface Action {}

data class Order(val piece: Piece, val action: Action)

object Holds: Action;

@JvmInline
value class Moves(val space: Space): Action

@JvmInline
value class Supports(val order: Order): Action
