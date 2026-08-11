#[cfg(feature = "desktop")]
pub mod persistence_desktop;
#[cfg(feature = "desktop")]
pub use persistence_desktop::*;

pub mod examples;
