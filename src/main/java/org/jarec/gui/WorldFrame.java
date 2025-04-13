package org.jarec.gui;

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

public class WorldFrame extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(WorldFrame.class);

    private static WorldFrame instance;
    private WorldPanel world;
    private static JTextArea statsPanel;
    private JLabel statusBar;
    private JMenuItem startItem, pauseItem, stopItem;
    private JLabel splashImage;
    private JPanel wrapper;

    private static int nestCount = Integer.parseInt(PropertyHandler.get("gui.nest.default.number", "2"));
    private static int nestFood = Integer.parseInt(PropertyHandler.get("nest.default.setup.food", "100"));
    private String currentStatusMessage = "";
    private int messageTurnSet = -1;
    private static final int MESSAGE_DURATION_TURNS = 50;
    private boolean startup = true;


    // Private constructor ensures no auto-start
    private WorldFrame() throws HeadlessException {
        setTitle("Splurg World");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(
                Integer.parseInt(PropertyHandler.get("gui.frame.width", "800")),
                Integer.parseInt(PropertyHandler.get("gui.frame.height", "600"))
        );
        setLayout(new BorderLayout());

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

        setVisible(true);

        if (startup) {
            // Add this to your constructor after setVisible(true)
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
            splashImage.setHorizontalAlignment(JLabel.CENTER);
            splashImage.setVerticalAlignment(JLabel.CENTER);

            // Make sure the label doesn't stretch the image
            splashImage.setPreferredSize(new Dimension(scaledWidth, scaledHeight));

            // Use a wrapper panel with GridBagLayout for perfect centering
            wrapper = new JPanel(new GridBagLayout());
            wrapper.add(splashImage);

            // Remove any existing center component first
            Component center = ((BorderLayout)getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
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
        if (instance == null) {
            try {
                instance = new WorldFrame();
            } catch (HeadlessException e) {
                log.error("Error creating WorldFrame", e);
            }
        }
        return instance;
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
                GameLoop.getInstance().stop();
                updateStatus("Game stopped");
                updateMenuItemsState();
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
            // Remove the splash image and add the game world
            world = new WorldPanel();   // Create the game world panel
            add(world, BorderLayout.CENTER);  // Add the WorldPanel
            revalidate();  // Revalidate the frame to apply the changes
            repaint();  // Repaint the frame
            new GameStart(nestCount, nestFood);
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
            GameLoop.getInstance().stop();
            updateStatus("Game stopped");
            updateMenuItemsState();
        });

        gameMenu.add(startItem);
        gameMenu.add(pauseItem);
        gameMenu.add(stopItem);

        JMenu settingsMenu = new JMenu("Settings");

        // Create ButtonGroup for radio buttons
        ButtonGroup hiveGroup = new ButtonGroup();
        ButtonGroup foodGroup = new ButtonGroup();

        // Hive Configuration radio buttons
        JRadioButtonMenuItem twoHivesItem = new JRadioButtonMenuItem("2 Hives", nestCount == 2);
        twoHivesItem.addActionListener(e -> {
            if (twoHivesItem.isSelected()) {
                setNestCount(2);
            }
        });

        JRadioButtonMenuItem fourHivesItem = new JRadioButtonMenuItem("4 Hives", nestCount == 4);
        fourHivesItem.addActionListener(e -> {
            if (fourHivesItem.isSelected()) {
                setNestCount(4);
            }
        });

        // Add radio buttons to the ButtonGroup
        hiveGroup.add(twoHivesItem);
        hiveGroup.add(fourHivesItem);

        // Food setup radio buttons
        JRadioButtonMenuItem oneHundredFoodItem = new JRadioButtonMenuItem("100 Food", nestFood == 100);
        oneHundredFoodItem.addActionListener(e -> {
            if (oneHundredFoodItem.isSelected()) {
                setNestFood(100);
            }
        });

        JRadioButtonMenuItem twoHundredFoodItem = new JRadioButtonMenuItem("200 Food", nestFood == 200);
        twoHundredFoodItem.addActionListener(e -> {
            if (twoHundredFoodItem.isSelected()) {
                setNestFood(200);
            }
        });

        JRadioButtonMenuItem threeHundredFoodItem = new JRadioButtonMenuItem("300 Food", nestFood == 300);
        threeHundredFoodItem.addActionListener(e -> {
            if (threeHundredFoodItem.isSelected()) {
                setNestFood(300);
            }
        });

        JRadioButtonMenuItem fourHundredFoodItem = new JRadioButtonMenuItem("400 Food", nestFood == 400);
        fourHundredFoodItem.addActionListener(e -> {
            if (fourHundredFoodItem.isSelected()) {
                setNestFood(400);
            }
        });

        // Add food radio buttons to the ButtonGroup
        foodGroup.add(oneHundredFoodItem);
        foodGroup.add(twoHundredFoodItem);
        foodGroup.add(threeHundredFoodItem);
        foodGroup.add(fourHundredFoodItem);

        settingsMenu.add(twoHivesItem);
        settingsMenu.add(fourHivesItem);
        settingsMenu.addSeparator();
        settingsMenu.add(oneHundredFoodItem);
        settingsMenu.add(twoHundredFoodItem);
        settingsMenu.add(threeHundredFoodItem);
        settingsMenu.add(fourHundredFoodItem);

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

    private void setNestFood(int food) {
        nestFood = food;
    }

    private void setNestCount(int nests) {
        nestCount = nests;
    }

    private void openHelpWindow() {
        JTextArea helpText = new JTextArea();
        helpText.setText("""
                This is the help text for the Splurg World game.
                Use the Game menu to Start, Pause, or Stop the game.\n
                You can also use the following keys:
                Space Bar: Pause/Unpause
                Left and Right Arrow: Slow down and speed up the game
                Up Arrow: Returns the game to its default speed
                Esc: Ends the current game\n
                When paused you can mouse click on the world to see the
                details of the Splurgs in that area
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
            Developed by %s\n
            Welcome to Splurg World, a place populated by the Splurgs.
            Splurgs are Amoeba that just like to float about and fight. Fighting gives them energy that they can take
            back to their Hives to make more Splurgs. Splurgs can also sometimes spawn new Splurgs when they meet.
            """, version, author));
        aboutText.setEditable(false);
        aboutText.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(aboutText), "About", JOptionPane.INFORMATION_MESSAGE);
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
