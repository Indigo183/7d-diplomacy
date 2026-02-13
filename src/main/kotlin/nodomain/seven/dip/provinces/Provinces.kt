package nodomain.seven.dip.provinces

interface Province {
    infix fun isAdjencentTo(other: Province): Boolean;
}