use dioxus::prelude::*;
use xmltree::Element as XMLElement;

use crate::{components::Board, props::MapDisplayMode};

const PADDING_FACTOR: f64 = 0.5;

fn translation(width: f64, height: f64, padding: f64, x: u8, y: u8) -> String {
    format!(
        "transform: translate({}px, {}px);",
        (width + padding) * x as f64,
        (height + padding) * y as f64,
    )
}

#[component]
pub fn MapBuilder(display_mode: MapDisplayMode) -> Element {
    let svg_raw = std::fs::read_to_string("assets/imperial-diplomacy-2-5/map.svg")?;
    let attributes = XMLElement::parse(svg_raw.as_bytes())?.attributes;
    let width = attributes
        .get("width")
        .expect("MUST HAVE WIDTH")
        .parse::<f64>()?;
    let height = attributes
        .get("height")
        .expect("MUST HAVE HEIGHT")
        .parse::<f64>()?;
    let padding = width.min(height) * PADDING_FACTOR;

    rsx! {
        div {
            class: "relative",
            div {
                class: "absolute",
                style: translation(width, height, padding, 0, 0),
                Board { }
            }
            div {
                class: "absolute",
                style: translation(width, height, padding, 1, 0),
                Board { }
            }
            div {
                class: "absolute",
                style: translation(width, height, padding, 0, 1),
                Board { }
            }
        }
    }
}
