// TODO: maybe have some kind of pre-optimisation that is cached instead of / alongside the svg?
// TODO: OPTIMISE, please no more .clone()
// TODO: fix NPO5 "hitbox" covering entirety of NPO

use dioxus::prelude::*;
use xmltree::{Element as XMLElement, XMLNode};

use crate::models::MapConfig;

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
pub fn Board(mut xml: XMLElement, config: MapConfig) -> Element {
    let attributes = &xml.attributes;

    rsx! {
        svg {
            width: attributes.get("width"),
            height: attributes.get("height"),
            view_box: attributes.get("viewBox"),
            spacing: attributes.get("space"),

            for layer in xml.children.iter().filter_map(XMLNode::as_element) {
                Layer { layer: layer.clone(), config: config.clone() } // TODO: AAAAAAAAAAAAAAAAAAAAAAAA
            }
        }
    }
}
