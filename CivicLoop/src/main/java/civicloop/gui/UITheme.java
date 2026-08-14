package civicloop.gui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class UITheme {

    // ================== COLORS ==================
    public static final Color PRIMARY       = new Color(0x4361EE); // vivid indigo
    public static final Color PRIMARY_DARK  = new Color(0x3A0CA3); // deep violet-indigo
    public static final Color SECONDARY     = new Color(0x4CC9F0); // sky blue
    public static final Color ACCENT        = new Color(0x06D6A0); // teal-green

    public static final Color BACKGROUND    = new Color(0xF4F6FB); // app background
    public static final Color PANEL_BG      = Color.WHITE;
    public static final Color CARD_BG       = Color.WHITE;

    public static final Color BUTTON_BG     = PRIMARY;
    public static final Color BUTTON_FG     = Color.WHITE;
    public static final Color HEADER_FG     = Color.WHITE;

    public static final Color TABLE_ALT_ROW = new Color(0xF2F4FA);
    public static final Color TABLE_HEADER_BG = PRIMARY_DARK;

    public static final Color SUCCESS       = new Color(0x2ECC71); // green
    public static final Color WARNING       = new Color(0xFFB703); // amber
    public static final Color DANGER        = new Color(0xEF476F); // red-pink

    public static final Color TEXT_MAIN     = new Color(0x1B1F27);
    public static final Color TEXT_MUTED    = new Color(0x6C757D);
    public static final Color BORDER_COLOR  = new Color(0xDDE3ED);

    // ================== FONTS ==================
    public static final Font HEADER_FONT  = new Font("Segoe UI", Font.BOLD, 21);
    public static final Font TITLE_FONT   = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font LABEL_FONT   = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT  = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font SMALL_FONT   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font BADGE_FONT   = new Font("Segoe UI", Font.BOLD, 11);

    // ================== BORDERS ==================
    public static final Border PANEL_BORDER = BorderFactory.createEmptyBorder(15, 15, 15, 15);

    public static final Border COMPOUND_BORDER = BorderFactory.createCompoundBorder(
            new RoundedLineBorder(BORDER_COLOR, 1, 12),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
    );

    public static final Border TEXT_BORDER = BorderFactory.createCompoundBorder(
            new RoundedLineBorder(BORDER_COLOR, 1, 8),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
    );

    /** Build a rounded border with a custom color/radius on demand. */
    public static Border roundedBorder(int radius, Color line) {
        return BorderFactory.createCompoundBorder(
                new RoundedLineBorder(line, 1, radius),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
    }

    // ================== ROUNDED LINE BORDER ==================
    /**
     * A simple rounded-rectangle line border, drawn with anti-aliasing.
     * Use this instead of BorderFactory.createLineBorder for a modern look.
     */
    public static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radius;

        public RoundedLineBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(
                    x + thickness / 2f, y + thickness / 2f,
                    width - thickness, height - thickness,
                    radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness + 2, thickness + 2, thickness + 2, thickness + 2);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(thickness + 2, thickness + 2, thickness + 2, thickness + 2);
            return insets;
        }
    }

    // ================== ROUNDED BUTTON ==================
    /**
     * A flat, rounded, colorful button with hover/press shading.
     * Prefer this over styleButton() for a more modern feel.
     */
    public static class RoundedButton extends JButton {
        private Color baseColor;
        private int radius = 14;

        public RoundedButton(String text, Color baseColor) {
            super(text);
            this.baseColor = baseColor;
            setFont(BUTTON_FONT);
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { repaint(); }
                @Override public void mouseExited(MouseEvent e) { repaint(); }
                @Override public void mousePressed(MouseEvent e) { repaint(); }
                @Override public void mouseReleased(MouseEvent e) { repaint(); }
            });
        }

        public void setBaseColor(Color c) { this.baseColor = c; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = baseColor;
            if (getModel().isPressed()) {
                fill = baseColor.darker();
            } else if (getModel().isRollover()) {
                fill = brighten(baseColor, 0.12f);
            }

            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }

        private Color brighten(Color c, float factor) {
            int r = Math.min(255, (int) (c.getRed() + 255 * factor));
            int g = Math.min(255, (int) (c.getGreen() + 255 * factor));
            int b = Math.min(255, (int) (c.getBlue() + 255 * factor));
            return new Color(r, g, b);
        }
    }

    /** Factory helper: build a themed rounded button in one line. */
    public static RoundedButton createRoundedButton(String text, Color color) {
        return new RoundedButton(text, color);
    }

    // ================== ROUNDED PANEL (CARD) ==================
    /**
     * A JPanel that paints itself as a white rounded card with a soft border.
     * Useful for login forms, profile cards, post cards, etc.
     */
    public static class RoundedCardPanel extends JPanel {
        private int radius = 16;
        private Color bg = CARD_BG;

        public RoundedCardPanel() {
            setOpaque(false);
        }

        public void setRadius(int radius) { this.radius = radius; }
        public void setCardColor(Color c) { this.bg = c; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            g2.setColor(BORDER_COLOR);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================== STATUS BADGE ==================
    /**
     * Returns a small colored "pill" label — e.g. for table status columns
     * (Available / Borrowed / Busy) or trust levels.
     */
    public static JLabel statusBadge(String text, Color color) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 35));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(BADGE_FONT);
        badge.setForeground(color.darker());
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        return badge;
    }

    /** Pick a badge color automatically based on common status strings. */
    public static Color statusColor(String status) {
        if (status == null) return TEXT_MUTED;
        String s = status.toLowerCase();
        if (s.contains("available")) return SUCCESS;
        if (s.contains("borrowed") || s.contains("busy")) return WARNING;
        return TEXT_MUTED;
    }

    // ================== AVATAR CIRCLE ==================
    /**
     * Generates a small colored circle icon with the first letter of a name,
     * useful for user avatars in the feed, header, trust panel, etc.
     * Color is derived deterministically from the seed string (e.g. userId)
     * so the same user always gets the same color.
     */
    public static Icon avatarCircle(String name, String seed, int size) {
        Color[] palette = {
                new Color(0x4361EE), new Color(0xF72585), new Color(0x06D6A0),
                new Color(0xFFB703), new Color(0x7209B7), new Color(0x3A86FF),
                new Color(0xEF476F), new Color(0x2A9D8F)
        };
        int hash = (seed == null ? "?" : seed).hashCode();
        Color color = palette[Math.abs(hash) % palette.length];
        String letter = (name == null || name.isEmpty()) ? "?" : name.substring(0, 1).toUpperCase();

        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(x, y, size, size);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (size - fm.stringWidth(letter)) / 2;
                int ty = y + (size - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(letter, tx, ty);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    // ================== GRADIENT PANEL ==================
    /**
     * A JPanel that paints a diagonal gradient background between two colors.
     * Drop this in as a header or login-screen backdrop.
     */
    public static class GradientPanel extends JPanel {
        private final Color from, to;

        public GradientPanel(Color from, Color to) {
            this.from = from;
            this.to = to;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, from, getWidth(), getHeight(), to);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================== EMOJI-SAFE TEXT ==================
    /**
     * Combines an emoji icon with label text so the emoji renders in a
     * proper emoji-capable font ("Segoe UI Emoji") while the rest of the
     * text keeps the normal theme font. Works for JLabel, JButton, and
     * JTabbedPane tab titles since all of them support basic HTML rendering.
     *
     * Usage: UITheme.iconText("🔎", "Search by Owner:")
     */
    public static String iconText(String emoji, String text) {
        return "<html><span style='font-family:Segoe UI Emoji;'>" + emoji
                + "</span>&nbsp;" + text + "</html>";
    }

    // ================== LEGACY HELPERS (kept for compatibility) ==================

    /**
     * Style a plain JButton with our theme and a hover effect.
     * Kept for existing code (ItemPanel, ServicePanel, etc.) that still uses
     * plain JButton + UITheme.styleButton(). New code should prefer
     * createRoundedButton() for a nicer look.
     */
    public static void styleButton(JButton button) {
        button.setBackground(BUTTON_BG);
        button.setForeground(BUTTON_FG);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setBorderPainted(false);
        Color original = button.getBackground();
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(button.getBackground().darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(original);
            }
        });
    }

    /** Style a header label. */
    public static void styleHeader(JLabel label) {
        label.setFont(HEADER_FONT);
        label.setForeground(PRIMARY_DARK);
    }

    /** Style a panel background and border. */
    public static void stylePanel(JPanel panel) {
        panel.setBackground(BACKGROUND);
        panel.setBorder(PANEL_BORDER);
    }

    /** Apply global UI defaults for dialogs and tooltips to match theme. */
    public static void applyGlobalUI() {
        UIManager.put("OptionPane.background", PANEL_BG);
        UIManager.put("OptionPane.messageFont", LABEL_FONT);
        UIManager.put("OptionPane.buttonFont", BUTTON_FONT);
        UIManager.put("Panel.background", PANEL_BG);
        UIManager.put("OptionPane.border", PANEL_BORDER);
        UIManager.put("TextField.font", LABEL_FONT);
        UIManager.put("TextArea.font", LABEL_FONT);
        UIManager.put("Label.font", LABEL_FONT);
        UIManager.put("Button.font", BUTTON_FONT);
        UIManager.put("Table.font", LABEL_FONT);
        UIManager.put("Table.rowHeight", 32);
        UIManager.put("TableHeader.font", BUTTON_FONT);
        UIManager.put("List.font", LABEL_FONT);
        UIManager.put("TabbedPane.font", BUTTON_FONT);
        UIManager.put("ToolTip.background", BACKGROUND);
        UIManager.put("ToolTip.foreground", PRIMARY_DARK);
    }
}