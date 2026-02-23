## How to Run the Game
To ensure the game functions correctly, follow this specific order:

### 1. Start the Server
   - Navigate to src/server/GameServer.java.
   - Run the main method.
   - Console Output: You should see "Server started on port 5000...".

### 2. Start the Clients (Players)
   - Navigate to src/client/NetworkClient.java.
   - Run the main method for each player (Supports up to 5 players).
   - Each player will be assigned a unique color (Green, Pink, Cyan, etc.).

## Game Controls & Mechanics
   - Movement: Use W, A, S, D keys to navigate the maze.
   - Fog of War: You can only see a small circular area around your character.

    The Goal:

   - Find the Yellow Key hidden in the maze.
   - Once found, a notification will appear for all players.
   - The Key Holder must lead the team to the correct exit door.
   - The real exit turns Green only for the Key Holder.

## Network Troubleshooting
   - Local Play: By default, NetworkClient.java connects to localhost.
   - LAN Play: If playing on different laptops, change the serverAddress in NetworkClient.java from "localhost" to the IP Address of the computer running the GameServer.