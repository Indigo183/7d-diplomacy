use dioxus::prelude::*;

const HEADER_SVG: Asset = asset!("/assets/header.svg");

/// The menu for hosting a new game locally.
#[component]
pub fn HostNewGame() -> Element {
    rsx! {
        div {
            id: "menu",
            img { src: HEADER_SVG, id: "header" }
            div { id: "menu-options",
                input { placeholder: "name" }
            }
        }
    }
}
