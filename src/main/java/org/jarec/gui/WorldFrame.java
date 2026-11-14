package org.jarec.gui;

import org.jarec.data.Hive;
import org.jarec.game.GameEndState;
import org.jarec.game.GameLoop;
import org.jarec.game.GameStart;
import org.jarec.util.GameEvents;
import org.jarec.util.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

import static javax.swing.SwingConstants.CENTER;
import static org.jarec.game.GameEndCheck.determineEndState;

public class WorldFrame extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(WorldFrame.class);
    private static final String TURN_PREFIX = "Turn: ";
    private static final String STATUS_SEPARATOR = "      ";

    private static final WorldFrame INSTANCE = new WorldFrame();
    private WorldPanel world;
    private JLabel statusBar;
    private static JTextArea statsPanel;
    private JMenuItem startItem;
    private JMenuItem pauseItem;
    private JMenuItem stopItem;
    private JLabel splashImage;
    private JPanel wrapper;

    private static int hiveCount = PropertyHandler.getInt("gui.hive.default.number", 2);
    private static int hiveEnergy = PropertyHandler.getInt("hive.default.setup.energy", 100);
    private static volatile int maxPopulation = PropertyHandler.getInt("game.max.concurrent.medium", 750);
    private static boolean zombiesActive = PropertyHandler.getBoolean("game.default.zombie", false);
    private static boolean herdingActive = PropertyHandler.getBoolean("game.default.herding", false);
    private String currentStatusMessage = "";
    private int messageTurnSet = -1;
    private static final int MESSAGE_DURATION_TURNS = 50;
    private boolean startup = true;
    private final AtomicBoolean endGameShown = new AtomicBoolean();
    private static final LatestEdtUpdate<String> statsUpdate =
            new LatestEdtUpdate<>(value -> statsPanel.setText("Splurg World Stats\n" + value));
    private final transient LatestEdtUpdate<StatusUpdate> statusUpdate =
            new LatestEdtUpdate<>(this::applyStatusUpdate);
    private final transient LatestEdtUpdate<Integer> statusRefresh =
            new LatestEdtUpdate<>(this::applyStatusRefresh);

    private record StatusUpdate(int turn, String message) {}

    private WorldFrame() {
        setTitle("Splurg World");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(
                PropertyHandler.getInt("gui.frame.width", 800),
                PropertyHandler.getInt("gui.frame.height", 600)
        );
        setLayout(new BorderLayout());
        GameEvents.setStatusListener(this::updateStatus);
        getContentPane().setBackground(UiTheme.BACKGROUND);

        // Center the frame on the screen
        setLocationRelativeTo(null);

        statsPanel = new JTextArea();
        statsPanel.setText("Splurg World Stats");
        statsPanel.setEditable(false);
        statsPanel.setFocusable(false);
        statsPanel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statsPanel.setForeground(UiTheme.INK);
        statsPanel.setBackground(UiTheme.SURFACE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JScrollPane statsScroll = new JScrollPane(statsPanel);
        statsScroll.setPreferredSize(new Dimension(245, getHeight()));
        statsScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, UiTheme.BORDER),
                BorderFactory.createEmptyBorder(0, 1, 0, 0)));
        statsScroll.getViewport().setBackground(UiTheme.SURFACE);
        add(statsScroll, BorderLayout.EAST);

        setupKeyBindings();
        setupMenuBar();
        setupStatusBar();

        // Set custom window icon
        URL iconURL = getClass().getClassLoader().getResource("icon.png");
        if (iconURL != null) {
            Image iconImage = Toolkit.getDefaultToolkit().getImage(iconURL);
            setIconImage(iconImage);
        } else {
            log.warn("Custom icon image not found.");
        }

        setVisible(true);

        if (startup) {
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    // Only redraw if we're showing the splash screen
                    if (splashImage != null && splashImage.getParent() != null && world == null) {
                        displaySplashImage();
                    }
                }
            });
            displaySplashImage();
            startup = false;
        }
        log.info("World Frame created");
        statusBar.setText("Welcome to Splurg World");
    }

    private void displaySplashImage() {
        splashImage = new JLabel();
        URL imageUrl = getClass().getClassLoader().getResource("splash_image.png");

        if (imageUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imageUrl);
            Image originalImage = originalIcon.getImage();

            // Calculate available space (subtracting stats panel and status bar)
            int containerWidth = getContentPane().getWidth() - statsPanel.getWidth();
            int containerHeight = getContentPane().getHeight() - statusBar.getHeight();

            int originalWidth = originalIcon.getIconWidth();
            int originalHeight = originalIcon.getIconHeight();

            // Calculate scaling factor to fit inside container (never crop)
            double scale = Math.min(
                    (double) containerWidth / originalWidth,
                    (double) containerHeight / originalHeight
            );

            int scaledWidth = (int) (originalWidth * scale);
            int scaledHeight = (int) (originalHeight * scale);

            // Scale smoothly
            Image scaledImage = originalImage.getScaledInstance(
                    scaledWidth,
                    scaledHeight,
                    Image.SCALE_SMOOTH
            );

            // Create a new Icon with the scaled image
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            splashImage.setIcon(scaledIcon);

            // Center the label
            splashImage.setHorizontalAlignment(CENTER);
            splashImage.setVerticalAlignment(CENTER);

            // Make sure the label doesn't stretch the image
            splashImage.setPreferredSize(new Dimension(scaledWidth, scaledHeight));

            // Use a wrapper panel with GridBagLayout for perfect centering
            wrapper = new JPanel(new GridBagLayout());
            wrapper.add(splashImage);

            // Remove any existing center component first
            Component center = ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (center != null) {
                remove(center);
            }

            add(wrapper, BorderLayout.CENTER);
            revalidate();
            repaint();
        } else {
            log.error("Splash image not found!");
        }
    }

    public static synchronized WorldFrame getInstance() {
        return INSTANCE;
    }

    public static void updateStats(String stats) {
        statsUpdate.submit(stats);
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
                updateStatus("Game stopped");
                updateMenuItemsState();
                displayEndGamePanel(null);
            }
        });

        // LEFT arrow key
        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "leftArrowPressed");
        actionMap.put("leftArrowPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GameLoop.getInstance().setGameSpeed(5);
            }
        });

        // RIGHT arrow key
        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "rightArrowPressed");
        actionMap.put("rightArrowPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GameLoop.getInstance().setGameSpeed(-5);
            }
        });

        // UP arrow key
        inputMap.put(KeyStroke.getKeyStroke("UP"), "upArrowPressed");
        actionMap.put("upArrowPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GameLoop.getInstance().resetGameSpeed();
            }
        });
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER));

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
        exitItem.addActionListener(e -> {
            log.info("Exiting application...");
            System.exit(0);
        });
        fileMenu.add(exitItem);

        // Game menu
        JMenu gameMenu = new JMenu("Game");

        startItem = new JMenuItem("Start");
        startItem.addActionListener(e -> {
            endGameShown.set(false);
            world = new WorldPanel();
            add(world, BorderLayout.CENTER);
            revalidate();
            repaint();
            new GameStart(hiveCount, hiveEnergy, zombiesActive, herdingActive);
            remove(wrapper);
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
        stopItem.setAccelerator(KeyStroke.getKeyStroke("ESCAPE"));
        stopItem.addActionListener(e -> {
            updateStatus("Game stopped");
            updateMenuItemsState();
            displayEndGamePanel(null);
        });

        gameMenu.add(startItem);
        gameMenu.add(pauseItem);
        gameMenu.add(stopItem);

        JMenu settingsMenu = getSettingsMenu();

        // Help menu
        JMenu helpMenu = new JMenu("Help");

        JMenuItem helpItem = new JMenuItem("Help");
        helpItem.addActionListener(e -> openHelpWindow());

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> openAboutWindow());

        helpMenu.add(helpItem);
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(gameMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        updateMenuItemsState();
    }

    public void displayEndGamePanel(Hive winningHive) {
        if (!endGameShown.compareAndSet(false, true)) return;
        GameLoop.getInstance().stop();
        GameEndState endState = determineEndState(winningHive);
        SwingUtilities.invokeLater(() -> {
            WinnerPanel.createAndShowWinnerPanel(endState);
            updateMenuItemsState();
        });
    }

    private static JMenu getSettingsMenu() {
        JMenu settingsMenu = new JMenu("Settings");

        ButtonGroup hiveGroup = new ButtonGroup();
        ButtonGroup energyGroup = new ButtonGroup();
        ButtonGroup populationGroup = new ButtonGroup();

        int defaultHiveCount = PropertyHandler.getInt("gui.hive.default.number", 2);
        int defaultHiveEnergy = PropertyHandler.getInt("hive.default.setup.energy", 100);
        int defaultPopulation = PropertyHandler.getInt("game.max.concurrent.medium", 750);

        addRadioItems(settingsMenu, hiveGroup,
                new int[]{2, 3, 4},
                defaultHiveCount,
                count -> count + " Hives",
                WorldFrame::setHiveCount
        );

        settingsMenu.addSeparator();

        addRadioItems(settingsMenu, energyGroup,
                new int[]{100, 200, 300, 400},
                defaultHiveEnergy,
                energy -> energy + " Energy",
                WorldFrame::setHiveEnergy
        );

        settingsMenu.addSeparator();

        int[] populationLevels = {
                PropertyHandler.getInt("game.max.concurrent.low", 500),
                defaultPopulation,
                PropertyHandler.getInt("game.max.concurrent.high", 1000),
                PropertyHandler.getInt("game.max.concurrent.extreme", 1500)
        };
        String[] populationLabels = {
                "Max Population Low", "Max Population Medium", "Max Population High", "Max Population Extreme"
        };

        for (int i = 0; i < populationLevels.length; i++) {
            int level = populationLevels[i];
            boolean isSelected = level == defaultPopulation;
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(populationLabels[i], isSelected);
            item.addActionListener(e -> {
                if (item.isSelected()) setMaxPopulation(level);
            });
            populationGroup.add(item);
            settingsMenu.add(item);
        }

        settingsMenu.addSeparator();

        JCheckBoxMenuItem herdingToggle = new JCheckBoxMenuItem("Herding", herdingActive);
        herdingToggle.addActionListener(e -> herdingActive = herdingToggle.isSelected());
        settingsMenu.add(herdingToggle);

        settingsMenu.addSeparator();

        JCheckBoxMenuItem zombiesToggle = new JCheckBoxMenuItem("Zombies", zombiesActive);
        zombiesToggle.addActionListener(e -> zombiesActive = zombiesToggle.isSelected());
        settingsMenu.add(zombiesToggle);

        return settingsMenu;
    }


    private static void addRadioItems(JMenu menu, ButtonGroup group, int[] values, int defaultValue,
                                      IntFunction<String> labelFunc, IntConsumer actionFunc) {
        for (int val : values) {
            boolean selected = val == defaultValue;
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(labelFunc.apply(val), selected);
            item.addActionListener(e -> {
                if (item.isSelected()) actionFunc.accept(val);
            });
            group.add(item);
            menu.add(item);
        }
    }


    private static void setHiveEnergy(int energy) {
        hiveEnergy = energy;
    }

    private static void setMaxPopulation(int population) {
        maxPopulation = population;
    }

    public static int getMaxPopulation() {
        return maxPopulation;
    }

    private static void setHiveCount(int hives) {
        hiveCount = hives;
    }

    private void openHelpWindow() {
        UiTheme.showInfoDialog(this, "How to play", "Set up a world, then see which colony survives", """
                START A WORLD
                Choose the number of hives, their starting energy, and a population limit in Settings. You can also enable herding or zombies. Select Start from the Game menu when you are ready.

                WHAT TO WATCH
                Splurgs wander, raid rival hives for energy, and fight rival creatures. Energy helps them recover and reproduce. Their traits shape how they forage, fight, and follow leaders. A colony wins when it is the last one with living Splurgs.

                CONTROLS
                Space                  Pause or resume
                Esc                    End the game and show the result
                Left / Right Arrow     Slow down / speed up
                Up Arrow               Restore the default speed

                INSPECT YOUR WORLD
                Pause the simulation, then click near Splurgs to see who is there. Select a creature in the list to inspect its traits and status. Click the winner image or press Escape to close it.
                """);
    }

    private void openAboutWindow() {
        var author = PropertyHandler.get("author", "Jarec");
        var version = PropertyHandler.get("version", "0.0");
        UiTheme.showInfoDialog(this, "Splurg World", "Version " + version + "  ·  Created by " + author, """
                A living world, one simulation tick at a time.

                Splurgs are small, evolving creatures with distinct traits. They roam the world, search for food and energy, raid rival hives, and fight. When conditions are right, they reproduce and pass some shared traits to their offspring.

                Each colony depends on its hive and the energy its creatures bring home. Age, injury, and changing fortunes can thin a population. Turn on herding to encourage social behavior, or introduce zombies to see how infection reshapes the contest.
                """);
    }

    private void updateMenuItemsState() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::updateMenuItemsState);
            return;
        }

        boolean isRunning = GameLoop.getInstance().isRunning();
        boolean isStarted = GameLoop.getInstance().isStarted();

        startItem.setEnabled(!isStarted);
        pauseItem.setEnabled(isStarted);
        stopItem.setEnabled(isStarted);

        if (GameLoop.getInstance().isPaused()) {
            pauseItem.setText("Resume");
        } else {
            pauseItem.setText("Pause");
        }
    }

    private void setupStatusBar() {
        statusBar = new JLabel("");
        statusBar.setOpaque(true);
        statusBar.setBackground(UiTheme.SURFACE);
        statusBar.setForeground(UiTheme.MUTED);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        add(statusBar, BorderLayout.SOUTH);
    }

    public void updateStatus(String message) {
        log.info(message);
        statusUpdate.submit(new StatusUpdate(GameLoop.getInstance().getTurn(), message));
    }

    private void applyStatusUpdate(StatusUpdate update) {
        currentStatusMessage = update.message();
        messageTurnSet = update.turn();
        statusBar.setText(TURN_PREFIX + update.turn() + STATUS_SEPARATOR + update.message());
    }

    public void refreshStatus() {
        statusRefresh.submit(GameLoop.getInstance().getTurn());
    }

    private void applyStatusRefresh(int currentTurn) {
        if (currentTurn - messageTurnSet < MESSAGE_DURATION_TURNS) {
            statusBar.setText(TURN_PREFIX + currentTurn + STATUS_SEPARATOR + currentStatusMessage);
        } else {
            currentStatusMessage = "";
            statusBar.setText(TURN_PREFIX + currentTurn + STATUS_SEPARATOR);
        }
    }

    public WorldPanel getWorldPanel() {
        return world;
    }
}
