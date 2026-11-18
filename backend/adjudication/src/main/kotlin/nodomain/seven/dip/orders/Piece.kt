package nodomain.seven.dip.orders

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import nodomain.seven.dip.utils.Spot
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.provinces.ProvincialSpot
import java.io.Serializable

@JsonTypeInfo(
    use = JsonTypeInfo.Id.SIMPLE_NAME,
    property = "unit_type"
)
sealed interface Piece: Serializable {
    val spot: Spot
    fun asLocal(): String

    @get:JsonIgnore
    val holds: HoldOrder
        get() = HoldOrder(this)

    // Accepts both local and non-local moves
    infix fun M(destination: Spot): MoveOrder = MoveOrder(this, Moves(destination))
    infix fun M(destination: ProvincialSpot): MoveOrder = M(Spot(destination, spot.boardIndex))

    infix fun S(supporting: () -> Order): SupportOrder {
        val order = supporting()
        return SupportOrder(this, Supports(if (order.action is Supports) order.piece.holds else order))
    }

    operator fun unaryPlus(): Build = Build(this)
    operator fun unaryMinus(): Disband = Disband(this)

    // Creates a new piece of the same type but with the new location
    infix fun moveTo(destination: Spot): Piece = when (this) {
        is Army -> Army(destination)
        is Fleet -> Fleet(destination)
    }
}

// Support square bracket notation for creating a `Location` out of its components
operator fun BoardIndex.get(provincialSpot: ProvincialSpot): Spot = Spot(provincialSpot, this)

data class Army(override val spot: Spot): Piece {
    override fun toString(): String = "${spot.boardIndex} A ${spot.provincialSpot}"
    override fun asLocal(): String = "A ${spot.provincialSpot}"
}
infix fun BoardIndex.A(provincialSpot: ProvincialSpot): Army = Army(Spot(provincialSpot, this))

data class Fleet(override val spot: Spot): Piece {
    override fun toString(): String = "${spot.boardIndex} F ${spot.provincialSpot}"
    override fun asLocal(): String = "F ${spot.provincialSpot}"
}
infix fun BoardIndex.F(provincialSpot: ProvincialSpot): Fleet = Fleet(Spot(provincialSpot, this))
