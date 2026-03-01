package nodomain.seven.dip.orders

import nodomain.seven.dip.orders.Parser.Format.*
import nodomain.seven.dip.orders.Parser.NationalisedFormat.*
import nodomain.seven.dip.provinces.RomanPlayers
import nodomain.seven.dip.provinces.RomanPlayers.*
import nodomain.seven.dip.provinces.Romans
import nodomain.seven.dip.provinces.Romans.*
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.utils.*
import org.assertj.core.api.WithAssertions
import kotlin.test.Test

class ParseTest: WithAssertions {
    @Test
    fun parseBoardIndexTest() {
        val boardIndex = DefaultNotation.asBoardIndex("1+17iT2")

        assertThat(boardIndex).isEqualTo(BoardIndex(1 + 17*i, 2))
    }

    @Test
    fun parseFullOrderVerboselyTest() {
        val orderString = "0+0iT0 A POM S 0+0iT0 A CAT M 0+0iT0 BRU i^2"

        val orders = getParser<RomanPlayers, Romans>().parseOrderSet(orderString, VERBOSE)

        assertThat(orders).containsOnly(T(0.c, 0) A POM S { T(0.c, 0) A CAT M BRU i 2})
    }

    @Test
    fun parseMultipleSimpleOrderVerboselyWithPlayerTest() {
        val orderString = """
            |Cato 0+0iT0 A CAT holds 
            |Pompey 0+0iT0 A POM holds""".trimMargin()

        val orders = getParser<RomanPlayers, Romans>().parseOrderSet(orderString, VERBOSE_WITH_PLAYER)

        assertThat(orders)
            .containsEntry(Cato, listOf((T(0.c, 0) A CAT).holds))
            .containsEntry(Pompey, listOf((T(0.c, 0) A POM).holds))
    }
}
