package civicloop.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UITheme {

    // ---- Colors ----
    public static final Color PRIMARY = new Color(0x2C3E50);          // dark blue
    public static final Color SECONDARY = new Color(0x5D8AA8);        // soft blue
    public static final Color ACCENT = new Color(0x6A9C89);           // muted green
    public static final Color BACKGROUND = new Color(0xF8F9FA);       // light gray
    public static final Color PANEL_BG = Color.WHITE;
    public static final Color BUTTON_BG = SECONDARY;
    public static final Color BUTTON_FG = Color.WHITE;
    public static final Color HEADER_FG = PRIMARY;
    public static final Color TABLE_ALT_ROW = new Color(0xF2F4F6);
    public static final Color SUCCESS = new Color(0x38A169);          // green
    public static final Color WARNING = new Color(0xED8936);          // orange
    public static final Color DANGER = new Color(0xE53E3E);           // red
    public static final Color BORDER_COLOR = new Color(0xCBD5E0);     // light gray

    // ---- Fonts ----
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    // ---- Borders ----
    public static final Border PANEL_BORDER = BorderFactory.createEmptyBorder(15, 15, 15, 15);
    public static final Border COMPOUND_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
    );
    public static final Border TEXT_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
    );

    /**
     * Style a button with our theme and a hover effect.
     */
    public static void styleButton(JButton button) {
        button.setBackground(BUTTON_BG);
        button.setForeground(BUTTON_FG);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(button.getBackground().darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(BUTTON_BG);
            }
        });
    }

    /**
     * Style a header label.
     */
    public static void styleHeader(JLabel label) {
        label.setFont(HEADER_FONT);
        label.setForeground(HEADER_FG);
    }

    /**
     * Style a panel background and border.
     */
    public static void stylePanel(JPanel panel) {
        panel.setBackground(PANEL_BG);
        panel.setBorder(PANEL_BORDER);
    }

    /**
     * Apply global UI defaults for dialogs and tooltips to match theme.
     */
    public static void applyGlobalUI() {
        UIManager.put("OptionPane.background", PANEL_BG);
        UIManager.put("OptionPane.messageFont", LABEL_FONT);
        UIManager.put("OptionPane.buttonFont", BUTTON_FONT);
        UIManager.put("OptionPane.border", PANEL_BORDER);
        UIManager.put("TextField.font", LABEL_FONT);
        UIManager.put("TextArea.font", LABEL_FONT);
        UIManager.put("Label.font", LABEL_FONT);
        UIManager.put("Button.font", BUTTON_FONT);
        UIManager.put("Table.font", LABEL_FONT);
        UIManager.put("TableHeader.font", BUTTON_FONT);
        UIManager.put("List.font", LABEL_FONT);
        UIManager.put("TabbedPane.font", BUTTON_FONT);
        UIManager.put("ToolTip.background", BACKGROUND);
        UIManager.put("ToolTip.foreground", PRIMARY);
    }
}