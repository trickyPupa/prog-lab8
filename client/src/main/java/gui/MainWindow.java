package gui;

import common.commands.abstractions.Command;
import common.commands.implementations.*;
import common.model.entities.Movie;
import common.model.entities.Person;
import common.utils.Funcs;
import gui.utils.LocaleListCellRenderer;
import network.CommandRequest;
import network.GetDataRequest;
import network.GetDataResponse;
import network.Response;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.function.Function;

public class MainWindow extends JFrame {
    private JPanel mainPanel;
    private JPanel buttons;
    private JScrollPane centerPane;

    private JLabel info;
    private JTextArea textArea;
    private JLabel textAreaLabel;
    private JTable table;
    private MovieTableModel tableModel;

    private JComboBox filterComboBox;
    private JTextField filterField;
    private JLabel tableLabel;
    private JLabel filterLabel;

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
    private Receiver receiver;

    protected class Receiver {
        public void executeCommand(Function<Object[], Command> commandFunction, Object[] args){
            var rm = managers.getRequestManager();
            var command = commandFunction.apply(args);
            command.setArgs(Funcs.concatObjects(new Object[]
                    {command, managers.getSession().getUser()}, command.getArgs()));

            rm.makeRequest(new CommandRequest(command, managers.getHistory()));

            try {
                Response response = managers.getRequestManager().getResponse();
                managers.history = response.getHistory();

                System.out.println(response.getMessage());

                loadData();

//                System.out.println(data);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void executeCommand(Function<Object[], Command> commandFunction){
            executeCommand(commandFunction, new Object[0]);
        }

        public void clear(){
            executeCommand(ClearCommand::new);
        }

        public void removeLower(Movie movie){
            executeCommand(RemoveLowerCommand::new, new Object[]{movie});
        }

        public void removeById(int id){
            executeCommand(RemoveByIdCommand::new, new Object[]{id});
        }

        public void removeByGP(int gp){
            executeCommand(RemoveAllByGoldenPalmCountCommand::new, new Object[]{gp});
        }

        public void history(){
            StringBuilder res = new StringBuilder();
            for(Command i : managers.history){
                res.append(i.getName()).append("\n");
            }
            res.append("\n");
            textArea.setText(String.valueOf(res));

            executeCommand(HistoryCommand::new);
        }

        public void executeScript(){
            ;
        }
    }

    public MainWindow(ManagersContainer managersContainer) {
        this.managers = managersContainer;
        curBundle = ResourceBundle.getBundle("gui", managers.getCurrentLocale());
        formatter = DateTimeFormatter.ofPattern(curBundle.getString("date.format"));
//        switchLocale(managers.getCurrentLocale());
        receiver = new Receiver();

        authentication();

        setName("movie app");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setSize(700, 700);
        setLocationRelativeTo(null);
        setContentPane(mainPanel);

        initData();
        initText();

        language.setModel(new DefaultComboBoxModel(managers.enabledLocales));
        language.setRenderer(new LocaleListCellRenderer());

        filterComboBox.setModel(new DefaultComboBoxModel(tableModel.columnNames));
//        filterComboBox.setRenderer(new LocaleListCellRenderer());
        filterField.setColumns(20);

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
        // при изменении размера окна, изменять таблицу
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustColumnWidths();
            }
        });
    }

    private void showCommands() {
        var dialog = new CommandsDialog(this, curBundle, receiver);
        dialog.setVisible(true);
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

        protected int[] minColumnWidths;

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
                case 0 -> movie.getId();
                case 1 -> movie.getCreator();
                case 2 -> movie.getName();
                case 3 -> movie.getLength();
                case 4 -> movie.getOscarsCount();
                case 5 -> movie.getGoldenPalmCount();
                case 6 -> movie.getCoordinates();
                case 7 -> movie.getMpaaRating();
                case 8 -> movie.getCreationDate().format(formatter);
                case 9 -> director.getName();
                case 10 -> director.getBirthday().format(formatter);
                case 11 -> director.getEyeColor();
                case 12 -> director.getHairColor();
                case 13 -> director.getNationality();
                case 14 -> director.getLocation();
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

    private int[] calculateColumnWidths(){
        int columnCount = table.getColumnCount();
        int[] minColumnWidths = new int[columnCount];

        for (int column = 0; column < columnCount; column++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = tableColumn.getMinWidth();
            int maxWidth = tableColumn.getMaxWidth();

            // Вычисляем минимальную ширину содержимого для каждого столбца
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component c = table.prepareRenderer(cellRenderer, row, column);
                int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);

                if (preferredWidth >= maxWidth) {
                    preferredWidth = maxWidth;
                    break;
                }
            }

            // Определяем ширину заголовка столбца
            int headerWidth = table.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(table, tableColumn.getHeaderValue(),
                            false, false, 0, column)
                    .getPreferredSize().width + table.getIntercellSpacing().width;

            // Учитываем ширину заголовка
            preferredWidth = Math.max(preferredWidth, headerWidth);

            // Запоминаем минимальную ширину столбца
            minColumnWidths[column] = preferredWidth;
        }

        return minColumnWidths;
    }

    private void adjustColumnWidths() {
        int totalMinWidth = 0;
        int totalWidth = table.getParent().getWidth();
        int columnCount = table.getColumnCount();

        for (int column = 0; column < columnCount; column++) {
            totalMinWidth += tableModel.minColumnWidths[column];
        }


        // Если ширина всех столбцов меньше, чем доступное пространство, расширяем столбцы
        if (totalMinWidth < totalWidth) {
            int realWidth = 0;
            for (int column = 0; column < columnCount - 1; column++) {
                TableColumn tableColumn = table.getColumnModel().getColumn(column);
                int additionalWidth = (int) Math.round((double) (totalWidth - totalMinWidth) / columnCount);
                tableColumn.setPreferredWidth(tableModel.minColumnWidths[column] + additionalWidth);
                realWidth += tableModel.minColumnWidths[column] + additionalWidth;
            }
            TableColumn tableColumn = table.getColumnModel().getColumn(columnCount - 1);
            tableColumn.setPreferredWidth(totalWidth - realWidth);

        } else {
            // Если ширина всех столбцов больше или равна доступному пространству, используем минимальные ширины
            for (int column = 0; column < columnCount; column++) {
                TableColumn tableColumn = table.getColumnModel().getColumn(column);
                tableColumn.setMinWidth(tableModel.minColumnWidths[column]);
            }
        }
    }

    private void initData() {
        loadData();

        tableModel = new MovieTableModel(data, initColumns(), formatter);
        // изменения модели таблицы, чтобы обновлять ширину столбцов при изменении данных
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                tableModel.minColumnWidths = calculateColumnWidths();
                adjustColumnWidths();
            }
        });
        table.setModel(tableModel);

        TableRowSorter<MovieTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                newFilter();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                newFilter();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                newFilter();
            }

            private void newFilter() {
                RowFilter<MovieTableModel, Object> rf = null;
                int selected = filterComboBox.getSelectedIndex();
                String text = filterField.getText();

                try {
                    for (int number : new int[]{7, 11, 12, 13}) {
                        if (number == selected) {
                            text = text.toUpperCase(managers.currentLocale);
                            break;
                        }
                    }

                    rf = RowFilter.regexFilter(text, selected);
                } catch (java.util.regex.PatternSyntaxException e) {
                    e.printStackTrace();
                    return;
                }
                sorter.setRowFilter(rf);
            }
        });

        tableModel.minColumnWidths = calculateColumnWidths();
        adjustColumnWidths();
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

        if (tableModel != null) {
            tableModel.updateMovies(data);
            adjustColumnWidths();
        }
    }

    private void initText() {
        info.setText(curBundle.getString("main_info_label") + " " + managers.getSession().getUser().getLogin());

        createButton.setText(curBundle.getString("main_create_button"));
        editButton.setText(curBundle.getString("main_edit_button"));
        deleteButton.setText(curBundle.getString("main_delete_button"));
        commandsButton.setText(curBundle.getString("main_command_button"));
        visualizeButton.setText(curBundle.getString("main_vis_button"));

        tableLabel.setText(curBundle.getString("main_table_label"));
        filterLabel.setText(curBundle.getString("main_filter_label"));

        tableModel.setColumns(initColumns());
    }

    private String[] initColumns(){
        String[] columns = new String[15];
        String[] keys = new String[]{"movie_creator", "movie_title", "movie_length", "movie_oscars",
                "movie_golden_palms", "movie_coordinates", "movie_mpaa", "movie_creation_date", "director_name",
                "director_birthday", "director_eyes", "director_hair", "director_country", "director_location"};
        columns[0] = "ID";
        for (int i = 1; i < keys.length + 1; i++) {
            columns[i] = curBundle.getString(keys[i - 1]);
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
