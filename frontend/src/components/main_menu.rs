use crate::Route;
use dioxus::prelude::*;
use dioxus_motion::prelude::*;

const HEADER_SVG: Asset = asset!("/assets/header.svg");

#[component]
pub fn MainMenu() -> Element {
    // Define custom spring configuration
    let spring = use_signal(|| Spring {
        stiffness: 200.0, // Higher = faster, snappier
        damping: 30.0,    // Higher = less bounce
        mass: 0.8,        // Lower = more responsive
        velocity: 0.0,    // Initial velocity
    });

    // Provide spring context to all child components
    use_context_provider(|| spring);

    rsx! {
        // We can create elements inside the rsx macro with the element name followed by a block of attributes and children.
        div {
            // Attributes should be defined in the element before any children
            class: "menu",
            // After all attributes are defined, we can define child elements and components
            img { src: HEADER_SVG, id: "header" }
            div { class: "menu-options",
                // The RSX macro also supports text nodes surrounded by quotes
                Link { to: Route::ResumeGame {}, "Resume Game" }
                Link { to: Route::JoinNewGame {}, "Join New Game" }
                Link { to: Route::HostNewGame {}, "Host New Game" }
            }
        }
    }
}
