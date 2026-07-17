use crate::settings::*;

use dioxus::prelude::*;
use tokio::time::{Duration, sleep};

const THROBBER: Asset = asset!("/assets/styling/throbber.css"); //OwO
const HEADER_SVG: Asset = asset!("/assets/header.svg");

async fn load_game(
    game_link: String,
    mut is_loading: Signal<bool>,
    mut loaded: Signal<bool>,
) -> Result<GameConfig, &'static str> {
    is_loading.set(true);

    sleep(Duration::from_secs(2)).await;

    is_loading.set(false);
    loaded.set(true);
    Ok(GameConfig::builder()
        .with_name(String::from("5D \"Double Trouble\" Diplomacy"))
        .with_link(game_link)
        .build())
}

/// The menu for joining a new game remotely.
#[component]
pub fn JoinNewGame() -> Element {
    let mut game_link = use_signal(|| String::new());
    let mut game_config: Signal<Option<GameConfig>> = use_signal(|| None);

    // apparently don't need to be mutable???
    let is_loading = use_signal(|| false);
    let loaded = use_signal(|| false);

    rsx! {
        document::Link { rel: "stylesheet", href: THROBBER }
        div { class: "menu",
            img { src: HEADER_SVG, id: "header" }
            div { class: "menu-options",
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
                        div { class: "left-col", "Name" }
                        div { class: "right-col", {game_config.read().as_ref().unwrap().name.clone()} }
                    }
                    div {
                        div { class: "left-col", "Variant" }
                        div { class: "right-col",
                            {game_config.read().as_ref().unwrap().variant.name.clone()}
                        }
                    }
                    div {
                        div { class: "left-col", "Adjacencies" }
                        div { class: "right-col",
                            {format!("{}", game_config.read().as_ref().unwrap().adjacencies)}
                        }
                    }
                }
            }
        }
    }
}
