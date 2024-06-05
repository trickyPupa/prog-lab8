package gui;

import client.ManagersContainer;
import common.commands.abstractions.Command;
import common.commands.implementations.AuthCommand;
import common.utils.Funcs;
import network.UserAuthRequest;
import network.UserAuthResponse;
import network.UserRegisterRequest;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class AuthenticationForm{
    private JTextField loginField;
    private JPasswordField passwordField;
    private JPanel mainPanel;
    private JPanel authPanel;
    private JButton submitButton;
    private JLabel warningsLabel;
    private JLabel pictureLabel;
    private JLabel infoLabel;
    private JLabel loginLabel;
    private JLabel pwdLabel;

    protected ManagersContainer managers;

    private ResourceBundle curBundle;
    protected Boolean isOk = false;

    public AuthenticationForm(ManagersContainer managers) {
        this.managers = managers;
        curBundle = ResourceBundle.getBundle("gui", managers.getCurrentLocale());

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    authorize();
                }
                catch (RuntimeException err){
                    warningsLabel.setText(curBundle.getString("auth_error") + err);
                }
            }
        });

//        warningsLabel.setVisible(false);
        initText();
    }

    private void initText(){
        infoLabel.setText(curBundle.getString("auth_info_label"));
        loginLabel.setText(curBundle.getString("auth_login_label"));
        pwdLabel.setText(curBundle.getString("auth_pwd_label"));

        submitButton.setText(curBundle.getString("auth_submit_button"));


//        pictureLabel.setIcon();
    }

    protected void switchLocale(Locale locale){
        curBundle = ResourceBundle.getBundle("gui", locale);
        managers.setCurrentLocale(locale);
        initText();
    }

    public JPanel getPanel(){
        return mainPanel;
    }

    protected void authorize(){
        String login = loginField.getText();
        String password = Arrays.toString(passwordField.getPassword());

        Command currentCommand = new AuthCommand(new Object[]{});
        currentCommand.setArgs(Funcs.concatObjects(new Object[] {currentCommand}, currentCommand.getArgs()));

        currentCommand.execute(managers.getReceiver());

        // проверка пользователя на сервере
        managers.getRequestManager().makeRequest(new UserAuthRequest(currentCommand));

        // сделать ожидание
        UserAuthResponse response = null;
        try {
            response = (UserAuthResponse) managers.getRequestManager().getResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String result = response.getMessage();
        print(result);

        if (response.getStatus()){
//            history = response.getHistory();
            next();
            isOk = true;
            isOk.notify();
            return;
        }
//        print("Попробуйте еще раз.");
        warningsLabel.setText(curBundle.getString("auth_auth_problem"));
    }

    protected void register(){
        ;
    }

    protected void print(String message){
        warningsLabel.setText(message);
    }

    private void next(){

    }
}
