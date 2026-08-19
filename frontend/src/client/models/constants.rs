use crate::client::models::*;

pub const ORIGIN: ComplexNumber = ComplexNumber {
    real: 0,
    imaginary: 0,
};

pub const ORIGIN_BOARD_INDEX: BoardIndex = BoardIndex {
    coordinate: ORIGIN,
    timeplane: 0,
};
