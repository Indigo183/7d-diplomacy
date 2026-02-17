package nodomain.seven.dip.utils

import nodomain.seven.dip.provinces.Province

// Location of a board
// A null BoardIndex represents that a board is in Limbo
data class BoardIndex(val coordinate: ComplexNumber, val timeplane: Int = 0) {
    override fun toString(): String = "($coordinate, T$timeplane)"
}

// Location of a unit/province
// A null Location represents that a board is in Limbo
data class Location(val province: Province, val boardIndex: BoardIndex)