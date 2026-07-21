use crate::Route;
use crate::components::ResumeGameList;
use dioxus::prelude::*;

const HEADER_SVG: Asset = asset!("/assets/header.svg");

#[component]
pub fn MainMenu() -> Element {
    rsx! {
        // We can create elements inside the rsx macro with the element name followed by a block of attributes and children.
        div {
            // Attributes should be defined in the element before any children
            class: "menu",
            // After all attributes are defined, we can define child elements and components
            img { src: HEADER_SVG, id: "header" }
            div { class: "menu-options",
                // The RSX macro also supports text nodes surrounded by quotes
                ResumeGameList {  }
                Link {
                    to: Route::JoinNewGame {},
                    class: "menu-options m-auto hover:bg-gray-800 rounded-[10] flex justify-between w-[25vw] py-3 ",
                    "Join New Game"
                }
                Link {
                    to: Route::HostNewGame {},
                    class: "menu-options m-auto hover:bg-gray-800 rounded-[10] flex justify-between w-[25vw] py-3 ",
                    "Host New Game"
                }
            }
        }
    }
}
