package server;

import java.io.*;
import java.net.*;
import java.util.*;
import shared.PacketProtocol;

public class GameServer {
    private static List<ClientHandler> clients = new ArrayList<>();
    private static int keyGridX = 10, keyGridY = 15;
    private static boolean keyPickedUp = false;
    private static int keyHolderId = -1;
    private static int realDoorIndex = new Random().nextInt(2); // 0 = Door A, 1 = Door B

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

// Key Pickup Logic
        if (!keyPickedUp && gX == keyGridX && gY == keyGridY) {
            keyPickedUp = true; keyHolderId = id;
            broadcast("KEY_FOUND|" + id + "|" + realDoorIndex);
        }

// Win Condition: Leader touches the correct door
        if (id == keyHolderId) {
            boolean atDoorA = (map[gY][gX] == 2);
            boolean atDoorB = (map[gY][gX] == 3);
            if ((realDoorIndex == 0 && atDoorA) || (realDoorIndex == 1 && atDoorB)) {
                broadcast("GAME_OVER|" + id + "|500"); // Notify all clients
            }
        }
        broadcast("UPDATE|" + id + "|" + x + "|" + y);
    }
}