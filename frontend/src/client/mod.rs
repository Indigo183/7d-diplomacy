#[cfg(test)]
mod test;

mod models;

use crate::client::models::*;
use reqwest;
use reqwest::{Client, Url};
use serde::ser::SerializeStruct;
use serde::{Serialize, Serializer};
use std::fmt::{Display, Formatter};
use std::sync::LazyLock;
use std::time::Duration;

pub const DEFAULT_URL_AS_STR: &'static str = "http://localhost:9090/";
pub const DEFAULT_URL: LazyLock<Url> =
    LazyLock::new(|| Url::parse("http://localhost:9090/").unwrap());

const TIMEOUT: Duration = Duration::from_secs(10);
const CLIENT: LazyLock<Client> =
    LazyLock::new(|| Client::builder().timeout(TIMEOUT).build().unwrap());

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize)]
pub enum GameProperty {
    Started,
    Ended,
}

impl Display for GameProperty {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "{}",
            match self {
                Self::Started => "started",
                Self::Ended => "ended",
            }
        )
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum GMAction {
    Adjudicate,
    SetProperty(GameProperty),
}

impl Serialize for GMAction {
    fn serialize<S: Serializer>(&self, serializer: S) -> Result<S::Ok, S::Error> {
        let mut serialiser = serializer.serialize_struct("GMAction", 2)?;

        match self {
            Self::Adjudicate => serialiser.serialize_field("action", "adjudicate")?,
            Self::SetProperty(property) => {
                serialiser.serialize_field("action", "set-property")?;
                serialiser.serialize_field("property", &property.to_string())?;
            }
        };

        serialiser.end()
    }
}

// GENERAL FORMAT
//
// Each request must first be sent and awaited, and any errors bubbled up. The response must then be
// checked with `error_for_status()`, which will convert the response into an error if it receives
// an HTTP error, and any errors are then bubbled up. Finally, the response can be deserialised (and
// awaited?), and any deserialisation errors are to be bubbled up (in order to cast them to the
// correct error type).

//-- /api/game --//

// GET
pub async fn get_game_names(url: Url) -> anyhow::Result<Vec<String>> {
    let request_url = url.join("api/game")?;

    let game_names = CLIENT
        .get(request_url)
        .send()
        .await?
        .error_for_status()?
        .json()
        .await?;

    Ok(game_names)
}

// POST
pub async fn create_game(url: Url, id: &str) -> anyhow::Result<String> {
    let request_url = url.join("api/game/")?;

    let response = CLIENT
        .post(request_url)
        .query(&[("id", id)])
        .send()
        .await?
        .error_for_status()?
        .text()
        .await?;

    Ok(response)
}

//-- /api/game/{id} --//

// PATCH
pub async fn gm_action(
    url: Url,
    token: &str,
    id: &str,
    action: GMAction,
) -> anyhow::Result<String> {
    let request_url = url.join("api/game/")?.join(id)?;

    let response = CLIENT
        .patch(request_url)
        .bearer_auth(token)
        .query(&action)
        .send()
        .await?
        .error_for_status()?
        .text()
        .await?;

    Ok(response)
}

// POST
pub async fn get_player_token(
    url: Url,
    id: &str,
    country: &str,
    recovery_key: Option<&str>,
) -> anyhow::Result<String> {
    let request_url = url.join("api/game/")?.join(id)?;

    let mut query = Vec::with_capacity(2);
    query.push(("country", country));
    if let Some(key) = recovery_key {
        query.push(("recovery-key", key))
    }

    let token = CLIENT
        .post(request_url)
        .query(&query)
        .send()
        .await?
        .error_for_status()?
        .text()
        .await?;

    Ok(token)
}

// GET
pub async fn get_game(url: Url, id: &str) -> anyhow::Result<Game> {
    let request_url = url.join("api/game/")?.join(id)?;

    let game = CLIENT
        .get(request_url)
        .send()
        .await?
        .error_for_status()?
        .json()
        .await?;

    Ok(game)
}

//-- /api/game/{id}/{country} --//

// POST
pub async fn post_text_orders(
    url: Url,
    token: &str,
    id: &str,
    country: &str,
    orders: String,
) -> anyhow::Result<Vec<Inputtable>> {
    let request_url = url.join(&format!("api/game/{id}/{country}"))?;

    let orders = CLIENT
        .post(request_url)
        .bearer_auth(token)
        .body(orders)
        .send()
        .await?
        .error_for_status()?
        .json()
        .await?;

    Ok(orders)
}

// POST
pub async fn post_json_orders(
    url: Url,
    token: &str,
    id: &str,
    country: &str,
    orders: Vec<Inputtable>,
) -> anyhow::Result<Vec<Inputtable>> {
    let request_url = url.join(&format!("api/game/{id}/{country}"))?;

    let orders = CLIENT
        .post(request_url)
        .bearer_auth(token)
        .body(serde_json::to_string(&orders)?)
        .send()
        .await?
        .error_for_status()?
        .json()
        .await?;

    Ok(orders)
}

// GET
pub async fn get_orders(
    url: Url,
    token: &str,
    id: &str,
    country: &str,
) -> anyhow::Result<Vec<Inputtable>> {
    let request_url = url.join(&format!("api/game/{id}/{country}"))?;

    let orders = CLIENT
        .get(request_url)
        .bearer_auth(token)
        .send()
        .await?
        .error_for_status()?
        .json()
        .await?;

    Ok(orders)
}

//-- /api/game/{id}/{country}/ready --//

// POST
pub async fn set_ready(
    url: Url,
    token: &str,
    id: &str,
    country: &str,
    ready: bool,
) -> anyhow::Result<()> {
    let request_url = url.join(&format!("api/game/{id}/{country}/ready"))?;

    CLIENT
        .post(request_url)
        .bearer_auth(token)
        .query(&[("ready", ready)])
        .send()
        .await?
        .error_for_status()?
        .text()
        .await?;

    Ok(())
}
// GET
pub async fn get_ready(url: Url, token: &str, id: &str, country: &str) -> anyhow::Result<bool> {
    let request_url = url.join(&format!("api/game/{id}/{country}/ready"))?;

    let ready = CLIENT
        .get(request_url)
        .bearer_auth(token)
        .send()
        .await?
        .error_for_status()?
        .json()
        .await?;

    Ok(ready)
}

//-- /api/game/{id}/{country}/token-log --//

// GET
pub async fn get_token_access_log(
    url: Url,
    token: &str,
    id: &str,
    country: &str,
) -> anyhow::Result<TokenAccess> {
    let request_url = url.join(&format!("api/game/{id}/{country}/token-log"))?;

    let token_access = CLIENT
        .post(request_url)
        .bearer_auth(token)
        .send()
        .await?
        .error_for_status()?
        .json()
        .await?;

    Ok(token_access)
}
