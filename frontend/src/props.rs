use crate::models::*;
use dioxus::prelude::*;

#[derive(Debug, PartialEq, Clone, Props)]
pub struct GameHeaderProps {
    pub game_name: String,
    pub player: Player,
    pub turn: Turn,
    pub status: Status,
}
