package nodomain.seven.dip.utils

import kotlin.math.absoluteValue

// Used in ComplexNumber shorthand notation, e.g. `1 + 2*i`
val i = ComplexNumber(0, 1)

// Used in Int to ComplexNumber conversion, e.g. `1.c`
val Int.c get() = ComplexNumber(this, 0)

// Used to indicate the location of a board on a timeplane
data class ComplexNumber(val real: Int, val imaginary: Int) {
    operator fun plus(other: ComplexNumber) =
        ComplexNumber(this.real + other.real, this.imaginary + other.imaginary)
    operator fun times(other: ComplexNumber) =
        ComplexNumber(real*other.real - imaginary*other.imaginary, real*other.imaginary + other.real*imaginary)

    operator fun unaryMinus() =
        ComplexNumber(-this.real, -this.imaginary)
    operator fun minus(other: ComplexNumber) =
        ComplexNumber(this.real - other.real,  this.imaginary - other.imaginary)

    fun isEven(): Boolean = real + imaginary % 2 == 0

    override fun toString(): String = if (imaginary == 0) "$real" else {
        val op = if (imaginary < 0) "-" else "+"
        val imaginary = imaginary.absoluteValue
        if (imaginary == 1) "$real $op i" else "$real $op ${imaginary}i"
    }
}

// Used for ComplexNumber shorthand notation, e.g. `1 + 2*i`
operator fun Int.plus(complexNumber: ComplexNumber) =
    ComplexNumber(this + complexNumber.real, complexNumber.imaginary)
operator fun Int.minus(complexNumber: ComplexNumber) =
    ComplexNumber(this - complexNumber.real, -complexNumber.imaginary)
operator fun Int.times(complexNumber: ComplexNumber) =
    ComplexNumber(this * complexNumber.real, this * complexNumber.imaginary)

