package nodomain.seven.dip.utils

val i = ComplexNumber(0, 1)

data class ComplexNumber(val real: Int, val imaginary: Int) {
    operator fun plus(other: ComplexNumber) =
        ComplexNumber(this.real + other.real, this.imaginary + other.imaginary)
    operator fun times(other: ComplexNumber) =
        ComplexNumber(real*other.real - imaginary*other.imaginary, real*other.imaginary + other.real*imaginary)

    operator fun unaryMinus() =
        ComplexNumber(-this.real, -this.imaginary)
    operator fun minus(other: ComplexNumber) =
        ComplexNumber(this.real - other.real,  this.imaginary - other.imaginary)
}

operator fun Int.plus(complexNumber: ComplexNumber) =
    ComplexNumber(this + complexNumber.real, complexNumber.imaginary)
operator fun Int.minus(complexNumber: ComplexNumber) =
    ComplexNumber(this - complexNumber.real, -complexNumber.imaginary)
operator fun Int.times(complexNumber: ComplexNumber) =
    ComplexNumber(this * complexNumber.real, this * complexNumber.imaginary)
