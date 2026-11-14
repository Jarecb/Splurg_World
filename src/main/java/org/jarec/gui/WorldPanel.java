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
        setBackground(UiTheme.BACKGROUND);
        setOpaque(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (GameLoop.getInstance().isPaused()) {
                    Location clickLocation = new Location(e.getX(), e.getY());
                    List<Splurg> localSplurgs = Splurgs.getInstance().getSplurgsInVicinity(clickLocation);

                    if (!localSplurgs.isEmpty()) {
                        showSplurgPopup(e, localSplurgs);
                    }
                }
            }
        });
    }

    private void showSplurgPopup(MouseEvent event, List<Splurg> splurgs) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JMenuItem heading = new JMenuItem("Nearby Splurgs  ·  " + splurgs.size());
        heading.setEnabled(false);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD));
        popup.add(heading);
        popup.addSeparator();
        for (Splurg splurg : splurgs) {
            popup.add(createSplurgMenuItem(splurg));
        }
        popup.show(this, event.getX(), event.getY());
    }

    private JMenuItem createSplurgMenuItem(Splurg splurg) {
        JMenuItem item = new JMenuItem(splurg.getName() + "  ·  " + splurg.getHomeHive().getName()
                + "  ·  Health " + splurg.getHealth());
        item.setToolTipText("View this Splurg's attributes and current state");
        item.addActionListener(event -> showSplurgDetails(splurg));
        return item;
    }

    private void showSplurgDetails(Splurg splurg) {
        String details = """
                Colony: %s
                Health: %d
                Energy: %d

                Aggression: %d
                Foraging: %d
                Strength: %d
                Toughness: %d
                Size: %d
                Speed: %d
                Charisma: %d
                Loner: %d

                Position: %d, %d
                """.formatted(
                splurg.getHomeHive().getName(), splurg.getHealth(), splurg.getEnergy(),
                splurg.getAggression().getValue(), splurg.getForaging().getValue(),
                splurg.getStrength().getValue(), splurg.getToughness().getValue(),
                splurg.getSize().getValue(), splurg.getSpeed().getValue(),
                splurg.getCharisma().getValue(), splurg.getLoner().getValue(),
                splurg.getLocation().getX(), splurg.getLocation().getY());
        UiTheme.showInfoDialog(SwingUtilities.getWindowAncestor(this), splurg.getName(),
                "Splurg status and traits", details);
    }

    @Override
    protected synchronized void paintComponent(Graphics g) {
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

    public synchronized Graphics2D getBackgroundGraphics() {
        if (backgroundBuffer == null) {
            backgroundBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        }
        return backgroundBuffer.createGraphics();
    }

    /**
     * Pushes background buffer to display buffer and repaints.
     */
    public synchronized void publish() {
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