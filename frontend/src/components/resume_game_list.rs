use crate::Route;
use crate::props::*;
use dioxus::prelude::*;

#[component]
pub fn ResumeGameList(items: Vec<GameHeaderProps>) -> Element {
    rsx! {
        div { class: "menu-options resume-list divide-y divide-gray-600",
            for item in items {
                Item { game: item }
            }
        }
    }
}

#[component]
fn Item(game: GameHeaderProps) -> Element {
    let name = game.game_name.clone();
    let (title_props, body_props) = game.split_title_and_body();

    rsx! {
        Link { to: Route::Game { id: name },
            div { class: "group m-5 py-3 px-10 \
                    hover:bg-gray-800 bg-clip-border \
                    rounded-md transition-colors duration-200",
                ItemTitle { title_props }

                div { class: "grid grid-rows-[0fr] opacity-0 \
                        transition-[grid-template-rows,opacity] \
                        duration-300 \
                        group-hover:grid-rows-[1fr] \
                        group-hover:opacity-100 \
                        group-hover:delay-1000",
                    div { class: "pt-3 space-y-3 overflow-hidden",
                        ItemBody { body_props }
                    }
                }
            }
        }
    }
}

#[component]
fn ItemTitle(title_props: ResumeGameListTitleProps) -> Element {
    rsx! {
        div { class: "flex justify-between",
            div { class: "flex gap-3",
                h1 { class: "text-nowrap", {title_props.name} }
                h1 { class: "text-nowrap text-sm/9.5 text-gray-400 bottom-0", "as" }
                h1 {
                    class: "text-nowrap",
                    style: "color:{title_props.player.colour}",
                    "{title_props.player.name}"
                }
            }

            div { class: "flex gap-3",
                h1 { class: "text-nowrap", "{title_props.turn}" }
                h1 { class: "text-nowrap", "-" }
                h1 { class: "text-nowrap text-{title_props.status.tailwind_colour()}",
                    "{title_props.status}"
                }
            }
        }
    }
}

#[component]
fn ItemBody(body_props: ResumeGameListBodyProps) -> Element {
    rsx! {
        div { class: "flex justify-between text-sm",
            p { {body_props.name} }
            div { class: "h-4 w-px bg-gray-600" }
            p { "{body_props.time_travel}" }
            div { class: "h-4 w-px bg-gray-600" }
            p {
                if body_props.auto_adjudicate {
                    "Adjudicates Automatically"
                } else {
                    "Manual Adjudication"
                }
            }
        }

        p { class: "text-sm text-gray-400 text-left", {body_props.server_address} }
    }
}
