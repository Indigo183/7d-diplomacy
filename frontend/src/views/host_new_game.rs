use dioxus::prelude::*;

#[derive(PartialEq)]
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

fn create_new_game() -> Result<(), &'static str> {
    // Err("error")
    Ok(())
}

const HEADER_SVG: Asset = asset!("/assets/header.svg");

/// The menu for hosting a new game locally.
#[component]
pub fn HostNewGame() -> Element {
    let mut name = use_signal(|| String::new());
    let mut adjacencies = use_signal(|| Adjacencies::NotSelected);
    let mut creating: Signal<Option<Result<(), &'static str>>> = use_signal(|| None);

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
                        id: "left-button",
                        style: if let Adjacencies::Strict = *adjacencies.read() {
                            "background-color: darkslategray;"
                        },
                        onclick: move |_event| adjacencies.set(Adjacencies::Strict),
                        "Strict"
                    }
                    button {
                        id: "right-button",
                        style: if let Adjacencies::Loose = *adjacencies.read() {
                            "background-color: darkslategray;"
                        },
                        onclick: move |_event| adjacencies.set(Adjacencies::Loose),
                        "Loose"
                    }
                }
                button {
                    color: match *creating.read() {
                        Some(Ok(_)) => "gray",
                        Some(Err(_)) => "red",
                        None if *adjacencies.read() == Adjacencies::NotSelected || name().is_empty() => "gray",
                        _ => "white",
                    },
                    // if let Adjacencies::NotSelected = *adjacencies.read() { "gray" }
                    // else if name().is_empty() { "gray" },
                    onclick: move |_event| if *adjacencies.read() != Adjacencies::NotSelected && !name().is_empty() {
                        creating.set(Some(create_new_game()));
                    },
                    match creating() {
                        None => "Submit",
                        Some(Err(err)) => err,
                        Some(Ok(())) => "Creating..."
                    }
                }
            }
            // div {
            //     p { "NAME: \"{name}\"" }
            //     p { "ADJACENCIES: {adjacencies}" }
            // }
        }
    }
}
