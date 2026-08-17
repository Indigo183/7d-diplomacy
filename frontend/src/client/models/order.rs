use crate::client::models::utils::*;
use serde::{Deserialize, Serialize};

// trait Action<'a>: Debug + Clone + PartialEq + Eq + Serialize + Deserialize<'a> { }
//
// #[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
// pub struct Holds;
// impl Action<'_> for Holds { }
// #[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
// pub struct Moves { to: Location }
// impl Action<'_> for Moves { }
// #[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
// pub struct Supports { order: Order }
// impl Action<'_> for Supports { }
// #[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
// pub struct Convoys { order: MoveOrder }
// impl Action<'_> for Convoys { }

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub enum Action {
    Holds,
    Moves(Moves),
    Supports(Supports),
    Convoys(Convoys),
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct Moves {
    to: Location,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct Supports {
    order: SupportableOrder,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct Convoys {
    order: MoveOrder,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub enum Order {
    HoldOrder,
    MoveOrder(MoveOrder),
    SupportOrder(SupportOrder),
    ConvoyOrder(ConvoyOrder),
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub enum SupportableOrder {
    HoldOrder,
    MoveOrder(MoveOrder),
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct MoveOrder {
    piece: Piece,
    moves: Moves,
    flare: Option<TemporalFlare>,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct SupportOrder {
    piece: Piece,
    supports: Supports,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ConvoyOrder {
    piece: Piece,
    convoys: Convoys,
}

// trait Inputtable<'a>: Debug + Clone + PartialEq + Eq + Serialize + Deserialize<'a> {
//     fn get_piece() -> Piece;
//     fn get_from() -> Location { Self::get_piece().location }
//     fn is_local() -> bool { true }
// }

// #[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
// pub struct Order {
//     piece: Piece,
//     action: Box<dyn Action>,
// }

// // An action and the piece ordering it
// sealed class Order(override val piece: Piece, @JsonIgnore val symbol: String): Inputtable {
// abstract val action: Action
// fun asLocal(): String = toString()
//
// override fun equals(other: Any?): Boolean =
// other is Order && other.from == from && other.action == action
//
// override fun toString(): String = if (isLocal()) {
// "${piece.asLocal()}$symbol${action.asLocal()}"
// } else {
// "$piece$symbol$action"
// }
// override fun hashCode(): Int = piece.hashCode() * 31 + action.hashCode()
// }

// #[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
// pub struct ConvoyOrder {
//     pub piece: Piece,
//     #[serde(deserialize_with = "deserialize_convoy_action")]
//     pub action: MoveOrder,
// }

// #[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
// pub struct MoveOrder {
//     pub piece: Piece,
//     pub action: Moves,
//     pub flare: Option<TemporalFlare>,
// }

// #[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
// pub struct Order {
//     pub piece: Piece,
//     pub action: dyn Action,
// }

// #[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
// pub struct SupportOrder {
//     pub piece: Piece,
//     #[serde(deserialize_with = "deserialize_support_action")]
//     pub action: Order,
// }

// fn deserialize_move_action<'de, D>(deserializer: D) -> Result<Location, D::Error>
// where
//     D: Deserializer<'de>,
// {
//     #[derive(Deserialize)]
//     #[serde(untagged)]
//     enum WireAction {
//         Direct(Location),
//         Wrapped { to: Location },
//     }
//
//     Ok(match WireAction::deserialize(deserializer)? {
//         WireAction::Direct(location) | WireAction::Wrapped { to: location } => location,
//     })
// }
//
// fn deserialize_support_action<'de, D>(deserializer: D) -> Result<Order, D::Error>
// where
//     D: Deserializer<'de>,
// {
//     #[derive(Deserialize)]
//     #[serde(untagged)]
//     enum WireAction {
//         Direct(Order),
//         Wrapped { order: Order },
//     }
//
//     Ok(match WireAction::deserialize(deserializer)? {
//         WireAction::Direct(order) | WireAction::Wrapped { order } => order,
//     })
// }
//
// fn deserialize_convoy_action<'de, D>(deserializer: D) -> Result<MoveOrder, D::Error>
// where
//     D: Deserializer<'de>,
// {
//     #[derive(Deserialize)]
//     #[serde(untagged)]
//     enum WireAction {
//         Direct(MoveOrder),
//         Wrapped { r#move: MoveOrder },
//     }
//
//     Ok(match WireAction::deserialize(deserializer)? {
//         WireAction::Direct(order) | WireAction::Wrapped { r#move: order } => order,
//     })
// }
