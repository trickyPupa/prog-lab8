package gui;

import common.model.entities.Movie;
import network.GetDataRequest;
import network.GetDataResponse;
import network.Response;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.util.*;

public class MainWindow extends JFrame {
    private JPanel mainPanel;
    private JPanel buttons;
    private JScrollPane centerPane;

    private JLabel info;
    private JTextArea textArea;
    private JLabel textAreaLabel;
    private JTable table;

    private JButton createButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton visualizeButton;
    private JButton commandsButton;

    private ManagersContainer managers;
    private ResourceBundle curBundle;
    private ArrayList<Movie> data;
    private Receiver rec;

    protected class Receiver {
        ;
    }

    public MainWindow(ManagersContainer managersContainer) {
        this.managers = managersContainer;
        curBundle = ResourceBundle.getBundle("gui", managers.getCurrentLocale());
        rec = new Receiver();

        authentication();

        setName("movie app");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setSize(700, 700);
        setLocationRelativeTo(null);
        setContentPane(mainPanel);

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

        initData();
        initText();
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
            data.add(result);
            // отправить запрос на сервер

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
        private final String[] columnNames = {"ID", "Title", "Year", "Director", "Rating"};

        public MovieTableModel(Collection<Movie> movies) {
            this.movies = (ArrayList<Movie>) movies;
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
            return switch (columnIndex) {
                case 0 -> movie.getId();
                case 1 -> movie.getName();
                case 2 -> movie.getLength();
                default -> null;
            };
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }
    }

    private void initData() {
        loadData();
        var tableModel = new MovieTableModel(data);

        table.setModel(tableModel);
    }

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
    }

    private void initText() {
        info.setText(curBundle.getString("main_info_label") + " " + managers.getSession().getUser().getLogin());

        createButton.setText(curBundle.getString("main_create_button"));
        editButton.setText(curBundle.getString("main_edit_button"));
        deleteButton.setText(curBundle.getString("main_delete_button"));
        commandsButton.setText(curBundle.getString("main_command_button"));
        visualizeButton.setText(curBundle.getString("main_vis_button"));
    }

    protected void switchLocale(Locale locale) {
        curBundle = ResourceBundle.getBundle("gui", locale);
        managers.setCurrentLocale(locale);
        initText();
    }

    private void close() {
        dispose();
        System.exit(0);
    }
}
