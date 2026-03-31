package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static int keyGridX = -1, keyGridY = -1;
    public static Map<Integer, String> playerNames = new ConcurrentHashMap<>();
    
    private static Set<Integer> readyPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static boolean gameStarted = false;
    private static final int VOTES_REQUIRED = 3; 

    private static boolean keyPickedUp = false;
    private static int keyHolderId = -1; // The "Key Founder"
    private static int realDoorIndex = new Random().nextInt(2);

    private static int[][] map = {
            {1,1,1,1,1,2,1,1,1,1,1,1,1,1,1,3,1,1,1,1},
            {1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,1,0,1,0,1,1,1,1,1,1,1,1,0,1},
            {1,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,1},
            {1,0,1,0,1,0,1,1,1,1,1,1,0,1,1,1,0,1,0,3},
            {1,0,0,0,1,0,0,0,0,0,0,1,0,1,0,0,0,0,0,1},
            {1,1,1,0,1,1,1,1,1,1,0,1,0,1,0,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,1},
            {2,0,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,0,1},
            {1,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,1},
            {1,0,1,0,1,1,0,1,1,1,1,1,1,1,1,1,0,1,0,1},
            {1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1},
            {1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1},
            {1,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,1},
            {1,0,1,1,1,0,1,0,1,1,1,1,0,1,1,1,1,1,0,1},
            {1,0,1,0,0,0,0,0,1,0,0,1,0,0,0,0,0,1,0,1},
            {1,0,1,0,1,1,1,1,1,0,0,1,1,1,1,1,0,1,0,1},
            {1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Server started on port 5000...");
        randomizeKeyPosition(); 
        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket, clients.size());
            clients.add(handler);
            new Thread(handler).start();
        }
    }

    public static synchronized void broadcast(String msg) {
        for (ClientHandler c : clients) c.sendMessage(msg);
    }

    public static synchronized void handleReadyVote(int id) {
        if (gameStarted) return;
        readyPlayers.add(id);
        broadcast("LOBBY_UPDATE|" + readyPlayers.size() + "/" + clients.size());
        if (readyPlayers.size() >= VOTES_REQUIRED && clients.size() >= VOTES_REQUIRED) {
            gameStarted = true;
            broadcast("START_GAME");
        }
    }

    public static void registerPlayer(int id, String name) {
        playerNames.put(id, name);
        broadcast("NAME|" + id + "|" + name);
        ClientHandler newGuy = clients.get(id);
        for (Map.Entry<Integer, String> entry : playerNames.entrySet()) {
            if (entry.getKey() != id) {
                newGuy.sendMessage("NAME|" + entry.getKey() + "|" + entry.getValue());
                if (readyPlayers.contains(entry.getKey())) {
                    newGuy.sendMessage("PLAYER_READY|" + entry.getKey());
                }
            }
        }
    }

    public static void handleMove(int id, int x, int y) {
        if (!gameStarted) return;
        int gX = x / 30, gY = y / 30;
        if (gY < 0 || gY >= 20 || gX < 0 || gX >= 20 || map[gY][gX] == 1) return;

        // Key Pickup
        if (!keyPickedUp && gX == keyGridX && gY == keyGridY) {
            keyPickedUp = true; 
            keyHolderId = id; // Store the founder
            broadcast("KEY_FOUND|" + id + "|" + realDoorIndex);
        }

        // Win Logic: If key is picked up, ANYONE can enter the real door
        if (keyPickedUp) {
            boolean isAtRealDoor = (realDoorIndex == 0 && map[gY][gX] == 2) || (realDoorIndex == 1 && map[gY][gX] == 3);
            if (isAtRealDoor) {
                // Key founder earns 500 bonus, others get standard score
                int finalScore = (id == keyHolderId) ? 500 : 100;
                broadcast("GAME_OVER|" + id + "|" + finalScore);
                resetGame();
                return;
            }
        }
        broadcast("UPDATE|" + id + "|" + x + "|" + y);
    }

    private static void resetGame() {
        keyPickedUp = false; keyHolderId = -1; gameStarted = false;
        readyPlayers.clear(); realDoorIndex = new Random().nextInt(2);
        randomizeKeyPosition(); 
        // We don't clear clients/names here so players can stay connected for next round
    }

    public static void randomizeKeyPosition() {
        Random rand = new Random();
        while (true) {
            int rX = rand.nextInt(20), rY = rand.nextInt(20);
            if (map[rY][rX] == 0 && (rX != 1 || rY != 1)) {
                keyGridX = rX; keyGridY = rY; break;
            }
        }
    }

    public static int getKeyX() { return keyGridX; }
    public static int getKeyY() { return keyGridY; }
}