use crate::models::*;
use crate::props::*;
use std::sync::LazyLock;

pub const HEADER_PROPS: LazyLock<Vec<GameHeaderProps>> = LazyLock::new(|| {
    vec![
        GameHeaderProps {
            game_name: "T1S02 Holland".to_string(),
            player: Player {
                name: "France".to_string(),
                colour: RGBA::from(0x2B79EAFF),
            },
            turn: Turn {
                number: 1,
                year: "1901".to_string(),
                phase: Phase::Spring,
                is_retreats: false,
            },
            status: Status::Ready,
            map_name: "Classic".to_string(),
            time_travel: TimeTravelDetails::from(Some(TimeTravel {
                time_travel_type: TimeTravelType::FiveDimensional,
                adjacencies: Adjacencies::Loose,
            })),
            auto_adjudicate: true,
            server_address: "http://5d-diplomacy.keine-panik.net:5173/".to_string(),
        },
        GameHeaderProps {
            game_name: "Double Trouble".to_string(),
            player: Player {
                name: "Serbia-Moscow".to_string(),
                colour: RGBA::from(0xB370BAFF),
            },
            turn: Turn {
                number: 2,
                year: "1901".to_string(),
                phase: Phase::Fall,
                is_retreats: false,
            },
            status: Status::Locked,
            map_name: "Classic (Double Trouble)".to_string(),
            time_travel: TimeTravelDetails::from(Some(TimeTravel {
                time_travel_type: TimeTravelType::FiveDimensional,
                adjacencies: Adjacencies::Strict,
            })),
            auto_adjudicate: false,
            server_address: "http://5d-diplomacy.keine-panik.net:5170/".to_string(),
        },
        GameHeaderProps {
            game_name: "Romans".to_string(),
            player: Player {
                name: "Cato".to_string(),
                colour: RGBA::from(0x2B79EAFF),
            },
            turn: Turn {
                number: 7,
                year: "32 BCE".to_string(),
                phase: Phase::Spring,
                is_retreats: false,
            },
            status: Status::Unsubmitted,
            map_name: "Romans".to_string(),
            time_travel: TimeTravelDetails::from(Some(TimeTravel {
                time_travel_type: TimeTravelType::SevenDimensional,
                adjacencies: Adjacencies::Strict,
            })),
            auto_adjudicate: true,
            server_address: "http://7d-diplomacy.keine-panik.net:8080/".to_string(),
        },
        GameHeaderProps {
            game_name: "Torture".to_string(),
            player: Player {
                name: "Epstein Island".to_string(),
                colour: RGBA::from(0x75401AFF),
            },
            turn: Turn {
                number: 5,
                year: "2014".to_string(),
                phase: Phase::Fall,
                is_retreats: false,
            },
            status: Status::Locked,
            map_name: "Crowded Imperial Diplomacy".to_string(),
            time_travel: TimeTravelDetails::from(None),
            auto_adjudicate: false,
            server_address: "http://5d-diplomacy.keine-panik.net:5173/".to_string(),
        },
        GameHeaderProps {
            game_name: "Variant Champion 2026".to_string(),
            player: Player {
                name: "Gamemaster".to_string(),
                colour: RGBA::from(0xFFFFFFFF),
            },
            turn: Turn {
                number: 10,
                year: "17776 CE".to_string(),
                phase: Phase::Spring,
                is_retreats: false,
            },
            status: Status::Submitted,
            map_name: "Classic".to_string(),
            time_travel: TimeTravelDetails::from(Some(TimeTravel {
                time_travel_type: TimeTravelType::SevenDimensional,
                adjacencies: Adjacencies::Loose,
            })),
            auto_adjudicate: false,
            server_address: "https://discord.gg/k6bwkadDKr/".to_string(),
        },
    ]
});
