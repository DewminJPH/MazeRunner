package client;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class LobbyWindow extends JFrame {
    private JLabel statusLabel;
    private JButton readyButton;
    private DefaultListModel<String> playerListModel;
    private JList<String> playerList;
    private NetworkClient networkClient;

    // A simple custom icon for the checkmark
    private static final Icon CHECK_ICON = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(50, 255, 50)); // Neon Green
            g2.setStroke(new BasicStroke(3));
            // Draw a checkmark shape
            g2.drawLine(x + 2, y + 8, x + 7, y + 13);
            g2.drawLine(x + 7, y + 13, x + 15, y + 3);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
    };

    public LobbyWindow(NetworkClient client) {
        this.networkClient = client;
        setTitle("Labyrinth of Sockets - Lobby");
        setSize(450, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(15, 15, 20)); 
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); 

        statusLabel = new JLabel("WAITING FOR PLAYERS...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        statusLabel.setForeground(new Color(0, 255, 255));
        mainPanel.add(statusLabel, BorderLayout.NORTH);

        playerListModel = new DefaultListModel<>();
        playerList = new JList<>(playerListModel);
        
        // --- THE MAGIC PART: SETTING THE CUSTOM RENDERER ---
        playerList.setCellRenderer(new PlayerCellRenderer());
        
        playerList.setBackground(new Color(25, 25, 30));
        playerList.setForeground(new Color(50, 255, 50));
        playerList.setFont(new Font("SansSerif", Font.BOLD, 18));
        
        JScrollPane scrollPane = new JScrollPane(playerList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 45)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        readyButton = new JButton("VOTE TO START");
        readyButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        readyButton.setBackground(new Color(0, 100, 150));
        readyButton.setForeground(Color.WHITE);
        readyButton.setFocusPainted(false);
        readyButton.setPreferredSize(new Dimension(0, 50));

        readyButton.addActionListener(e -> {
            networkClient.sendReady(); 
            readyButton.setEnabled(false);
            readyButton.setText("VOTED!");
            readyButton.setBackground(new Color(40, 40, 40));
        });

        mainPanel.add(readyButton, BorderLayout.SOUTH);
        add(mainPanel);
    }

    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status + " READY"));
    }

    public void updatePlayerList(Collection<String> names, String myName) {
        SwingUtilities.invokeLater(() -> {
            playerListModel.clear();
            for (String name : names) {
                // We add a prefix so the renderer knows which ones are "You"
                if (name.equals(myName)) {
                    playerListModel.addElement("SELF:" + name);
                } else {
                    playerListModel.addElement("OTHER:" + name);
                }
            }
        });
    }

    // --- CUSTOM RENDERER CLASS ---
    private class PlayerCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            String text = value.toString();
            label.setIcon(CHECK_ICON); // Always show the graphic icon
            label.setIconTextGap(15);
            
            if (text.startsWith("SELF:")) {
                label.setText(text.substring(5) + " (You)");
                label.setForeground(new Color(50, 255, 50)); // Bright Green
            } else {
                label.setText(text.substring(6));
                label.setForeground(new Color(150, 255, 150)); // Slightly dimmer
            }
            
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            return label;
        }
    }
}