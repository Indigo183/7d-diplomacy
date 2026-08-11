use reqwest;

//-- /api/game --//

// GET
pub fn get_game_names() {}
// POST
pub fn create_game() {}

//-- /api/game/{id} --//

// PATCH
pub fn gm_action() {}
// POST
pub fn get_player_token() {}
// GET
pub fn get_game() {}

//-- /api/game/{id}/{country} --//

// POST
pub fn post_json_orders() {}
// GET
pub fn get_orders() {}

//-- /api/game/{id}/{country}/ready --//

// POST
pub fn set_ready() {}
// GET
pub fn get_ready() {}

//-- /api/game/{id}/{country}/token-log --//

// GET
pub fn get_token_access_log() {}
