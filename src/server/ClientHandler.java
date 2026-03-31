package server;
import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private int myId;

    public ClientHandler(Socket socket, int id) { 
        this.socket = socket; 
        this.myId = id; 
    }
    
    public void sendMessage(String msg) { 
        out.println(msg); 
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            out.println("ID|" + myId); 
            out.println("KEY_POS|" + GameServer.getKeyX() + "|" + GameServer.getKeyY());

            String input;
            while ((input = in.readLine()) != null) {
                String[] p = input.split("\\|");
                if (p[0].equals("MOVE")) {
                    GameServer.handleMove(myId, Integer.parseInt(p[2]), Integer.parseInt(p[3]));
                }
                if (p[0].equals("JOIN")) {
                    GameServer.registerPlayer(myId, p[1]); 
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}
