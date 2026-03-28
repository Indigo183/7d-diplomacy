package nodomain.seven.dip.provinces

import nodomain.seven.dip.orders.Army
import nodomain.seven.dip.orders.T
import nodomain.seven.dip.provinces.StandardProvince.*
import nodomain.seven.dip.utils.Location
import nodomain.seven.dip.utils.c

val origin = T(0.c, 0)
object A {
    operator fun get(province: StandardProvince): Army = Army(Location(province, origin))
}

enum class StandardProvince(override val isSupplyCentre: Boolean): Province {
    POR(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(SPA) } },
    SPA(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(POR, MAR, GAS) } },
    MAR(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(GAS, SPA, BUR, PIE) } },
    BRE(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(PIC, PAR, GAS) } },
    PAR(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(PIC, BRE, GAS, BUR) } },
    BEL(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(PIC, BUR, RUH, HOL) } },
    HOL(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(BEL, KIE, RUH) } },
    KIE(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(HOL, RUH, MUN, BER, DEN) } },
    BER(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(KIE, MUN, SIL, PRU) } },
    MUN(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(BUR, RUH, KIE, BER, SIL, BOH, TYR) } },
    DEN(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(KIE, SWE) } },
    SWE(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(DEN, NWY, FIN) } },
    NWY(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(SWE, FIN, STP) } },
    STP(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(NWY, FIN, LIV, MOS) } },
    MOS(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(STP, LIV, WAR, UKR, SEV) } },
    WAR(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(SIL, PRU, LIV, MOS, UKR, GAL) } },
    SEV(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(MOS, UKR, RUM, ARM) } },
    SMY(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(CON, ANK, ARM, SYR) } },
    ANK(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(ARM, SMY, CON) } },
    CON(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(ANK, SMY, BUL) } },
    BUL(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(CON, RUM, SER, GRE) } },
    RUM(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(GAL, UKR, SEV, BUL, SER, BUD) } },
    SER(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(RUM, BUL, GRE, ALB, TRI, BUD) } },
    GRE(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(BUL, SER, ALB) } },
    TRI(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(ALB, SER, BUD, VIE, VEN, TYR) } },
    BUD(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(SER, RUM, GAL, VIE, TRI) } },
    VIE(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(TYR, BOH, GAL, BUD, TRI) } },
    VEN(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(TRI, TYR, PIE, ROM, TUS, APU) } },
    ROM(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(TUS, VEN, APU, NAP) } },
    NAP(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(ROM, APU) } },

    GAS(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(SPA, BRE, PAR, BUR, MAR) } },
    BUR(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(MAR, GAS, PAR, PIC, BEL, RUH, MUN) } },
    PIC(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(BRE, PAR, BUR, BEL) } },
    RUH(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(HOL, BEL, BUR, MUN, KIE) } },
    SIL(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(MUN, BER, PRU, WAR, GAL, BOH) } },
    PRU(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(BER, SIL, WAR, LIV) } },
    FIN(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(SWE, NWY, STP) } },
    LIV(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(STP, MOS, WAR, PRU) } },
    UKR(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(MOS, WAR, GAL, RUM, SEV) } },
    ARM(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(SEV, SYR, ANK, SMY) } },
    SYR(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(SMY, ARM) } },
    ALB(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(GRE, SER, TRI) } },
    GAL(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(SIL, WAR, UKR, RUM, BUD, VIE, BOH) } },
    BOH(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(MUN, SIL, GAL, VIE, TYR) } },
    TYR(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(MUN, BOH, VIE, TRI, VEN, PIE) } },
    PIE(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(MAR, TYR, VEN, TUS) } },
    TUS(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(PIE, VEN, ROM, ) } },
    APU(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(NAP, ROM, VEN) } };

    abstract val adjacency: Set<StandardProvince>
    override fun isAdjacent(other: Province): Boolean = other in adjacency
}

fun main() {
    for (province in StandardProvince.entries)
        if (!province.adjacency.all { it.isAdjacent(province) }) println("$province is not symmetrically linked")
}

enum class StandardPlayer(override val homeCentres: List<Province>): Player {
    Austria(listOf(VIE, BUD, TRI)),
    France(listOf(PAR, BRE, MAR)),
    Italy(listOf(VEN, ROM, NAP)),
    Russia(listOf(STP, MOS, SEV, WAR)),
    Turkey(listOf(CON, ANK, SMY)),
    Germany(listOf(BER, MUN, KIE))
}
