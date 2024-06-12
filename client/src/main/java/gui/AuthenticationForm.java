package gui;

import builders.AuthUser;
import builders.RegisterUser;
import common.abstractions.AbstractAuthInterface;
import common.abstractions.AbstractReceiver;
import common.commands.abstractions.Command;
import common.commands.implementations.AuthCommand;
import common.commands.implementations.RegisterCommand;
import common.user.Session;
import common.user.User;
import common.utils.Funcs;
import gui.utils.LocaleListCellRenderer;
import network.UserAuthRequest;
import network.UserAuthResponse;
import network.UserRegisterRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class AuthenticationForm extends JDialog {
    private JPanel mainPanel;
    private JTextArea warnings;
    private JLabel pictureLabel;
    private JButton changeMode;

    private JPanel authPanel;
    private JLabel authInfoLabel;
    private JLabel loginLabel;
    private JLabel pwdLabel;
    private JTextField loginField;
    private JPasswordField passwordField;
    private JButton submitButton;


    private JPanel registerPanel;
    private JLabel registerInfoLabel;
    private JTextField registerLoginField;
    private JPasswordField registerPwdField;
    private JButton registerSubmitButton;
    private JLabel registerLoginLabel;
    private JLabel registerPwdLabel;
    private JLabel repeatPwdLabel;
    private JPanel contentPane;
    private JComboBox language;
    private JPasswordField repeatPwdField;

    protected ManagersContainer managers;
    private GuiAuthenticationReceiver authReceiver;

    private ResourceBundle curBundle;
    protected Boolean isOk = false;

    private String[] changeModeTexts;
    private int mode = 0;

    public class GuiAuthenticationReceiver extends AbstractReceiver implements AbstractAuthInterface {

        public GuiAuthenticationReceiver() {
            super(null, null);
        }

        public void authUser(Object[] args) {
            String login = loginField.getText();
            String password = Arrays.toString(passwordField.getPassword());

            var pair = AuthUser.getUser(login, password, managers.getRequestManager());
            User user = pair.getFirst();
            String salt = pair.getSecond();

            managers.setSession(new Session(user, salt));

            if (user == null){
                warnings.setText("Login not found");
                return;
            }

            addArg(args, user);
        }

        public void registerUser(Object[] args) {
            String login = registerLoginField.getText();
            String password = Arrays.toString(registerPwdField.getPassword());
            String repeatPassword = Arrays.toString(repeatPwdField.getPassword());
            if (!password.equals(repeatPassword)){
                warnings.setText("Passwords do not match");
                return;
            }

            var pair = RegisterUser.getUser(login, password, managers.getRequestManager());
            User user = pair.getFirst();
            String salt = pair.getSecond();

            managers.setSession(new Session(user, salt));

            if (user == null){
                warnings.setText("Login already exists");
                return;
            }

            addArg(args, user);
        }

        public void logOut(Object[] args) {

        }
    }

    public AuthenticationForm(ManagersContainer managers, JFrame parent) {
        super(parent, true);
        this.managers = managers;
        authReceiver = new GuiAuthenticationReceiver();
        curBundle = ResourceBundle.getBundle("gui", managers.getCurrentLocale());
        changeModeTexts = new String[]{curBundle.getString("auth_change_1"), curBundle.getString("auth_change_2")};

        setContentPane(contentPane);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getRootPane().setDefaultButton(submitButton);

        loginField.setText("abob");
        passwordField.setText("1234");

        pack();
        setMinimumSize(new Dimension(700, 500));
        setSize(700, 700);
        setLocationRelativeTo(null);
        setTitle(curBundle.getString("auth_title"));

        initText();

        warnings.setOpaque(false);
        /*String[] displayed = new String[managers.enabledLocales.length];
        for (int i = 0; i < managers.enabledLocales.length; i++) {
            displayed[i] = managers.enabledLocales[i].getDisplayName();
        }*/
        language.setModel(new DefaultComboBoxModel(managers.enabledLocales));
        language.setRenderer(new LocaleListCellRenderer());

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    authorize(1);
                } catch (RuntimeException err) {
                    warnings.setText(curBundle.getString("auth_error") + err);
                }
            }
        });

        registerSubmitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    authorize(2);
                } catch (RuntimeException err) {
                    warnings.setText(curBundle.getString("auth_error") + err);
                }
            }
        });

        changeMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout) (mainPanel.getLayout());
                cl.next(mainPanel);
                mode = (mode + 1) % 2;
                changeMode.setText(changeModeTexts[mode]);
                warnings.setText("");
            }
        });

        language.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchLocale((Locale) language.getSelectedItem());
            }
        });

//        warningsLabel.setVisible(false);
    }

    private void initText() {
        authInfoLabel.setText(curBundle.getString("auth_info_label"));
        loginLabel.setText(curBundle.getString("auth_login_label"));
        pwdLabel.setText(curBundle.getString("auth_pwd_label"));
        submitButton.setText(curBundle.getString("auth_submit_button"));

        registerInfoLabel.setText(curBundle.getString("reg_info_label"));
        registerLoginLabel.setText(curBundle.getString("auth_login_label"));
        registerPwdLabel.setText(curBundle.getString("auth_pwd_label"));
        repeatPwdLabel.setText(curBundle.getString("auth_repeat_pwd_label"));
        registerSubmitButton.setText(curBundle.getString("reg_submit_button"));

        changeMode.setText(changeModeTexts[mode]);
//        pictureLabel.setIcon();
    }

    protected void switchLocale(Locale locale) {
        curBundle = ResourceBundle.getBundle("gui", locale);
        changeModeTexts[0] = curBundle.getString("auth_change_1");
        changeModeTexts[1] = curBundle.getString("auth_change_2");
        managers.setCurrentLocale(locale);
        initText();
    }

    protected void authorize(int a) {
        // a == 1 - авторизация; a == 2 - регистрация
        if (a == 1) {
            Command currentCommand = new AuthCommand(new Object[]{});
            currentCommand.setArgs(Funcs.concatObjects(new Object[]{currentCommand}, currentCommand.getArgs()));
            currentCommand.execute(authReceiver);

            // проверка пользователя на сервере
            managers.getRequestManager().makeRequest(new UserAuthRequest(currentCommand));
        } else {
            Command currentCommand = new RegisterCommand(new Object[]{});
            currentCommand.setArgs(Funcs.concatObjects(new Object[]{currentCommand}, currentCommand.getArgs()));
            currentCommand.execute(authReceiver);

            managers.getRequestManager().makeRequest(new UserRegisterRequest(currentCommand, managers.getSession().getSalt()));
        }

        // сделать ожидание
        UserAuthResponse response = null;
        try {
            response = (UserAuthResponse) managers.getRequestManager().getResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String result = response.getMessage();
        print(result);

        if (response.getStatus()) {
            managers.history = response.getHistory();
            next();
            isOk = true;
//            notify();
            dispose();
            return;
        }

        managers.setSession(null);
        warnings.setText(curBundle.getString("auth_auth_problem"));
    }

    protected void print(String message) {
        warnings.setText(message);
    }

    private void next() {
        ;
    }
}
