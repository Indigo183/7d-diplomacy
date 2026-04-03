package nodomain.seven.dip.orders

import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.provinces.Provinces
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.utils.ComplexNumber
import nodomain.seven.dip.utils.Location
import nodomain.seven.dip.utils.c
import java.util.LinkedList
import java.util.Queue

inline fun <reified Pl, reified Pr> getParser(
    crossinline playerTrim: String.() -> String = String::trim,
    crossinline provinceTrim: String.() -> String = String::trim
): Parser where Pl: Player, Pl : Enum<Pl>, Pr : Province, Pr : Enum<Pr> =
    Parser(
        { PartiallyParsed { enumValueOf<Pl>(it.playerTrim()) }},
        { PartiallyParsed { enumValueOf<Pr>(it.provinceTrim()) }}
    )

inline fun <reified Pl> getParser(
    provinces: Provinces<out Province>,
    crossinline playerTrim: String.() -> String = String::trim,
): Parser where Pl: Player, Pl : Enum<Pl> =
    Parser(
        { PartiallyParsed { enumValueOf<Pl>(it.playerTrim()) }},
        { provinces.asProvince(it) }
    )


class IncompatibleParserException() : RuntimeException()

interface Notation {
    fun asBoardIndex(string: String): BoardIndex

    fun asUnitType(string: String): (Location) ->  Piece

    fun asAction(string: String): Char

    fun asTemporalFlare(string: String): Int

    fun asBuildAction(string: String): (Piece) -> BuildOrder
}

typealias OwnedOrder = Pair<Order, Player>
typealias OwnedBuild = Pair<BuildOrder, Player>

fun interface PartiallyParsed<out R> {
    fun isComplete(): Boolean = true
    fun feed(string: String) {}
    fun provideComplete(): R
}

fun <R> Queue<String>.take(asR: (String) -> PartiallyParsed<R>): R {
    val partiallyParsed = asR(remove())
    while (!partiallyParsed.isComplete()) {
        partiallyParsed.feed(remove())
    }
    return partiallyParsed.provideComplete()
}

class Parser(
    val asPlayer: (String) -> PartiallyParsed<Player>,
    val asProvince: (String) -> PartiallyParsed<Province>,
    val notation: Notation = DefaultNotation
){
    enum class Format(val getFormatter: Parser.() -> ParsingHelper<Order>): (Parser) -> ParsingHelper<Order> {
        VERBOSE({Verbose()});

        override fun invoke(parser: Parser): ParsingHelper<Order> = parser.getFormatter()
    }
    enum class NationalisedFormat(val getFormatter: Parser.() -> ParsingHelper<OwnedOrder>): (Parser) -> ParsingHelper<OwnedOrder> {
        VERBOSE_WITH_PLAYER({National(Verbose())}),
        DATC({ this.DATC() });

        override fun invoke(parser: Parser): ParsingHelper<OwnedOrder> = parser.getFormatter()
    }

    fun parseOrderSet(from: String, format: (Parser) -> ParsingHelper<Order>, delimiter: String = "\n\n"): List<Order> =
        from.split(delimiter).flatMap(format(this)::parseOrders)

    fun parseOrderSet(from: String, format: (Parser) -> ParsingHelper<OwnedOrder>, delimiter: String = "\n\n"): Map<Player, List<Order>> {
        return from.split(delimiter)
            .flatMap(format(this)::parseOrders)
            .groupBy({(_, player) -> player },  {(order, _) -> order})
    }

    sealed interface ParsingHelper<T> {
        fun parseOrders(from: String, separatedBy: String = "\n"): List<T>
    }

    private interface Formatted<T>: ParsingHelper<T> {
        fun parseOrderInPieces(queue: Queue<String>): T

        fun parseOrder(asString: String): T =
            parseOrderInPieces(asString.trim().split(" ").toCollection(LinkedList()))

        fun parseOrderOrNull(asString: String): T? =
            try { parseOrder(asString) }
            catch (_ : Exception) { null }

        override fun parseOrders(from: String, separatedBy: String): List<T> =
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
            BeingParsed(remaining, combiningInto(parseT,  combiner = combiner))

        fun <T, R> combiningManyNext(parseT: (String) -> PartiallyParsed<T>,  combiner: (T, I) -> R): BeingParsed<R> =
            BeingParsed(remaining, combiningManyInto(parseT,  combiner = combiner))

        fun <T, R> combiningInto(parseT: (String) -> T, default: T? = null, combiner: (T, I) -> R): R =
            if (remaining.isEmpty() && default !== null)
                combiner(default, intermediateResult)
            else
                combiner(parseT(remaining.remove()), intermediateResult)

        fun <T, R> combiningManyInto(parseT: (String) -> PartiallyParsed<T>,  combiner: (T, I) -> R): R {
            return combiner(remaining.take(parseT), intermediateResult)
        }
    }

    private inner class Verbose: Formatted<Order> {
        override fun parseOrderInPieces(queue: Queue<String>): Order {
            return queue.withFirst(notation::asBoardIndex)
                .combiningNext(notation::asUnitType) { unitType, board -> { province: Province -> unitType(Location(province, board))}}
                .combiningManyNext(asProvince) { province, board  -> board(province)}
                .combiningInto(notation::asAction, 'H') { action, piece -> when(action) {
                    'H' -> piece.holds
                    'S' -> piece S { parseOrderInPieces(queue) }
                    'M' -> piece M queue.withFirst(notation::asBoardIndex)
                        .combiningManyInto(asProvince, combiner = ::Location) i notation.asTemporalFlare(queue.remove())
                    else -> throw IllegalStateException()
                } }
        }
    }

    private inner class DATC: Announced<OwnedOrder> {
        val origin = BoardIndex(0.c)
        lateinit var owner: Player

        override fun parseHeader(asString: String) {
            owner = asPlayer(asString.substringBefore(':')).provideComplete()
        }

        override fun parseOrderInPieces(queue: Queue<String>): OwnedOrder {
            return queue.withFirst(notation::asUnitType)
                .combiningManyNext(asProvince) { province, unitType -> unitType(Location(province, origin))}
                .combiningInto(notation::asAction, 'H') {action, piece -> Pair(when(action) {
                    'H' -> piece.holds
                    'S' -> piece S { parseOrderInPieces(queue).first }
                    '-' -> piece M queue.take(asProvince) i 0
                    else -> throw IllegalStateException()}, owner) }
        }
    }

    private inner class DATCbuild: Announced<OwnedBuild> {
        val origin = BoardIndex(0.c)
        lateinit var owner: Player

        override fun parseHeader(asString: String) {
            owner = asPlayer(asString.substringBefore(':')).provideComplete()
        }

        override fun parseOrderInPieces(queue: Queue<String>): OwnedBuild {
            return queue.withFirst(notation::asBuildAction)
                .combiningNext(notation::asUnitType) { unitType, action -> { province: Province -> action(unitType(Location(province, origin)))}}
                .combiningManyInto(asProvince) {province, action -> action(province) to owner}
        }
    }


    private inner class National(val basedOn: Formatted<Order>): Formatted<OwnedOrder> {
        override fun parseOrderInPieces(queue: Queue<String>): Pair<Order, Player> {
            val player = queue.take(asPlayer)
            return Pair(basedOn.parseOrderInPieces(queue), player)
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
        'F' -> ::Fleet
        else -> throw IncompatibleParserException()
    }

    override fun asAction(string: String): Char = string.first().uppercaseChar()

    override fun asTemporalFlare(string: String): Int = string.last().toString().toInt()

    override fun asBuildAction(string: String): (Piece) -> BuildOrder = when (string.lowercase()) {
        "+", "build" -> ::Build
        "-", "disband","remove" -> ::Disband
        else -> throw IncompatibleParserException()
    }
}
