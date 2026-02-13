package nodomain.`7dip`.orders

import org.example.ComplexNumber
import org.example.Location
import nodomain.`7dip`.provinces.Romans.*
import org.example.nodomain.`7dip`.orders.*

val T0_0 = Location(ComplexNumber(0, 0), 0)

fun main() { // manual testing for now; at some point we should probably adapt some testing framework. Junit combined with AssertJ works well for Java
    listOf(
        T0_0 A CAT M BRU i 0,
        T0_0 A POM S {T0_0 A CAE S {(T0_0 A CAT).holds}}
    ).forEach(::println)
}