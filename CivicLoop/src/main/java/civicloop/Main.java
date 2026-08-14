package civicloop;

import civicloop.gui.LoginFrame;
import civicloop.gui.UITheme;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        // Apply FlatLaf modern Look & Feel
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            UITheme.applyGlobalUI();
            new LoginFrame().setVisible(true);
        });
    }
}