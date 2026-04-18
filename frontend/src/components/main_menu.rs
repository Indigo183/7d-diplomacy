use dioxus::prelude::*;

const HEADER_SVG: Asset = asset!("/assets/header.svg");

#[component]
pub fn MainMenu() -> Element {
    let mut last_pressed = use_signal(|| "Select an option");

    rsx! {
        // We can create elements inside the rsx macro with the element name followed by a block of attributes and children.
        div {
            // Attributes should be defined in the element before any children
            id: "menu",
            // After all attributes are defined, we can define child elements and components
            img { src: HEADER_SVG, id: "header" }
            div { id: "menu-buttons",
                // The RSX macro also supports text nodes surrounded by quotes
                button { onclick: move |_event| last_pressed.set("Selected: Resume Game"), "Resume Game" }
                button { onclick: move |_event| last_pressed.set("Selected: Join New Game"), "Join New Game" }
                button { onclick: move |_event| last_pressed.set("Selected: Host New Game"), "Host New Game" }
            }
            div {
                h1 {
                    "{last_pressed}"
                }
            }
        }
    }
}
