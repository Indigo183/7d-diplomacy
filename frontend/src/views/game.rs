use dioxus::prelude::*;

use crate::components::Interface;

#[component]
pub fn Game(id: String) -> Element {
    rsx! {
        Interface { }

        div { class: "relative z-1",
            h1 { class: "width-9/10 text-2xl py-10 text-center",
                "Welcome to 0D Diplomacy with No-verse Time (and Space) Stagnation!"
            }

            p { class: "text-gray-400 text-center", "Game ID: {id}" }
        }
    }
}
