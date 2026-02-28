package nodomain.seven.dip.orders

import nodomain.seven.dip.orders.Parser.Formatted
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.utils.ComplexNumber
import nodomain.seven.dip.utils.Location
import java.util.LinkedList
import java.util.Queue

inline fun <reified Pl, reified Pr> getParser(): Parser where Pl: Player, Pl : Enum<Pl>, Pr : Province, Pr : Enum<Pr> =
    Parser({ enumValueOf<Pl>(it.trim()) }, { enumValueOf<Pr>(it.trim().uppercase()) })

class IncompatibleParserException() : RuntimeException()

interface Notation {
    fun asBoardIndex(string: String): BoardIndex

    fun asUnitType(string: String): (Location) ->  Piece

    fun asAction(string: String): Char

    fun asTemporalFlare(string: String): Int
}

typealias OwnedOrder = Pair<Order, Player>

class Parser(
    val asPlayer: (String) -> Player,
    val asProvince: (String) -> Province,
    val notation: Notation = DefaultNotation
){
    fun parseOrderSet(from: String, format: (Parser) -> Formatted<Order>, delimiter: String = "\n\n"): List<Order> =
        from.split(delimiter).flatMap(format(this)::parseOrders)

    fun parseOrderSet(from: String, format: (Parser) -> Formatted<OwnedOrder>, delimiter: String = "\n\n"): Map<Player, List<Order>> {
        return from.split(delimiter)
            .flatMap(format(this)::parseOrders)
            .groupBy({(_, player) -> player },  {(order, _) -> order})
    }


    interface Formatted<T> {
        fun Queue<String>.parseOrderInPieces(): T

        fun parseOrderInPieces(queue: Queue<String>): T = queue.parseOrderInPieces()

        fun parseOrder(asString: String): T =
            asString.split(" ").toCollection(LinkedList()).parseOrderInPieces()

        fun parseOrderOrNull(asString: String): T? =
            try { parseOrder(asString) }
            catch (_ : Exception) { null }

        fun parseOrders(from: String, separatedBy: String = "\n"): List<T> =
            from.split(separatedBy).asSequence().map(this::parseOrderOrNull).filterNotNull().toList()
    }

    private interface Announced<T>: Formatted<T> {
        fun parseHeader(asString: String)

        override fun parseOrders(from: String, separatedBy: String): List<T> {
            val orderStrings = from.split(separatedBy)
            parseHeader(orderStrings[0])
            return orderStrings.asSequence().drop(1).map(this::parseOrderOrNull).filterNotNull().toList()
        }
    }

    private fun <T> Queue<String>.withFirst(parseT: (String) -> T) = BeingParsed(this, parseT(remove()))

    private data class BeingParsed<I>(val remaining: Queue<String>, val intermediateResult: I) {
        fun <T, R> combiningNext(parseT: (String) -> T,  combiner: (T, I) -> R): BeingParsed<R> =
            BeingParsed(remaining, combiningInto(parseT,  combiner))

        fun <T, R> combiningInto(parseT: (String) -> T,  combiner: (T, I) -> R): R =
            combiner(parseT(remaining.remove()), intermediateResult)
    }

    inner class Verbose: Formatted<Order> {
        override fun Queue<String>.parseOrderInPieces(): Order {
            return withFirst(notation::asBoardIndex)
                .combiningNext(notation::asUnitType) { unitType, board -> { province: Province -> unitType(Location(province, board))}}
                .combiningNext(asProvince) { province, board  -> board(province)}
                .combiningInto(notation::asAction) { action, piece -> when(action) {
                    'H' -> piece.holds
                    'S' -> piece S { parseOrderInPieces() }
                    'M' -> piece M withFirst(notation::asBoardIndex)
                        .combiningInto(asProvince, ::Location) i notation.asTemporalFlare(remove())
                    else -> throw IllegalStateException()
                } }
        }
    }

    inner class National(val basedOn: Formatted<Order>): Formatted<OwnedOrder> {
        override fun Queue<String>.parseOrderInPieces(): Pair<Order, Player> {
            return withFirst(asPlayer).combiningInto( { basedOn.parseOrderInPieces(this) } , ::Pair)
        }
    }
}

object DefaultNotation: Notation {
    override fun asBoardIndex(string: String): BoardIndex {
        return BoardIndex(
            ComplexNumber(
                string.substringBefore('+').trim().toInt(),
                string.substring(string.indexOf('+'), string.indexOf('i')).trim().toInt()
            ), string.substringAfter('T').trim().toInt())
    }

    override fun asUnitType(string: String): (Location) -> Piece = when (string.first()) {
        'A' -> ::Army
        else -> throw IncompatibleParserException()
    }

    override fun asAction(string: String): Char = string.first().uppercaseChar()

    override fun asTemporalFlare(string: String): Int = string.last().toString().toInt()
}

enum class Format(val getFormatter: Parser.() -> Formatted<Order>): (Parser) -> Formatted<Order> {
    VERBOSE({Verbose()});

    override fun invoke(parser: Parser): Formatted<Order> {
        return parser.getFormatter()
    }
}
enum class NationalisedFormat(val getFormatter: Parser.() -> Formatted<OwnedOrder>): (Parser) -> Formatted<OwnedOrder> {
    VERBOSE_WITH_PLAYER({National(Verbose())});

    override fun invoke(parser: Parser): Formatted<OwnedOrder> {
        return parser.getFormatter()
    }
}

