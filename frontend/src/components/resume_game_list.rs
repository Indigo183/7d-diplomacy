use crate::Route;
use crate::models::*;
use crate::props::GameHeaderProps;
use dioxus::prelude::*;

#[component]
pub fn ResumeGameList() -> Element {
    let items = vec![
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
    ];

    rsx! {
        div { class: "menu-options resume-list divide-y divide-gray-600",
            for item in items {
                Item { game: item }
            }
        }
    }
}

#[component]
fn Item(game: GameHeaderProps) -> Element {
    rsx! {
        Link {
            to: Route::Game {
                id: game.game_name.clone(),
            },
            div { class: "group m-5 py-3 px-10 hover:bg-gray-800 bg-clip-border rounded-md transition-colors duration-200",
                ItemTitle { game: game.clone() }

                div { class: "grid grid-rows-[0fr] opacity-0
                        transition-[grid-template-rows,opacity]
                        duration-300
                        group-hover:grid-rows-[1fr]
                        group-hover:opacity-100
                        group-hover:delay-1000",
                    div { class: "pt-3 space-y-3 overflow-hidden",
                        ItemBody { game }
                    }
                }
            }
        }
    }
}

#[component]
fn ItemTitle(game: GameHeaderProps) -> Element {
    rsx! {
        div { class: "flex justify-between",
            div { class: "flex gap-3",
                h1 { class: "text-nowrap", {game.game_name} }
                h1 { class: "text-nowrap text-sm/9.5 text-gray-400 bottom-0", "as" }
                h1 { class: "text-nowrap", style: "color:{game.player.colour}", "{game.player.name}" }
            }

            div { class: "flex gap-3",
                h1 { class: "text-nowrap", "{game.turn}" }
                h1 { class: "text-nowrap", "-" }
                h1 { class: "text-nowrap text-{game.status.tailwind_colour()}", "{game.status}" }
            }
        }
    }
}

#[component]
fn ItemBody(game: GameHeaderProps) -> Element {
    rsx! {
        div { class: "flex justify-between text-sm",
            p { { game.map_name } }
            div { class: "h-4 w-px bg-gray-600" }
            p { "{game.time_travel}" }
            div { class: "h-4 w-px bg-gray-600" }
            p { if game.auto_adjudicate { "Adjudicates Automatically" } else { "Manual Adjudication" } }
        }

        p { class: "text-sm text-gray-400 text-left",
            { game.server_address }
        }
    }
}
