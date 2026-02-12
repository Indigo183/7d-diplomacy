package org.example.nodomain.`7dip`.orders

import org.example.Location
import org.example.nodomain.`7dip`.provinces.Province

sealed interface Piece {
    fun holds(): Order = Order(this, Holds)

    operator fun minus(to: Space): Order = Order(this, Moves(to))

    infix fun S(supporting: () -> Order): Order = Order(this, Supports(supporting()))
}

data class Space(val province: Province, val board: Location);

@JvmInline
value class Army(val space: Space): Piece

sealed interface Action {}

data class Order(val piece: Piece, val action: Action)

object Holds: Action;

@JvmInline
value class Moves(val space: Space): Action

@JvmInline
value class Supports(val order: Order): Action
