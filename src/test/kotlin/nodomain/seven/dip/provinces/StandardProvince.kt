package nodomain.seven.dip.provinces

import nodomain.seven.dip.orders.Army
import nodomain.seven.dip.orders.IncompatibleParserException
import nodomain.seven.dip.orders.PartiallyParsed
import nodomain.seven.dip.orders.T
import nodomain.seven.dip.provinces.StandardInLand.*
import nodomain.seven.dip.provinces.StandardCoast.*
import nodomain.seven.dip.provinces.StandardSea.*
import nodomain.seven.dip.utils.Location
import nodomain.seven.dip.utils.c
import org.assertj.core.api.WithAssertions
import kotlin.test.Test

val origin = T(0.c, 0)
object A {
    operator fun get(province: StandardProvince): Army = Army(Location(province, origin))
}

sealed interface StandardProvince: Province{
    val adjacency: Set<StandardProvince>
    override fun isAdjacent(other: Province): Boolean = other in adjacency
    val nameWordCount: Int

    companion object: Provinces<StandardProvince> {
        override val entries: List<StandardProvince> =
            sequenceOf(StandardSea.entries, StandardInLand.entries, StandardCoast.entries).flatten().toList()

        override fun trivialPartialParser(string: String): PartiallyParsed<StandardProvince> {
            val province = valueOf(string.trim().substring(0, 3).uppercase())
            return TakeN(province.nameWordCount, province)
        }

        override val nonTrivialNames: Map<String, () -> PartiallyParsed<StandardProvince>> =
            mapOf<String, StandardProvince>(
                "live" to LVP,
                "livo" to LVN,
                "norwa" to NWY,
                "norwe" to NWG,
                "st" to STP,
                "mid" to MAO,
                "tyrrh" to TYR,
            ).mapValues { (_, province) -> {TakeN(province.nameWordCount, province)} }
                .plus(mapOf<String, () -> PartiallyParsed<StandardProvince>>(
                    "gulf" to { Defer({3}, 3) {valueOf(it)} },
                    "north"  to { Defer(StandardProvince::nameWordCount, 2) {when(it.lowercase()) {
                        "sea" -> NTH
                        "africa" -> NAF
                        "atlantic" -> NAO
                        else -> throw IncompatibleParserException()
                    } } })
                )
    }
}

enum class StandardSea(override val nameWordCount: Int): Sea, StandardProvince {
    BAR(2) { override val adjacency: Set<StandardProvince> by lazy { setOf(STP, NWG, NWY) } },
    NWG(2) { override val adjacency: Set<StandardProvince> by lazy { setOf(BAR, NWY, NAO, CLY, EDI, NTH) } },
    NTH(2) { override val adjacency: Set<StandardProvince> by lazy { setOf(NWG, NWY, SKA, DEN, HEL, HOL, BEL, ENG, LON, YOR, EDI) } },
    SKA(1) { override val adjacency: Set<StandardProvince> by lazy { setOf(NTH, NWY, SWE, DEN) } },
    HEL(2) { override val adjacency: Set<StandardProvince> by lazy { setOf(NTH, DEN, KIE, HOL) } },
    BAL(2) { override val adjacency: Set<StandardProvince> by lazy { setOf(SWE, DEN, KIE, BER, PRU, LVN, BOT) } },
    BOT(3) { override val adjacency: Set<StandardProvince> by lazy { setOf(BAL, SWE, FIN, STP, LVN) } },
    ENG(2) { override val adjacency: Set<StandardProvince> by lazy { setOf(BEL, PIC, BRE, MAO, IRI, WAL, LON, NTH) } },
    IRI(2) { override val adjacency: Set<StandardProvince> by lazy { setOf(MAO, ENG, NAO, WAL, LVP) } },
    NAO(3) { override val adjacency: Set<StandardProvince> by lazy { setOf(MAO, NWG, CLY, MAO, LVP, IRI) } },
    MAO(3) { override val adjacency: Set<StandardProvince> by lazy { setOf(NAO, IRI, ENG, BRE, GAS, SPA, POR, WES, NAF) } },
    WES(2) { override val adjacency: Set<StandardProvince> by lazy { setOf(NAF, MAO, SPA, LYO, TYS, TUN) } },
    LYO(3) { override val adjacency: Set<StandardProvince> by lazy { setOf(SPA, MAR, PIE, TUS, TYS, WES) } },
    TYS(2) { override val adjacency: Set<StandardProvince> by lazy { setOf(LYO, TUS, ROM, NAP, ION, TUN, WES) } },
    ION(2) { override val adjacency: Set<StandardProvince> by lazy { setOf(TUN, TYS, NAP, APU, ADR, ALB, GRE, AEG, EAS) } },
    ADR(2) { override val adjacency: Set<StandardProvince> by lazy { setOf(ION, APU, VEN, TRI, ALB) } },
    AEG(2) { override val adjacency: Set<StandardProvince> by lazy { setOf(ION, GRE, BUL, CON, SMY, EAS) } },
    EAS(2) { override val adjacency: Set<StandardProvince> by lazy { setOf(ION, AEG, SMY, SYR) } },
    BLA(2) { override val adjacency: Set<StandardProvince> by lazy { setOf(CON, BUL, RUM, SEV, ARM, ANK) } };

    override val isSupplyCentre: Boolean get() = false
}

enum class StandardCoast(override val isSupplyCentre: Boolean): Coast, StandardProvince {
    POR(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(SPA, MAO) } },
    SPA(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(POR, MAR, GAS, MAO, WES, LYO) } },
    BRE(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(PIC, PAR, GAS, MAO, ENG) } },
    BEL(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(PIC, BUR, RUH, HOL, ENG,  NTH) } },
    HOL(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(BEL, KIE, RUH, NTH, HEL) } },
    KIE(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(HOL, RUH, MUN, BER, DEN, HEL, BAL) } },
    BER(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(KIE, MUN, SIL, PRU, BAL) } },
    DEN(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(KIE, SWE, HEL, SKA, NTH, BAL) } },
    SWE(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(DEN, NWY, FIN, SKA, BAL, BOT) } },
    NWY(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(SWE, FIN, STP, NTH, SKA, NWG, BAR) } },
    STP(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(NWY, FIN, LVN, MOS, BOT, BAR) }
                override val nameWordCount: Int = 2 },
    SEV(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(MOS, UKR, RUM, ARM, BLA) } },
    CON(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(ANK, SMY, BUL, BLA, AEG) } },
    BUL(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(CON, RUM, SER, GRE, BLA, AEG) } },
    RUM(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(GAL, UKR, SEV, BUL, SER, BUD, BLA) } },
    GRE(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(BUL, SER, ALB, AEG, ION) } },
    TRI(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(ALB, SER, BUD, VIE, VEN, TYR, ADR) } },
    NAP(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(ROM, APU, TYS, ION) } },
    LON(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(YOR, WAL, ENG, NTH) } },
    TUN(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(NAF, WES, TYS, ION) } },

    MAR(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(GAS, SPA, BUR, PIE, LYO) }
                override fun hasInlandBorderWith(coast: Coast): Boolean =  equals(GAS) },
    VEN(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(TRI, TYR, PIE, ROM, TUS, APU, ADR) }
                override fun hasInlandBorderWith(coast: Coast): Boolean = equals(PIE) || equals(TUS) || equals(ROM) },
    ROM(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(TUS, VEN, APU, NAP, TYS) }
                override fun hasInlandBorderWith(coast: Coast): Boolean =  equals(VEN) },
    SMY(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(CON, ANK, ARM, SYR, AEG, EAS) }
                override fun hasInlandBorderWith(coast: Coast): Boolean =  equals(ANK) || equals(ARM) },
    ANK(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(ARM, SMY, CON, BLA) }
                override fun hasInlandBorderWith(coast: Coast): Boolean =  equals(SMY) },
    EDI(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(CLY, LVP, YOR, NTH, NWG) }
                override fun hasInlandBorderWith(coast: Coast): Boolean =  equals(LVP) },
    LVP(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(CLY, EDI, YOR, WAL, IRI, NAO) }
                override fun hasInlandBorderWith(coast: Coast): Boolean =  equals(EDI) || equals(YOR) },

    PIC(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(BRE, PAR, BUR, BEL, ENG) } },
    PRU(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(BER, SIL, WAR, LVN, BAL) } },
    FIN(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(SWE, NWY, STP, BOT) } },
    LVN(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(STP, MOS, WAR, PRU, BAL, BOT) } },
    ALB(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(GRE, SER, TRI, ION, ADR) } },
    APU(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(NAP, ROM, VEN, ION, ADR) } },
    CLY(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(EDI, LVP, NAO, NWG) } },
    NAF(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(TUN, MAO, WES) }
                 override val nameWordCount: Int = 2 },

    GAS(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(SPA, BRE, PAR, BUR, MAR, MAO) }
                 override fun hasInlandBorderWith(coast: Coast): Boolean =  equals(MAR) },
    PIE(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(MAR, TYR, VEN, TUS, LYO) }
                 override fun hasInlandBorderWith(coast: Coast): Boolean =  equals(VEN) },
    TUS(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(PIE, VEN, ROM, TYS, LYO) }
                 override fun hasInlandBorderWith(coast: Coast): Boolean =  equals(VEN) },
    ARM(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(SEV, SYR, ANK, SMY, BLA) }
                 override fun hasInlandBorderWith(coast: Coast): Boolean =  equals(SMY) || equals(SYR) },
    SYR(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(SMY, ARM, EAS) }
                 override fun hasInlandBorderWith(coast: Coast): Boolean =  equals(ARM) },
    YOR(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(EDI, LVP, WAL, LON, NTH) }
                 override fun hasInlandBorderWith(coast: Coast): Boolean =  equals(LVP) || equals(WAL) },
    WAL(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(LVP, LON, ENG, IRI, YOR) }
                 override fun hasInlandBorderWith(coast: Coast): Boolean =  equals(YOR) };

    override fun hasInlandBorderWith(coast: Coast): Boolean = false
    override val nameWordCount: Int = 1
}

enum class StandardInLand(override val isSupplyCentre: Boolean): InLand, StandardProvince {
    PAR(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(PIC, BRE, GAS, BUR) } },
    MUN(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(BUR, RUH, KIE, BER, SIL, BOH, TYR) } },
    MOS(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(STP, LVN, WAR, UKR, SEV) } },
    WAR(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(SIL, PRU, LVN, MOS, UKR, GAL) } },
    SER(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(RUM, BUL, GRE, ALB, TRI, BUD) } },
    BUD(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(SER, RUM, GAL, VIE, TRI) } },
    VIE(true) { override val adjacency: Set<StandardProvince> by lazy { setOf(TYR, BOH, GAL, BUD, TRI) } },

    BUR(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(MAR, GAS, PAR, PIC, BEL, RUH, MUN) } },
    RUH(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(HOL, BEL, BUR, MUN, KIE) } },
    SIL(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(MUN, BER, PRU, WAR, GAL, BOH) } },
    UKR(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(MOS, WAR, GAL, RUM, SEV) } },
    GAL(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(SIL, WAR, UKR, RUM, BUD, VIE, BOH) } },
    BOH(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(MUN, SIL, GAL, VIE, TYR) } },
    TYR(false) { override val adjacency: Set<StandardProvince> by lazy { setOf(MUN, BOH, VIE, TRI, VEN, PIE) } };

    override val nameWordCount: Int = 1
}

object StandardProvinceTest: WithAssertions {
    @Test
    fun standardProvinceIsSymmetricNonReflectiveAndComplete() {
        for (province in StandardProvince.entries) {
            assertThat(province.adjacency).allMatch { it is StandardProvince && it.isAdjacent(province) }
            assertThat(province).doesNotMatch { it.isAdjacent(it) }
        }
    }
}

enum class StandardPlayer(override val homeCentres: List<StandardProvince>): Player {
    Austria(listOf(VIE, BUD, TRI)),
    France(listOf(PAR, BRE, MAR)),
    Italy(listOf(VEN, ROM, NAP)),
    Russia(listOf(STP, MOS, SEV, WAR)),
    Turkey(listOf(CON, ANK, SMY)),
    Germany(listOf(BER, MUN, KIE)),
    England(listOf(LON, EDI, LVP))
}
