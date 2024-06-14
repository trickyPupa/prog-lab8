package gui;

import client.ClientReceiver;
import client.InputManager;
import common.abstractions.IInputManager;
import common.abstractions.IOutputManager;
import common.commands.abstractions.Command;
import common.commands.implementations.*;
import common.exceptions.*;
import common.model.entities.Movie;
import common.model.entities.Person;
import common.utils.Funcs;
import exceptions.ConnectionsFallsExcetion;
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
import java.awt.event.*;
import java.io.*;
import java.net.PortUnreachableException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MainWindow extends JFrame {
    private JPanel mainPanel;
    private JPanel buttons;
    private JScrollPane tableScrollPane;
    private JPanel graphicsPanel;
    private VisualizationPanel visualizationPanel;

    private JLabel info;
    private JTextArea textArea;
    private JLabel textAreaLabel;
    private JTable table;
    private MovieTableModel tableModel;
    private JPanel tablePanel;

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
    private JPanel centerPanel;
    private JLabel warningsLabel;
    private JButton logOutButton;

    private ManagersContainer managers;
    private ResourceBundle curBundle;
    private DateTimeFormatter formatter;
    private ArrayList<Movie> data = null;
    private Receiver receiver;
    private int graphicsMode = 0; // 0 - таблица, 1 - график

    protected class Receiver extends ClientReceiver {
        private int recur_param = 0;
        private int cur_recur_param = 0;

        public Receiver() {
            super(null, null);
            setOutput(new GuiOutputManager());
        }

        @Override
        protected void modelObjectInput(Object[] args) {
            addArg(args, Movie.createMovieNoText(inputManager, outputManager));
        }

        protected class GuiOutputManager implements IOutputManager {

            @Override
            public void print(String s) {
                MainWindow.this.textArea.append(s);
            }

            @Override
            public void print(Object s) {
                print(s.toString());
            }
        }

        private void setInput(IInputManager inp) {
            inputManager = inp;
        }

        private void setOutput(IOutputManager out) {
            outputManager = out;
        }

        public void nextCommand(String line) throws IOException {
            line = line.strip();
            String commandName;
            Object[] args = {};
            var commands = managers.commands;

            if (line.contains(" ")) {
                commandName = line.substring(0, line.indexOf(" ")).strip();
                args = line.substring(1 + commandName.length()).split(" ");
            } else {
                commandName = line.strip();
//            args = new String[]{""};
            }

            if (!commands.containsKey(commandName)) {
                throw new NoSuchCommandException(line);
            }
            Command currentCommand = commands.get(commandName).apply(args);
            currentCommand.setArgs(Funcs.concatObjects(new Object[]
                    {currentCommand, managers.getSession().getUser()}, currentCommand.getArgs()));

//        System.out.println(Arrays.toString(currentCommand.getArgs()));

            // выполнение команды и отправка запроса серверу

            if (currentCommand.getClass() == ExitCommand.class) {
                managers.getRequestManager().makeRequest(new CommandRequest(currentCommand, managers.history));
                currentCommand.execute(this);
            } else {
                if (currentCommand.getClass() == HistoryCommand.class) {
                    history();
                }
                currentCommand.execute(this);
                managers.getRequestManager().makeRequest(new CommandRequest(currentCommand, managers.history));
            }

            // получить ответ сервера
            Response response = managers.getRequestManager().getResponse();
            String result = response.getMessage();
            managers.history = response.getHistory();

            outputManager.print(result);
        }

        public void nextCommand() throws Exception {
            String line = inputManager.nextLine().strip();

            if (line.contains("$$$"))
                throw new Exception();

            nextCommand(line);
        }


        public Response executeCommand(Function<Object[], Command> commandFunction, Object[] args) {
            var command = commandFunction.apply(args);
            command.setArgs(Funcs.concatObjects(new Object[]
                    {command, managers.getSession().getUser()}, command.getArgs()));

//            System.out.println(Arrays.toString(command.getArgs()));

            return executeCommand(command);
        }

        public Response executeCommand(Command command) {
            var rm = managers.getRequestManager();
            rm.makeRequest(new CommandRequest(command, managers.getHistory()));

            try {
                Response response = managers.getRequestManager().getResponse();
                managers.history = response.getHistory();

                loadData();

                if (!warningsLabel.getText().isEmpty())
                    warningsLabel.setText("");

                return response;
            } catch (PortUnreachableException e) {
                serverUnavailable();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        public Response executeCommand(Function<Object[], Command> commandFunction) {
            return executeCommand(commandFunction, new Object[0]);
        }

        public void clear() {
            var res = executeCommand(ClearCommand::new);
            JOptionPane.showMessageDialog(MainWindow.this, curBundle.getString("removal_all"),
                    curBundle.getString("clearing_title"), JOptionPane.INFORMATION_MESSAGE);
        }

        public void removeLower(Movie movie) {
            executeCommand(RemoveLowerCommand::new, new Object[]{movie});
            JOptionPane.showMessageDialog(MainWindow.this, curBundle.getString("removal_all"),
                    curBundle.getString("removal_title"), JOptionPane.INFORMATION_MESSAGE);
        }

        public void removeById(int id) {
            var res = executeCommand(RemoveByIdCommand::new, new Object[]{id});
            if (res.getError().isEmpty()) {
                JOptionPane.showMessageDialog(MainWindow.this, curBundle.getString("removal_succeed"),
                        curBundle.getString("removal_title"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(MainWindow.this,
                        curBundle.getString("removal_failed"),
                        curBundle.getString("removal_title"), JOptionPane.ERROR_MESSAGE);
            }
        }

        public void removeByGP(int gp) {
            var res = executeCommand(RemoveAllByGoldenPalmCountCommand::new, new Object[]{gp});
            JOptionPane.showMessageDialog(MainWindow.this,
                    curBundle.getString("removal_all"),
                    curBundle.getString("removal_title"), JOptionPane.INFORMATION_MESSAGE);
        }

        public void history() {
            StringBuilder res = new StringBuilder();
            for (Command i : managers.history) {
                res.append(i.getName()).append("\n");
            }
            res.append("\n");
            textArea.setText(String.valueOf(res));

            executeCommand(HistoryCommand::new);
        }

        public void executeScript(File file) {
            StringBuilder writer = new StringBuilder();

            try {
                int local_recur_param = checkRecursion(Path.of(file.getAbsolutePath()), new ArrayDeque<>(), 0);
                if (recur_param == 0) recur_param = local_recur_param;

                BufferedReader bufReader = new BufferedReader(new FileReader(file));

                String temp;
                while ((temp = bufReader.readLine()) != null) {
                    if (temp.strip().startsWith("execute_script")) { // && temp.strip().substring(14).strip().startsWith(args[0])
                        cur_recur_param++;
                        if (cur_recur_param == recur_param) {
                            JOptionPane.showMessageDialog(MainWindow.this,
                                    curBundle.getString("recursion_error"),
                                    curBundle.getString("removal_title"), JOptionPane.INFORMATION_MESSAGE);
                            recur_param = 0;
                            cur_recur_param = 0;
                            break;
                        }

                    }
                    writer.append(temp).append("\n");
                }
                writer.append("$$$");

                CharArrayReader car = new CharArrayReader(writer.toString().toCharArray());

//                inputManager.setTemporaryInput(new BufferedReader(car));
                setInput(new InputManager(new BufferedReader(car)));

            } catch (FileNotFoundException e) {
                throw new FileException("Нет файла с указанным именем");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            while (true) {
                try {
                    nextCommand();
                    System.out.println("A");
                } catch (WrongArgumentException e) {
                    outputManager.print(e.toString());
                } catch (InterruptException e) {
                    outputManager.print("Ввод данных остановлен.");
                } catch (NoSuchCommandException e) {
                    outputManager.print("Нет такой команды в доступных.");
                } catch (RecursionException e) {
                    outputManager.print("Рекурсия в исполняемом файле.");
                    break;
                } catch (FileException e) {
                    outputManager.print(e.getMessage());
                } catch (ConnectionsFallsExcetion e) {
                    outputManager.print("Произошел разрыв соединения с сервером.");
                    break;
                } catch (RuntimeException e) {
                    System.out.println("Непредвиденная ошибка в ходе выполнения программы.");
                    System.out.println(e);
                    outputManager.print(e);
                    outputManager.print(Arrays.toString(e.getStackTrace()));
                    break;
                } catch (Exception e) {
                    break;
                }
            }

            loadData();
        }

        private int checkRecursion(Path path, ArrayDeque<Path> stack, int j) throws IOException {
            int i = 0;

            if (stack.contains(path)) return j;
            stack.addLast(path);
            String str = Files.readString(path);

            Pattern pattern = Pattern.compile("execute_script .*");
            var patternMatcher = pattern.matcher(str);
            while (patternMatcher.find()) {
                i++;
                Path newPath = Path.of(patternMatcher.group().split(" ")[1]);
//            if(checkRecursion(newPath, stack, i) != 0) return i;
                int a = checkRecursion(newPath, stack, i);
                if (a != 0) return a + j;
            }
            stack.removeLast();
            return 0;
        }

    }

    public MainWindow(ManagersContainer managersContainer) {
        this.managers = managersContainer;
        curBundle = ResourceBundle.getBundle("gui", managers.getCurrentLocale());
        formatter = DateTimeFormatter.ofPattern(curBundle.getString("date.format"));
//        switchLocale(managers.getCurrentLocale());
        receiver = new Receiver();

        authentication();

        setName(curBundle.getString("window_title"));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setSize(700, 700);
        setLocationRelativeTo(null);
        setContentPane(mainPanel);

//        initData();
//        initText();
//        initGraphics();

        language.setModel(new DefaultComboBoxModel(managers.enabledLocales));
        language.setRenderer(new LocaleListCellRenderer());

        filterComboBox.setModel(new DefaultComboBoxModel(tableModel.columnNames));
//        filterComboBox.setRenderer(new LocaleListCellRenderer());
        filterField.setColumns(20);

        warningsLabel.setForeground(Color.RED);

        // кнопки
        {
            // создание нового фильма
            createButton.addActionListener(e -> create());
            // изменение выделенного фильма
            editButton.addActionListener(e -> edit());
            // удаление выделенного фильма
            deleteButton.addActionListener(e -> removeMovie());
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
            // выйти из аккаунта
            logOutButton.addActionListener(e -> logOut());

            // при изменении размера окна, изменять таблицу
            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    adjustColumnWidths();
                }
            });
        }

        //
        // фоновое обновление данных раз в 10 секунд
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        int initialDelay = 15; // начальная задержка
        int period = 15; // интервал между выполнениями

        scheduler.scheduleAtFixedRate(() -> {
            loadData();
//            System.out.println("scheduled update");
        }, initialDelay, period, TimeUnit.SECONDS);
    }

    private void showCommands() {
        var dialog = new CommandsDialog(this, curBundle, receiver);
        dialog.setVisible(true);
    }

    private void graphicsArea() {
        CardLayout cl = (CardLayout) (centerPanel.getLayout());
        cl.next(centerPanel);
        graphicsMode = (graphicsMode + 1) % 2;
        visualizeButton.setText(curBundle.getString("main_vis_button" + graphicsMode));

        // анимация
        if (graphicsMode == 1)
            visualizationPanel.update(data);
    }

    private void removeMovie() {
        Movie movie;
        if (graphicsMode == 0) {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                movie = data.get(table.convertRowIndexToModel(selectedRow));
            } else {
                JOptionPane.showMessageDialog(this, curBundle.getString("unselected_movie"),
                        curBundle.getString("removal_title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            movie = visualizationPanel.getSelected();
            if (movie == null) {
                JOptionPane.showMessageDialog(this, curBundle.getString("unselected_movie"),
                        curBundle.getString("removal_title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Response result = receiver.executeCommand(RemoveByIdCommand::new, new Object[]{movie.getId()});
        if (result == null) {
            return;
        }

        if (result.getError().isEmpty()) {
            JOptionPane.showMessageDialog(this, curBundle.getString("removal_succeed"),
                    curBundle.getString("removal_title"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, curBundle.getString("access_error"),
                    curBundle.getString("removal_title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void edit() {
        Movie movie;
        if (graphicsMode == 0) {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int modelRow = table.convertRowIndexToModel(selectedRow);
                movie = data.get(modelRow);
            } else {
                JOptionPane.showMessageDialog(this, curBundle.getString("unselected_movie"),
                        curBundle.getString("update_title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            System.out.println(0);
        } else {
            movie = visualizationPanel.getSelected();
            if (movie == null) {
                JOptionPane.showMessageDialog(this, curBundle.getString("unselected_movie"),
                        curBundle.getString("removal_title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // если недостаточно прав для изменения
        if (!Objects.equals(movie.getCreator(), managers.getSession().getUser().getLogin())) {
            JOptionPane.showMessageDialog(this, curBundle.getString("access_error"),
                    curBundle.getString("update_title"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        var dialog = new CreationDialog(this, curBundle, movie);
        dialog.setVisible(true);

        Movie result = dialog.getResult();

        if (result != null) {
            var response = receiver.executeCommand(UpdateCommand::new, new Object[]{movie.getId(), result});

            System.out.println(response);

            if (response.getError().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        curBundle.getString("update_succeed"),
                        curBundle.getString("update_title"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        curBundle.getString(response.getError().get().getMessage()),
                        curBundle.getString("update_title"), JOptionPane.ERROR_MESSAGE);
            }

//                loadData();
        }
    }

    private void create() {
        var dialog = new CreationDialog(this, curBundle);
        dialog.setVisible(true);

        Movie result = dialog.getResult();

        if (result != null) {
            var command = new AddCommand(new Object[]{});
            command.setArgs(new Object[]{command, managers.getSession().getUser(), result});

            var response = receiver.executeCommand(AddCommand::new, new Object[]{result});

            if (response.getError().isEmpty()) {
                var a = data.stream().filter(x -> Objects.equals(x.getName(), result.getName())).findFirst();
                a.ifPresent(movie -> JOptionPane.showMessageDialog(this,
                        curBundle.getString("movie_creation_message") + " " + movie.getId(),
                        curBundle.getString("creation_title"), JOptionPane.INFORMATION_MESSAGE));
            } else {
                JOptionPane.showMessageDialog(this,
                        curBundle.getString(response.getError().get().getMessage()),
                        curBundle.getString("creation_title"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void logOut() {
        managers.setSession(null);
        authentication();
    }

    protected void authentication() {
        setVisible(false);
        var authForm = new AuthenticationForm(managers, this);
        authForm.setVisible(true);

        if (authForm.isOk) {
            initData();
            initText();
            initGraphics();

            setVisible(true);
        } else {
            close();
        }
    }

    protected class MovieTableModel extends AbstractTableModel {
        private String[] columnNames;
        private DateTimeFormatter formatter;

        protected int[] minColumnWidths;

        public MovieTableModel(String[] columnNames, DateTimeFormatter formatter) {
            this.columnNames = columnNames;
            this.formatter = formatter;
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Movie movie = data.get(rowIndex);
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

        public void updateMovies() {
            fireTableDataChanged();
        }
    }

    private int[] calculateColumnWidths() {
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
                int width = c.getMinimumSize().width + table.getIntercellSpacing().width;
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
                    .getMinimumSize().width + table.getIntercellSpacing().width;

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

        tableModel = new MovieTableModel(initColumns(), formatter);
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
                } catch (PatternSyntaxException e) {
                    e.printStackTrace();
                    return;
                }
                sorter.setRowFilter(rf);
            }
        });

        tableModel.minColumnWidths = calculateColumnWidths();
        adjustColumnWidths();

        graphicsMode = 0;
    }

    // загрузка данных о фильмах с сервера
    private void loadData() {
        var requestManager = managers.getRequestManager();
        // запрос на получение коллекции фильмов
        var getDataRequest = new GetDataRequest();
        requestManager.makeRequest(getDataRequest);
        Response response;
        try {
            response = requestManager.getResponse();
        } catch (PortUnreachableException e) {
            serverUnavailable();
            return;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        data = new ArrayList<>(List.of(((GetDataResponse) response).getData()));

        if (!warningsLabel.getText().isEmpty())
            warningsLabel.setText("");

        if (tableModel != null) {
            tableModel.updateMovies();
            adjustColumnWidths();
        }
        if (graphicsMode == 1)
            visualizationPanel.update(data);
    }

    private void initText() {
        info.setText(curBundle.getString("main_info_label") + " " + managers.getSession().getUser().getLogin());

        createButton.setText(curBundle.getString("main_create_button"));
        editButton.setText(curBundle.getString("main_edit_button"));
        deleteButton.setText(curBundle.getString("main_delete_button"));
        commandsButton.setText(curBundle.getString("main_command_button"));
        visualizeButton.setText(curBundle.getString("main_vis_button" + graphicsMode));
        logOutButton.setText(curBundle.getString("main_logout_button"));

        tableLabel.setText(curBundle.getString("main_table_label"));
        filterLabel.setText(curBundle.getString("main_filter_label"));

        tableModel.setColumns(initColumns());
        tableModel.fireTableStructureChanged();

        filterComboBox.setModel(new DefaultComboBoxModel(tableModel.columnNames));
    }

    private void initGraphics() {
        try {
            graphicsPanel.remove(0);
        } catch (IndexOutOfBoundsException ignored) {
        }

        visualizationPanel = new VisualizationPanel(data);

        JScrollPane scrollPane = new JScrollPane(visualizationPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
//        scrollPane.setWheelScrollingEnabled(false);

        MouseAdapter ma = new MouseAdapter() {
            private Point origin;

            @Override
            public void mousePressed(MouseEvent e) {
                origin = new Point(e.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                origin = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (origin != null) {
                    JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, visualizationPanel);
                    if (viewPort != null) {
                        int deltaX = origin.x - e.getX();
                        int deltaY = origin.y - e.getY();

                        Rectangle view = viewPort.getViewRect();
                        view.x += (int) Math.round(deltaX * 1.5);
                        view.y += (int) Math.round(deltaY * 1.5);

                        visualizationPanel.scrollRectToVisible(view);
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                for (Movie obj : visualizationPanel.objects) {
                    if (visualizationPanel.containsMovie(obj, e.getX(), e.getY())) {
                        visualizationPanel.selectedObject = obj;
                        JOptionPane.showMessageDialog(null, obj.toString(),
                                curBundle.getString("movie_info"),
                                JOptionPane.INFORMATION_MESSAGE);
                        visualizationPanel.repaint();
                        return;
                    }
                }
                visualizationPanel.selectedObject = null;
            }
        };

        visualizationPanel.addMouseListener(ma);
        visualizationPanel.addMouseMotionListener(ma);
//        scrollPane.addMouseWheelListener(mouseWheel);

        graphicsPanel.add(scrollPane);

        JViewport viewport = scrollPane.getViewport();
        viewport.setViewPosition(new Point(500, 300));

        CardLayout cl = (CardLayout) (centerPanel.getLayout());
        cl.show(centerPanel, "table");
    }

    private String[] initColumns() {
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

    private void serverUnavailable() {
        warningsLabel.setText(curBundle.getString("server_unavailable"));
    }

    private void close() {
        dispose();
        System.exit(0);
    }
}
