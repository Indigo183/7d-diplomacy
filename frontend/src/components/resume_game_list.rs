use crate::Route;
use dioxus::prelude::*;

#[component]
pub fn ResumeGameList() -> Element {
    rsx! {
        div { class: "menu-options resume-list ",

            Item { game_name: "T1S02 Holland", power_name: "France",  power_colour: "#2B79EA", game_turn: 1 }
            hr { class: "w-95/100 center-0 m-auto" }
            Item { game_name: "Double Trouble", power_name: "Serbia-Moscow",  power_colour: "#B370BA", game_turn: 2 }
            hr { class: "w-95/100 center-0 m-auto" }
            Item { game_name: "Romans", power_name: "Cato",  power_colour: "#2B79EA", game_turn: 7 }
            hr { class: "w-95/100 center-0 m-auto" }
            Item {  game_name: "Torture", power_name: "Epstein Island",  power_colour: "#75401a", game_turn: 5  }
            hr { class: "w-95/100 center-0 m-auto" }
            Item { game_name: "Variant Champion 2026", power_name: "Gamemaster",  power_colour: "#ffffff", game_turn: 10 }
        }
    }
}

#[component]
fn Item(game_name: String, power_name: String, power_colour: String, game_turn: usize) -> Element {
    rsx! {
        Link {
            to: Route::Game { id: game_name.clone() },
            div { class: "group m-5 hover:bg-gray-800 bg-clip-border rounded-[10] transition-colors duration-200",
                ItemTitle {
                    game_name: game_name.clone(),
                    power_name: power_name.clone(),
                    power_colour: power_colour.clone(),
                    game_turn
                }

                div { class: "grid grid-rows-[0fr] opacity-0
                        transition-[grid-template-rows,opacity]
                        duration-300
                        group-hover:grid-rows-[1fr]
                        group-hover:opacity-100
                        group-hover:delay-1000",
                    div { class: "overflow-hidden",
                        ItemBody {
                            game_name,
                            power_name,
                            power_colour,
                            game_turn
                        }
                    }
                }
            }
        }
    }
}

#[component]
fn ItemTitle(
    game_name: String,
    power_name: String,
    power_colour: String,
    game_turn: usize,
) -> Element {
    rsx! {
        div { class: "flex justify-between py-3 px-10",
            div { class: "flex gap-3",
                h1 { class: "text-nowrap", {game_name} }
                h1 { class: "text-nowrap text-sm/9.5 text-gray-400 bottom-0", "as" }
                h1 { class: "text-nowrap", style: "color:{power_colour}", "{power_name}" }
                }

            div { class: "flex gap-3",
                h1 { class: "text-nowrap", "Turn {game_turn}" }
                h1 { class: "text-nowrap", "-" }
                h1 { class: "text-nowrap text-red-400", "Unsubmitted" }
            }
        }
    }
}

#[component]
fn ItemBody(
    game_name: String,
    power_name: String,
    power_colour: String,
    game_turn: usize,
) -> Element {
    rsx! {
        div { class: "flex justify-between text-sm py-3 px-10",
            p { "7D Diplomacy - Romans" }
            p { class: "text-gray-500", "|" }
            p { "Loose Adjacencies" }
            p { class: "text-gray-500", "|" }
            p { "Manual Adjudication" }
        }
        p { class: "text-sm text-gray-400 py-3 px-10 text-left",
            "https://7d-diplomacy.panik!.net:80085"
        }
    }
}
