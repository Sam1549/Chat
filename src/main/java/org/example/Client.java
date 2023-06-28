package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Scanner;

public class Client implements TCPConnectionObserver {
    private static String exit = "/exit";
    private final String IP_ADDRESS = "192.168.31.109";
    private final String PORT = "8189";
    private Socket socket = getSocket();
    private TCPConnection connection;

    private Scanner scanner;


    public static void main(String[] args) {
        Client client = new Client();
        while (true) {
            String text = client.scanner.nextLine();
            if (text.equalsIgnoreCase(exit)) {
                client.connection.disconnect();
                break;
            } else {
                client.connection.sendMsg(text);
            }
        }
    }

    private Client() {
        try {
            scanner = new Scanner(System.in);
            connection = new TCPConnection(this, socket);
        } catch (IOException e) {
            printMsg("Connection exception: c1 " + e);
        }
    }

    @Override
    public void connectionReady(TCPConnection tcpConnection) {
        printMsg("Connection ready...");
    }

    @Override
    public void receiveString(TCPConnection tcpConnection, String value) {
        if (value.equals(exit)) {
            tcpConnection.disconnect();
        } else {
            printMsg(value);
        }
    }

    @Override
    public void tcpDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close");
        tcpConnection.disconnect();

    }

    @Override
    public void tcpException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception: c2" + e);
    }

    private synchronized static void printMsg(String msg) {
        if (!msg.isEmpty() && msg != null) System.out.println(msg);
    }

    private Socket getSocket() {
        try (BufferedReader bufferedReader = Files.newBufferedReader(Path.of("./property.txt"))) {
            Properties properties = new Properties();
            properties.load(bufferedReader);
            Socket socket1 = new Socket(properties.getProperty("host"), Integer.parseInt(properties.getProperty("port")));
            return socket1;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
