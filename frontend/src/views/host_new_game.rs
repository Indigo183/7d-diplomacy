use std::env;
use std::fs;

use crate::Route;
use crate::settings::*;

use dioxus::prelude::*;
use tokio::time::Duration;
use tokio::time::sleep;

fn sanitise(input: String) -> String {
    input
        .to_ascii_lowercase()
        .chars()
        .map(|c| if c == ' ' { '-' } else { c })
        .filter(|&c| c.is_ascii_alphanumeric() || c == '-')
        .collect()
}

fn validate_game_id(id: &str) -> Result<(), String> {
    if id == "select-game" {
        return Err(String::from("game id `select-game` is invalid"));
    }
    let game_directory = env::home_dir()
        .expect("cannot detect user's home directory")
        .join(".7dip");
    let game_cache_directory = game_directory.join("joined-games/");
    let game_storage_directory = game_directory.join("hosted-games/");
    if fs::exists(game_cache_directory.join(id)).expect("could not access file")
        || fs::exists(game_storage_directory.join(id)).expect("could not access file")
    {
        Err(format!("game id `{id}` already exists"))
    } else {
        Ok(())
    }
}

async fn create_new_game(
    name: String,
    id: String,
    adjacencies: Adjacencies,
) -> Result<Route, &'static str> {
    sleep(Duration::from_secs(2)).await;
    println!("{name}\n{id}\n{adjacencies}");
    Ok(Route::Game { id })
}

const HEADER_SVG: Asset = asset!("/assets/header.svg");

/// The menu for hosting a new game locally.
#[component]
pub fn HostNewGame() -> Element {
    let mut name = use_signal(|| String::new());
    let mut id = use_signal(|| String::new());
    let mut adjacencies: Signal<Option<Adjacencies>> = use_signal(|| None);
    let mut config: Signal<Option<Result<(), String>>> = use_signal(|| None);

    let input_is_valid = move || adjacencies().is_some() && !id().is_empty();
    let is_loading = move || config() == Some(Ok(()));

    rsx! {
        div {
            class: "menu",
            onmousedown: move |_event| if let Some(Err(_)) = config() { config.set(None) },
            onkeydown: move |_event| if let Some(Err(_)) = config() { config.set(None) },
            img { src: HEADER_SVG, id: "header" }
            div {
                class: "menu-options",
                input {
                    placeholder: "Game Name",
                    oninput: move |event| name.set(event.value()),
                    disabled: is_loading(),
                }
                input {
                    placeholder: "Game ID",
                    value: id(),
                    text_transform: "lowercase",
                    oninput: move |event| id.set(sanitise(event.value())),
                    disabled: is_loading(),
                }
                div {
                    id: "adjacencies",
                    button {
                        id: "left-button",
                        style: if let Some(Adjacencies::Strict) = *adjacencies.read() {
                            "background-color: darkslategray;"
                        },
                        onclick: move |_event| adjacencies.set(Some(Adjacencies::Strict)),
                        disabled: is_loading(),
                        "Strict"
                    }
                    button {
                        id: "right-button",
                        style: if let Some(Adjacencies::Loose) = *adjacencies.read() {
                            "background-color: darkslategray;"
                        },
                        onclick: move |_event| adjacencies.set(Some(Adjacencies::Loose)),
                        disabled: is_loading(),
                        "Loose"
                    }
                }
                button {
                    color: if let Some(Err(_)) = config() { "red" },
                    onclick: move |_event| async move {
                        if input_is_valid() && !is_loading() {
                            let status = validate_game_id(&id());
                            config.set(Some(status.clone()));

                            if let Ok(_) = status {
                                let result = create_new_game(
                                    name(),
                                    id(),
                                    adjacencies().expect("should be checked by `input_is_valid()`"),
                                ).await;
                                if let Ok(route) = result {
                                    use_navigator().push(route);
                                } else {
                                    config.set(Some(Err(String::from(result.unwrap_err()))));
                                }
                            }
                        }
                    },
                    disabled: !input_is_valid() || is_loading(),
                    match config() {
                        None => String::from("Submit"),
                        Some(Err(err)) => err,
                        Some(Ok(())) => String::from("Creating..."),
                    }
                }
            }
        }
    }
}
