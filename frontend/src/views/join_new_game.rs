use dioxus::prelude::*;

const THROBBER: Asset = asset!("/assets/styling/throbber.css");
const HEADER_SVG: Asset = asset!("/assets/header.svg");

/// The menu for joining a new game remotely.
#[component]
pub fn JoinNewGame() -> Element {
    let mut is_loading = use_signal(|| false);

    rsx! {
        document::Link { rel: "stylesheet", href: THROBBER }
        div {
            class: "menu",
            img { src: HEADER_SVG, id: "header" }
            div {
                class: "menu-options",
                if is_loading() {
                    span { class: "loader" }
                } else {
                    input {
                        placeholder: "https://example.org:9090/",
                        onkeydown: move |event| if event.key() == Key::Enter { is_loading.set(true) },
                    }
                }
            }
        }
    }
}
