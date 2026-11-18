package nodomain.seven.dip.provinces

import nodomain.seven.dip.orders.PartiallyParsed
import nodomain.seven.dip.orders.Piece
import nodomain.seven.dip.orders.T
import nodomain.seven.dip.orders.A
import nodomain.seven.dip.utils.c

// TODO: Parse specific variant details from external config file, rather than having province and player details
//   hard-coded into an import

enum class Romans(override val isSupplyCentre: Boolean): ProvincialSpot, Province {
    CAT(true ) { override val adjacency: Set<Romans> by lazy { setOf(CAE, BRU) } },
    CAE(false) { override val adjacency: Set<Romans> by lazy { setOf(CAT, BRU, POM) } },
    POM(true ) { override val adjacency: Set<Romans> by lazy { setOf(CAE, BRU) } },
    BRU(false) { override val adjacency: Set<Romans> by lazy { setOf(CAE, CAT, POM) } };

    abstract val adjacency: Set<Romans>
    override val province: Province get() = this
    override fun isAdjacent(other: ProvincialSpot): Boolean = other in adjacency
}

enum class RomanPlayers(override val homeCentres: List<Romans>): Player {
    Cato(listOf(Romans.CAT)),
    Pompey(listOf(Romans.POM));

    override val startingUnits: List<Piece>
        get() = homeCentres.map { T(0.c, 0) A it }
}

object RomanProvinces: Provinces<Romans> {
    override val entries: List<Romans> = Romans.entries
    override val nonTrivialNames: Map<String, () -> PartiallyParsed<Romans>> = mapOf()
}