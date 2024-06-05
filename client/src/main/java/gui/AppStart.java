package gui;

import client.ClientRequestManager;
import client.ManagersContainer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.ResourceBundle;

public class AppStart extends JDialog {
    private JPanel contentPane;
    private JButton submitButton;
    private JButton exitButton;
    private JTextField hostField;
    private JTextField portField;
    private JLabel hostLabel;
    private JLabel portLabel;
    private JLabel info;
    private JLabel warnings;

    private final ManagersContainer managers;

    private ResourceBundle curBundle;

    public AppStart(ManagersContainer managers) {
        this.managers = managers;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(submitButton);

        setResizable(false);
        setMaximumSize(new Dimension(200, 500));
        setLocation(700, 300);
        setTitle("App Start");

        curBundle = ResourceBundle.getBundle("gui", managers.getCurrentLocale());
        initText();

        hostField.setText("localhost");
        portField.setText("1783");

        pack();
        setSize(400, 300);

        submitButton.addActionListener(e -> submit());
        exitButton.addActionListener(e -> {
            dispose();
        });

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


    }

    private void initText() {
        info.setText(curBundle.getString("app_start_info_label"));
        hostLabel.setText(curBundle.getString("app_start_host_label"));
        portLabel.setText(curBundle.getString("app_start_port_label"));

        submitButton.setText(curBundle.getString("app_start_ok_button"));
        exitButton.setText(curBundle.getString("app_start_exit_button"));

        if (!warnings.getText().isEmpty()){
            warnings.setText(curBundle.getString("app_start_warnings"));
        }
    }

    protected void switchLocale(Locale locale) {
        curBundle = ResourceBundle.getBundle("gui", locale);
        managers.setCurrentLocale(locale);
        initText();
    }

    private void submit() {
        String host = hostField.getText();
        String host_name;
        int port;
        try {
            port = Integer.parseInt(portField.getText());

            if (host == null || host.isBlank())
                host_name = "localhost";
            else
                host_name = host;

            runApp(port, host_name);
        }
        catch (NumberFormatException e){
            warnings.setText(curBundle.getString("app_start_wrong_port"));
        }
    }

    private void runApp(int port, String host) {
        try{
            var requestManager = new ClientRequestManager(host, port);
            managers.setRequestManager(requestManager);
        } catch (UnknownHostException e) {
            warnings.setText(curBundle.getString("app_start_connection_error"));
            return;
        }

        SwingUtilities.invokeLater(() -> {
            MainWindow mw = new MainWindow(managers);
            mw.setVisible(true);
        });
        dispose();
    }
}
