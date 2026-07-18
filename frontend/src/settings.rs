use std::option::Option;

use serde::{Deserialize, Serialize};

/// The time travel details of a game.
#[derive(Debug, PartialEq, Eq, Clone, Copy, Serialize, Deserialize)]
pub enum TimeTravel {
    FiveDimensional,
    SevenDimensional,
}
impl std::fmt::Display for TimeTravel {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::FiveDimensional => write!(f, "5D"),
            Self::SevenDimensional => write!(f, "7D"),
        }
    }
}
impl Default for TimeTravel {
    /// The default is currently 7D, as that is what is implemented in the backend.
    fn default() -> Self {
        Self::SevenDimensional
    }
}

/// The time travel adjacencies of a game.
#[derive(Debug, PartialEq, Eq, Clone, Copy, Serialize, Deserialize)]
pub enum Adjacencies {
    Strict,
    Loose,
}
impl std::fmt::Display for Adjacencies {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::Strict => write!(f, "Strict"),
            Self::Loose => write!(f, "Loose"),
        }
    }
}
impl Default for Adjacencies {
    /// The default is currently strict adjacencies, as it makes 7D ever-so-slightly playable.
    fn default() -> Self {
        Self::Strict
    }
}

/// A wrapper struct for RGBA colours, purely for convenience.
#[derive(Debug, Clone, Copy, Serialize, Deserialize)]
pub struct RGBA {
    /// RED, THE BLOOD OF ANGRY MEN
    pub red: u8,
    /// ewwwww no
    pub green: u8,
    /// da fing wot fleets go on innit
    pub blue: u8,
    /// *absolute make-a-wish-maxxing*
    pub alpha: u8,
}

impl RGBA {
    /// Constructs an RGBA colour from an RGB colour, as represented by a `u32`. Any bits above the
    /// twenty-fourth bit will be thrown away.
    pub fn from_rgb(rgb: u32) -> Self {
        Self::from(rgb << 8 | 0xFF)
    }

    /// Constructs an RGBA colour from an ARGB colour, as represented by a `u32`.
    pub fn from_argb(argb: u32) -> Self {
        Self::from(argb.rotate_left(8))
    }
}

impl From<u32> for RGBA {
    fn from(value: u32) -> Self {
        RGBA {
            red: (value & 0xFF000000 >> 24) as u8,
            green: (value & 0xFF0000 >> 16) as u8,
            blue: (value & 0xFF00 >> 8) as u8,
            alpha: (value & 0xFF) as u8,
        }
    }
}

impl Into<u32> for RGBA {
    fn into(self) -> u32 {
        (self.red as u32) << 24
            | (self.green as u32) << 16
            | (self.blue as u32) << 8
            | self.alpha as u32
    }
}

impl From<&str> for RGBA {
    fn from(value: &str) -> Self {
        todo!()
    }
}
// TODO: impl RGBA

/// The associated data for a player, parsed from JSON.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Player {
    /// The name of the player to be displayed.
    pub name: String,
    /// The colour associated with a player.
    pub colour: RGBA,
}
impl Player {
    pub fn new(name: String, colour: RGBA) -> Self {
        Player { name, colour }
    }
}

/// The variant data for a specific variant, parsed from JSON.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Variant {
    /// The variant's name (not including time travel details).
    pub name: String,
    /// The variant's map data (TODO: NOT IMPLEMENTED).
    map: (),
    /// The variant's player list.
    pub player_list: Vec<Player>,
}
impl Default for Variant {
    fn default() -> Self {
        Self {
            name: String::from("Romans"),
            map: (),
            player_list: vec![
                Player::new(String::from("Cato"), RGBA::from("#265BA5")),
                Player::new(String::from("Pompey"), RGBA::from("#972530")),
            ],
        }
    }
}

/// The configuration for a given game instance (e.g "5D Diplomacy AC").
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GameConfig {
    /// The game's non-unique official name.
    pub name: String,
    /// The game's unique alphanumeric identifier.
    pub id: String,
    /// A link to the game hosted on a potentially external server.
    pub link: String,
    /// The game's variant data.
    pub variant: Variant,
    /// The game's time travel details.
    pub time_travel: TimeTravel,
    /// The game's adjacency settings.
    pub adjacencies: Adjacencies,
    /// Whether the game will adjudicate itself automatically at the specified deadline.
    pub automatic_adjudication: bool,
}
impl GameConfig {
    /// Instantiates a builder for `settings::GameConfig`.
    pub fn builder() -> GameConfigBuilder {
        GameConfigBuilder {
            name: String::new(),
            id: String::new(),
            link: String::from("localhost:9090"),
            variant: Variant::default(),
            time_travel: TimeTravel::default(),
            adjacencies: Adjacencies::default(),
            automatic_adjudication: false,
        }
    }
}

/// A builder type for `settings::GameConfig`.
pub struct GameConfigBuilder {
    /// The game's non-unique official name.
    name: String,
    /// The game's unique alphanumeric identifier.
    id: String,
    /// A link to the game hosted on a potentially external server.
    link: String,
    /// The game's variant data.
    variant: Variant,
    /// The game's time travel details.
    time_travel: TimeTravel,
    /// The game's adjacency settings.
    adjacencies: Adjacencies,
    /// Whether the game will adjudicate itself automatically at the specified deadline.
    automatic_adjudication: bool,
}
impl GameConfigBuilder {
    /// The game's non-unique official name.
    pub fn with_name(self, name: String) -> Self {
        Self { name, ..self }
    }
    /// The game's unique alphanumeric identifier.
    pub fn with_id(self, id: String) -> Self {
        Self { id, ..self }
    }
    /// A link to the game hosted on a potentially external server.
    pub fn with_link(self, link: String) -> Self {
        Self {
            link: link.strip_suffix('/').unwrap_or(&link).to_string(),
            ..self
        }
    }
    /// The game's variant data.
    pub fn with_variant(self, variant: Variant) -> Self {
        Self { variant, ..self }
    }
    /// The game's time travel details.
    pub fn with_time_travel(self, time_travel: TimeTravel) -> Self {
        Self {
            time_travel,
            ..self
        }
    }
    /// The game's adjacency settings.
    pub fn with_adjacencies(self, adjacencies: Adjacencies) -> Self {
        Self {
            adjacencies,
            ..self
        }
    }
    /// Whether the game will adjudicate itself automatically at the specified deadline.
    pub fn with_adjudication(self, automatic_adjudication: bool) -> Self {
        Self {
            automatic_adjudication,
            ..self
        }
    }
    /// Builds an instance of `settings::GameConfig` from the builder.
    pub fn build(self) -> GameConfig {
        GameConfig {
            name: self.name,
            id: self.id,
            link: self.link,
            variant: self.variant,
            time_travel: self.time_travel,
            adjacencies: self.adjacencies,
            automatic_adjudication: self.automatic_adjudication,
        }
    }
}

/// The current phase of the game.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum Phase {
    Spring,
    Fall,
    Winter,
}

/// The current turn of the game.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Turn {
    /// The absolute turn number, either zero- or one-indexed.
    pub number: u8,
    /// The year of the game with any additional formatting (e.g. "224 BCE")
    pub year: String,
    /// The current phase of the game.
    pub phase: Phase,
    /// Whether the game is currently in retreats.
    pub is_retreats: bool,
}

/// A game instance, storing all current game state.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Game {
    /// The configuration of the game instance.
    pub config: GameConfig,
    /// The player / nation being played.
    pub player: Player,
    /// The current turn of the game.
    pub turn: Turn,
}

/// A struct containing the information about a `Game` and its state only concerning/privy to one `Player`.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PlayerSpecifics {
    pub player: Player,
    // order_drafts: Vec<OrderSet> // for example
}

/// A wrapper struct around `Game` and `PlayerSpecifics` for more compact serialisation
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GameCache {
    pub game: Game,
    pub player_specifics: Vec<PlayerSpecifics>
}

impl GameCache {
    pub fn get_player_specifics(&self, player_name: String) -> Option<&PlayerSpecifics> {
        self.player_specifics.iter().find(|x| x.player.name == player_name)
    }
}

impl From<Game> for GameCache {
    fn from(value: Game) -> Self {
        GameCache { game: value, player_specifics: vec![] }
    }
}