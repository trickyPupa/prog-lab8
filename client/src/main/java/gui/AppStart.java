package gui;

import com.sun.tools.javac.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AppStart extends JDialog {
    private JPanel contentPane;
    private JButton submitButton;
    private JButton exitButton;
    private JTextField textField1;
    private JTextField textField2;
    private JLabel label1;
    private JLabel label2;
    private JLabel info;

    public AppStart() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(submitButton);

        setSize(1000, 1000);
        setResizable(false);
//        setMaximumSize(new Dimension(200, 500));
        setLocation(700, 300);

        submitButton.addActionListener(e -> submit());

        exitButton.addActionListener(e -> dispose());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        /*addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });*/

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        init();
    }

    private void init() {
        info.setText("Привет");
        label1.setText("enter server port");
        label2.setText("enter server host name");

        textField2.setText("localhost");
        textField1.setText("1783");
    }

    private void submit() {
        String host = textField2.getText();
        String host_name;
        int port;
        try {
            port = Integer.parseInt(textField1.getText());

            if (host == null || host.isBlank())
                host_name = "localhost";
            else
                host_name = host;

            runApp(port, host_name);
        }
        catch (NumberFormatException e){
            label1.setText("Port must be an integer.");
        }
    }

    private void runApp(int port, String host) {
//        var app = new MainWindow();

        SwingUtilities.invokeLater(MainWindow::new);
        dispose();
    }
}
