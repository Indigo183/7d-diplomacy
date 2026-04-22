use crate::components::MainMenu;
use dioxus::prelude::*;

/// The Home page component that will be rendered when the current route is `[Route::Home]`
#[component]
pub fn Home() -> Element {
    rsx! {
        div {
        style: "animation: spin 2s linear infinite;",
        "Hello"
    }
        MainMenu {}
    }
}
