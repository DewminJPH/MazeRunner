# Maze Runner - Network Multiplayer Game

## Module Context
This project is developed for the Operating Systems and Network Programming module.

It is designed to satisfy the given project guidelines:

1. Unique project title: Maze Runner (Labyrinth of Sockets).
2. Demonstrates threads: server-side and client-side multithreading is used.
3. Communicates between at least two hosts: TCP socket-based communication is implemented.
4. Any language/library allowed: implemented in Java using standard libraries (java.net, java.io, Swing, java.util.concurrent).

Group size target: 5 members.

## Project Summary
Maze Runner is a real-time multiplayer maze game where multiple clients connect to one server, move through a maze, find a key, and unlock the correct exit door to win.

Main idea:
- Server controls shared game state and broadcasts updates.
- Clients render the maze and players locally using Swing.
- Communication happens through text packets over TCP sockets.

## Tech Stack
- Language: Java
- UI: Java Swing (JFrame, JPanel)
- Networking: TCP sockets (ServerSocket, Socket)
- Concurrency: Thread, Runnable, ConcurrentHashMap, CopyOnWriteArrayList
- Build: javac (no external build tool required)

## Project Structure

```text
src/
  client/
    GameWindow.java
    NetworkClient.java
  server/
    ClientHandler.java
    GameServer.java
    PlayerData.java
  shared/
    PacketProtocol.java
```

## Core Components

### 1) Server Layer

#### GameServer.java
Responsibilities:
- Starts TCP server on port 5000.
- Accepts new client connections.
- Creates one thread per connected client via ClientHandler.
- Maintains global game state:
  - key position
  - key holder
  - real door index
  - list of connected clients
  - player names map
- Validates movement against maze walls.
- Broadcasts updates/events to all clients.
- Handles game-over and round reset.

Important details:
- Uses CopyOnWriteArrayList for client list safety during iteration.
- Uses ConcurrentHashMap for player name mapping.
- Uses synchronized broadcast for safer concurrent message dispatch.

#### ClientHandler.java
Responsibilities:
- Runs per client in a dedicated thread.
- Sends initial packets to client (ID and key position).
- Reads incoming client packets in a loop.
- Forwards movement and join-name events to GameServer.

Thread usage:
- Implements Runnable and is executed using new Thread(handler).start().

#### PlayerData.java
Responsibilities:
- Utility class to append game result records to highscores.txt.
- Demonstrates file I/O persistence helper.

Current status:
- Available in codebase; not actively integrated in end-of-round flow.

### 2) Client Layer

#### NetworkClient.java
Responsibilities:
- Entry point for each player process.
- Gets player name from dialog.
- Connects to server using command-line host/port or defaults to localhost:5000.
- Creates and shows Swing game window.
- Starts a listener thread to receive server packets continuously.
- Updates UI state according to packets (ID, positions, names, key found, game over).

Thread usage:
- A background thread handles incoming network messages to keep UI responsive.

#### GameWindow.java
Responsibilities:
- Renders maze, players, key, doors, fog-of-war, and score sidebar.
- Handles keyboard input (W/A/S/D).
- Sends MOVE packets to server via PrintWriter.
- Stores local and remote player render state.

UI features:
- Tile-based maze rendering.
- Distinct player colors.
- Key holder indicator.
- Live scorecard panel.
- Fog-of-war effect around local player.

### 3) Shared Layer

#### PacketProtocol.java
Responsibilities:
- Defines packet formatting and parsing conventions.
- Provides reusable methods for protocol strings.

Benefits:
- Keeps packet language centralized.
- Reduces string-format mistakes.

## Networking Design

### Transport
- Protocol: TCP
- Port: 5000
- Pattern: one server, multiple clients

### Communication Model
- Client -> Server:
  - JOIN|name
  - MOVE|id|x|y
- Server -> Client:
  - ID|id
  - KEY_POS|x|y
  - UPDATE|id|x|y
  - NAME|id|name
  - KEY_FOUND|id|realDoorIndex
  - GAME_OVER|winnerId|score

### Packet Format
All messages use pipe-separated text packets:

```text
TYPE|field1|field2|...
```

Example:

```text
UPDATE|2|120|210
```

## Threading and OS Concepts Demonstrated

This project demonstrates key Operating Systems concepts:

1. Concurrency
- Multiple client handlers run in parallel on server side.
- Client network listener runs concurrently with UI event dispatch.

2. Process and thread separation
- Each game client runs as a separate process.
- Server spawns per-client threads for scalable connection handling.

3. Synchronization and thread-safe collections
- synchronized method for broadcast.
- CopyOnWriteArrayList and ConcurrentHashMap to reduce race risks.

4. I/O blocking behavior
- Server accept/read loops and client read loops show blocking network I/O behavior.

## How to Compile and Run

Run from project root.

### Step 1: Compile

```bash
javac src/shared/*.java src/server/*.java src/client/*.java
```

### Step 2: Start Server (Terminal 1)

```bash
java -cp src server.GameServer
```

Expected output:

```text
Server started on port 5000...
```

### Step 3: Start Client 1 (Terminal 2)

```bash
java -cp src client.NetworkClient
```

or (explicit host/port):

```bash
java -cp src client.NetworkClient <server-ip> <port>
```

### Step 4: Start Client 2+ (Terminal 3, 4, ...)

```bash
java -cp src client.NetworkClient <server-ip> <port>
```

## Running Across Two Different Hosts

Client defaults to localhost:5000 if no arguments are provided.

For two-host deployment:
1. Start server on Host A.
2. Find Host A IP (for example 192.168.1.10).
3. Run clients from Host B/C using Host A IP.
4. Example command:

```bash
java -cp src client.NetworkClient 192.168.1.10 5000
```

5. Ensure firewall allows TCP 5000 on Host A.

## Game Flow

1. Server starts and randomizes key position.
2. Client connects and receives ID and key coordinates.
3. Client sends JOIN with name.
4. Players move using W/A/S/D.
5. First player touching key becomes key holder.
6. Server reveals which door is real for key-holder validation.
7. Key holder reaching correct door triggers GAME_OVER.
8. Server resets round state.

## Mapping to Module Guidelines

1) Unique title
- Maze Runner (Labyrinth of Sockets).

2) Demonstrate threads
- Server: one thread per client via ClientHandler.
- Client: dedicated listener thread for incoming packets.

3) Communicate between at least two hosts
- TCP socket architecture supports distributed host setup.
- Client target host can be configured to server IP for cross-machine demo.

4) Any library/language
- Java standard libraries are used throughout.

## Demo Plan (4 Minutes)

Suggested timeline:

- 0:00-0:45
  - Explain architecture (server, client, shared protocol).
- 0:45-1:30
  - Start server and two clients.
- 1:30-2:45
  - Show live movement sync and scoreboard updates.
- 2:45-3:30
  - Show key pickup and door logic.
- 3:30-4:00
  - Show GAME_OVER and summarize threads/network concepts.

## Viva Preparation (2 Minutes)

Likely questions and points to answer:

1. Where are threads used?
- Server creates one thread per client connection.
- Client has a network listener thread besides Swing UI thread.

2. How does communication work?
- TCP socket, text packet protocol with pipe-separated fields.

3. How is shared state synchronized?
- synchronized broadcast and concurrent collections.

4. How to run on two hosts?
- Start server on one host, run clients with server IP/port args, and allow port 5000.

5. What does shared package do?
- Centralized protocol formatting/parsing.

## Team Contribution Template (5 Members)

Replace with real names/IDs:

1. Member 1
- Server socket setup, accept loop, game state core.

2. Member 2
- Client networking, packet handling, join/update flow.

3. Member 3
- Swing UI rendering, controls, fog-of-war and visual elements.

4. Member 4
- Protocol design, shared packet utility, integration testing.

5. Member 5
- Gameplay logic (key/door/win), demo prep, documentation.

## Limitations and Improvement Ideas

Current limitations:
- No reconnect mechanism after disconnect.
- Basic error dialogs/logging.
- PlayerData persistence helper is not fully integrated into game-over pipeline.

Potential enhancements:
- Add reconnect and timeout handling.
- Add lobby and room support.
- Persist high scores and show leaderboard in UI.
- Add unit/integration tests for packet parsing and move validation.

## Troubleshooting

1. Client shows connection refused
- Server is not running or wrong host/port.

2. No GUI window appears
- Start server first, then client.
- Ensure Java runtime supports Swing UI in your environment.

3. Port already in use
- Another server instance is running on 5000.

4. Multiplayer not syncing
- Verify all clients connect to the same server instance and network.

## Conclusion
Maze Runner demonstrates practical operating systems and network programming concepts through a real-time multiplayer game: process/thread coordination, socket communication, protocol design, and concurrent shared-state handling.
