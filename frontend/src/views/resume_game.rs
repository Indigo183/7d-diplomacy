use dioxus::prelude::*;

/// The menu for resuming a locally saved game.
#[component]
pub fn ResumeGame(id: String) -> Element {
    if &id == "select-game" {
        rsx! { "NOTHING" }
    } else {
        rsx! { { id } }
    }
}
