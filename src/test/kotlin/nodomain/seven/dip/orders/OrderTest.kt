package nodomain.seven.dip.orders

import nodomain.seven.dip.utils.ComplexNumber.*
import nodomain.seven.dip.provinces.Romans.*

fun main() { // manual testing for now; at some point we should probably adapt some testing framework. Junit combined with AssertJ works well for Java
    listOf(
        T(0 + 0*i, 0) A CAT M BRU i 0,
        T(1 + 0*i, 0) A CAT M T(0 + 0*i, 0)[BRU] i 3,
        T(0 + 0*i, 0) A POM S {T(0 + 0*i, 0) A CAE S {(T(0 + 0*i, 0) A CAT).holds}}
    ).forEach(::println)
}