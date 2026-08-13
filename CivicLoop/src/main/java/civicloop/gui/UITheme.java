package civicloop.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Central UI theme to keep styling consistent and easy to change.
 */
public class UITheme {

    // Colours
    public static final Color PRIMARY = new Color(0x2C3E50);
    public static final Color SECONDARY = new Color(0x3498DB);
    public static final Color ACCENT = new Color(0x1ABC9C);
    public static final Color BACKGROUND = new Color(0xECF0F1);
    public static final Color PANEL_BG = Color.WHITE;
    public static final Color BUTTON_BG = SECONDARY;
    public static final Color BUTTON_FG = Color.WHITE;
    public static final Color HEADER_FG = PRIMARY;
    public static final Color TABLE_ALT_ROW = new Color(0xF2F2F2);
    public static final Color SUCCESS = new Color(0x27AE60);
    public static final Color WARNING = new Color(0xF39C12);
    public static final Color DANGER = new Color(0xE74C3C);

    // Fonts
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 13);

    // Borders
    public static final Border PANEL_BORDER = BorderFactory.createEmptyBorder(10, 10, 10, 10);
    public static final Border COMPOUND_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xBDC3C7), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
    );

    /**
     * Applies a consistent button style.
     */
    public static void styleButton(JButton button) {
        button.setBackground(BUTTON_BG);
        button.setForeground(BUTTON_FG);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Applies a consistent header label style.
     */
    public static void styleHeader(JLabel label) {
        label.setFont(HEADER_FONT);
        label.setForeground(HEADER_FG);
    }

    /**
     * Applies a consistent panel background.
     */
    public static void stylePanel(JPanel panel) {
        panel.setBackground(PANEL_BG);
        panel.setBorder(PANEL_BORDER);
    }
}