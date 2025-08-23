# clj-chess

A real-time multiplayer chess game built with Clojure and WebSockets.

## Overview

This project implements a complete chess engine with web-based gameplay featuring:

- **Backend**: Clojure server with HTTP-Kit WebSockets for real-time communication
- **Game Logic**: Complete chess rule engine with move validation and game state management
- **Frontend**: Interactive HTML5 chess board with piece movement and legal move highlighting
- **Authentication**: JWT-based player authentication system

## Structure

- `src/chess/core.clj` - Main server entry point and HTTP-Kit configuration
- `src/chess/server.clj` - WebSocket routing and request handling
- `src/chess/game_logic/` - Chess engine implementation
  - `game_state.clj` - Game state management and initialization
  - `actions.clj` - Move processing and game actions
  - `actions_validations.clj` - Chess rule validation
- `chess-board.html` - Interactive web client with real-time gameplay

## Usage

Run with `lein run` or specify port: `lein run 3000`

## License

Copyright © 2020 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
