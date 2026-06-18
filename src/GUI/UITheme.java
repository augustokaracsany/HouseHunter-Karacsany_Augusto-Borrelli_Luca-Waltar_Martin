package GUI;

import javax.swing.*;
import java.awt.*;

public class UITheme {
    // Darker, richer palette: deep navy BG, chocolate primary, deep blue accent, warm brown accents
    public static final Color BG = new Color(21, 32, 43); // deep navy
    public static final Color PRIMARY = new Color(0,0,0); // chocolate / saddle brown
    public static final Color ACCENT = new Color(48, 117, 159); // deep blue (kept)
    public static final Color ACCENT_BROWN = new Color(120, 85, 60); // subtle brown for highlights
    public static final Color TEXT = new Color(255,255,255); // golden text for contrast on dark backgrounds
    public static final Font DEFAULT_FONT = new Font("Georgia", Font.PLAIN, 12);

    public static void applyTheme(JFrame frame) {
        // Buttons and controls
        UIManager.put("Button.background", ACCENT);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Button.font", DEFAULT_FONT.deriveFont(Font.BOLD, 12f));
        UIManager.put("Button.margin", new Insets(6, 12, 6, 12));

        // Labels and text
        UIManager.put("Label.font", DEFAULT_FONT);
        UIManager.put("Label.foreground", TEXT);

        // Text inputs
        UIManager.put("TextField.font", DEFAULT_FONT);
        UIManager.put("TextField.background", PRIMARY);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("PasswordField.font", DEFAULT_FONT);
        UIManager.put("PasswordField.background", PRIMARY);
        UIManager.put("PasswordField.foreground", TEXT);

        // Text areas and tables
        UIManager.put("TextArea.font", DEFAULT_FONT);
        UIManager.put("TextArea.background", new Color(15, 25, 35));
        UIManager.put("TextArea.foreground", TEXT);

        UIManager.put("Table.font", DEFAULT_FONT);
        UIManager.put("Table.rowHeight", 22);
        UIManager.put("Table.background", new Color(15, 25, 35));
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", ACCENT_BROWN);
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("TableHeader.font", DEFAULT_FONT.deriveFont(Font.BOLD));
        UIManager.put("TableHeader.background", PRIMARY.darker());

        // Tabs, combos and panels
        UIManager.put("TabbedPane.font", DEFAULT_FONT.deriveFont(Font.BOLD));
        UIManager.put("TabbedPane.background", BG);
        // Make tab titles white for better contrast (selected and unselected)
        UIManager.put("TabbedPane.foreground", Color.WHITE);
        UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
        UIManager.put("TabbedPane.unselectedForeground", Color.WHITE);
        UIManager.put("ComboBox.font", DEFAULT_FONT);
        UIManager.put("ComboBox.background", PRIMARY);
 
UIManager.put("Panel.background", BG);
UIManager.put("ScrollPane.background", BG);
UIManager.put("OptionPane.background", BG);
UIManager.put("OptionPane.messageForeground", TEXT);
UIManager.put("ToolTip.background", PRIMARY);
UIManager.put("ToolTip.foreground", TEXT);        // Apply to existing component tree
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().setBackground(BG);
            updateRecursively(frame);
        });
    }

    private static void updateRecursively(Component comp) {
        // Update UI for the component
        try {
            SwingUtilities.updateComponentTreeUI(comp);
        } catch (Exception ignored) {}

        // Force backgrounds and foregrounds for common Swing containers so no white areas remain
        if (comp instanceof JPanel) {
            comp.setBackground(BG);
        }
        if (comp instanceof JTabbedPane) {
            JTabbedPane tp = (JTabbedPane) comp;
            tp.setBackground(BG);
            tp.setForeground(Color.WHITE);
            tp.setOpaque(true);
            for (int i = 0; i < tp.getTabCount(); i++) {
                Component tabComp = tp.getComponentAt(i);
                if (tabComp != null) tabComp.setBackground(BG);
            }
        }
        if (comp instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) comp;
            sp.setBackground(BG);
            if (sp.getViewport() != null) sp.getViewport().setBackground(BG);
        }
        if (comp instanceof JViewport) {
            comp.setBackground(BG);
        }
        if (comp instanceof JTable) {
            JTable t = (JTable) comp;
            t.setBackground(new Color(15, 25, 35));
            t.setForeground(TEXT);
            if (t.getTableHeader() != null) {
                t.getTableHeader().setBackground(PRIMARY.darker());
                t.getTableHeader().setForeground(TEXT);
            }
        }
        if (comp instanceof JRootPane) {
            comp.setBackground(BG);
        }
        if (comp instanceof Window) {
            comp.setBackground(BG);
        }

        if (comp instanceof Container) {
            for (Component c : ((Container) comp).getComponents()) {
                updateRecursively(c);
            }
        }
    }
}
