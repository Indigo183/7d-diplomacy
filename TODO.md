# TODOs

Note: Ideas that have not been discussed and confirmed to be wanted features should be *italicised*.

## Frontend

- [ ] Dynamically hide the window title bar based on whether or not you're a tiling window manager user (stinky)
- [ ] Make it not cooked on web
- [ ] ADD A GODVERDOMME BACK BUTTON TO THIS INTERFACE PLEASE

### Landing Page
- [ ] Join New Game button
- [ ] Host New Game Button
- [ ] List of games
    - [ ] Resume button in leiu temp. 
    - [ ] Automatically add joined games to the list
    - [ ] Way of removing games from the list
    - [ ] Archiving games so that you can see the results from local caches of them?

### Host New Game Page
- [ ] Creating a new game
    - [ ] Text fields 
- [ ] Create local (cached) copy of a game
- [ ] Create a copy in the backend

### Join Game Page
- [ ] Text field for web adress
- [ ] Country selection field (*post-connect*)
    - [ ] Ability to join random unclaimed country
    - [ ] Password protection for players
        - [ ] *Password per player/per country*
    - [ ] GMs can see and set passwords
- [ ] Spectator account
- [ ] GM account

### Main Game Interface
- [ ] Having 2D interactive boards
    - [ ] Hover to reveal *province name and abbrev*
    - [ ] Clicking on boards to enter orders
    - [ ] Arrows for orders
- [ ] Layer changing
- [ ] Axis changing
- [ ] 3D isometric views
- [ ] Usual buttons at the bottom for order entry
- [ ] Ghost board

### Player List
- [ ] List of all the powers
- [ ] Number of centres controlled (and victory condition)
- [ ] Pictoral representation of owned centres
    - [ ] Hovering displays board indices
    - [ ] Clicking a province cycles through eachg timeline on which it's owned and auto-zooms to it

### Order Entry
- [ ] Use the buttons
- [ ] Use hotkeys
- [ ] Text order input
    - [ ] Order text editor
    - [ ] Interaction between board order entry and the text editor
    - [ ] Undo history (if clicking on the board adds an order, )
    - [ ] LSP-like thingamajig to aid order entry
    - [ ] Clicking on an order to highlight it
- [ ] Allow for comments
- [ ] Text export of orders for logs
- [ ] Quick copy entered orders

### Order Submission
- [ ] Submit ordedrs
- [ ] Resubmit different orders to change them
- [ ] Undo previous submissions (backend integration)
    - [ ] *Diff display for past sets of orders*
    - [ ] *Stack-based in backend*

### *Sandbox mode*
- [ ] *Add a separate sandbox mode*
- [ ] *Allow for order entry for any units*
- [ ] *Allow for the server to be asked to adjudicate a position*
    - [ ] *This is stupid, the frontend should be able to adjudicate offline without depending on Indigo's uptime. Rust rewrite of the adjudicator anyone?*
- [ ] *Sandbox ghost board*
- [ ] *Save/load sandbox states*

## Backend

- [ ] Integrate some of Ludo's order logger code to generate order logs?
- [ ] Add passwords for joining countries
- [ ] *Add order stack for each player*
