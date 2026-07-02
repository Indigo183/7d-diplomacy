use dioxus::prelude::*;

const THROBBER: Asset = asset!("/assets/styling/throbber.css");
const HEADER_SVG: Asset = asset!("/assets/header.svg");

enum Adjacencies {
    Strict,
    Loose,
}
impl std::fmt::Display for Adjacencies {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        if let Self::Strict = self {
            write!(f, "Strict")
        } else {
            write!(f, "Loose")
        }
    }
}

enum TimeTravel {
    FiveDimensional,
    SevenDimensional,
}

struct Variant {
    /// The variant's name (not including time travel details).
    name: String,
    /// The variant's time travel details.
    time_travel: TimeTravel,
    /// The variant's map data (TODO: NOT IMPLEMENTED).
    map: (),
    /// The variant's player list.
    player_list: Vec<String>,
}

struct Config {
    /// The game's non-unique official name.
    name: String,
    /// The game's unique alphanumeric identifier.
    id: String,
    /// The game's variant data.
    variant: Variant,
    /// The game's adjacency settings.
    adjacencies: Adjacencies,
}

async fn load_game(
    game_link: String,
    mut is_loading: Signal<bool>,
    mut loaded: Signal<bool>,
) -> Result<Config, &'static str> {
    is_loading.set(true);

    smol::Timer::after(std::time::Duration::from_secs(2)).await;

    is_loading.set(false);
    loaded.set(true);
    Ok(Config {
        name: String::from("5D \"Double Trouble\" Diplomacy"),
        id: game_link,
        variant: Variant {
            name: String::from("Double Trouble"),
            time_travel: TimeTravel::FiveDimensional,
            map: (),
            player_list: vec![String::from("I'm too lazy :P")],
        },
        adjacencies: Adjacencies::Strict,
    })
}

/// The menu for joining a new game remotely.
#[component]
pub fn JoinNewGame() -> Element {
    let mut game_link = use_signal(|| String::new());
    let mut game_config: Signal<Option<Config>> = use_signal(|| None);

    // apparently don't need to be mutable???
    let is_loading = use_signal(|| false);
    let loaded = use_signal(|| false);

    rsx! {
        document::Link { rel: "stylesheet", href: THROBBER }
        div {
            class: "menu",
            img { src: HEADER_SVG, id: "header" }
            div {
                class: "menu-options",
                if is_loading() {
                    span { class: "loader" }
                } else if !loaded() {
                    input {
                        placeholder: "https://example.org:9090/",
                        oninput: move |event| game_link.set(event.value()),
                        onkeydown: move |event| async move {
                            if event.key() == Key::Enter {
                                game_config.set(load_game(game_link(), is_loading, loaded).await.ok());
                            }
                        },
                    }
                } else {
                    div {
                        div {
                            class: "left-col",
                            "Name",
                        }
                        div {
                            class: "right-col",
                            { game_config.read().as_ref().unwrap().name.clone() },
                        }
                    }
                    div {
                        div {
                            class: "left-col",
                            "Variant",
                        }
                        div {
                            class: "right-col",
                            { game_config.read().as_ref().unwrap().variant.name.clone() },
                        }
                    }
                    div {
                        div {
                            class: "left-col",
                            "Adjacencies",
                        }
                        div {
                            class: "right-col",
                            { format!("{}", game_config.read().as_ref().unwrap().adjacencies) },
                        }
                    }
                    // div { "Name: <NAME>" }
                    // div { "Variant: <VARIANT>" }
                    // div { "Adjacencies: <LOOSE|STRICT>" }
                }
            }
        }
    }
}
