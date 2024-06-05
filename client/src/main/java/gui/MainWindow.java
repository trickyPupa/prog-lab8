package gui;

import client.*;
import common.commands.abstractions.Command;
import common.commands.implementations.ShowCommand;
import common.model.entities.Movie;
import network.CommandRequest;
import network.Response;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;

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

    protected class Receiver {
        ;
    }

    public MainWindow(ManagersContainer managersContainer) {
        this.managers = managersContainer;
        curBundle = ResourceBundle.getBundle("gui", managers.getCurrentLocale());

        setName("movie app");
        initData();
        initText();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setSize(700, 700);
        setLocationRelativeTo(null);

        authentication();
    }

    protected void authentication(){
        var authForm = new AuthenticationForm(managers);

        setContentPane(authForm.getPanel());
        setVisible(true);

        while(true){
            try {
                authForm.isOk.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (authForm.isOk){
                setContentPane(mainPanel);
                break;
            }
        }
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

    private void initData(){
        var tableModel = new MovieTableModel(data);

        table.setModel(tableModel);
    }

    private void loadData() {
        /*var requestManager = managers.getRequestManager();
        // сделать запрос
        // GetDataRequest GetDataResponse
        var getDataRequest = new CommandRequest(new ShowCommand(new Object[]{}), new ArrayList<>());
        requestManager.makeRequest(getDataRequest);
        Response response;
        try {
            response = requestManager.getResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        data = (Response) response.getData();*/

        data = new ArrayList<>();
        data.add(new Movie());
    }

    private void initText(){
        info.setText(curBundle.getString("main_info_label"));

        createButton.setText(curBundle.getString("main_create_button"));
        editButton.setText(curBundle.getString("main_edit_button"));
        deleteButton.setText(curBundle.getString("main_delete_button"));
        commandsButton.setText(curBundle.getString("main_command_button"));
        visualizeButton.setText(curBundle.getString("main_vis_button"));
    }

    protected void switchLocale(Locale locale){
        curBundle = ResourceBundle.getBundle("gui", locale);
        managers.setCurrentLocale(locale);
        initText();
    }

    /*public void start() {
        IOutputManager outputManager = new OutputManager();
        AbstractReceiver clientReceiver = new ClientReceiver(inputManager, outputManager);

        AbstractClientRequestManager clientRequestManager = new ClientRequestManager(HOST_NAME, PORT);
        AbstractAuthenticationReceiver receiver = new ClientAuthenticationReceiver(clientReceiver, clientRequestManager);

        ClientCommandHandler handler = new ClientCommandHandler(inputManager, outputManager, clientRequestManager,
                receiver);

        // соединение с сервером
        clientRequestManager.makeRequest(new ConnectionRequest());
        var answer = (ConnectionResponse) clientRequestManager.getResponse();
        outputManager.print(answer.getMessage());

        if (!answer.isSuccess()) {
            outputManager.print("Попробуйте позже. Завершение работы...");
            System.exit(0);
        }
        handler.setCommands(answer.getCommandList());
        outputManager.print("Начало работы.");

        // бесполезно
            *//*{ // отключение от сервера при экстренном завершении программы
                class MyHook extends Thread {
                    private AbstractClientRequestManager crm;

                    public MyHook(AbstractClientRequestManager crm) {
                        super();
                        this.crm = crm;
                    }

                    @Override
                    public void run() {
                        if (crm != null)
                            crm.makeRequest(new DisconnectionRequest());
                    }
                }

                Runtime.getRuntime().addShutdownHook(new MyHook(clientRequestManager));
            }*//*

        // основной блок выполнения команд
        while (true) {
            try {
                handler.nextCommand();

            } catch (WrongArgumentException e) {
                outputManager.print(e.toString());
            } catch (InterruptException e) {
                outputManager.print("Ввод данных остановлен.");
            } catch (NoSuchCommandException e) {
                outputManager.print("Нет такой команды в доступных.");
            } catch (RecursionException e) {
                outputManager.print("Рекурсия в исполняемом файле.");
            } catch (FileException e) {
                outputManager.print(e.getMessage());
            } catch (ConnectionsFallsExcetion e) {
                outputManager.print("Произошел разрыв соединения с сервером.");
                break;
            } catch (RuntimeException e) {
                System.out.println("Непредвиденная ошибка в ходе выполнения программы.");
                outputManager.print(e);
                outputManager.print(Arrays.toString(e.getStackTrace()));
//                    System.out.println("main catch runtime");

//                    clientRequestManager.makeRequest(new DisconnectionRequest());
                receiver.exit(null);
                throw e;
            }
        }
    *//*catch(UnknownHostException e){
        System.out.println("Адрес сервера не найден");
    }
    catch(PortUnreachableException e){
        System.out.println("Невозможно подключиться к заданному порту");
    }
    catch(IOException e){
        System.out.println("Ошибка при чтении данных");
        System.out.println(e);
        System.out.println(Arrays.toString(e.getStackTrace()));
//            System.out.println("main catch io");
    }
    catch(RuntimeException e){
        System.out.println("Непредвиденная ошибка в ходе выполнения программы.");
//            System.out.println(e.getMessage());
        System.out.println(e);
        e.printStackTrace();
    } finally{
        System.out.println("Завершение работы.");
    }*//*
    }*/
}
