use super::*;
use crate::client::GMAction::{Adjudicate, SetProperty};
use crate::client::GameProperty::Started;

#[tokio::test]
pub async fn test_get_game_names() {
    let result = get_game_names(DEFAULT_URL.clone()).await;
    let _ = dbg!(result);
}

#[tokio::test]
pub async fn test_create_game() {
    let result = create_game(DEFAULT_URL.clone(), "test-game").await;
    let _ = dbg!(result);
}

#[tokio::test]
pub async fn test_gm_action() {
    let result = gm_action(DEFAULT_URL.clone(), "test-game", "eyJhbGciOiJIUzI1NiJ9.eyJnYW1lSWQiOiJ0ZXN0LWdhbWUiLCJpc0dNIjp0cnVlfQ.ZZUGfKi3DPcpyYjlLNW1fFmJvSQlwwdeYut8rwOS9fk", SetProperty(Started)).await;
    let _ = dbg!(result);
}

#[tokio::test]
pub async fn test_get_player_token() {
    let result = get_player_token(DEFAULT_URL.clone(), "test-game", "Cato", None).await;
    let _ = dbg!(result);
}

#[tokio::test]
pub async fn test_get_game() {
    let result = get_game(DEFAULT_URL.clone(), "test-game").await;
    let _ = dbg!(result);
}

#[tokio::test]
pub async fn test_post_json_orders() {
    let result = post_json_orders(DEFAULT_URL.clone(), "eyJhbGciOiJIUzI1NiJ9.eyJnYW1lSWQiOiJ0ZXN0LWdhbWUiLCJjb3VudHJ5IjoiQ2F0byJ9.6KNmg6TetboEy0zwpRWOsgX4VUIaVqlGWLnnaGsnLHk", "test-game", "Cato", vec![]).await;
    let _ = dbg!(result);
}

#[tokio::test]
pub async fn test_get_orders() {
    let result = get_orders(DEFAULT_URL.clone(), "eyJhbGciOiJIUzI1NiJ9.eyJnYW1lSWQiOiJ0ZXN0LWdhbWUiLCJjb3VudHJ5IjoiQ2F0byJ9.6KNmg6TetboEy0zwpRWOsgX4VUIaVqlGWLnnaGsnLHk", "test-game", "Cato").await;
    let _ = dbg!(result);
}

#[tokio::test]
pub async fn test_set_ready() {
    let result = set_ready(DEFAULT_URL.clone(), "eyJhbGciOiJIUzI1NiJ9.eyJnYW1lSWQiOiJ0ZXN0LWdhbWUiLCJjb3VudHJ5IjoiQ2F0byJ9.6KNmg6TetboEy0zwpRWOsgX4VUIaVqlGWLnnaGsnLHk", "test-game", "Cato", true).await;
    let _ = dbg!(result);
}

#[tokio::test]
pub async fn test_get_ready() {
    let result = get_ready(DEFAULT_URL.clone(), "toeyJhbGciOiJIUzI1NiJ9.eyJnYW1lSWQiOiJ0ZXN0LWdhbWUiLCJjb3VudHJ5IjoiQ2F0byJ9.6KNmg6TetboEy0zwpRWOsgX4VUIaVqlGWLnnaGsnLHksken", "test-game", "Cato").await;
    let _ = dbg!(result);
}

#[tokio::test]
pub async fn test_get_token_access_log() {
    let result = get_token_access_log(DEFAULT_URL.clone(), "toeyJhbGciOiJIUzI1NiJ9.eyJnYW1lSWQiOiJ0ZXN0LWdhbWUiLCJjb3VudHJ5IjoiQ2F0byJ9.6KNmg6TetboEy0zwpRWOsgX4VUIaVqlGWLnnaGsnLHksken", "test-game", "Cato").await;
    let _ = dbg!(result);
}
