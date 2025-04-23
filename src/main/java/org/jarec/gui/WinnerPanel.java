package org.jarec.gui;

import org.jarec.game.GameEndState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class WinnerPanel {
    private static final Logger log = LoggerFactory.getLogger(WinnerPanel.class);

    private WinnerPanel(){}

    public static void createAndShowWinnerPanel(GameEndState gameEndStata) {
        try {
            BufferedImage image = loadImageFromResources(gameEndStata.getfileName());

            if (image != null && (image.getWidth() != 512 || image.getHeight() != 512)) {
                log.error("Image must be 512x512 pixels");
            }

            JPanel panel = getWinnerPanel(image);

            JDialog dialog = new JDialog(WorldFrame.getInstance(), "Image Viewer", true);
            dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            dialog.setUndecorated(true);
            dialog.getContentPane().add(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

        } catch (IOException e) {
            log.error("Failed to load image: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        } catch (NullPointerException e) {
            log.error("Failed to load the required image");
        }
    }

    private static JPanel getWinnerPanel(BufferedImage image) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, this);
            }
        };
        panel.setPreferredSize(new Dimension(512, 512));

        // Add mouse listener to close on click
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Window window = SwingUtilities.getWindowAncestor(panel);
                if (window != null) {
                    window.dispose();
                }
            }
        });
        return panel;
    }

    private static BufferedImage loadImageFromResources(String path) throws IOException {
        // Debugging output
        log.debug("Attempting to load: {}", path);
        URL resourceUrl = WinnerPanel.class.getClassLoader().getResource(path);
        log.debug("Resource URL: {}", resourceUrl);

        if (resourceUrl == null) {
            log.error("Image not found at: {} Make sure it's in your resources folder and the path is correct.", path);
            return null;
        }

        try (InputStream inputStream = resourceUrl.openStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                log.error("Failed to read image (possibly corrupt or wrong format)");
            }
            return image;
        }
    }
}