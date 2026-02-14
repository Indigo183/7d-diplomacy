package nodomain.seven.dip.provinces

interface Province {
    infix fun isAdjacentTo(other: Province): Boolean;
}

interface Player