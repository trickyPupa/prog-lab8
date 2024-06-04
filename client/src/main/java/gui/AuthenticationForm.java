package gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AuthenticationForm{
    private JTextField loginField;
    private JPasswordField passwordField;
    private JPanel mainPanel;
    private JPanel authPanel;
    private JButton submitButton;
    private JLabel warningsLabel;
    private JLabel pictureLabel;
    private JLabel infoLabel;

    public AuthenticationForm() {
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // обработка авторизации
                ;
            }
        });

        warningsLabel.setVisible(false);
    }

    public JPanel getPanel(){
        return mainPanel;
    }
}
