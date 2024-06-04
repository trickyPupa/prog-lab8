package gui;

import client.*;
import common.OutputManager;
import common.abstractions.AbstractAuthenticationReceiver;
import common.abstractions.AbstractReceiver;
import common.abstractions.IOutputManager;
import common.exceptions.*;
import exceptions.ConnectionsFallsExcetion;
import network.ConnectionRequest;
import network.ConnectionResponse;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class MainWindow extends JFrame {
    private JTable table;
    private JPanel mainPanel;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JPanel commands;
    private JButton button4;
    private JButton button5;
    private JPanel helping;
    private JTextField dataInput;
    private JLabel info;

    public MainWindow() {
        setName("movie app");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 300);
        setLocationRelativeTo(null);

        var authForm = new AuthenticationForm();

        setContentPane(authForm.getPanel());
        setVisible(true);
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
