#[cfg(test)]
mod test;

use reqwest;
use reqwest::Url;
use std::sync::LazyLock;

pub const DEFAULT_URL: LazyLock<Url> =
    LazyLock::new(|| Url::parse("http://localhost:9090/api/game/").unwrap());

//-- /api/game --//

// GET
pub async fn get_game_names(url: Url) -> anyhow::Result<Vec<String>> {
    todo!()
}
// POST
pub async fn create_game(url: Url, id: &str) -> anyhow::Result<()> {
    todo!()
}

//-- /api/game/{id} --//

// PATCH
pub async fn gm_action(url: Url) -> anyhow::Result<()> {
    todo!()
}
// POST
pub async fn get_player_token(url: Url) -> anyhow::Result<()> {
    todo!()
}
// GET
pub async fn get_game(url: Url) -> anyhow::Result<()> {
    todo!()
}

//-- /api/game/{id}/{country} --//

// POST
pub async fn post_json_orders(url: Url) -> anyhow::Result<()> {
    todo!()
}
// GET
pub async fn get_orders(url: Url) -> anyhow::Result<()> {
    todo!()
}

//-- /api/game/{id}/{country}/ready --//

// POST
pub async fn set_ready(url: Url) -> anyhow::Result<()> {
    todo!()
}
// GET
pub async fn get_ready(url: Url) -> anyhow::Result<()> {
    todo!()
}

//-- /api/game/{id}/{country}/token-log --//

// GET
pub async fn get_token_access_log(url: Url) -> anyhow::Result<()> {
    todo!()
}
