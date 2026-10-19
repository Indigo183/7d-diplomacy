use anyhow::Result;
use dioxus::{html::g::dangerous_inner_html, prelude::*};
use roxmltree::Document;

fn provinces<'a>(svg: &'a roxmltree::Document) -> Result<roxmltree::Children<'a, 'a>> {
    Ok(svg
        .root_element()
        .children()
        .find(|node| node.attribute("label") == Some("Region Colors"))
        .expect("help")
        .children())
}

fn serialise_node_internal(node: roxmltree::Node) -> String {
    let mut output = String::new();

    match node.node_type() {
        roxmltree::NodeType::Element => {
            output.push('<');
            output.push_str(node.tag_name().name());

            for attr in node.attributes() {
                output.push(' ');
                output.push_str(attr.name());
                output.push_str("=\"");
                output.push_str(attr.value());
                output.push('"');
            }

            let mut children = node.children().peekable();

            if children.peek().is_none() {
                output.push_str("/>");
                return output;
            }

            output.push('>');

            for child in children {
                output.push_str(&serialise_node_internal(child));
            }

            output.push_str("</");
            output.push_str(node.tag_name().name());
            output.push('>');
        }

        roxmltree::NodeType::Text => {
            output.push_str(node.text().unwrap_or(""));
        }

        roxmltree::NodeType::Comment => {
            output.push_str("<!--");
            output.push_str(node.text().unwrap_or(""));
            output.push_str("-->");
        }

        _ => {}
    }

    output
}

fn serialise_node(node: roxmltree::Node) -> String {
    String::from("<svg>") + &serialise_node_internal(node) + "</svg>"
}

#[component]
pub fn Board() -> Element {
    let svg_raw = std::fs::read_to_string("assets/classic.svg")?;
    // let svg_raw = std::fs::read_to_string("assets/imperial-diplomacy-2-5.svg")?;
    let svg = Document::parse(&svg_raw)?;
    let root = svg.root_element();
    let view_box = root
        .attribute("viewBox")
        .map(String::from)
        .unwrap_or(format!(
            "0 0 {} {}",
            root.attribute("width").unwrap(),
            root.attribute("height").unwrap(),
        ));
    let provinces = provinces(&svg)?;

    rsx! {
        svg {
            width: "100%",
            height: "100%",
            view_box: view_box,
            preserve_aspect_ratio: "xMidYMid slice",
            // dangerous_inner_html: svg_raw,

            for child in svg.root_element().children().filter(|n|
                    n.attribute("label") != Some("Region Colors") && n.attribute("label") != Some("Sea Adjacencies") && n.attribute("label") != Some("Island Adjacencies")
                ) {
                g {
                    dangerous_inner_html: serialise_node(child)
                }
            }

            for (i, province) in provinces.into_iter().enumerate() {
                path {
                    style: &province.attribute("style"),
                    class: "hover:!fill-green-300",
                    onclick: move |_event| println!("{i}"),
                    d: &province.attribute("d"),
                }
            }
        }
    }
}
