use dioxus::prelude::*;

use crate::props::MapDisplayMode;

#[component]
pub fn MapBuilder(display_mode: MapDisplayMode) -> Element {
    // temporary test SVG stuff

    let mut radius = use_signal(|| 50);

    rsx! {
        rect {
            x: "0",
            y: "0",
            width: "1000",
            height: "1000",
            fill: "#111827",
        }

        circle {
            cx: "500",
            cy: "500",
            r: "200",
            fill: "#3b82f6",
        }

        circle {
            cx: "250",
            cy: "250",
            r: "{radius}",
            fill: "#ef4444",
            onclick: move |_| {
                radius.set(radius() + 5)
            }
        }

        circle {
            cx: "750",
            cy: "750",
            r: "{radius * 2}",
            fill: "#22c55e",
            onclick: move |_| {
                radius.set(radius() - 5)
            }
        }
    }
}
