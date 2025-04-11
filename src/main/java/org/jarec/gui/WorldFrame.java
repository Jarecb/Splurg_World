package org.jarec.gui;

import org.jarec.data.Nest;
import org.jarec.game.GameLoop;
import org.jarec.game.GameStart;
import org.jarec.game.resources.Splurgs;
import org.jarec.util.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Map;

public class WorldFrame extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(WorldFrame.class);

    private static WorldFrame instance;
    private WorldPanel world;
    private JLabel statusBar;
    private JMenuItem startItem, pauseItem, stopItem;

    // Private constructor ensures no auto-start
    private WorldFrame() throws HeadlessException {
        setTitle("Splurg World");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(
                Integer.parseInt(PropertyHandler.get("gui.frame.width", "800")),
                Integer.parseInt(PropertyHandler.get("gui.frame.height", "600"))
        );
        setLayout(new BorderLayout());

        world = new WorldPanel();
        add(world, BorderLayout.CENTER);

        setupKeyBindings();
        setupMenuBar();
        setupStatusBar();

        setVisible(true);
        log.info("World Frame created");
        statusBar.setText("Welcome to Splurg World");
    }

    public static synchronized WorldFrame getInstance() {
        if (instance == null) {
            try {
                instance = new WorldFrame();
            } catch (HeadlessException e) {
                log.error("Error creating WorldFrame", e);
            }
        }
        return instance;
    }

    private void setupKeyBindings() {
        JRootPane root = getRootPane();

        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = root.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("SPACE"), "spacePressed");
        actionMap.put("spacePressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info("Space bar pressed!");
                GameLoop.getInstance().pause();
                updateStatus(GameLoop.getInstance().isRunning() ? "Running" : "Paused");
                updateMenuItemsState();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "escapePressed");
        actionMap.put("escapePressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info("Escape key pressed. Stopping game loop.");
                GameLoop.getInstance().stop();
                updateStatus("Game stopped");
                updateMenuItemsState();
            }
        });
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            log.info("Exiting application...");
            System.exit(0);
        });
        fileMenu.add(exitItem);

        // Game menu
        JMenu gameMenu = new JMenu("Game");

        startItem = new JMenuItem("Start");
        startItem.addActionListener(e -> {
            new GameStart();
            updateStatus("Game started");
            updateMenuItemsState();
        });

        pauseItem = new JMenuItem("Pause/Resume");
        pauseItem.addActionListener(e -> {
            GameLoop.getInstance().pause();
            updateStatus(GameLoop.getInstance().isRunning() ? "Running" : "Paused");
            updateMenuItemsState();
        });

        stopItem = new JMenuItem("Stop");
        stopItem.addActionListener(e -> {
            GameLoop.getInstance().stop();
            updateStatus("Game stopped");
            updateMenuItemsState();
        });

        gameMenu.add(startItem);
        gameMenu.add(pauseItem);
        gameMenu.add(stopItem);

        menuBar.add(fileMenu);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);

        updateMenuItemsState();
    }

    private void updateMenuItemsState() {
        if (GameLoop.getInstance().isStarted()) {
            startItem.setEnabled(false);
            pauseItem.setEnabled(true);
            stopItem.setEnabled(true);
        } else {
            startItem.setEnabled(true);
            pauseItem.setEnabled(false);
            stopItem.setEnabled(false);
        }

        if (GameLoop.getInstance().isPaused()) {
            pauseItem.setText("Resume");
        } else {
            pauseItem.setText("Pause");
        }
    }

    private String getStats(){
        Map<Nest, Long> counts = Splurgs.getInstance().getCounts();
        StringBuilder stats = new StringBuilder();

        counts.forEach((nest, count) -> {
            if (stats.length() > 0) {
                stats.append(" | ");
            }
            stats.append(nest.getName()).append(": ").append(count);
        });

        stats.append("      ");

        return stats.toString();
    }

    private String getTurn() {
        return "Turn: " + GameLoop.getInstance().getTurn() + "      ";
    }

    private void setupStatusBar() {
        statusBar = new JLabel("");
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusBar, BorderLayout.SOUTH);
    }

    public void updateStatus(String message) {
        statusBar.setText(getTurn() + getStats() + message);
    }

    public WorldPanel getWorldPanel(){
        return world;
    }
}
