package civicloop;

import civicloop.gui.LoginFrame;
import civicloop.gui.UITheme;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UITheme.applyGlobalUI();   // <-- apply theme to all components
            new LoginFrame().setVisible(true);
        });
    }
}