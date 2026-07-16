use dioxus::prelude::*;

#[component]
pub fn ResumeGameList() -> Element {
    rsx! {
        div { class: "menu-options resume-list",

            ResumeGameListSmall {  }
            hr { class: "w-95/100 center-0 m-auto" }
            ResumeGameListSmall {  }
            hr { class: "w-95/100 center-0 m-auto" }
            ResumeGameListBig {  }
            hr { class: "w-95/100 center-0 m-auto" }
            ResumeGameListBig {  }
            hr { class: "w-95/100 center-0 m-auto" }
            ResumeGameListSmall {  }
        }
    }
}

#[component]
fn ResumeGameListBig() -> Element {
    let game_name: String = "Romans".to_owned();
    let power_name: String = "Cato".to_owned();
    let power_colour: String = "#2B79EA".to_owned();
    let game_turn: usize = 5;
    // let status: SubmissionStatus = SubmissionStatus::Ready;

    rsx! {
        div { class: "menu-options p-5",
            div { class: "flex justify-between w-[80vw] py-3 px-10",
                div { class: "flex gap-3",
                    h1 { class: "text-nowrap ", {game_name} }
                    h1 { class: "text-nowrap text-sm/9.5 text-gray-400 bottom-0", "as" }
                    h1 { class: format!("text-nowrap"), style: "color:{power_colour}", "{power_name}" }
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

#[component]
fn ResumeGameListSmall() -> Element {
    let game_name: String = "Double Trouble".to_owned();
    let power_name: String = "Serbia-Moscow".to_owned();
    let power_colour: String = "#B370BA".to_owned();
    let game_turn: usize = 2;
    // let status: SubmissionStatus = SubmissionStatus::Ready;

    rsx! {
        div { class: "menu-options p-5",
            div { class: "flex justify-between w-[80vw] py-3 px-10",
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
