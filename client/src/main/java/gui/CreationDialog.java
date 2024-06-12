package gui;

import common.model.entities.Coordinates;
import common.model.entities.Location;
import common.model.entities.Movie;
import common.model.entities.Person;
import common.model.enums.Country;
import common.model.enums.EyeColor;
import common.model.enums.HairColor;
import common.model.enums.MpaaRating;
import gui.utils.NumberFilter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ResourceBundle;

public class CreationDialog extends JDialog {
    private JLabel header;
    private JPanel contentPane;
    private JPanel buttonsPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JScrollPane scrollPane;

    // movie
    private JLabel label1;
    private JLabel label2;
    private JLabel label3;
    private JLabel label4;
    private JLabel label5;
    private JLabel label6;
    private JTextField textField1;  // name
    private JTextField textField2;  // oscarsCount
    private JTextField textField3;  // goldenPalmCount
    private JTextField textField4;  // length
    private JPanel coordinatesPanel;
    private JSpinner spinner51;     // coordinates x
    private JSpinner spinner52;     // coordinates y
    private JComboBox comboBox6;    // mpaa

    // person
    private JLabel personLabel;
    private JLabel label7;
    private JLabel label8;
    private JLabel label9;
    private JLabel label10;
    private JLabel label11;
    private JLabel label12;

    private JTextField textField7;  // name
    private JSpinner dateSpinner;   // birthday
    private JComboBox comboBox9;    // eyeColor
    private JComboBox comboBox10;   // hairColor
    private JComboBox comboBox11;   // nationality
    private JPanel locationPanel;
    private JSpinner locXSpinner;   // location x
    private JSpinner locYSpinner;   // location y
    private JSpinner locZSpinner;   // location z

    private JLabel[] labels = {label1, label2, label3, label4, label5, label6, label7, label8, label9, label10, label11, label12};
    private JTextField[] fields = {textField1, textField2, textField3, textField4, textField7};

    private ResourceBundle curBundle;

    protected Movie changingMovie = null; // если != null, значит окно редактирования
    protected Movie result = null;

    public CreationDialog(Window parent, ResourceBundle bundle) {
        super(parent, ModalityType.APPLICATION_MODAL);
        curBundle = bundle;

        init();
    }

    public CreationDialog(Window parent, ResourceBundle bundle, Movie movie) {
        super(parent, ModalityType.APPLICATION_MODAL);
        curBundle = bundle;
        changingMovie = movie;

        init();
    }

    private void init(){
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setUpComponents();

        setLocation(600, 200);
        pack();
        setSize(1100, 700);
        setResizable(false);
    }

    private void setUpComponents() {
        ((AbstractDocument) textField2.getDocument()).setDocumentFilter(new NumberFilter(0));
        ((AbstractDocument) textField3.getDocument()).setDocumentFilter(new NumberFilter(0));
        ((AbstractDocument) textField4.getDocument()).setDocumentFilter(new NumberFilter(1));

        class MyDocumentListener implements DocumentListener {
            private JTextField field;

            public MyDocumentListener(JTextField field) {
                this.field = field;
            }

            private void updateBackgroundColor() {
                if (field.getText().isBlank()) {
                    field.setBackground(Color.RED);
                } else {
                    field.setBackground(Color.WHITE);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateBackgroundColor();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateBackgroundColor();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateBackgroundColor();
            }
        }

        for (JTextField i : fields){
            i.getDocument().addDocumentListener(new MyDocumentListener(i));
        }

        spinner51.setModel(new SpinnerNumberModel(0, (int) Coordinates.X_MIN_VALUE, (int) Coordinates.X_MAX_VALUE, 1));
        spinner51.setEditor(new JSpinner.NumberEditor(spinner51));
        spinner52.setModel(new SpinnerNumberModel(0, (long) Coordinates.Y_MIN_VALUE, (long) Coordinates.Y_MAX_VALUE, 1));
        spinner52.setEditor(new JSpinner.NumberEditor(spinner52));

        /*JSpinner.NumberEditor editor = (JSpinner.NumberEditor) spinner51.getEditor();
        JTextField textField = editor.getTextField();
        Dimension preferredSize = textField.getPreferredSize();
        preferredSize.width = 50;  // Можно настроить ширину по вашему усмотрению
        textField.setPreferredSize(preferredSize);*/

        dateSpinner.setModel(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, curBundle.getString("date.format")));

        comboBox6.setModel(new DefaultComboBoxModel(MpaaRating.values()));
        comboBox9.setModel(new DefaultComboBoxModel(EyeColor.values()));
        comboBox10.setModel(new DefaultComboBoxModel(HairColor.values()));
        comboBox11.setModel(new DefaultComboBoxModel(Country.values()));

        locXSpinner.setModel(new SpinnerNumberModel(0.0, -Float.MAX_VALUE, Float.MAX_VALUE, 0.1));
        locXSpinner.setEditor(new JSpinner.NumberEditor(locXSpinner, "0.0"));
        locYSpinner.setModel(new SpinnerNumberModel(0L, Long.MIN_VALUE, Long.MAX_VALUE, 1));
        locYSpinner.setEditor(new JSpinner.NumberEditor(locYSpinner));
        locZSpinner.setModel(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
        locZSpinner.setEditor(new JSpinner.NumberEditor(locZSpinner));

        initText();
    }

    private void initText() {
        if(changingMovie == null) {
            setTitle(curBundle.getString("creation_title"));
            header.setText(curBundle.getString("creation_header"));
        } else {
            setTitle(curBundle.getString("update_title"));
            header.setText(curBundle.getString("update_header"));

            // поля Movie
            textField1.setText(changingMovie.getName());
            textField2.setText(String.valueOf(changingMovie.getOscarsCount()));
            textField3.setText(changingMovie.getGoldenPalmCount().toString());
            textField4.setText(String.valueOf(changingMovie.getLength()));

            spinner51.setValue(changingMovie.getCoordinates().getX());
            spinner52.setValue(changingMovie.getCoordinates().getY());

            comboBox6.setSelectedItem(changingMovie.getMpaaRating());

            // поля Person
            Person director = changingMovie.getDirector();
            textField7.setText(director.getName());

            dateSpinner.setValue(Date.valueOf(director.getBirthday()));

            comboBox9.setSelectedItem(director.getEyeColor());
            comboBox10.setSelectedItem(director.getHairColor());
            comboBox11.setSelectedItem(director.getLocation());

            locXSpinner.setValue(director.getLocation().getX());
            locYSpinner.setValue(director.getLocation().getY());
            locZSpinner.setValue(director.getLocation().getZ());
        }
        personLabel.setText(curBundle.getString("creation_person"));

        buttonOK.setText(curBundle.getString("creation_ok_button"));
        buttonCancel.setText(curBundle.getString("creation_cancel_button"));
        for (int i = 0; i < labels.length; i++) {
            labels[i].setText(curBundle.getString("creation_label" + (i + 1)) + ":");
        }
    }

    private Movie createObject() {
        try {
            String name = textField1.getText();
            int oscars = Integer.parseInt(textField2.getText());
            Integer goldenPalms = textField3.getText().isBlank() ? null : Integer.parseInt(textField3.getText());
            long length = Long.parseLong(textField4.getText());
            Coordinates coords = new Coordinates(((Number) spinner51.getValue()).intValue(), ((Number) spinner52.getValue()).longValue());
            MpaaRating mpaa = (MpaaRating) comboBox6.getSelectedItem();

            String personName = textField7.getText();
            LocalDate date = ((SpinnerDateModel) dateSpinner.getModel()).getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            EyeColor eyeColor = (EyeColor) comboBox9.getSelectedItem();
            HairColor hairColor = (HairColor) comboBox10.getSelectedItem();
            Country country = (Country) comboBox11.getSelectedItem();
            Location location = new Location(((Number) locXSpinner.getValue()).floatValue(),
                    ((Number) locYSpinner.getValue()).longValue(), ((Number) locZSpinner.getValue()).intValue());

            Person director = new Person(personName, date, eyeColor, hairColor, country, location);
            Movie movie = new Movie(name, oscars, goldenPalms, length, coords, mpaa, director);

            return movie;
        } catch (NumberFormatException e){
            checkFields();
            JOptionPane.showMessageDialog(this, curBundle.getString("invalid_args"),
                    curBundle.getString("error_title"), JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public void checkFields(){
        for(var i : fields){
            if(i.getText().isBlank()){
                i.setBackground(Color.RED);
            }
        }
    }

    private void onOK() {
        result = createObject();
        if (result == null)
            return;

        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public Movie getResult() {
        return result;
    }

    /*public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        var frame = new JFrame();
        ResourceBundle bundle = ResourceBundle.getBundle("gui", Locale.getDefault());
        CreationDialog mw = new CreationDialog(frame, bundle);
        mw.setVisible(true);
        System.exit(0);
    }*/

}
