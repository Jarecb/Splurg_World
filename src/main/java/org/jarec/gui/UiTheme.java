package org.jarec.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Window;

public final class UiTheme {
    private static final String FONT_FAMILY = "SansSerif";

    public static final Color BACKGROUND = new Color(244, 247, 251);
    public static final Color SURFACE = Color.WHITE;
    public static final Color INK = new Color(34, 45, 59);
    public static final Color MUTED = new Color(103, 116, 132);
    public static final Color ACCENT = new Color(50, 105, 166);
    public static final Color BORDER = new Color(220, 227, 235);

    private UiTheme() {}

    public static void install() {
        Font regular = new Font(FONT_FAMILY, Font.PLAIN, 13);
        Font bold = regular.deriveFont(Font.BOLD);
        for (String key : new String[]{
                "Label.font", "Button.font", "ToggleButton.font", "Menu.font", "MenuItem.font",
                "CheckBoxMenuItem.font", "RadioButtonMenuItem.font", "TextArea.font",
                "OptionPane.messageFont", "OptionPane.buttonFont", "TabbedPane.font"
        }) {
            UIManager.put(key, regular);
        }
        UIManager.put("Label.foreground", INK);
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", INK);
        UIManager.put("MenuBar.background", SURFACE);
        UIManager.put("MenuBar.foreground", INK);
        UIManager.put("Menu.background", SURFACE);
        UIManager.put("Menu.foreground", INK);
        UIManager.put("MenuItem.background", SURFACE);
        UIManager.put("MenuItem.foreground", INK);
        UIManager.put("MenuItem.selectionBackground", new Color(229, 239, 249));
        UIManager.put("MenuItem.selectionForeground", INK);
        UIManager.put("CheckBoxMenuItem.font", regular);
        UIManager.put("RadioButtonMenuItem.font", regular);
        UIManager.put("Button.font", bold);
        UIManager.put("Button.background", ACCENT);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.margin", new Insets(8, 16, 8, 16));
        UIManager.put("TextArea.background", SURFACE);
        UIManager.put("TextArea.foreground", INK);
        UIManager.put("PopupMenu.background", SURFACE);
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(BORDER));
        UIManager.put("ToolTip.background", INK);
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("defaultFont", regular);
    }

    public static void styleTextArea(JTextArea textArea) {
        textArea.setFont(new Font(FONT_FAMILY, Font.PLAIN, 13));
        textArea.setForeground(INK);
        textArea.setBackground(SURFACE);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        textArea.setMargin(new Insets(0, 0, 0, 0));
    }

    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(FONT_FAMILY, Font.BOLD, 20));
        label.setForeground(INK);
        return label;
    }

    public static JLabel caption(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(FONT_FAMILY, Font.PLAIN, 12));
        label.setForeground(MUTED);
        return label;
    }

    public static void showInfoDialog(Window owner, String title, String subtitle, String content) {
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(SURFACE);
        body.setBorder(BorderFactory.createEmptyBorder(22, 24, 18, 24));

        JPanel titleBlock = new JPanel(new BorderLayout(0, 5));
        titleBlock.setBackground(SURFACE);
        titleBlock.add(heading(title), BorderLayout.NORTH);
        titleBlock.add(caption(subtitle), BorderLayout.SOUTH);

        JTextArea message = new JTextArea(content);
        message.setEditable(false);
        message.setFocusable(false);
        message.setColumns(64);
        message.setRows(estimateRows(content, 64));
        styleTextArea(message);
        JScrollPane contentScroll = new JScrollPane(message);
        contentScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        contentScroll.getViewport().setBackground(SURFACE);
        contentScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JButton close = new JButton("Close");
        close.addActionListener(event -> dialog.dispose());
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.add(close, BorderLayout.EAST);

        body.add(titleBlock, BorderLayout.NORTH);
        body.add(contentScroll, BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(body);
        dialog.getRootPane().setDefaultButton(close);
        dialog.getRootPane().registerKeyboardAction(
                event -> dialog.dispose(),
                javax.swing.KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(620, 280));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private static int estimateRows(String content, int columns) {
        int rows = 0;
        for (String line : content.split("\\R", -1)) {
            rows += Math.max(1, (int) Math.ceil(line.length() / (double) columns));
        }
        return Math.max(8, rows);
    }
}
