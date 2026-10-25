// TODO: maybe have some kind of pre-optimisation that is cached instead of / alongside the svg?
// TODO: OPTIMISE, please no more .clone()
// TODO: fix NPO5 "hitbox" covering entirety of NPO

use dioxus::prelude::*;
use serde::Deserialize;
use xmltree::{Element as XMLElement, XMLNode};

#[derive(Debug, Clone, PartialEq, Deserialize)]
struct MapConfig {
    pub impassibles: Option<Vec<String>>,
    pub coasts: Option<toml::Table>,
}

impl MapConfig {
    fn is_region(&self, name: Option<&String>) -> bool {
        self.impassibles
            .as_ref() // ownership, am I right?
            .is_none_or(|impassibles| name.is_some_and(|name| !impassibles.contains(name)))
    }
}

#[component]
fn Layer(layer: XMLElement, config: MapConfig) -> Element {
    // TODO: &MapConfig PLEASE

    match layer.attributes.get("label").map(AsRef::as_ref) {
        Some("Region Colors") => rsx! {
            for province in layer.children.iter().filter_map(XMLNode::as_element) {
                path {
                    transform: [
                        layer.attributes.get("transform"),
                        province.attributes.get("transform"),
                    ]
                    .into_iter()
                    .flatten()
                    .cloned()
                    .collect::<Vec<_>>()
                    .join(" "),
                    style: province.attributes.get("style"),
                    class: if config.is_region(province.attributes.get("label")) {
                        "hover:!fill-green-300"
                    },
                    onclick: {
                        let province = province.clone();
                        let config = config.clone();

                        move |_event| {
                            if config.is_region(province.attributes.get("label")) {
                                // let mut buf = Vec::new();
                                // province.write(&mut buf).unwrap();
                                // println!("{}", String::from_utf8(buf).unwrap())
                            }
                        }
                    },
                    d: province.attributes.get("d"),
                }
            }
        },

        Some("Island Adjacencies") | Some("Sea Adjacencies") => rsx! {
            for province in layer.children.iter().filter_map(XMLNode::as_element) {
                path {
                    transform: [
                        layer.attributes.get("transform"),
                        province.attributes.get("transform"),
                    ]
                    .into_iter()
                    .flatten()
                    .cloned()
                    .collect::<Vec<_>>()
                    .join(" "),
                    class: if config.is_region(province.attributes.get("label")) {
                        "fill-transparent hover:!fill-green-300"
                    } else { "fill-none" },
                    onclick: {
                        let province = province.clone();
                        let config = config.clone();

                        move |_event| {
                            if config.is_region(province.attributes.get("label")) {
                                // let mut buf = Vec::new();
                                // province.write(&mut buf).unwrap();
                                // println!("{}", String::from_utf8(buf).unwrap())
                            }
                        }
                    },
                    d: province.attributes.get("d"),
                }
            }
        },

        Some("Titles") => rsx!(),

        Some(_) => {
            let mut buf = Vec::new();
            layer.write(&mut buf)?;
            if layer.attributes.get("label").map(AsRef::as_ref) == Some("Background") {
                println!("hi");
                println!("{:?}", String::from_utf8(buf.clone()));
            }

            rsx! {
                g {
                    pointer_events: "none",
                    dangerous_inner_html: Some(String::from_utf8(buf)?),
                }
            }
        }

        _ => rsx!(),
    }
}

#[component]
pub fn Board() -> Element {
    // let svg_raw = std::fs::read_to_string("assets/classic.svg")?;
    let svg_raw = std::fs::read_to_string("assets/imperial-diplomacy-2-5/map.svg")?;
    let config: MapConfig = toml::from_str(&std::fs::read_to_string(
        "assets/imperial-diplomacy-2-5/config.toml",
    )?)?;
    dbg!(&config);

    // TODO: edit svg before rendering
    let mut xml = XMLElement::parse(svg_raw.as_bytes())?;
    let attributes = &xml.attributes;

    rsx! {
        svg {
            width: attributes.get("width"),
            height: attributes.get("height"),
            spacing: attributes.get("space"),

            for layer in xml.children.iter().filter_map(XMLNode::as_element) {
                Layer { layer: layer.clone(), config: config.clone() } // TODO: AAAAAAAAAAAAAAAAAAAAAAAA
            }
        }
    }
}
