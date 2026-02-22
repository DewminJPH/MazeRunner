package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.io.PrintWriter;

public class GameWindow extends JPanel {
    private final int TILE_SIZE = 30;
    public int myId = -1, keyHolderId = -1, realDoorIdx = -1;
    public int myX = 30, myY = 30;
    private boolean showingGhost = false;
    private PrintWriter out;

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
        this.setPreferredSize(new Dimension(600, 600));
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



    private void drawPlayer(Graphics g, int x, int y, Color c) {
        g.setColor(c); g.fillRoundRect(x + 2, y + 2, 26, 26, 10, 10);
        g.setColor(Color.WHITE); g.fillRect(x + 6, y + 8, 8, 8); g.fillRect(x + 16, y + 8, 8, 8);
        g.setColor(Color.BLACK); g.fillRect(x + 8, y + 10, 4, 4); g.fillRect(x + 18, y + 10, 4, 4);
    }

    private void drawFogOfWar(Graphics2D g2d) {
        int radius = 120;
        Area fog = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
        Ellipse2D view = new Ellipse2D.Double(myX + 15 - radius, myY + 15 - radius, radius * 2, radius * 2);
        fog.subtract(new Area(view));
        g2d.setColor(new Color(0, 0, 0, 245)); g2d.fill(fog);
        RadialGradientPaint glow = new RadialGradientPaint(myX + 15, myY + 15, radius, new float[]{0f, 1f}, new Color[]{new Color(0,0,0,0), new Color(0,0,0,245)});
        g2d.setPaint(glow); g2d.fill(view);
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

        if (keyHolderId == -1) { g.setColor(Color.YELLOW); g.fillOval(10*30+5, 15*30+10, 15, 15); }
        drawPlayer(g, myX, myY, Color.GREEN);
        drawFogOfWar(g2d);

    }

}