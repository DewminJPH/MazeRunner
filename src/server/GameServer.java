package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {
    // Upgraded to CopyOnWriteArrayList to prevent crashes when clearing the list!
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static int keyGridX = -1, keyGridY = -1;
    public static Map<Integer, String> playerNames = new java.util.concurrent.ConcurrentHashMap<>();
    private static boolean keyPickedUp = false;
    private static int keyHolderId = -1;
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

    public static void handleMove(int id, int x, int y) {
        int gX = x / 30, gY = y / 30;
        if (gY < 0 || gY >= 20 || gX < 0 || gX >= 20 || map[gY][gX] == 1) return;

        if (!keyPickedUp && gX == keyGridX && gY == keyGridY) {
            keyPickedUp = true; keyHolderId = id;
            broadcast("KEY_FOUND|" + id + "|" + realDoorIndex);
        }

        if (id == keyHolderId) {
            boolean atDoorA = (map[gY][gX] == 2);
            boolean atDoorB = (map[gY][gX] == 3);
            if ((realDoorIndex == 0 && atDoorA) || (realDoorIndex == 1 && atDoorB)) {
                broadcast("GAME_OVER|" + id + "|500");
                
                // --- SERVER RESET LOGIC ---
                // This prepares the server for the next round instantly!
                keyPickedUp = false;
                keyHolderId = -1;
                realDoorIndex = new Random().nextInt(2);
                randomizeKeyPosition();
                clients.clear();
                playerNames.clear();
            }
        }
        broadcast("UPDATE|" + id + "|" + x + "|" + y);
    }

    public static void randomizeKeyPosition() {
        Random rand = new Random();
        while (true) {
            int rX = rand.nextInt(20);
            int rY = rand.nextInt(20);
            if (map[rY][rX] == 0 && (rX != 1 || rY != 1)) {
                keyGridX = rX; 
                keyGridY = rY;
                break;
            }
        }
    }

    public static void registerPlayer(int id, String name) {
        playerNames.put(id, name);
        broadcast("NAME|" + id + "|" + name);
        for (Integer existingId : playerNames.keySet()) {
            if (existingId != id) {
                clients.get(id).sendMessage("NAME|" + existingId + "|" + playerNames.get(existingId));
            }
        }
    }

    public static int getKeyX() { return keyGridX; }
    public static int getKeyY() { return keyGridY; }
}
