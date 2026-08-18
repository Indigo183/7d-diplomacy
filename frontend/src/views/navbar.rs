use crate::Route;
use dioxus::prelude::*;

const NAVBAR_CSS: Asset = asset!("/assets/styling/navbar.css");

/// The Navbar component that will be rendered on all pages of our app since every page is under the layout.
///
///
/// This layout component wraps the UI of [Route::Home] and [Route::Blog] in a common navbar. The contents of the Home and Blog
/// routes will be rendered under the outlet inside this component
#[component]
pub fn Navbar() -> Element {

    let navigator = use_navigator();

    // so navbar is re-rendered each time a page is moved - necessary to grey out "back" button at correct times
    let _route = use_route::<Route>();

    rsx! {
        document::Link { rel: "stylesheet", href: NAVBAR_CSS }

        div { id: "navbar",
             // back button
            if navigator.can_go_back() {
                button {
                    onclick: move |_event| {
                        navigator.go_back()
                    },
                    disabled: !navigator.can_go_back(),
                    aria_label: "Go back",
                    title: "Go back",
                    "←"
                }
            }


            // wrapped so the pair centres as a single grid item - see navbar.css
            div { id: "navbar-links",
                Link { to: Route::Home {}, "Home" }
                Link { to: Route::Blog { id: 1 }, "Blog" }
            }
        }

        // The `Outlet` component is used to render the next component inside the layout. In this case, it will render either
        // the [`Home`] or [`Blog`] component depending on the current route.
        Outlet::<Route> {}
    }
}
