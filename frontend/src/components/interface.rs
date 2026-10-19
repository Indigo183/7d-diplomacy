use std::fmt::Display;

use dioxus::{html::geometry::PixelsRect, prelude::*};

use crate::{components::MapBuilder, props::MapDisplayMode};

#[derive(Debug, Clone, Copy)]
struct Transform {
    x: f64,
    y: f64,
    scale: f64,
}

impl Transform {
    fn new() -> Self {
        Self {
            x: 0.0,
            y: 0.0,
            scale: 3.0,
        }
    }
}

impl Display for Transform {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "translate({}px, {}px) scale({})",
            -self.x, -self.y, self.scale,
        )
    }
}

/// The main game interface, to be placed fullscreen in the background of the
/// game view.
#[component]
pub fn Interface() -> Element {
    // dimensions of viewport
    let mut viewport = use_signal(PixelsRect::zero);
    // cached mouse location
    let mut mouse_location = use_signal(|| (0.0_f64, 0.0_f64));
    let mut transform = use_signal(|| Transform::new());
    let mut is_dragging = use_signal(|| false);
    // map display mode
    #[allow(unused_mut)]
    let mut map_display_mode = use_signal(MapDisplayMode::default);

    rsx! {
        div {
            class: "absolute inset-0 z-0 h-full w-full
                    overflow-hidden select-none",

            onmounted: move |e| async move {
                viewport.set(e.get_client_rect().await.unwrap_or_default());
            },

            div {
                class: "transform-gpu",
                position: "absolute",
                top: 0,
                left: 0,
                transform: "{transform}",
                // transition: "scale 0.1s ease",
                transform_origin: "0 0",

                // zoom with mouse wheel
                onwheel: move |event| {
                    event.prevent_default();
                    let mouse = event.data().coordinates().client();
                    let delta = event.data().delta().strip_units();
                    let scroll = delta.y;

                    // hopefully shouldn't trigger, but no problem if it does?
                    if scroll == 0.0 {
                        return;
                    }

                    let old = transform();

                    // zooming out is positive
                    let scale_factor = (1.0 - scroll * 0.001).clamp(0.5, 2.0);

                    transform.set(Transform {
                        x: (old.x + mouse.x) * scale_factor - mouse.x,
                        y: (old.y + mouse.y) * scale_factor - mouse.y,
                        scale: scale_factor * old.scale,
                    })
                },

                // start panning
                onmousedown: move |event| {
                    let pos = event.data().coordinates().client();
                    mouse_location.set((pos.x, pos.y));
                    is_dragging.set(true);
                },

                // pan whilst dragging
                onmousemove: move |event| {
                    if is_dragging() {
                        let pos = event.data().coordinates().client();
                        let (last_x, last_y) = mouse_location();

                        let old = transform();

                        let dx = pos.x - last_x;
                        let dy = pos.y - last_y;

                        transform.set(Transform {
                            x: old.x - dx,
                            y: old.y - dy,
                            scale: old.scale,
                        });

                        mouse_location.set((pos.x, pos.y));
                    }
                },

                onmouseup: move |_| {
                    is_dragging.set(false);
                },

                onmouseleave: move |_| {
                    is_dragging.set(false);
                },

                MapBuilder { display_mode: map_display_mode() }
            }
        }
    }
}
