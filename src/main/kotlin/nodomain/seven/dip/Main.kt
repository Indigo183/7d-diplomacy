package nodomain.seven.dip

import nodomain.seven.dip.game.Game
import nodomain.seven.dip.game.Board
import nodomain.seven.dip.provinces.RomanPlayers.Cato
import nodomain.seven.dip.provinces.RomanPlayers.Pompey
import nodomain.seven.dip.provinces.Romans.CAT
import nodomain.seven.dip.provinces.Romans.POM
import nodomain.seven.dip.utils.*
import nodomain.seven.dip.utils.BoardIndex

fun main() {
    val game = Game()
    game.addChild(
        game.getBoard(BoardIndex(0.c))!!,
        BoardIndex(1.c),
        mapOf(CAT to Cato, POM to Pompey),
        mapOf(CAT to Cato, POM to Pompey),
    )

    for (timeplane in game.timeplanes) {
        for (board in timeplane.values) {
            testBoard(board)
        }
    }
}

fun testBoard(board: Board) {
    println("This is, in fact, a Board!\n$board\n\n")
}