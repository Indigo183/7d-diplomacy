//! The views module contains the components for all Layouts and Routes for our app. Each layout and route in our [`Route`]
//! enum will render one of these components.
//!
//!
//! The [`Home`] and [`Blog`] components will be rendered when the current route is [`Route::Home`] or [`Route::Blog`] respectively.
//!
//!
//! The [`Navbar`] component will be rendered on all pages of our app since every page is under the layout. The layout defines
//! a common wrapper around all child routes.

mod home;
pub use home::Home;

mod resume_game;
pub use resume_game::ResumeGame;

mod join_new_game;
pub use join_new_game::JoinNewGame;

mod host_new_game;
pub use host_new_game::HostNewGame;

mod blog;
pub use blog::Blog;

mod navbar;
pub use navbar::Navbar;
