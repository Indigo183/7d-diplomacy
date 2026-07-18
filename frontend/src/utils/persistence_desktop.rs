use std::{
    fs::{self, File},
    path::PathBuf,
};

use anyhow::{Ok, Result, anyhow};

use crate::settings::{Game, GameCache};

/// The path to the directory to search for caches.
pub const JOINED_GAMES_PATH: &str = "~/.7dip/joined-games/";

/// Generates the expected path of the cache for a given game, as specified by id and string
pub fn get_cache_path(id: &str) -> PathBuf {
    PathBuf::from(JOINED_GAMES_PATH.to_owned() + id + ".json")
}

/// Updates the cache for a game
pub fn cache_game(game_cache: &GameCache) -> Result<()> {
    let f = File::open(get_cache_path(&game_cache.game.config.id))?;
    serde_json::to_writer(f, game_cache)?;

    Ok(())
}

/// Attempts to load a game from the cache.
pub fn load_cached_game(id: &str) -> Result<GameCache> {
    let f = File::open(PathBuf::try_from(get_cache_path(id))?)?;
    Ok(serde_json::from_reader(f)?)
}

/// Returns a list of game ids and players currently cached.
pub fn get_cached_games() -> Result<Vec<(String, String)>> {
    let mut cached_games = vec![];
    for entry in fs::read_dir(JOINED_GAMES_PATH)? {
        let path = entry?.path();
        if path.extension().ok_or(anyhow!(
            "File in cached games directory had no extension ({}).",
            path.display()
        ))? == "json"
        {
            let (id, name) = path
                .file_prefix()
                .ok_or(anyhow!(
                    "File in cached games directory had no file name ({}).",
                    path.display()
                ))?
                .to_str()
                .ok_or(anyhow!(
                    "File name in cached games directory was not valid unicode ({})",
                    path.display()
                ))?
                .split_once('_')
                .ok_or(anyhow!(
                    "File name in cached gamed directory was not formatted correctly ({})",
                    path.display()
                ))?;
            cached_games.push((id.to_owned(), name.to_owned()));
        }
    }

    Ok(cached_games)
}

/// Returns whether or not a game is cached
pub fn is_game_cached(id: &str) -> bool {
    get_cache_path(id).exists()
}
