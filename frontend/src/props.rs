use crate::models::*;
use dioxus::prelude::*;

#[derive(Debug, PartialEq, Eq, Clone, Props)]
pub struct GameHeaderProps {
    pub game_name: String,
    pub player: Player,
    pub turn: Turn,
    pub status: Status,
    pub map_name: String,
    pub time_travel: TimeTravelDetails,
    pub auto_adjudicate: bool,
    pub server_address: String,
}
