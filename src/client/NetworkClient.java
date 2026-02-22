package client;

import javax.swing.*;
import java.io.*;
import java.net.*;
import shared.PacketProtocol;

public class NetworkClient {
    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost", 5000);
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
                        if (data[0].equals("ID")) gamePanel.myId = Integer.parseInt(data[1]);

                        // Popup for Key Pickup
                        if (data[0].equals("KEY_FOUND")) {
                            gamePanel.keyHolderId = Integer.parseInt(data[1]);
                            gamePanel.realDoorIdx = Integer.parseInt(data[2]);
                            gamePanel.repaint();
                            SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(frame, "The Key has been found! Follow the leader!"));
                        }

                        // Popup for Game Over
                        if (data[0].equals("GAME_OVER")) {
                            String winner = data[1];
                            String score = data[2];
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(frame, "GAME OVER\nWinner: Player " + winner + "\nScore: " + score);
                                System.exit(0);
                            });
                        }
                    }
                } catch (IOException e) { e.printStackTrace(); }
            }).start();

        } catch (IOException e) { e.printStackTrace(); }
    }
}