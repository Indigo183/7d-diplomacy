package nodomain.seven.dip.provinces

/*
TODO: Parse specific variant details from external config file, rather than having province and player details
hard-coded into an import
 */

enum class Romans: Province {
    CAT{ override val adjacency: Set<Romans> by lazy { setOf(CAE, BRU) } },
    CAE{ override val adjacency: Set<Romans> by lazy { setOf(CAT, BRU, POM) } },
    POM{ override val adjacency: Set<Romans> by lazy { setOf(CAE, BRU) } },
    BRU{ override val adjacency: Set<Romans> by lazy { setOf(CAE, CAT, POM) } };

    abstract val adjacency: Set<Romans>
    override fun isAdjacentTo(other: Province): Boolean = other in adjacency
}

enum class RomanPlayers: Player {
    Cato,
    Pompey
}