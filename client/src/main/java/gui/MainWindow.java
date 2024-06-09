package gui;

import common.commands.implementations.AddCommand;
import common.model.entities.Movie;
import common.model.entities.Person;
import common.utils.Funcs;
import gui.utils.LocaleListCellRenderer;
import network.CommandRequest;
import network.GetDataRequest;
import network.GetDataResponse;
import network.Response;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainWindow extends JFrame {
    private JPanel mainPanel;
    private JPanel buttons;
    private JScrollPane centerPane;

    private JLabel info;
    private JTextArea textArea;
    private JLabel textAreaLabel;
    private JTable table;
    private MovieTableModel tableModel;

    private JButton createButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton visualizeButton;
    private JButton commandsButton;
    private JComboBox language;

    private ManagersContainer managers;
    private ResourceBundle curBundle;
    private DateTimeFormatter formatter;
    private ArrayList<Movie> data;
    private Receiver rec;

    /*private void createUIComponents() {
        loadData();

        var tableModel = new MovieTableModel(data, initColumns(), formatter);

        table = new JTable(tableModel);
    }*/

    protected class Receiver {
        ;
    }

    public MainWindow(ManagersContainer managersContainer) {
        this.managers = managersContainer;
        curBundle = ResourceBundle.getBundle("gui", managers.getCurrentLocale());
        formatter = DateTimeFormatter.ofPattern(curBundle.getString("date.format"));
//        switchLocale(managers.getCurrentLocale());
        rec = new Receiver();

        authentication();

        setName("movie app");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setSize(700, 700);
        setLocationRelativeTo(null);
        setContentPane(mainPanel);

        language.setModel(new DefaultComboBoxModel(managers.enabledLocales));
        language.setRenderer(new LocaleListCellRenderer());

        // создание нового фильма
        createButton.addActionListener(e -> create());
        // изменение выделенного фильма
        editButton.addActionListener(e -> edit());
        // удаление выделенного фильма
        deleteButton.addActionListener(e -> deleteMovie());
        // область визуализации
        visualizeButton.addActionListener(e -> graphicsArea());
        // показать команды
        commandsButton.addActionListener(e -> showCommands());
        // сменить язык
        language.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchLocale((Locale) language.getSelectedItem());
            }
        });

        initData();
//        initText();
    }

    private void showCommands() {
    }

    private void graphicsArea() {
    }

    private void deleteMovie() {
    }

    private void edit() {
    }

    private void create() {
        var dialog = new CreationDialog(this, curBundle);
        dialog.setVisible(true);

        /*Movie result = dialog.getResult();
        while(true){
            try {
                result.wait();
                break;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }*/

        Movie result = dialog.getResult();

        if (result != null) {
//            data.add(result);
            // отправить запрос на сервер

            var command = new AddCommand(new Object[]{});
            command.setArgs(Funcs.concatObjects(new Object[]
                    {command, managers.getSession().getUser(), result}, command.getArgs()));

            managers.getRequestManager().makeRequest(new CommandRequest(command, managers.getHistory()));

            try {
                Response response = managers.getRequestManager().getResponse();
//            String result = response.getMessage();
                managers.history = response.getHistory();

                loadData();

                System.out.println(data);
//                SwingUtilities.invokeLater(() -> tableModel.fireTableDataChanged());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void authentication() {
        var authForm = new AuthenticationForm(managers, this);
        authForm.setVisible(true);

        if (authForm.isOk) {
//            setContentPane(mainPanel);
            setVisible(true);
        } else {
            System.out.println(-1);
            close();
        }

        /*synchronized (authForm.isOk) {
            while (true) {
                try {
                    authForm.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (authForm.isOk) {
                    setContentPane(mainPanel);
                    setVisible(true);

                    break;
                } else {
    //                System.out.println(-1);
                }
            }
        }*/
    }

    protected static class MovieTableModel extends AbstractTableModel {
        private ArrayList<Movie> movies;
        private String[] columnNames;
        private DateTimeFormatter formatter;

        public MovieTableModel(Collection<Movie> movies, String[] columnNames, DateTimeFormatter formatter) {
            this.movies = (ArrayList<Movie>) movies;
            this.columnNames = columnNames;
            this.formatter = formatter;
        }

        @Override
        public int getRowCount() {
            return movies.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Movie movie = movies.get(rowIndex);
            Person director = movie.getDirector();
            return switch (columnIndex) {
                case 0 -> movie.getCreator();
                case 1 -> movie.getName();
                case 2 -> movie.getLength();
                case 3 -> movie.getOscarsCount();
                case 4 -> movie.getGoldenPalmCount();
                case 5 -> movie.getCoordinates();
                case 6 -> movie.getMpaaRating();
                case 7 -> movie.getCreationDate().format(formatter);
                case 8 -> director.getName();
                case 9 -> director.getBirthday().format(formatter);
                case 10 -> director.getEyeColor();
                case 11 -> director.getHairColor();
                case 12 -> director.getNationality();
                case 13 -> director.getLocation();
                default -> null;
            };
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        public void setColumns(String[] columnNames) {
            this.columnNames = columnNames;
        }

        public void setDateFormat(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        public void updateMovies(Collection<Movie> newMovies) {
            this.movies = new ArrayList<>(newMovies);
            fireTableDataChanged();
        }
    }

    private void initData() {
        loadData();

        tableModel = new MovieTableModel(data, initColumns(), formatter);

        table.setModel(tableModel);
    }

    // обновление данных о фильмах с сервера
    private void loadData() {
        var requestManager = managers.getRequestManager();
        // запрос на получение коллекции фильмов
        var getDataRequest = new GetDataRequest();
        requestManager.makeRequest(getDataRequest);
        Response response;
        try {
            response = requestManager.getResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        data = new ArrayList<>(List.of(((GetDataResponse) response).getData()));

        if (tableModel != null)
            tableModel.updateMovies(data);
    }

    private void initText() {
        info.setText(curBundle.getString("main_info_label") + " " + managers.getSession().getUser().getLogin());

        createButton.setText(curBundle.getString("main_create_button"));
        editButton.setText(curBundle.getString("main_edit_button"));
        deleteButton.setText(curBundle.getString("main_delete_button"));
        commandsButton.setText(curBundle.getString("main_command_button"));
        visualizeButton.setText(curBundle.getString("main_vis_button"));

        tableModel.setColumns(initColumns());
    }

    private String[] initColumns(){
        String[] columns = new String[14];
        String[] keys = new String[]{"movie_creator", "movie_title", "movie_length", "movie_oscars",
                "movie_golden_palms", "movie_coordinates", "movie_mpaa", "movie_creation_date", "director_name",
                "director_birthday", "director_eyes", "director_hair", "director_country", "director_location"};
        for (int i = 0; i < data.size(); i++) {
            columns[i + 1] = curBundle.getString(keys[i]);
        }
        return columns;
    }

    protected void switchLocale(Locale locale) {
        curBundle = ResourceBundle.getBundle("gui", locale);
        managers.setCurrentLocale(locale);
        formatter = DateTimeFormatter.ofPattern(curBundle.getString("date.format"));
        tableModel.setDateFormat(formatter);

        initText();
    }

    private void close() {
        dispose();
        System.exit(0);
    }
}
