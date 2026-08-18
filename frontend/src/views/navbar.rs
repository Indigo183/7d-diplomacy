use crate::Route;
use dioxus::prelude::*;

/// The Navbar component that will be rendered on all pages of our app since every page is under the layout.
///
/// This layout component wraps the UI of [Route::Home] and [Route::Blog] in a common navbar. The contents of the Home and Blog
/// routes will be rendered under the outlet inside this component
#[component]
pub fn Navbar() -> Element {
    let navigator = use_navigator();
    // so navbar is re-rendered each time a page is moved - necessary to grey out "back" button at correct times
    let _route = use_route::<Route>();

    rsx! {
        div { class: "flex relative justify-center",
             // Back button
            if navigator.can_go_back() {
                button { class: "absolute left-0 hover:cursor-pointer",
                    onclick: move |_event| navigator.go_back(),
                    aria_label: "Go back",
                    title: "Go back",
                    "←"
                }
            }

            // Wrapped so the pair centres as a single grid item
            div { class: "space-x-5",
                Link { to: Route::Home {}, "Home" }
                Link { to: Route::Blog { id: 1 }, "Blog" }
            }
        }

        // The `Outlet` component is used to render the next component inside the layout. In this case, it will render either
        // the [`Home`] or [`Blog`] component depending on the current route.
        Outlet::<Route> {}
    }
}
