package client;

import javax.swing.*;
import java.awt.Point;
import java.io.*;
import java.net.*;
import shared.PacketProtocol;

public class NetworkClient {
    private LobbyWindow lobbyWindow;
    private GameWindow gamePanel;
    private JFrame frame;
    private PrintWriter out; 
    private String myName;

    public static void main(String[] args) {
        // Create an instance to avoid static context issues
        NetworkClient clientInstance = new NetworkClient();
        clientInstance.init();
    }

    public void init() {
        String tempName = JOptionPane.showInputDialog(null, "Enter your player name:", "Player");
        if (tempName == null || tempName.trim().isEmpty()) {
            tempName = "Player";
        }
        this.myName = tempName;

        try {
            Socket s = new Socket("localhost", 5000);
            out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            // Initialize Game Frame
            frame = new JFrame("Labyrinth of Sockets");
            gamePanel = new GameWindow(out);
            frame.add(gamePanel); 
            frame.pack(); 
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            // Open Lobby immediately using 'this' instance
            lobbyWindow = new LobbyWindow(this);
            lobbyWindow.setVisible(true);

            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        String[] data = PacketProtocol.parse(line);
                        
                        if (data[0].equals("LOBBY_UPDATE")) {
                            if (lobbyWindow != null) {
                                lobbyWindow.updateStatus(data[1]);
                            }
                        }

                        if (data[0].equals("START_GAME")) {
                            SwingUtilities.invokeLater(() -> {
                                if (lobbyWindow != null) lobbyWindow.dispose();
                                frame.setVisible(true); 
                            });
                        }

                        if (data[0].equals("ID")) {
                            gamePanel.myId = Integer.parseInt(data[1]);
                            gamePanel.myName = myName; 
                            out.println("JOIN|" + myName); 
                        }
                        
                        if (data[0].equals("NAME")) {
                            int id = Integer.parseInt(data[1]);
                            String name = data[2];
                            gamePanel.playerNames.put(id, name);
                            if (lobbyWindow != null) {
                                lobbyWindow.updatePlayerList(gamePanel.playerNames.values(), myName);
                            }
                            gamePanel.repaint();
                        }

                        if (data[0].equals("KEY_POS")) {
                            gamePanel.keyX = Integer.parseInt(data[1]);
                            gamePanel.keyY = Integer.parseInt(data[2]);
                            gamePanel.repaint();
                        }

                        if (data[0].equals("UPDATE")) {
                            int id = Integer.parseInt(data[1]);
                            if (id != gamePanel.myId) {
                                gamePanel.otherPlayers.put(id, new Point(Integer.parseInt(data[2]), Integer.parseInt(data[3])));
                                gamePanel.repaint();
                            }
                        }

                        if (data[0].equals("KEY_FOUND")) {
                            gamePanel.keyHolderId = Integer.parseInt(data[1]);
                            gamePanel.realDoorIdx = Integer.parseInt(data[2]);
                            gamePanel.repaint();
                            String finderName = gamePanel.playerNames.getOrDefault(gamePanel.keyHolderId, "Player");
                            if (gamePanel.keyHolderId == gamePanel.myId) finderName = "You";
                            final String popupName = finderName;
                            SwingUtilities.invokeLater(() -> 
                                JOptionPane.showMessageDialog(frame, popupName + " found the Key! Follow them to the exit!"));
                        }

                        if (data[0].equals("GAME_OVER")) {
                            String winnerId = data[1];
                            String winnerName = gamePanel.playerNames.getOrDefault(Integer.parseInt(winnerId), "Player " + winnerId);
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(frame, "GAME OVER\nWinner: " + winnerName + "\nScore: " + data[2]);
                                System.exit(0);
                            });
                        }
                    }
                } catch (IOException e) { e.printStackTrace(); }
            }).start();
        } catch (IOException e) { 
            JOptionPane.showMessageDialog(null, "Server not found. Start GameServer first!");
        }
    }

    public void sendReady() {
        if (out != null) {
            out.println("READY");
        }
    }
}