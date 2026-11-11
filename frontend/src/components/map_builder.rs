use dioxus::prelude::*;
use xmltree::Element as XMLElement;

use crate::{components::Board, models::MapConfig, props::MapDisplayMode};

const PADDING_FACTOR: f64 = 0.5;

fn translation(width: f64, height: f64, padding: f64, x: u8, y: u8) -> String {
    format!(
        "transform: translate({}px, {}px);",
        (width + padding) * x as f64,
        (height + padding) * y as f64,
    )
}

#[component]
pub fn MapBuilder(display_mode: MapDisplayMode, variant: String) -> Element {
    let svg_raw = std::fs::read_to_string(format!("assets/{variant}/map.svg"))?;
    let xml = XMLElement::parse(svg_raw.as_bytes())?;

    let config: MapConfig = toml::from_str(&std::fs::read_to_string(format!(
        "assets/{variant}/config.toml"
    ))?)?;

    let width = xml
        .attributes
        .get("width")
        .expect("MUST HAVE WIDTH")
        .parse::<f64>()?;
    let height = xml
        .attributes
        .get("height")
        .expect("MUST HAVE HEIGHT")
        .parse::<f64>()?;
    let padding = width.min(height) * PADDING_FACTOR;

    let positions = [(0, 0), (0, 1), (1, 0)];

    rsx! {
        div {
            class: "relative",

            for (x, y) in positions {
                div {
                    class: "absolute",
                    style: translation(width, height, padding, x, y),
                    Board { xml: xml.clone(), config: config.clone() }
                }
            }
        }
    }
}
