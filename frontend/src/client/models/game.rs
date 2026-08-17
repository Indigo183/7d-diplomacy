use crate::client::models::order::*;
use crate::client::models::utils::*;
use serde::{Deserialize, Serialize};
use std::collections::HashMap;

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct RequiredRetreat {
    pub piece: Piece,
    pub temporal_flare: TemporalFlare,
    pub player: String,
    pub disallowed: Location,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize, Default)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum GameState {
    #[default]
    Moves,
    Retreats,
    Builds,
}

#[derive(Debug, Clone, PartialEq, Eq, Default, Serialize, Deserialize)]
pub struct TokenAccess {
    pub token_created_log: Option<Vec<i64>>,
    pub token_recovered_log: Option<Vec<i64>>,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct Board {
    pub board_index: BoardIndex,
    pub parent: Option<BoardIndex>,
    pub pieces: HashMap<String, Vec<Piece>>,
    pub original_pieces: HashMap<String, Vec<Piece>>,
    pub centres: HashMap<Province, String>, // TODO: String -> Province
    pub children: Vec<BoardIndex>,
    pub is_active: bool,
}

#[derive(Debug, Clone, PartialEq, Eq, Default, Serialize, Deserialize)]
pub struct Game {
    pub turn: i32,
    pub required_retreats: Vec<RequiredRetreat>,
    pub timeplanes: Vec<HashMap<String, Board>>, // TODO: String -> ComplexNumber
    pub limbo: Vec<Board>,
    pub game_state: GameState,
    pub moves: Vec<MoveOrder>,
    pub supports: Vec<SupportOrder>,
    pub convoys: Vec<ConvoyOrder>,
}
