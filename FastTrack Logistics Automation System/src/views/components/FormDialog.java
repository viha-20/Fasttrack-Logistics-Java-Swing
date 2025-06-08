package views.components;

import javax.swing.*;
import java.awt.*;

public class FormDialog extends JDialog {
    public FormDialog(Frame owner, String title, int width, int height) {
        super(owner, title, true);
        setSize(width, height);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
    }

    public void addFormPanel(JPanel formPanel) {
        add(formPanel, BorderLayout.CENTER);
    }

    public void addButtonPanel(JPanel buttonPanel) {
        add(buttonPanel, BorderLayout.SOUTH);
    }
}