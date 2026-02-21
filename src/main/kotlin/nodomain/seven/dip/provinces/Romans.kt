package nodomain.seven.dip.provinces

// TODO: Parse specific variant details from external config file, rather than having province and player details
//   hard-coded into an import

enum class Romans(override val isSupplyCenter: Boolean): Province {
    CAT(true ) { override val adjacency: Set<Romans> by lazy { setOf(CAE, BRU) } },
    CAE(false) { override val adjacency: Set<Romans> by lazy { setOf(CAT, BRU, POM) } },
    POM(true ) { override val adjacency: Set<Romans> by lazy { setOf(CAE, BRU) } },
    BRU(false) { override val adjacency: Set<Romans> by lazy { setOf(CAE, CAT, POM) } };

    abstract val adjacency: Set<Romans>
    override fun isAdjacentTo(other: Province): Boolean = other in adjacency
}

enum class RomanPlayers(override val homeCentres: List<Romans>): Player {
    Cato(listOf(Romans.CAT)),
    Pompey(listOf(Romans.POM)),
}