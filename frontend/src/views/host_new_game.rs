use dioxus::prelude::*;

enum Adjacencies {
    Strict,
    Loose,
    NotSelected,
}
impl std::fmt::Display for Adjacencies {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::Strict => write!(f, "Strict"),
            Self::Loose => write!(f, "Loose"),
            Self::NotSelected => write!(f, ""),
        }
    }
}

const HEADER_SVG: Asset = asset!("/assets/header.svg");

/// The menu for hosting a new game locally.
#[component]
pub fn HostNewGame() -> Element {
    let mut name = use_signal(|| String::new());
    let mut adjacencies = use_signal(|| Adjacencies::NotSelected);

    rsx! {
        div {
            class: "menu",
            img { src: HEADER_SVG, id: "header" }
            div {
                class: "menu-options",
                input {
                    placeholder: "Game Name",
                    value: "{name().to_ascii_uppercase()}", // avoids flashing lowercase
                    oninput: move |event| name.set(event.value().to_ascii_uppercase()),
                }
                div {
                    id: "adjacencies",
                    button {
                        id: "left",
                        style: if let Adjacencies::Strict = *adjacencies.read() {
                            "background-color: #203030;"
                        },
                        onclick: move |_event| adjacencies.set(Adjacencies::Strict),
                        "Strict"
                    }
                    button {
                        id: "right",
                        style: if let Adjacencies::Loose = *adjacencies.read() {
                            "background-color: #203030;"
                        },
                        onclick: move |_event| adjacencies.set(Adjacencies::Loose),
                        "Loose"
                    }
                }
            }
            div {
                p { "NAME: {name}" }
                p { "ADJACENCIES: {adjacencies}" }
            }
        }
    }
}
