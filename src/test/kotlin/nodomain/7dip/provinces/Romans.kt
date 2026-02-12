package nodomain.`7dip`.provinces

import org.example.nodomain.`7dip`.provinces.Province

enum class Romans: Province {
    CAT{ override val adjacency: Set<Romans> by lazy { setOf(CAE, BRU) } },
    CAE{ override val adjacency: Set<Romans> by lazy { setOf(CAT, BRU, POM) } },
    POM{ override val adjacency: Set<Romans> by lazy { setOf(CAE, BRU) } },
    BRU{ override val adjacency: Set<Romans> by lazy { setOf(CAE, CAT, POM) } };

    abstract val adjacency: Set<Romans>
    override fun isAdjencentTo(other: Province): Boolean = adjacency.contains(other)
}