package nodomain.seven.dip.orders

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import nodomain.seven.dip.utils.*
import java.io.Serializable
import kotlin.enums.enumEntries

// The "action" being done, without a piece to order it
sealed interface Action: Serializable {
    fun asLocal(): String = toString()
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "symbol",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = HoldOrder::class, name = " "),
    JsonSubTypes.Type(value = MoveOrder::class, name = " - "),
    JsonSubTypes.Type(value = SupportOrder::class, name = " S "),
    JsonSubTypes.Type(value = ConvoyOrder::class, name = " C "),
    JsonSubTypes.Type(value = Disband::class, name = "Disband "),
    JsonSubTypes.Type(value = Build::class, name = "Build ")
)
sealed interface Inputtable: Serializable {
    val symbol: String
    val piece: Piece
    @get:JsonIgnore
    val from: Location
        get() = piece.location
    @JsonIgnore
    fun isLocal(): Boolean = true
}

// An action and the piece ordering it
sealed class Order(override val piece: Piece, override val symbol: String): Inputtable {
    abstract val action: Action
    fun asLocal(): String = toString()

    override fun equals(other: Any?): Boolean =
        other is Order && other.from == from && other.action == action

    override fun toString(): String = if (isLocal()) {
                "${piece.asLocal()}$symbol${action.asLocal()}"
            } else {
                "$piece$symbol$action"
            }
    override fun hashCode(): Int = piece.hashCode() * 31 + action.hashCode()
}

// The temporal direction in which a move occurs
enum class TemporalFlare(val direction: ComplexNumber) {
    RIGHT(1.c),
    UP   (i),
    LEFT ((-1).c),
    DOWN (-i);
}

data object Holds: Action {
    private fun readResolve(): Any = Holds
    override fun toString(): String = "H"
}
class HoldOrder(piece: Piece): Order(piece, " ") {
    override val action: Holds = Holds
}

@JvmInline
value class Moves(val to: Location): Action {
    override fun toString(): String = "$to"
    override fun asLocal(): String = "${to.province}"
}
class MoveOrder(piece: Piece, override val action: Moves, override var flare: TemporalFlare? = null): RetreatOrder, Order(piece, " - ") {
    infix fun i(timeFlare: Int): MoveOrder {
        flare = enumEntries<TemporalFlare>()[timeFlare % 4]
        return this
    }
    override fun isLocal() = from.boardIndex == action.to.boardIndex
}

@JvmInline
value class Supports(val order: Order): Action {
    override fun toString(): String = "$order"
    override fun asLocal(): String = order.asLocal()
}
class SupportOrder(piece: Piece, override val action: Supports): Order(piece, " S ") {
    override fun isLocal() = from.boardIndex == action.order.from.boardIndex && action.order.isLocal()
}

@JvmInline
value class Convoys(val move: MoveOrder): Action {
    override fun toString(): String = "$move"
    override fun asLocal(): String = move.asLocal()
}
class ConvoyOrder(piece: Piece, override val action: Convoys): Order(piece, " C ") {
    override fun isLocal() = from.boardIndex == action.move.from.boardIndex && action.move.isLocal()
}

// Timeplane specifier shorthand
fun T(boardIndex: ComplexNumber, timeplane: Int): BoardIndex = BoardIndex(boardIndex, timeplane)

sealed interface Adjustment: Inputtable

sealed interface BuildOrder: Adjustment
sealed interface RetreatOrder: Adjustment {
    val flare: TemporalFlare?
}

class Build(override val piece: Piece): BuildOrder {
    override val symbol: String get() = "Build "
    override fun toString(): String = "Build ${piece.asLocal()}"
}
class Disband(override val piece: Piece, override val flare: TemporalFlare? = null): BuildOrder, RetreatOrder {
    override val symbol: String get() = "Disband "
    override fun toString(): String = "Disband ${piece.asLocal()}"

    infix fun i(timeFlare: Int) = Disband(piece, enumEntries<TemporalFlare>()[timeFlare % 4])

    infix fun withFlare(flare: TemporalFlare?) = Disband(piece, flare = flare)
}
