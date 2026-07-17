#[derive(PartialEq, Eq, Clone, Copy)]
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
    fn default() -> Self {
        Self::SevenDimensional
    }
}

#[derive(PartialEq, Eq, Clone, Copy)]
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
    fn default() -> Self {
        Self::Strict
    }
}

/// The variant data for a specific variant, parsed from JSON.
pub struct Variant {
    /// The variant's name (not including time travel details).
    pub name: String,
    /// The variant's map data (TODO: NOT IMPLEMENTED).
    map: (),
    /// The variant's player list.
    player_list: Vec<String>,
}
impl Default for Variant {
    fn default() -> Self {
        Self {
            name: String::from("Romans"),
            map: (),
            player_list: vec![String::from("Cato"), String::from("Pompey")],
        }
    }
}

/// The configuration for a given game instance (e.g "5D Diplomacy AC").
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
        Self { time_travel, ..self }
    }
    /// The game's adjacency settings.
    pub fn with_adjacencies(self, adjacencies: Adjacencies) -> Self {
        Self { adjacencies, ..self }
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
        }
    }
}
