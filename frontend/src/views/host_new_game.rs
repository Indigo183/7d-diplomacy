use dioxus::prelude::*;

#[derive(PartialEq, Clone, Copy)]
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

fn sanitise(input: String) -> String {
    input
        .to_ascii_lowercase()
        .chars()
        .map(|c| if c == ' ' { '-' } else { c })
        .filter(|&c| c.is_ascii_alphanumeric() || c == '-')
        .collect()
}

fn validate_new_game(name: &str, adjacencies: Adjacencies) -> Result<(), &'static str> {
    if name == "test" {
        return Err("game \"test\" already exists");
    }
    Ok(())
}

const HEADER_SVG: Asset = asset!("/assets/header.svg");

/// The menu for hosting a new game locally.
#[component]
pub fn HostNewGame() -> Element {
    let mut name = use_signal(|| String::new());
    let mut adjacencies = use_signal(|| Adjacencies::NotSelected);
    let mut config: Signal<Option<Result<(), &'static str>>> = use_signal(|| None);

    let input_is_valid =
        move || *adjacencies.read() != Adjacencies::NotSelected && !name().is_empty();
    let is_loading = move || config() == Some(Ok(()));

    rsx! {
        div {
            class: "menu",
            img { src: HEADER_SVG, id: "header" }
            div {
                class: "menu-options",
                input {
                    placeholder: "Game Name",
                    value: name(),
                    text_transform: "lowercase",
                    oninput: move |event| name.set(sanitise(event.value())),
                    disabled: is_loading(),
                }
                div {
                    id: "adjacencies",
                    button {
                        id: "left-button",
                        style: if let Adjacencies::Strict = *adjacencies.read() {
                            "background-color: darkslategray;"
                        },
                        onclick: move |_event| adjacencies.set(Adjacencies::Strict),
                        disabled: is_loading(),
                        "Strict"
                    }
                    button {
                        id: "right-button",
                        style: if let Adjacencies::Loose = *adjacencies.read() {
                            "background-color: darkslategray;"
                        },
                        onclick: move |_event| adjacencies.set(Adjacencies::Loose),
                        disabled: is_loading(),
                        "Loose"
                    }
                }
                button {
                    color: if let Some(Err(_)) = config() { "red" },
                    onclick: move |_event| if input_is_valid() && !is_loading() {
                        config.set(Some(validate_new_game(&name(), *adjacencies.read())));
                    },
                    disabled: !input_is_valid() || is_loading(),
                    match config() {
                        None => "Submit",
                        Some(Err(err)) => err,
                        Some(Ok(())) => "Creating..."
                    }
                }
            }
        }
    }
}
