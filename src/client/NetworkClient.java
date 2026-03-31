package client;

import javax.swing.*;
import java.awt.Point;
import java.io.*;
import java.net.*;
import shared.PacketProtocol;

public class NetworkClient {
    public static void main(String[] args) {
        String serverHost = "localhost";
        int serverPort = 5000;

        if (args.length >= 1 && args[0] != null && !args[0].trim().isEmpty()) {
            serverHost = args[0].trim();
        }
        if (args.length >= 2) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid port: " + args[1] + "\nUsage: java -cp src client.NetworkClient <server-ip> <port>");
                return;
            }
        }

        String tempName = JOptionPane.showInputDialog(null, "Enter your player name:", "Player");
        if (tempName == null || tempName.trim().isEmpty()) {
            tempName = "Player";
        }
        
        final String myName = tempName;

        try {
            Socket s = new Socket(serverHost, serverPort);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            JFrame frame = new JFrame("Labyrinth of Sockets");
            GameWindow gamePanel = new GameWindow(out);
            frame.add(gamePanel); frame.pack(); frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        String[] data = PacketProtocol.parse(line);
                        
                        if (data[0].equals("ID")) {
                            gamePanel.myId = Integer.parseInt(data[1]);
                            gamePanel.myName = myName; 
                            out.println("JOIN|" + myName); 
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

                        if (data[0].equals("NAME")) {
                            gamePanel.playerNames.put(Integer.parseInt(data[1]), data[2]);
                            gamePanel.repaint();
                        }

                        if (data[0].equals("KEY_FOUND")) {
                            gamePanel.keyHolderId = Integer.parseInt(data[1]);
                            gamePanel.realDoorIdx = Integer.parseInt(data[2]);
                            gamePanel.repaint();
                            
                            // Find out WHO got the key to make a personalized popup
                            String finderName = gamePanel.playerNames.getOrDefault(gamePanel.keyHolderId, "Player");
                            if (gamePanel.keyHolderId == gamePanel.myId) {
                                finderName = "You";
                            }
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
            JOptionPane.showMessageDialog(null,
                    "Unable to connect to server at " + serverHost + ":" + serverPort +
                    "\nStart the server first or check IP/port and firewall.");
            e.printStackTrace();
        }
    }
}
