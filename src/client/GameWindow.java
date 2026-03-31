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
    public int myId = -1, keyHolderId = -1, realDoorIdx = -1;
    public int myX = 30, myY = 30;
    private boolean showingGhost = false;
    private PrintWriter out;

    public String myName = "";
    public int keyX = 10, keyY = 15;
    public Map<Integer, Point> otherPlayers = new ConcurrentHashMap<>();
    public Map<Integer, String> playerNames = new ConcurrentHashMap<>();

    // Array of distinct colors for multiple players
    private final Color[] P_COLORS = {Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, new Color(150, 100, 255)};

    private int[][] map = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
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
        this.out = out; this.setFocusable(true);
        // INCREASED WIDTH to 800 to make room for the right sidebar
        this.setPreferredSize(new Dimension(800, 600));
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (showingGhost) return;
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

    private Color getPlayerColor(int id) {
        return P_COLORS[id % P_COLORS.length];
    }

    private void drawPlayer(Graphics g, int x, int y, Color c) {
        g.setColor(c); g.fillRoundRect(x + 2, y + 2, 26, 26, 10, 10);
        g.setColor(Color.WHITE); g.fillRect(x + 6, y + 8, 8, 8); g.fillRect(x + 16, y + 8, 8, 8);
        g.setColor(Color.BLACK); g.fillRect(x + 8, y + 10, 4, 4); g.fillRect(x + 18, y + 10, 4, 4);
    }

    private void drawFogOfWar(Graphics2D g2d) {
        int radius = 120;
        // Fog only covers the 600x600 maze, leaving the sidebar visible
        Area fog = new Area(new Rectangle(0, 0, 600, 600));
        Ellipse2D view = new Ellipse2D.Double(myX + 15 - radius, myY + 15 - radius, radius * 2, radius * 2);
        fog.subtract(new Area(view));
        g2d.setColor(new Color(0, 0, 0, 245)); g2d.fill(fog);
        RadialGradientPaint glow = new RadialGradientPaint(myX + 15, myY + 15, radius, new float[]{0f, 1f}, new Color[]{new Color(0,0,0,0), new Color(0,0,0,245)});
        g2d.setPaint(glow); g2d.fill(view);
    }

    private void drawScoreboard(Graphics g) {
        // Draw the dark background for the right sidebar
        g.setColor(new Color(25, 25, 25));
        g.fillRect(600, 0, 200, 600);

        // Draw the Scorecard box inside the sidebar
        g.setColor(new Color(40, 40, 40));
        g.fillRect(610, 10, 175, 40 + (otherPlayers.size() * 20));
        g.setColor(Color.WHITE);
        g.drawRect(610, 10, 175, 40 + (otherPlayers.size() * 20));
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("LIVE SCORECARD", 620, 25);

        int y = 45;
        g.setColor(Color.GREEN);
        g.drawString(myName + " (You)" + (myId == keyHolderId ? " [KEY]" : ""), 620, y);

        for (Integer id : otherPlayers.keySet()) {
            y += 20;
            String name = playerNames.getOrDefault(id, "Player " + id);
            // Match the text color on the scoreboard to the player's actual box color
            g.setColor(getPlayerColor(id));
            g.drawString(name + (id == keyHolderId ? " [KEY]" : ""), 620, y);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[r].length; c++) {
                int x = c * TILE_SIZE, y = r * TILE_SIZE;
                if (map[r][c] == 1) { g.setColor(new Color(115, 59, 0)); g.fillRect(x, y, TILE_SIZE, TILE_SIZE); }
                else if (map[r][c] == 0) { g.setColor((r+c)%2==0 ? new Color(43,59,106) : new Color(96,110,151)); g.fillRect(x, y, TILE_SIZE, TILE_SIZE); }
                else if (map[r][c] == 2 || map[r][c] == 3) {
                    boolean isReal = (map[r][c] == 2 && realDoorIdx == 0) || (map[r][c] == 3 && realDoorIdx == 1);
                    g.setColor(new Color(222, 184, 135)); g.fillRect(x+7, y, 15, 30);
                    g.setColor((myId == keyHolderId && isReal) ? Color.GREEN : Color.RED); g.fillRect(x+9, y+2, 11, 26);
                }
            }
        }

        if (keyHolderId == -1) { g.setColor(Color.YELLOW); g.fillOval(keyX * TILE_SIZE + 5, keyY * TILE_SIZE + 10, 15, 15); }

        drawPlayer(g, myX, myY, Color.GREEN);

        for (Integer id : otherPlayers.keySet()) {
            Point p = otherPlayers.get(id);
            // Draw other players using their assigned colors
            drawPlayer(g, p.x, p.y, getPlayerColor(id));
        }

        drawFogOfWar(g2d);
        drawScoreboard(g);
    }
}
