#[derive(PartialEq, Clone, Copy)]
pub enum Adjacencies {
    Strict,
    Loose,
}
impl std::fmt::Display for Adjacencies {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::Strict => write!(f, "Strict"),
            Self::Loose => write!(f, "Loose"),
        }
    }
}
