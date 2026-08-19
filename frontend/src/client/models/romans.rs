use crate::client::models::*;
use std::string::ToString;
use std::sync::LazyLock;

pub const CATO: &str = "Cato";
pub const POMPEY: &str = "Pompey";

pub const CAT: LazyLock<Province> = LazyLock::new(|| Province("CAT".to_string()));
pub const CAE: LazyLock<Province> = LazyLock::new(|| Province("CAE".to_string()));
pub const BRU: LazyLock<Province> = LazyLock::new(|| Province("BRU".to_string()));
pub const POM: LazyLock<Province> = LazyLock::new(|| Province("POM".to_string()));
