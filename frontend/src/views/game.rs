use dioxus::prelude::*;

#[component]
pub fn Game(id: String) -> Element {
    rsx! {

        h1 { class: "width-9/10 text-2xl py-10 text-center", "Welcome to 0D Diplomacy with No-verse Time (and Space) Stagnation!" }

        p {
            class: "text-gray-400 text-center",
            "Game ID: {id}"
        }
    }
}