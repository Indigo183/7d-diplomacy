package nodomain.seven.dip

data class ComplexNumber(val real: Int, val imaginary: Int)
data class Location(val boardIndex: ComplexNumber, val timeplane: Int = 0)

// TODO: move fields to constructor
class Game(var timeplanes: MutableList<Timeplane>, var limbo: MutableSet<Board>)
class Timeplane(var boards: MutableMap<ComplexNumber, Board>)
class Board(var location: Location?, val parent: Location? = null) {
    override fun toString(): String {
        return "nodomain.7dip.Board {\n    location: $location\n}" // JSON notation
    }
    var isActive = true
        private set
}

fun main() {
    val board = Board(Location(ComplexNumber(0, 0)))
    testBoard(board)
}

fun testBoard(board: Board) {
    println("This is, in fact, a nodomain.7dip.Board!\n\n$board")
}