package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameWindow extends JPanel {
    private final int TILE_SIZE = 30;
    private final int MAZE_SIZE = 600; // 20 tiles * 30px
    private final int SIDEBAR_WIDTH = 200;
    
    public int myId = -1, keyHolderId = -1, realDoorIdx = -1;
    public int myX = 30, myY = 30;
    private PrintWriter out;

    public String myName = "";
    public int keyX = 10, keyY = 15;
    public Map<Integer, Point> otherPlayers = new ConcurrentHashMap<>();
    public Map<Integer, String> playerNames = new ConcurrentHashMap<>();

    private final Color[] P_COLORS = {Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, new Color(150, 100, 255)};

    private int[][] map = {
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

    public GameWindow(PrintWriter out) {
        this.out = out; 
        this.setFocusable(true);
        this.setPreferredSize(new Dimension(MAZE_SIZE + SIDEBAR_WIDTH, MAZE_SIZE));
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int nX = myX, nY = myY;
                if (e.getKeyCode() == KeyEvent.VK_W) nY -= TILE_SIZE;
                if (e.getKeyCode() == KeyEvent.VK_S) nY += TILE_SIZE;
                if (e.getKeyCode() == KeyEvent.VK_A) nX -= TILE_SIZE;
                if (e.getKeyCode() == KeyEvent.VK_D) nX += TILE_SIZE;
                int gX = nX / TILE_SIZE, gY = nY / TILE_SIZE;
                if (gY >= 0 && gY < map.length && gX >= 0 && gX < map[0].length && map[gY][gX] != 1) {
                    myX = nX; myY = nY;
                    out.println("MOVE|" + myId + "|" + myX + "|" + myY);
                }
                repaint();
            }
        });
    }

    private void drawPlayer(Graphics g, int x, int y, Color c) {
        g.setColor(c); g.fillRoundRect(x + 2, y + 2, 26, 26, 10, 10);
        g.setColor(Color.WHITE); g.fillRect(x + 6, y + 8, 8, 8); g.fillRect(x + 16, y + 8, 8, 8);
        g.setColor(Color.BLACK); g.fillRect(x + 8, y + 10, 4, 4); g.fillRect(x + 18, y + 10, 4, 4);
    }

    private void drawFogOfWar(Graphics2D g2d) {
        int radius = 130;
        // CRITICAL FIX: The fog area must be restricted to the 600x600 maze area
        Area fog = new Area(new Rectangle(0, 0, MAZE_SIZE, MAZE_SIZE));
        Ellipse2D view = new Ellipse2D.Double(myX + 15 - radius, myY + 15 - radius, radius * 2, radius * 2);
        fog.subtract(new Area(view));
        g2d.setColor(new Color(0, 0, 0, 248)); 
        g2d.fill(fog);
    }

    private void drawScoreboard(Graphics g) {
        // Draw sidebar background
        g.setColor(new Color(30, 30, 35));
        g.fillRect(MAZE_SIZE, 0, SIDEBAR_WIDTH, MAZE_SIZE);
        
        // Draw header
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("PLAYERS", MAZE_SIZE + 20, 40);
        g.drawLine(MAZE_SIZE + 20, 50, MAZE_SIZE + 180, 50);

        int y = 80;
        // You
        g.setColor(Color.GREEN);
        String myStatus = (myId == keyHolderId) ? " [KEY FOUNDER]" : "";
        g.drawString(myName + " (You)" + myStatus, MAZE_SIZE + 20, y);

        // Others
        for (Integer id : otherPlayers.keySet()) {
            y += 30;
            g.setColor(P_COLORS[id % P_COLORS.length]);
            String otherStatus = (id == keyHolderId) ? " [KEY FOUNDER]" : "";
            g.drawString(playerNames.getOrDefault(id, "Player " + id) + otherStatus, MAZE_SIZE + 20, y);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // 1. Draw Maze
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[r].length; c++) {
                int x = c * TILE_SIZE, y = r * TILE_SIZE;
                if (map[r][c] == 1) { 
                    g.setColor(new Color(80, 50, 30)); 
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE); 
                } else if (map[r][c] == 0) { 
                    g.setColor((r+c)%2==0 ? new Color(40,40,50) : new Color(50,50,65)); 
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE); 
                } else if (map[r][c] == 2 || map[r][c] == 3) {
                    boolean isReal = (map[r][c] == 2 && realDoorIdx == 0) || (map[r][c] == 3 && realDoorIdx == 1);
                    g.setColor(new Color(180, 140, 100)); g.fillRect(x+5, y, 20, 30);
                    // DOOR OPENS FOR ALL IF KEY IS FOUND
                    g.setColor((keyHolderId != -1 && isReal) ? Color.GREEN : Color.RED); 
                    g.fillRect(x+8, y+2, 14, 26);
                }
            }
        }

        // 2. Draw Key if not picked up
        if (keyHolderId == -1) { 
            g.setColor(Color.YELLOW); 
            g.fillOval(keyX * TILE_SIZE + 7, keyY * TILE_SIZE + 7, 16, 16); 
        }

        // 3. Draw Players
        drawPlayer(g, myX, myY, Color.GREEN);
        for (Integer id : otherPlayers.keySet()) {
            Point p = otherPlayers.get(id);
            drawPlayer(g, p.x, p.y, P_COLORS[id % P_COLORS.length]);
        }

        // 4. Draw Fog (Maze only)
        drawFogOfWar(g2d);

        // 5. Draw Scoreboard (Top layer)
        drawScoreboard(g);
    }
}