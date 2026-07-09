package nodomain.seven.dip.utils

import nodomain.seven.dip.provinces.Province
import java.io.Serializable

// Location of a board
// A null timeplane represents that a board is in Limbo
data class BoardIndex(val coordinate: ComplexNumber, var timeplane: Int? = 0): Serializable {
    operator fun plus(other: ComplexNumber): BoardIndex = BoardIndex(coordinate + other, timeplane)
    operator fun minus(other: ComplexNumber): BoardIndex = BoardIndex(coordinate - other, timeplane)
    override fun toString(): String =
        if (timeplane === null) "($coordinate, Limbo)" else "($coordinate, T$timeplane)"
}

// Location of a unit/province
data class Location(val province: Province, val boardIndex: BoardIndex): Serializable {
    operator fun plus(other: ComplexNumber): Location = Location(province, boardIndex + other)
    operator fun minus(other: ComplexNumber): Location = Location(province, boardIndex - other)
    override fun toString(): String = "$boardIndex $province"
}
