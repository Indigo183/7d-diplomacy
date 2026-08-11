use crate::Route;
use crate::components::ResumeGameList;
use dioxus::prelude::*;

const HEADER_SVG: Asset = asset!("/assets/header.svg");

#[component]
pub fn MainMenu() -> Element {
    rsx! {
        div { class: "menu",
            img { src: HEADER_SVG, id: "header" }

            div { class: "menu-options",
                ResumeGameList { items: crate::utils::examples::HEADER_PROPS.clone() }

                div { class: "flex justify between w-[90vw] py-5" }

                Link {
                    to: Route::JoinNewGame {},
                    class: "menu-options m-auto hover:bg-gray-800 rounded-[10] flex justify-between w-[90vw] py-2 border",
                    "Join New Game"
                }

                div { class: "flex justify between w-[90vw] py-1" }

                Link {
                    to: Route::HostNewGame {},
                    class: "menu-options m-auto hover:bg-gray-800 rounded-[10] flex justify-between w-[90vw] py-2 border",
                    "Host New Game"
                }
            }
        }
    }
}
