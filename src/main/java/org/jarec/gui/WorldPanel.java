package org.jarec.gui;

import org.jarec.data.Location;
import org.jarec.data.creature.Splurg;
import org.jarec.game.GameLoop;
import org.jarec.game.resources.Splurgs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Coordinate 0:0 is top left corner
 */
public class WorldPanel extends JPanel {
    private transient BufferedImage displayBuffer;     // Screen buffer
    private transient BufferedImage backgroundBuffer;  // Swap buffer

    private final ArrayList<Drawable> drawables = new ArrayList<>();

    public WorldPanel() {
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (GameLoop.getInstance().isPaused()) {
                    Location clickLocation = new Location(e.getX(), e.getY());
                    List<Splurg> localSplurgs = Splurgs.getInstance().getSplurgsInVicinity(clickLocation);

                    if (!localSplurgs.isEmpty()) {
                        JPopupMenu popup = new JPopupMenu();

                        for (Splurg splurg : localSplurgs) {
                            JMenuItem item = new JMenuItem(splurg.toString());
                            popup.add(item);
                        }

                        popup.show(WorldPanel.this, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (displayBuffer == null || displayBuffer.getWidth() != getWidth() || displayBuffer.getHeight() != getHeight()) {
            initBuffers();
        }

        g.drawImage(displayBuffer, 0, 0, null);
    }

    private void initBuffers() {
        int width = getWidth();
        int height = getHeight();
        displayBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        backgroundBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        clearBuffer(displayBuffer);
        clearBuffer(backgroundBuffer);
    }

    private void clearBuffer(BufferedImage buffer) {
        Graphics2D g2 = buffer.createGraphics();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        g2.dispose();
    }

    public Graphics2D getBackgroundGraphics() {
        if (backgroundBuffer == null) {
            backgroundBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        }
        return backgroundBuffer.createGraphics();
    }

    /**
     * Pushes background buffer to display buffer and repaints.
     */
    public void publish() {
        if (displayBuffer == null || backgroundBuffer == null) return;

        Graphics2D displayG = displayBuffer.createGraphics();
        displayG.setComposite(AlphaComposite.Clear);
        displayG.fillRect(0, 0, displayBuffer.getWidth(), displayBuffer.getHeight());
        displayG.setComposite(AlphaComposite.SrcOver);

        displayG.drawImage(backgroundBuffer, 0, 0, null);

        for (Drawable d : drawables) {
            d.draw(displayG);
        }

        displayG.dispose();

        Graphics2D bgG = backgroundBuffer.createGraphics();
        bgG.setComposite(AlphaComposite.Clear);
        bgG.fillRect(0, 0, backgroundBuffer.getWidth(), backgroundBuffer.getHeight());
        bgG.dispose();

        repaint();
    }

    public int getWorldWidth() {
        return getWidth();
    }

    public int getWorldHeight() {
        return getHeight();
    }
}