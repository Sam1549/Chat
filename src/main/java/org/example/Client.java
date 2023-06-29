package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Scanner;

public class Client implements TCPConnectionObserver, Runnable {
    private static final String exit = "/exit";
    private TCPConnection connection;

    private Scanner scanner;
    private Thread thread;


    public static void main(String[] args) {
        new Client();
    }

    private Client() {
        try {
            scanner = new Scanner(System.in);
            Socket socket = getSocket();
            assert socket != null;
            connection = new TCPConnection(this, socket);
            thread = new Thread(this);
            thread.start();
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
//            tcpConnection.disconnect();
        } else {
            printMsg(value);
        }
    }

    @Override
    public void tcpDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close");
        tcpConnection.disconnect();
        thread.interrupt();


    }

    @Override
    public void tcpException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception: c2" + e);
    }

    @Override
    public void tcpSetName(TCPConnection tcpConnection, String name) {
        tcpConnection.setName(name);
    }

    private synchronized static void printMsg(String msg) {
        if (!msg.isEmpty()) System.out.println(msg);
    }

    private Socket getSocket() {
        try (BufferedReader bufferedReader = Files.newBufferedReader(Path.of("./property.txt"))) {
            Properties properties = new Properties();
            properties.load(bufferedReader);
            return new Socket(properties.getProperty("host"), Integer.parseInt(properties.getProperty("port")));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {
        while (!thread.isInterrupted()) {
            String text = this.scanner.nextLine();
            if (text.equalsIgnoreCase(exit)) {
                this.connection.disconnect();
                thread.interrupt();
            } else {
                this.connection.sendMsg(text);
            }
        }
    }
}
