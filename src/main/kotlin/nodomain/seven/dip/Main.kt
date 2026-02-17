package nodomain.seven.dip

import nodomain.seven.dip.game.*
import nodomain.seven.dip.utils.ComplexNumber.*
import nodomain.seven.dip.utils.BoardIndex

fun main() {
    val game = Game()
    game.addChild(game.getBoard(BoardIndex(0.c))!!, BoardIndex(1.c))

    for (timeplane in game.timeplanes) {
        for (board in timeplane.values) {
            testBoard(board)
        }
    }
}

fun testBoard(board: Board) {
    println("This is, in fact, a Board!\n$board\n\n")
}