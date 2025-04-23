package org.jarec.gui;

import org.jarec.data.Hive;
import org.jarec.game.GameEndState;
import org.jarec.game.GameLoop;
import org.jarec.game.GameStart;
import org.jarec.util.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

import static javax.swing.SwingConstants.CENTER;
import static org.jarec.game.GameEndCheck.determineEndState;

public class WorldFrame extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(WorldFrame.class);

    private static final WorldFrame INSTANCE = new WorldFrame();
    private WorldPanel world;
    private static JTextArea statsPanel;
    private JLabel statusBar;
    private JMenuItem startItem;
    private JMenuItem pauseItem;
    private JMenuItem stopItem;
    private JLabel splashImage;
    private JPanel wrapper;

    private static int hiveCount = Integer.parseInt(PropertyHandler.get("gui.hive.default.number", "2"));
    private static int hiveEnergy = Integer.parseInt(PropertyHandler.get("hive.default.setup.energy", "100"));
    private static int maxPopulation = Integer.parseInt(PropertyHandler.get("game.max.concurrent.medium", "750"));
    private static boolean zombiesActive = Boolean.parseBoolean(PropertyHandler.get("game.default.zombie", "false"));
    private String currentStatusMessage = "";
    private int messageTurnSet = -1;
    private static final int MESSAGE_DURATION_TURNS = 50;
    private boolean startup = true;


    // Private constructor ensures no auto-start
    private WorldFrame() {
        setTitle("Splurg World");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(
                Integer.parseInt(PropertyHandler.get("gui.frame.width", "800")),
                Integer.parseInt(PropertyHandler.get("gui.frame.height", "600"))
        );
        setLayout(new BorderLayout());

        // Center the frame on the screen
        setLocationRelativeTo(null);

        statsPanel = new JTextArea();
        statsPanel.append("Splurg World Stats");
        statsPanel.setEditable(false);
        statsPanel.setFocusable(false);
        statsPanel.setPreferredSize(new Dimension(200, getHeight()));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        add(statsPanel, BorderLayout.EAST);

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
        var output = "Splurg World Stats\n";
        statsPanel.setText(output + stats);
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
            world = new WorldPanel();
            add(world, BorderLayout.CENTER);
            revalidate();
            repaint();
            new GameStart(hiveCount, hiveEnergy, zombiesActive);
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
        GameLoop.getInstance().stop();
        GameEndState endState = determineEndState(winningHive);
        SwingUtilities.invokeLater(() -> WinnerPanel.createAndShowWinnerPanel(endState));
        updateMenuItemsState();
    }

    private static JMenu getSettingsMenu() {
        JMenu settingsMenu = new JMenu("Settings");

        ButtonGroup hiveGroup = new ButtonGroup();
        ButtonGroup energyGroup = new ButtonGroup();
        ButtonGroup populationGroup = new ButtonGroup();

        int defaultHiveCount = Integer.parseInt(PropertyHandler.get("gui.hive.default.number", "2"));
        int defaultHiveEnergy = Integer.parseInt(PropertyHandler.get("hive.default.setup.energy", "100"));
        int defaultPopulation = Integer.parseInt(PropertyHandler.get("game.max.concurrent.medium", "750"));

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
                Integer.parseInt(PropertyHandler.get("game.max.concurrent.low", "500")),
                defaultPopulation,
                Integer.parseInt(PropertyHandler.get("game.max.concurrent.high", "1000")),
                Integer.parseInt(PropertyHandler.get("game.max.concurrent.extreme", "1500"))
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
        JTextArea helpText = new JTextArea();
        helpText.setText("""
                How to run Splurg World.
                                
                Use the Game menu to Start, Pause, or Stop the game.
                You can also use the following keys:
                Space Bar: Pause/Unpause
                Left and Right Arrow: Slow down and speed up the game
                Up Arrow: Returns the game to its default speed
                Esc: Ends the current game
                When paused you can mouse click on the world to see the
                details of the Splurgs in that area
                                
                You can use the Settings Menu to choose how many Hives
                to start the game with and their starting Energy.
                """);
        helpText.setEditable(false);
        helpText.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(helpText), "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openAboutWindow() {
        var author = PropertyHandler.get("author", "Jarec");
        var version = PropertyHandler.get("version", "0.0");
        JTextArea aboutText = new JTextArea();
        aboutText.setText(String.format("""
                Splurg World
                Version %s
                Developed by %s
                            
                Welcome to Splurg World, a place populated by the Splurgs.
                Splurgs are Amoeba that just like to float about, raid each
                others Hives and fight. Fighting and raiding gives them
                energy that they can take back to their Hives to make more
                Splurgs. Splurgs can also sometimes spawn new Splurgs when
                they meet.
                """, version, author));
        aboutText.setEditable(false);
        aboutText.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(aboutText), "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateMenuItemsState() {
        boolean isRunning = GameLoop.getInstance().isRunning();
        boolean isStarted = GameLoop.getInstance().isStarted();

        startItem.setEnabled(!isStarted || !isRunning);
        pauseItem.setEnabled(isStarted);
        stopItem.setEnabled(isStarted);

        if (GameLoop.getInstance().isPaused()) {
            pauseItem.setText("Resume");
        } else {
            pauseItem.setText("Pause");
        }
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
        currentStatusMessage = message;
        messageTurnSet = GameLoop.getInstance().getTurn();
        statusBar.setText(getTurn() + message);
        log.info(message);
    }

    public void refreshStatus() {
        int currentTurn = GameLoop.getInstance().getTurn();
        if (currentTurn - messageTurnSet < MESSAGE_DURATION_TURNS) {
            statusBar.setText(getTurn() + currentStatusMessage);
        } else {
            currentStatusMessage = "";
            statusBar.setText(getTurn());
        }
    }

    public WorldPanel getWorldPanel() {
        return world;
    }
}
