use crate::Route;
use dioxus::prelude::*;

#[component]
pub fn ResumeGameList() -> Element {
    rsx! {
        div { class: "menu-options resume-list ",

            ResumeGameListSmall { game_name: "T1S02 Holland", power_name: "France",  power_colour: "#2B79EA", game_turn: 1 }
            hr { class: "w-95/100 center-0 m-auto" }
            ResumeGameListSmall { game_name: "Double Trouble", power_name: "Serbia-Moscow",  power_colour: "#B370BA", game_turn: 2 }
            hr { class: "w-95/100 center-0 m-auto" }
            ResumeGameListBig { game_name: "Romans", power_name: "Cato",  power_colour: "#2B79EA", game_turn: 7 }
            hr { class: "w-95/100 center-0 m-auto" }
            ResumeGameListBig {  game_name: "Torture", power_name: "Epstein Island",  power_colour: "#75401a", game_turn: 5  }
            hr { class: "w-95/100 center-0 m-auto" }
            ResumeGameListSmall { game_name: "Variant Champion 2026", power_name: "Gamemaster",  power_colour: "#ffffff", game_turn: 10 }
        }
    }
}

#[component]
fn ResumeGameListBig(
    game_name: String,
    power_name: String,
    power_colour: String,
    game_turn: usize,
) -> Element {
    // let status: SubmissionStatus = SubmissionStatus::Ready;

    rsx! {
        Link {
            to: Route::Game { id: game_name.clone() },
            div { class: "menu-options m-5 hover:bg-gray-800 bg-clip-border rounded-[10]",
                div { class: "flex justify-between w-[80vw] py-3 px-10 ",
                    div { class: "flex gap-3",
                        h1 { class: "text-nowrap ", { game_name } }
                        h1 { class: "text-nowrap text-sm/9.5 text-gray-400 bottom-0", "as" }
                        h1 {
                            class: "text-nowrap".to_string(),
                            style: "color:{power_colour}",
                            "{power_name}",
                        }
                    }

                    div { class: "flex gap-3",
                        h1 { class: "text-nowrap", "Turn {game_turn}" }
                        h1 { class: "text-nowrap", "-" }
                        h1 { class: "text-nowrap text-green-400", "Submitted and Ready" }
                    }
                }

                div { class: "flex justify-between text-sm py-3 px-10",
                    p { "7D Diplomacy - Romans" }
                    p { class: "text-gray-500", "|" }
                    p { "Loose Adjacencies" }
                    p { class: "text-gray-500", "|" }
                    p { "Manual Adjudication" }
                }

                p { class: "text-sm text-gray-400 py-3 px-10  text-left",
                    "https://7d-diplomacy.panik!.net:80085"
                }
            }
        }
    }
}

#[component]
fn ResumeGameListSmall(
    game_name: String,
    power_name: String,
    power_colour: String,
    game_turn: usize,
) -> Element {
    rsx! {
        Link {
            to: Route::Game { id: game_name.clone() },
            div { class: "menu-options m-5",
                div { class: "flex justify-between w-[80vw] py-3 px-10 hover:bg-gray-800 bg-clip-border rounded-[10]",
                    div { class: "flex gap-3",
                        h1 { class: "text-nowrap ", {game_name} }
                        h1 { class: "text-nowrap text-sm/9.5 text-gray-400 bottom-0", "as" }
                        h1 { class: "text-nowrap", style: "color:{power_colour}", "{power_name}" }
                    }

                    div { class: "flex gap-3",
                        h1 { class: "text-nowrap", "T{game_turn}" }
                        h1 { class: "text-nowrap", "-" }
                        h1 { class: "text-nowrap text-red-400", "Unsubmitted" }
                    }
                }
            }
        }
    }
}
