package gui;

import common.model.entities.Movie;
import gui.utils.NumberFilter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

public class CommandsDialog extends JDialog {
    private JPanel contentPane;
    private JButton exitButton;
    private JLabel infoLabel;

    private JButton deleteByIdButton;
    private JTextField deleteByIdField;
    private JButton deleteByGPButton;
    private JTextField deleteByGPField;

    private JButton scriptButton;
    private JButton deleteLowerButton;
    private JButton clearButton;
    private JButton historyButton;

    private ResourceBundle curBundle;
    private MainWindow.Receiver receiver;

    public CommandsDialog(JFrame parent, ResourceBundle bundle, MainWindow.Receiver rec) {
        super(parent, true);
        setContentPane(contentPane);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getRootPane().setDefaultButton(exitButton);

        curBundle = bundle;
        receiver = rec;

        exitButton.addActionListener(e -> dispose());

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        scriptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    System.out.println("Selected file: " + selectedFile.getAbsolutePath());

                    // выполнение команды

                    dispose();
                }
            }
        });
        deleteByGPButton.addActionListener(e -> {
            int gp = Integer.parseInt(deleteByGPField.getText());
            rec.removeByGP(gp);
            dispose();
        });
        deleteByIdButton.addActionListener(e -> {
            int id = Integer.parseInt(deleteByIdField.getText());
            rec.removeById(id);
            dispose();
        });
        deleteLowerButton.addActionListener(e -> {
            var dialog = new CreationDialog(this, curBundle);
            dialog.setVisible(true);

            Movie movie = dialog.getResult();
            System.out.println(movie);
            if (movie != null) {
                rec.removeLower(movie);
                dispose();
            }
        });

        clearButton.addActionListener(e -> {
            rec.clear();
            dispose();
        });

        historyButton.addActionListener(e -> {
            rec.history();
            dispose();
        });

        setUpComponents();

        setLocation(700, 200);
        pack();
        setSize(700, 700);
    }

    private void setUpComponents() {
        ((AbstractDocument) deleteByIdField.getDocument()).setDocumentFilter(new NumberFilter(0));
        ((AbstractDocument) deleteByGPField.getDocument()).setDocumentFilter(new NumberFilter(0));

        deleteByGPField.setColumns(20);
        deleteByIdField.setColumns(20);

        initText();
    }

    private void initText() {
        setTitle(curBundle.getString("commands_title"));

        infoLabel.setText(curBundle.getString("commands_info"));
        scriptButton.setText(curBundle.getString("command_execute_script"));
        deleteByIdButton.setText(curBundle.getString("command_delete_by_id"));
        deleteByGPButton.setText(curBundle.getString("command_delete_by_gp"));
        deleteLowerButton.setText(curBundle.getString("command_delete_lower"));
        clearButton.setText(curBundle.getString("command_clear"));
        historyButton.setText(curBundle.getString("command_history"));

        exitButton.setText(curBundle.getString("exit_button"));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        var frame = new JFrame();
        ResourceBundle bundle = ResourceBundle.getBundle("gui", Locale.getDefault());
        CommandsDialog mw = new CommandsDialog(frame, bundle, null);
        mw.setVisible(true);
        System.exit(0);
    }
}
