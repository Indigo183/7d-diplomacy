package nodomain.seven.dip.orders

import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.utils.ComplexNumber
import nodomain.seven.dip.utils.Location
import java.util.LinkedList
import java.util.Queue
import kotlin.enums.enumEntries

inline fun <reified Pl, reified Pr> getParser(): Parser where Pl: Player, Pl : Enum<Pl>, Pr : Province, Pr : Enum<Pr> =
    Parser(enumEntries<Pl>()) { enumValueOf<Pr>(it.trim().uppercase()) }

class IncompatibleParserException() : RuntimeException()

class Parser(val players: List<Player>, val asProvince: (String) -> Province){
    private interface FormattedParser {
        fun Queue<String>.parseOrder(): Order

        fun parseOrder(asString: String): Order =
            asString.split(" ").toCollection(LinkedList()).parseOrder()

        fun parseOrderOrNull(asString: String): Order? =
            try { parseOrder(asString) }
            catch (_ : Exception) { null }

        fun parseOrders(from: String, separatedBy: String = "\n"): List<Order> =
            from.split(separatedBy).asSequence().map(this::parseOrderOrNull).filterNotNull().toList()

        fun parseOrderSet(from: String, separatedBy: String = "\n\n"): List<Order> =
            from.split(separatedBy).flatMap(this::parseOrders)
    }

    private interface AnnouncedParser: FormattedParser {
        fun parseHeader(asString: String)

        override fun parseOrders(from: String, separatedBy: String): List<Order> {
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

    inner class Verbose: FormattedParser {
        override fun Queue<String>.parseOrder(): Order {
            return withFirst(::asBoardIndex)
                .combiningNext(::asUnitType) { unitType, board -> { province: Province -> unitType(Location(province, board))}}
                .combiningNext(asProvince) { province, board  -> board(province)}
                .combiningInto(::asAction) { action, piece -> when(action) {
                    'H' -> piece.holds
                    'S' -> piece S { parseOrder() }
                    'M' -> piece M withFirst(::asBoardIndex).combiningInto(asProvince, ::Location) i asTemporalFlare(remove())
                    else -> throw IllegalStateException()
                } }
        }
    }

    fun asBoardIndex(string: String): BoardIndex {
        return BoardIndex(
            ComplexNumber(
                string.substringBefore('+').trim().toInt(),
                string.substring(string.indexOf('+'), string.indexOf('i')).trim().toInt()
            ), string.substringAfter('T').trim().toInt())
    }

    fun asUnitType(string: String): (Location) ->  Piece = when (string.first()) {
        'A' -> ::Army
        else -> throw IncompatibleParserException()
    }

    fun asAction(string: String): Char = string.first().uppercaseChar()

    fun asTemporalFlare(string: String): Int = string.last().toString().toInt()
}
