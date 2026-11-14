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
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> createAndShowWinnerPanel(gameEndStata));
            return;
        }
        try {
            BufferedImage image = loadImageFromResources(gameEndStata.getfileName());
            if (image == null) {
                log.error("Winner image could not be loaded: {}", gameEndStata.getfileName());
                return;
            }
            if (image.getWidth() != 512 || image.getHeight() != 512) {
                log.error("Image must be 512x512 pixels");
            }

            WinnerContent content = getWinnerPanel(image, gameEndStata);

            JDialog dialog = new JDialog(WorldFrame.getInstance(), "Winner", false);
            dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            dialog.setResizable(false);
            dialog.getContentPane().add(content.panel());
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.getRootPane().setDefaultButton(content.closeButton());
            dialog.getRootPane().registerKeyboardAction(
                    event -> dialog.dispose(),
                    KeyStroke.getKeyStroke("ESCAPE"),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);
            dialog.setVisible(true);

        } catch (IOException e) {
            log.error("Failed to load winner image: {}", gameEndStata.getfileName(), e);
        } catch (IllegalArgumentException e) {
            log.error("Could not display winner image: {}", gameEndStata.getfileName(), e);
        }
    }

    private record WinnerContent(JPanel panel, JButton closeButton) {}

    private static WinnerContent getWinnerPanel(BufferedImage image, GameEndState state) {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(UiTheme.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(18, 18, 14, 18)));

        JPanel titleBlock = new JPanel(new BorderLayout(0, 4));
        titleBlock.setBackground(UiTheme.SURFACE);
        titleBlock.add(UiTheme.heading(getWinnerTitle(state)), BorderLayout.NORTH);
        titleBlock.add(UiTheme.caption("The world has reached its end state"), BorderLayout.SOUTH);

        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, this);
            }
        };
        imagePanel.setBackground(UiTheme.BACKGROUND);
        imagePanel.setPreferredSize(new Dimension(512, 512));
        imagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                closeDialog(panel);
            }
        });

        JButton close = new JButton("Close");
        close.addActionListener(event -> closeDialog(panel));
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.add(UiTheme.caption("Click the image or press Escape to close"), BorderLayout.WEST);
        actions.add(close, BorderLayout.EAST);

        panel.add(titleBlock, BorderLayout.NORTH);
        panel.add(imagePanel, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                closeDialog(panel);
            }
        });
        return new WinnerContent(panel, close);
    }

    private static String getWinnerTitle(GameEndState state) {
        return switch (state) {
            case BLUE -> "Blue colony wins";
            case GREEN -> "Green colony wins";
            case RED -> "Red colony wins";
            case YELLOW -> "Yellow colony wins";
            case ZOMBIE -> "The zombies take over";
            case STALEMATE -> "A world in balance";
        };
    }

    private static void closeDialog(Component component) {
        Window window = SwingUtilities.getWindowAncestor(component);
        if (window != null) window.dispose();
    }

    private static BufferedImage loadImageFromResources(String path) throws IOException {
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