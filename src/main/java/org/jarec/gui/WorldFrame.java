package org.jarec.gui;

import javax.swing.*;
import java.awt.*;

public class WorldFrame extends JFrame {
    private WorldPanel world;

    public WorldFrame() throws HeadlessException {
        setTitle("Splurg World");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new GridLayout(1, 2));

        world = new WorldPanel();

        add(world);
        setVisible(true);
    }
}
