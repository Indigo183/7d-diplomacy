package org.example

data class ComplexNumber(val real: Int, val imaginary: Int)
data class Location(val boardIndex: ComplexNumber, val timeplane: Int = 0)

// TODO: move fields to constructor
class Game(var timeplanes: MutableList<Timeplane>, var limbo: MutableSet<Board>)
class Timeplane(var boards: MutableMap<ComplexNumber, Board>)
class Board(var location: Location?, val parent: Location? = null) {
    override fun toString(): String {
        return "Board {\n    location: $location\n}" // JSON notation
    }
}

fun main() {
    val board = Board(Location(ComplexNumber(0, 0)))
    testBoard(board)
}

fun testBoard(board: Board) {
    println("This is, in fact, a Board!\n\n$board")
}