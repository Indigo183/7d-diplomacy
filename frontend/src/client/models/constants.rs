use crate::client::models::romans::*;
use crate::client::models::*;
use std::collections::HashMap;
use std::sync::LazyLock;

pub const ORIGIN: ComplexNumber = ComplexNumber {
    real: 0,
    imaginary: 0,
};

pub const ORIGIN_BOARD_INDEX: BoardIndex = BoardIndex {
    coordinate: ORIGIN,
    timeplane: 0,
};

pub const BASE_MAP_PIECES: LazyLock<HashMap<String, Vec<Piece>>> = LazyLock::new(|| {
    HashMap::from([
        (
            POMPEY.to_string(),
            vec![Piece {
                location: Location {
                    province: POM.clone(),
                    board_index: ORIGIN_BOARD_INDEX,
                },
                unit_type: UnitType::Army,
            }],
        ),
        (
            CATO.to_string(),
            vec![Piece {
                location: Location {
                    province: CAT.clone(),
                    board_index: ORIGIN_BOARD_INDEX,
                },
                unit_type: UnitType::Army,
            }],
        ),
    ])
});

pub const BASE_MAP: LazyLock<Game> = LazyLock::new(|| Game {
    turn: 1,
    required_retreats: Vec::new(),
    timeplanes: vec![HashMap::from([(
        "0".to_string(),
        Board {
            board_index: ORIGIN_BOARD_INDEX,
            parent: None,
            pieces: BASE_MAP_PIECES.clone(),
            original_pieces: BASE_MAP_PIECES.clone(),
            centres: HashMap::from([
                (POM.clone(), POMPEY.to_string()),
                (CAT.clone(), CATO.to_string()),
            ]),
            children: Vec::new(),
            is_active: true,
        },
    )])],
    limbo: Vec::new(),
    game_state: GameState::Moves,
    moves: Vec::new(),
    supports: Vec::new(),
    convoys: Vec::new(),
});
