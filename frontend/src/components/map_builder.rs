use dioxus::prelude::*;

use crate::{components::Board, props::MapDisplayMode};

#[component]
pub fn MapBuilder(display_mode: MapDisplayMode) -> Element {
    rsx! {
        Board { }
    }
}
