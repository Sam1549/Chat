package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Properties;

public class Server implements TCPConnectionObserver {
    private Socket clientSocket;
    private Logger logger = Logger.getInstance();
    private ArrayList<TCPConnection> connections = new ArrayList<>();
    private static DateTimeFormatter time = DateTimeFormatter.ofPattern("[HH:mm:ss YYYY]");
    private int port = getPort();


    public static void main(String[] args) {
        new Server();
    }


    private Server() {
        System.out.println("Server running");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    clientSocket = serverSocket.accept();
                    new TCPConnection(this, clientSocket);
                } catch (SocketException e) {
                    System.out.println("TCPConnection exception s1: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void connectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        sendToAllConnections("Client connected: " + tcpConnection); // toString
        tcpConnection.sendMsg("Здравствуйте, введите, пожалуйста, ваш никнейм!");
    }

    @Override
    public synchronized void receiveString(TCPConnection tcpConnection, String value) {
        if (value.equals("/exit")) {
            tcpConnection.disconnect();
        } else {
            sendToAllConnections(tcpConnection.getName() + ": " + value);
        }
    }

    @Override
    public synchronized void tcpDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections("Client disconnect: " + tcpConnection); // toString
        tcpConnection.disconnect();
    }

    @Override
    public synchronized void tcpException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception s2 " + e);
//        tcpConnection.disconnect();
    }

    @Override
    public void tcpSetName(TCPConnection tcpConnection, String name) {
        tcpConnection.setName(name);
        tcpConnection.sendMsg("Ваш ник: " + name);
    }

    private void sendToAllConnections(String text) {
        if (!text.isEmpty() && text != null) {
            logger.log("[" + time.format(LocalDateTime.now()) + "] " + text);
            System.out.println("[" + time.format(LocalDateTime.now()) + "] " + text);

            int cnt = connections.size();
            for (int i = 0; i < cnt; i++) {
                connections.get(i).sendMsg("[" + time.format(LocalDateTime.now()) + "] " + text);
            }
        }
    }

    private int getPort() {
        try (BufferedReader bufferedReader = Files.newBufferedReader(Path.of("./settings.txt"))) {
            Properties properties = new Properties();
            properties.load(bufferedReader);
            port = Integer.parseInt(properties.getProperty("port"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return port;
    }

}
