package nodomain.seven.dip.provinces

interface Province {
    infix fun isAdjacentTo(other: Province): Boolean;
    val isSupplyCenter: Boolean;
}

interface Player {
	// TODO: shoul this be a List or a Set?
	val homeCentres: List<Province>;
}