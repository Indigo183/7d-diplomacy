use crate::Route;
use dioxus::prelude::*;

const HEADER_SVG: Asset = asset!("/assets/header.svg");

#[component]
pub fn MainMenu() -> Element {
    rsx! {
        // We can create elements inside the rsx macro with the element name followed by a block of attributes and children.
        div {
            // Attributes should be defined in the element before any children
            id: "menu",
            // After all attributes are defined, we can define child elements and components
            img { src: HEADER_SVG, id: "header" }
            div { id: "menu-options",
                // The RSX macro also supports text nodes surrounded by quotes
                Link { to: Route::ResumeGame {}, "Resume Game" }
                Link { to: Route::JoinNewGame {}, "Join New Game" }
                Link { to: Route::HostNewGame {}, "Host New Game" }
            }
        }
    }
}
