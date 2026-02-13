package nodomain.seven.dip

data class ComplexNumber(val real: Int, val imaginary: Int)
data class Location(val boardIndex: ComplexNumber, val timeplane: Int = 0)

// TODO: move fields to constructor
class Game(private val _timeplanes: MutableList<Timeplane>, private val _limbo: MutableSet<Board>) {
    val timeplanes: List<Timeplane>
        get() = _timeplanes
    val limbo: Set<Board>
        get() = _limbo
}
typealias Timeplane = MutableMap<ComplexNumber, Board>
fun Timeplane.boards() = values
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
    println("This is, in fact, a Board!\n\n$board")
}