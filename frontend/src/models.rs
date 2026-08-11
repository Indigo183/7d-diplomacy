use anyhow::{Error, Result, anyhow};
use serde::{Deserialize, Serialize};
use std::fmt::{Display, Formatter};

/// The type of time travel used by a game.
#[derive(Debug, PartialEq, Eq, Clone, Copy, Serialize, Deserialize)]
pub enum TimeTravelType {
    FiveDimensional,
    SevenDimensional,
}

impl Display for TimeTravelType {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::FiveDimensional => write!(f, "5D"),
            Self::SevenDimensional => write!(f, "7D"),
        }
    }
}

impl Default for TimeTravelType {
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

impl Display for Adjacencies {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
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

/// The time travel details (if any) of a game. This is a wrapper type of [`Option<TimeTravel>`].
#[derive(Debug, PartialEq, Eq, Clone, Copy, Serialize, Deserialize)]
pub struct TimeTravelDetails(Option<TimeTravel>);

impl TimeTravelDetails {
    /// Creates a new instance of TimeTravelDetails.
    pub fn new(details: Option<TimeTravel>) -> Self {
        Self(details)
    }

    /// Returns the contained time travel details (if any).
    pub fn time_travel(&self) -> Option<TimeTravel> {
        self.0
    }
}

impl Default for TimeTravelDetails {
    /// The default is currently no time-travel, as most players may simply want to play a "regular"
    /// game of diplomacy. It is my opinion that time-travel should be strictly opt-in.
    fn default() -> Self {
        Self(None)
    }
}

impl From<Option<TimeTravel>> for TimeTravelDetails {
    fn from(value: Option<TimeTravel>) -> Self {
        Self(value)
    }
}

impl Display for TimeTravelDetails {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self.0 {
            Some(t) => write!(f, "{}", t),
            None => write!(f, "No Time Travel"),
        }
    }
}

/// The time travel details of a game.
#[derive(Debug, PartialEq, Eq, Clone, Copy, Serialize, Deserialize)]
pub struct TimeTravel {
    pub time_travel_type: TimeTravelType,
    pub adjacencies: Adjacencies,
}

impl Default for TimeTravel {
    /// The default is currently 7D with strict adjacencies, as that is what is implemented in the backend.
    fn default() -> Self {
        Self {
            time_travel_type: TimeTravelType::default(),
            adjacencies: Adjacencies::default(),
        }
    }
}

impl Display for TimeTravel {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "{} - {} Adjacencies",
            self.time_travel_type, self.adjacencies
        )
    }
}

/// A wrapper struct for RGBA colours, purely for convenience.
#[derive(Debug, PartialEq, Eq, Clone, Copy, Serialize, Deserialize)]
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
    /// Constructs an RGBA colour from an RGB colour, as represented by a [`u32`]. Any bits above the
    /// twenty-fourth bit will be thrown away.
    pub fn from_rgb(rgb: u32) -> Self {
        Self::from(rgb << 8 | 0xFF)
    }

    /// Constructs an RGBA colour from an ARGB colour, as represented by a [`u32`].
    pub fn from_argb(argb: u32) -> Self {
        Self::from(argb.rotate_left(8))
    }
}

impl From<u32> for RGBA {
    fn from(value: u32) -> Self {
        RGBA {
            red: ((value & 0xFF000000) >> 24) as u8,
            green: ((value & 0xFF0000) >> 16) as u8,
            blue: ((value & 0xFF00) >> 8) as u8,
            alpha: (value & 0xFF) as u8,
        }
    }
}

impl TryFrom<&str> for RGBA {
    type Error = Error;

    fn try_from(value: &str) -> Result<Self, Self::Error> {
        let hex_as_str = value.trim_matches('#').to_ascii_lowercase();
        let hex = u32::from_str_radix(&hex_as_str, 16)?;
        match hex_as_str.len() {
            // assumes RGB
            6 => Ok(RGBA::from_rgb(hex)),
            // assumes RGBA
            8 => Ok(RGBA::from(hex)),
            _ => Err(anyhow!("RGB/RGBA must be 6/8 characters long")),
        }
    }
}

impl Display for RGBA {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "#{:02X}{:02X}{:02X}{:02X}",
            self.red, self.green, self.blue, self.alpha
        )
    }
}

impl From<RGBA> for u32 {
    fn from(value: RGBA) -> Self {
        (value.red as u32) << 24
            | (value.green as u32) << 16
            | (value.blue as u32) << 8
            | value.alpha as u32
    }
}

/// The current status of a game instance.
#[derive(Debug, PartialEq, Eq, Clone, Copy, Serialize, Deserialize)]
pub enum Status {
    /// Indicates that orders are required, but have not yet been submitted.
    Unsubmitted,
    /// Indicates that orders have been submitted, but have not yet been marked as final.
    Submitted,
    /// Indicates that orders have been submitted and marked as final, but can still be edited.
    Ready,
    /// Indicates that orders have been locked and cannot be edited. This may also be used to
    /// indicate that no orders are required.
    Locked,
}

impl Status {
    pub fn tailwind_colour(&self) -> &'static str {
        "text-red-400 text-amber-400 text-green-400 text-gray-400"; // loads the tailwind colours
        match self {
            Self::Unsubmitted => "red-400",
            Self::Submitted => "amber-400",
            Self::Ready => "green-400",
            Self::Locked => "gray-400",
        }
    }
}

impl Default for Status {
    fn default() -> Self {
        Self::Unsubmitted
    }
}

impl Display for Status {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "{}",
            match self {
                Self::Unsubmitted => "Unsubmitted",
                Self::Submitted => "Draft Submitted",
                Self::Ready => "Ready",
                Self::Locked => "Locked",
            }
        )
    }
}

/// The associated data for a player, parsed from JSON.
#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
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

/// The variant data for a specific map, parsed from JSON.
#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
pub struct VariantMap {
    /// The variant's name (not including time travel details).
    pub name: String,
    /// The variant's map data. - TODO: NOT IMPLEMENTED
    map: (),
    /// The variant's player list.
    pub player_list: Vec<Player>,
}

impl Default for VariantMap {
    fn default() -> Self {
        Self {
            name: String::from("Romans"),
            map: (),
            player_list: vec![
                Player::new(String::from("Cato"), RGBA::try_from("#265BA5").unwrap()),
                Player::new(String::from("Pompey"), RGBA::try_from("#972530").unwrap()),
            ],
        }
    }
}

/// The configuration for a given game instance (e.g "5D Diplomacy AC").
#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
pub struct GameConfig {
    /// The game's non-unique official name.
    pub name: String,
    /// The game's unique alphanumeric identifier.
    pub id: String,
    /// A link to the game hosted on a potentially external server.
    pub link: String,
    /// The game's variant data.
    pub variant: VariantMap,
    /// The game's time travel details (if any).
    pub time_travel_details: TimeTravelDetails,
    /// The game's adjacency settings.
    pub adjacencies: Adjacencies,
    /// Whether the game will adjudicate itself automatically at the specified deadline.
    pub auto_adjudicate: bool,
}

impl GameConfig {
    /// Instantiates a builder for [`GameConfig`].
    pub fn builder() -> GameConfigBuilder {
        GameConfigBuilder {
            name: String::new(),
            id: String::new(),
            link: String::from("http://localhost:9090/"),
            variant: VariantMap::default(),
            time_travel_details: TimeTravelDetails::default(),
            adjacencies: Adjacencies::default(),
            auto_adjudicate: false,
        }
    }
}

/// A builder type for [`GameConfig`].
pub struct GameConfigBuilder {
    /// The game's non-unique official name.
    name: String,
    /// The game's unique alphanumeric identifier.
    id: String,
    /// A link to the game hosted on a potentially external server.
    link: String,
    /// The game's variant data.
    variant: VariantMap,
    /// The game's time travel details.
    time_travel_details: TimeTravelDetails,
    /// The game's adjacency settings.
    adjacencies: Adjacencies,
    /// Whether the game will adjudicate itself automatically at the specified deadline.
    auto_adjudicate: bool,
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
    pub fn with_variant(self, variant: VariantMap) -> Self {
        Self { variant, ..self }
    }
    /// The game's time travel details.
    pub fn with_time_travel(self, time_travel: TimeTravel) -> Self {
        Self {
            time_travel_details: TimeTravelDetails::new(Some(time_travel)),
            ..self
        }
    }
    /// The game's time travel details (if any).
    pub fn with_option_time_travel(self, option_time_travel: Option<TimeTravel>) -> Self {
        Self {
            time_travel_details: TimeTravelDetails::new(option_time_travel),
            ..self
        }
    }
    /// The game's time travel details (if any).
    pub fn with_time_travel_details(self, time_travel_details: TimeTravelDetails) -> Self {
        Self {
            time_travel_details,
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
    pub fn with_auto_adjudicate(self, auto_adjudicate: bool) -> Self {
        Self {
            auto_adjudicate,
            ..self
        }
    }
    /// Builds an instance of [`GameConfig`] from the builder.
    pub fn build(self) -> GameConfig {
        GameConfig {
            name: self.name,
            id: self.id,
            link: self.link,
            variant: self.variant,
            time_travel_details: self.time_travel_details,
            adjacencies: self.adjacencies,
            auto_adjudicate: self.auto_adjudicate,
        }
    }
}

/// The publicly available game state, including orders and their resulting boards.
#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
pub struct GameState {
    // orders: Vec<Order>,
    // boards: Vec<Board>,
    x: (),
}

/// The current phase of the game.
#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
pub enum Phase {
    Spring,
    Fall,
    Winter,
}

impl Display for Phase {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "{}",
            match self {
                Self::Spring => "Spring",
                Self::Fall => "Fall",
                Self::Winter => "Winter",
            }
        )
    }
}

/// The current turn of the game.
#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
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

impl Display for Turn {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "Turn {}: {} {}", self.number, self.phase, self.year)?;
        if self.is_retreats {
            write!(f, " Retreats")?;
        }

        Ok(())
    }
}

/// A game instance, storing all current game state.
#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
pub struct Game {
    /// The configuration of the game instance.
    pub config: GameConfig,
    /// The publicly available game state, including orders and their resulting boards.
    pub state: GameState,
    /// The player / nation being played.
    pub player: Player,
    /// The current turn of the game.
    pub turn: Turn,
}

/// A struct containing the information about a [`Game`] and its state only concerning/privy to one [`Player`].
#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
pub struct PlayerSpecifics {
    pub player: Player,
    pub status: Status,
    // order_drafts: Vec<OrderSet>, // for example
}

impl PlayerSpecifics {
    /// Constructs a new instance of [`PlayerSpecifics`].
    pub fn new(player: Player, status: Status) -> PlayerSpecifics {
        Self { player, status }
    }

    /// [`PlayerSpecifics`] may not implement [`Default`], as it requires a [`Player`] to be
    /// specified upon creation. However, all fields bar `player` *do* implement
    /// [`Default`], and this function makes use of that to create an instance of
    /// [`PlayerSpecifics`] with only a [`Player`] as input.
    pub fn default(player: Player) -> PlayerSpecifics {
        Self {
            player,
            status: Status::default(),
        }
    }
}

/// A wrapper struct around [`Game`] and [`PlayerSpecifics`] for more compact serialisation. This
/// struct stores all information required for any one game.
#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
pub struct GameCache {
    pub game: Game,
    pub player_specifics: Vec<PlayerSpecifics>,
}

impl GameCache {
    // pub fn get_player_specifics(&self, player_name: String) -> Option<&PlayerSpecifics> {
    //     self.player_specifics
    //         .iter()
    //         .find(|&&x| x.player.name == player_name)
    // }
}

impl From<Game> for GameCache {
    fn from(value: Game) -> Self {
        GameCache {
            game: value,
            player_specifics: vec![],
        }
    }
}

/// A single playable instance of a [`Game`]. This struct includes *references* to the [`Game`] and
/// [`PlayerSpecifics`] in question, and as such implements neither [`Serialize`] nor
/// [`Deserialize`].
#[derive(Debug, PartialEq, Eq, Clone)]
pub struct GameInstance<'a> {
    pub game: &'a Game,
    pub player_specifics: &'a PlayerSpecifics,
}

impl<'a> GameInstance<'a> {
    /// Constructs a [`GameInstance`] from *references* to the [`GameCache`] of the [game](Game)
    /// being played and the [`PlayerSpecifics`] of a specific player. This will check to see if the
    /// game contains the provided player specifics, and will panic if not.
    ///
    /// # Panics
    ///
    /// This function checks whether the game contains the provided player specifics, and will panic
    /// if it doesn't.
    pub fn new(game_cache: &'a GameCache, player_specifics: &'a PlayerSpecifics) -> Self {
        if !game_cache.player_specifics.contains(player_specifics) {
            panic!("expected provided player specifics to reference the provided game cache")
        }

        Self {
            game: &game_cache.game,
            player_specifics,
        }
    }

    /// Constructs a [`GameInstance`] from *references* to the [`GameCache`] of the [game](Game)
    /// being played and the [`PlayerSpecifics`] of a specific player. This will return [`Ok(_)`] if
    /// the game contains the provided player specifics, and [`anyhow::Error`](Error) if not.
    pub fn build(game_cache: &'a GameCache, player_specifics: &'a PlayerSpecifics) -> Result<Self> {
        if !game_cache.player_specifics.contains(player_specifics) {
            Err(anyhow!(
                "expected provided player specifics to reference the provided game cache"
            ))
        } else {
            Ok(Self {
                game: &game_cache.game,
                player_specifics,
            })
        }
    }

    /// Constructs a [`GameInstance`] from a [`Player`] and a mutable reference to the [`GameCache`]
    /// of the [game](Game) being played. This function will create a new instance of
    /// [`PlayerSpecifics`] and insert it into the game cache, and then return a [`GameInstance`]
    /// referencing the newly-created player specifics.
    pub fn from_player(game_cache: &'a mut GameCache, player: Player) -> Self {
        let player_specifics = PlayerSpecifics::new(player, Status::default());
        game_cache.player_specifics.push(player_specifics.clone());

        Self {
            game: &game_cache.game,
            player_specifics: &game_cache
                .player_specifics
                .iter()
                .rev()
                .find(|x| **x == player_specifics)
                .unwrap(),
        }
    }
}

// TODO: Store variants under a common trait such that functions can take a generic argument of type
//       T::Province or T::Order (for example). This could involve parsing an enum from a file, or
//       creating a wrapper type around a `Vec<_>` that can behave similarly to an enum.
