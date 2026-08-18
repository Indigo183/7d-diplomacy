use crate::models::*;
use dioxus::prelude::*;

/// The descriptive properties of a [`GameInstance`], bundled into a single header struct to be
/// passed into [`Component`]s.
///
/// This struct requires that the parameters are owned (or cloned).
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

impl<'a> From<GameInstance<'a>> for GameHeaderProps {
    fn from(value: GameInstance) -> Self {
        GameHeaderProps {
            game_name: value.game.config.name.clone(),
            player: value.player_specifics.player.clone(),
            turn: value.game.turn.clone(),
            status: value.player_specifics.status,
            map_name: value.game.config.variant.name.clone(),
            time_travel: value.game.config.time_travel_details,
            auto_adjudicate: value.game.config.auto_adjudicate,
            server_address: value.game.config.link.clone(),
        }
    }
}

impl GameHeaderProps {
    /// Consumes the [`GameHeaderProps`] instance and splits ownership of the parameters between new
    /// instances of [`ResumeGameListTitleProps`] and [`ResumeGameListBodyProps`], in order to avoid
    /// cloning the individual fields.
    pub fn split_title_and_body(self) -> (ResumeGameListTitleProps, ResumeGameListBodyProps) {
        (
            ResumeGameListTitleProps {
                name: self.game_name,
                player: self.player,
                turn: self.turn,
                status: self.status,
            },
            ResumeGameListBodyProps {
                name: self.map_name,
                time_travel: self.time_travel,
                auto_adjudicate: self.auto_adjudicate,
                server_address: self.server_address,
            },
        )
    }
}

/// The descriptive title properties of a [`GameInstance`], bundled into a single header struct to
/// be passed into the title of [`components::ResumeGameList`](crate::components::ResumeGameList).

/// This struct requires that the parameters are owned (or cloned).
#[derive(Debug, PartialEq, Eq, Clone, Props)]
pub struct ResumeGameListTitleProps {
    pub name: String,
    pub player: Player,
    pub turn: Turn,
    pub status: Status,
}

/// The descriptive title properties of a [`GameInstance`], bundled into a single header struct to
/// be passed into the body of [`components::ResumeGameList`](crate::components::ResumeGameList).
///
/// This struct requires that the parameters are owned (or cloned).
#[derive(Debug, PartialEq, Eq, Clone, Props)]
pub struct ResumeGameListBodyProps {
    pub name: String,
    pub time_travel: TimeTravelDetails,
    pub auto_adjudicate: bool,
    pub server_address: String,
}
