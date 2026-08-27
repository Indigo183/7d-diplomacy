use super::*;
use crate::client::models::romans::*;
use crate::utils::persistence;

const GAME_ID: &str = "test-game";

async fn test_cleanup() {
    let _ = std::fs::remove_dir_all(persistence::STORAGE_PATH.join("hosted-games").join(GAME_ID));
}

#[tokio::test]
pub async fn full_game_test() {
    // Clear old test game
    test_cleanup().await;
    assert!(
        !get_game_names(DEFAULT_URL.clone())
            .await
            .unwrap()
            .contains(&GAME_ID.to_string())
    );

    // Create new test game
    let gm_token = create_game(DEFAULT_URL.clone(), GAME_ID).await.unwrap();
    assert!(
        get_game_names(DEFAULT_URL.clone())
            .await
            .unwrap()
            .contains(&GAME_ID.to_string())
    );

    // Assert that created game is empty
    assert_eq!(
        get_game(DEFAULT_URL.clone(), GAME_ID).await.unwrap(),
        BASE_MAP.clone(),
    );

    // Register players
    let cato_token = get_player_token(DEFAULT_URL.clone(), GAME_ID, CATO, None)
        .await
        .unwrap();
    let pompey_token = get_player_token(DEFAULT_URL.clone(), GAME_ID, POMPEY, None)
        .await
        .unwrap();

    // Start the game
    let started_response = gm_action(
        DEFAULT_URL.clone(),
        &gm_token,
        GAME_ID,
        GMAction::SetProperty(GameProperty::Started),
    )
    .await
    .unwrap();
    assert_eq!(started_response.string().unwrap(), "[started]");

    // Assert that players can't sign up once game has started
    get_player_token(DEFAULT_URL.clone(), GAME_ID, CATO, None)
        .await
        .expect_err("game has already started");
    get_player_token(DEFAULT_URL.clone(), GAME_ID, POMPEY, None)
        .await
        .expect_err("game has already started");

    // Make sure that players can recover their token from the last ten characters
    let new_cato_token = get_player_token(
        DEFAULT_URL.clone(),
        GAME_ID,
        CATO,
        Some(&cato_token[cato_token.len() - 10..]),
    )
    .await
    .unwrap();
    assert_eq!(new_cato_token, cato_token);
    let new_pompey_token = get_player_token(
        DEFAULT_URL.clone(),
        GAME_ID,
        POMPEY,
        Some(&pompey_token[pompey_token.len() - 10..]),
    )
    .await
    .unwrap();
    assert_eq!(new_pompey_token, pompey_token);

    // Assert that no orders have been submitted
    assert!(
        get_orders(DEFAULT_URL.clone(), &cato_token, GAME_ID, CATO)
            .await
            .unwrap()
            .is_empty()
    );
    assert!(
        get_orders(DEFAULT_URL.clone(), &pompey_token, GAME_ID, POMPEY)
            .await
            .unwrap()
            .is_empty()
    );

    // Assert that no orders have been marked as ready
    assert!(
        !get_ready(DEFAULT_URL.clone(), &cato_token, GAME_ID, CATO)
            .await
            .unwrap()
    );
    assert!(
        !get_ready(DEFAULT_URL.clone(), &pompey_token, GAME_ID, POMPEY)
            .await
            .unwrap()
    );

    // Make sure that text order input works
    let cato_order_input = "Cato:\n0+0iT0 A CAT - 0+0iT0 BRU 2";
    let cato_order = post_text_orders(
        DEFAULT_URL.clone(),
        &cato_token,
        GAME_ID,
        CATO,
        cato_order_input.to_string(),
    )
    .await
    .unwrap()
    .into_iter() // for ownership
    .next()
    .expect("returned orders should be non-empty");
    assert_eq!(
        cato_order,
        Inputtable::Order(Order::Move(MoveOrder {
            piece: Piece {
                location: Location {
                    province: CAT.clone(),
                    board_index: ORIGIN_BOARD_INDEX,
                },
                unit_type: UnitType::Army,
            },
            moves: Moves {
                to: Location {
                    province: BRU.clone(),
                    board_index: ORIGIN_BOARD_INDEX,
                }
            },
            flare: Some(TemporalFlare::Left),
        })),
    );

    // Make sure that direct order input works
    let pompey_order_input = Inputtable::Order(Order::Move(MoveOrder {
        piece: Piece {
            location: Location {
                province: POM.clone(),
                board_index: ORIGIN_BOARD_INDEX,
            },
            unit_type: UnitType::Army,
        },
        moves: Moves {
            to: Location {
                province: BRU.clone(),
                board_index: ORIGIN_BOARD_INDEX,
            },
        },
        flare: Some(TemporalFlare::Up),
    }));
    let pompey_order = post_json_orders(
        DEFAULT_URL.clone(),
        &pompey_token,
        GAME_ID,
        POMPEY,
        vec![pompey_order_input.clone()],
    )
    .await
    .unwrap()
    .into_iter() // for ownership
    .next()
    .expect("returned orders should be non-empty");
    assert_eq!(pompey_order_input, pompey_order);

    // Assert that outputted orders contain nothing but the inputted orders
    assert_eq!(
        get_orders(DEFAULT_URL.clone(), &cato_token, GAME_ID, CATO)
            .await
            .unwrap(),
        vec![cato_order],
    );
    assert_eq!(
        get_orders(DEFAULT_URL.clone(), &pompey_token, GAME_ID, POMPEY)
            .await
            .unwrap(),
        vec![pompey_order],
    );

    // Assert that the game can't be adjudicated before all players have submitted orders
    gm_action(
        DEFAULT_URL.clone(),
        &gm_token,
        GAME_ID,
        GMAction::Adjudicate,
    )
    .await
    .expect_err("shouldn't allow adjudication before all players are ready");

    // Assert that `set_ready(...)` actually sets the ready status
    set_ready(DEFAULT_URL.clone(), &cato_token, GAME_ID, CATO, true)
        .await
        .unwrap();
    set_ready(DEFAULT_URL.clone(), &pompey_token, GAME_ID, POMPEY, true)
        .await
        .unwrap();
    assert!(
        get_ready(DEFAULT_URL.clone(), &cato_token, GAME_ID, CATO)
            .await
            .unwrap()
    );
    assert!(
        get_ready(DEFAULT_URL.clone(), &pompey_token, GAME_ID, POMPEY)
            .await
            .unwrap()
    );

    // Adjudicate the game
    let adjudicated_response = gm_action(
        DEFAULT_URL.clone(),
        &gm_token,
        GAME_ID,
        GMAction::Adjudicate,
    )
    .await
    .unwrap();

    // TODO
    dbg!(adjudicated_response);
}
