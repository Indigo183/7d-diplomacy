use serde::de::Error;
use serde::{Deserialize, Deserializer, Serialize};
use std::f32::consts::PI;
use std::ops::{Add, AddAssign, Sub, SubAssign};
use std::str::FromStr;

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize)]
pub struct ComplexNumber {
    pub real: i32,
    pub imaginary: i32,
}

impl ComplexNumber {
    pub const ZERO: Self = Self {
        real: 0,
        imaginary: 0,
    };

    pub fn modulus(&self) -> f32 {

        (self.real as f32).hypot(self.imaginary as f32)
    }
    
    pub fn argument(&self) -> f32 {
        (self.real as f32).atan2(self.imaginary as f32)
    }
}

impl PartialOrd for ComplexNumber {
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        match self.modulus().partial_cmp(&other.modulus()) {
            Some(core::cmp::Ordering::Equal) => {}
            ord => return ord,
        }
        (self.argument() + PI).partial_cmp(&(other.argument() + PI))
    }
}

impl Ord for ComplexNumber {
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        self.partial_cmp(other).unwrap()
    }
}

impl Add for ComplexNumber {
    type Output = Self;

    fn add(self, rhs: Self) -> Self::Output {
        ComplexNumber {
            real: self.real + rhs.real,
            imaginary: self.imaginary + rhs.imaginary,
        }
    }
}

impl Sub for ComplexNumber {
    type Output = Self;

    fn sub(self, rhs: Self) -> Self::Output {
        ComplexNumber {
            real: self.real - rhs.real,
            imaginary: self.imaginary - rhs.imaginary,
        }
    }
}

impl AddAssign for ComplexNumber {
    fn add_assign(&mut self, rhs: Self) {
        self.real += rhs.real;
        self.imaginary += rhs.imaginary;
    }
}

impl SubAssign for ComplexNumber {
    fn sub_assign(&mut self, rhs: Self) {
        self.real -= rhs.real;
        self.imaginary -= rhs.imaginary;
    }
}

impl FromStr for ComplexNumber {
    type Err = String;

    /// Expects a complex number of the form `(-)a(+/-)bi`.
    fn from_str(value: &str) -> Result<Self, Self::Err> {
        let value = value
            .strip_suffix('i')
            .ok_or("expected complex number to end with \'i\'")?;

        let separator = value
            .char_indices()
            .skip(1)
            .find(|(_, c)| matches!(c, '+' | '-'))
            .map(|(i, _)| i)
            .ok_or("expected \'+\' or \'-\' before imaginary component")?;

        let (real, imaginary) = value.split_at(separator);

        Ok(Self {
            real: real.parse().map_err(|_| "got invalid real component")?,
            imaginary: imaginary
                .parse()
                .map_err(|_| "got invalid imaginary component")?,
        })
    }
}

impl<'a> Deserialize<'a> for ComplexNumber {
    fn deserialize<T: Deserializer<'a>>(deserializer: T) -> Result<Self, T::Error> {
        #[derive(Deserialize)]
        #[serde(untagged)]
        enum Representation {
            Object { real: i32, imaginary: i32 },
            String(String),
        }

        match Representation::deserialize(deserializer)? {
            Representation::Object { real, imaginary } => Ok(Self { real, imaginary }),
            Representation::String(value) => value.parse().map_err(T::Error::custom),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(transparent)]
pub struct Province(pub String);

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub struct BoardIndex {
    pub coordinate: ComplexNumber,
    pub timeplane: i32,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct Location {
    pub province: Province,
    pub board_index: BoardIndex,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum UnitType {
    Army,
    Fleet,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct Piece {
    pub location: Location,
    pub unit_type: UnitType,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum TemporalFlare {
    Right,
    Up,
    Left,
    Down,
}
