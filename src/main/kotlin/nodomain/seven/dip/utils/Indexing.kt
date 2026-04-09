package nodomain.seven.dip.utils

import nodomain.seven.dip.provinces.Province

// Location of a board
// A null timeplane represents that a board is in Limbo
data class BoardIndex(val coordinate: ComplexNumber, var timeplane: Int? = 0) {
    operator fun plus(other: ComplexNumber): BoardIndex = BoardIndex(coordinate + other, timeplane)
    override fun toString(): String =
        if (timeplane === null) "($coordinate, Limbo)" else "($coordinate, T$timeplane)"
}

// Location of a unit/province
data class Location(val province: Province, val boardIndex: BoardIndex) {
    operator fun plus(other: ComplexNumber): Location = Location(province, boardIndex + other)
}
