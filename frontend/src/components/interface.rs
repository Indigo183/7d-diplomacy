use std::fmt::Display;

use dioxus::{html::geometry::PixelsRect, prelude::*};

const MAP_SVG: Asset = asset!("/assets/header.svg");

#[derive(Clone, Copy, Debug)]
struct ViewBox {
    x: f64,
    y: f64,
    width: f64,
    height: f64,
}

impl Display for ViewBox {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "{} {} {} {}",
            &self.x, &self.y, &self.width, &self.height
        )
    }
}

/// The main game interface, to be placed fullscreen in the background of the
/// game view.
#[component]
pub fn Interface() -> Element {
    // dimensions of viewport
    let mut viewport = use_signal(PixelsRect::zero);
    // starting view box of map SVG
    let mut view_box = use_signal(|| ViewBox {
        x: 0.0,
        y: 0.0,
        width: 1000.0,
        height: 1000.0,
    });
    // cached mouse location
    let mut mouse_location = use_signal(|| (0.0_f64, 0.0_f64));
    let mut is_dragging = use_signal(|| false);

    let mut radius = use_signal(|| 50);

    rsx! {
        div {
            class: "absolute inset-0 z-0 h-full w-full
                    overflow-hidden select-none",

            onmounted: move |e| async move {
                viewport.set(e.get_client_rect().await.unwrap_or_default());
            },

            svg {
                width: "100%",
                height: "100%",
                view_box: "{view_box()}",
                preserve_aspect_ratio: "xMidYMid slice",

                // zoom with mouse wheel
                onwheel: move |event| {
                    event.prevent_default();
                    let mouse = event.data().coordinates().element();
                    let delta = event.data().delta().strip_units();
                    let scroll = delta.y;

                    // hopefully shouldn't trigger, but no problem if it does?
                    if scroll == 0.0 {
                        return;
                    }

                    let old_view_box = view_box();

                    // zooming out is positive
                    let scale_factor = (1.0 + scroll * 0.001).clamp(0.5, 2.0);

                    let new_width = old_view_box.width * scale_factor;
                    let new_height = old_view_box.height * scale_factor;

                    // calculate normalised mouse position
                    let viewport_size = viewport().size;
                    let px = (mouse.x / viewport_size.width).clamp(0.0, 1.0);
                    let py = (mouse.y / viewport_size.height).clamp(0.0, 1.0);

                    let mouse_svg_x = old_view_box.x + px * old_view_box.width;
                    let mouse_svg_y = old_view_box.y + py * old_view_box.height;

                    let new_x = mouse_svg_x - px * new_width;
                    let new_y = mouse_svg_y - py * new_height;

                    view_box.set(ViewBox {
                        x: new_x,
                        y: new_y,
                        width: new_width,
                        height: new_height,
                    });
                },

                // start panning
                onmousedown: move |event| {
                    let pos = event.data().coordinates().element();
                    mouse_location.set((pos.x, pos.y));
                    is_dragging.set(true);
                },

                // pan whilst dragging
                onmousemove: move |event| {
                    if !is_dragging() {
                        return;
                    }

                    let pos = event.data().coordinates().element();
                    let (last_x, last_y) = mouse_location();

                    let dx = pos.x - last_x;
                    let dy = pos.y - last_y;

                    let old = view_box();

                    let viewport_size = viewport().size;
                    let svg_dx = dx * old.width / viewport_size.width;
                    let svg_dy = dy * old.height / viewport_size.height;

                    view_box.set(ViewBox {
                        x: old.x - svg_dx,
                        y: old.y - svg_dy,
                        width: old.width,
                        height: old.height,
                    });

                    mouse_location.set((pos.x, pos.y));
                },

                onmouseup: move |_| {
                    is_dragging.set(false);
                },

                onmouseleave: move |_| {
                    is_dragging.set(false);
                },

                // temporary test SVG stuff
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
    }
}
