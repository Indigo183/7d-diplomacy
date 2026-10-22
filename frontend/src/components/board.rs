use dioxus::prelude::*;
use xmltree::{Element as XMLElement, XMLNode};

#[component]
fn Layer(layer: XMLElement) -> Element {
    match layer.attributes.get("label").map(AsRef::as_ref) {
        Some("Region Colors") => {
            rsx! {
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
                        class: match province.attributes.get("transform") { // TODO: document properly and clean up
                            None => Some("hover:!fill-green-300"),
                            _ => None,
                        },
                        onclick: {
                            let province = province.clone();
                            move |_event| {
                                let mut buf = Vec::new();
                                province.write(&mut buf).unwrap();
                                println!("{}", String::from_utf8(buf).unwrap())
                            }
                        },
                        d: province.attributes.get("d"),
                    }
                }
            }
        }

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
pub fn Board() -> Element {
    // let svg_raw = std::fs::read_to_string("assets/classic.svg")?;
    let svg_raw = std::fs::read_to_string("assets/imperial-diplomacy-2-5.svg")?;

    // TODO: edit svg before rendering
    let mut xml = XMLElement::parse(svg_raw.as_bytes())?;
    let attributes = &xml.attributes;

    rsx! {
        svg {
            width: attributes.get("width"),
            height: attributes.get("height"),
            spacing: attributes.get("space"),

            for layer in xml.children.iter().filter_map(XMLNode::as_element) {
                Layer { layer: layer.clone() }
            }
        }
    }
}
