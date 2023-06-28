package org.example;

import javax.imageio.IIOException;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TCPConnection {
    private final Socket socket;
    private final Thread rThread;
    private final TCPConnectionObserver observer;
    private final BufferedReader in;
    private final BufferedWriter out;


    public TCPConnection(TCPConnectionObserver tcpConnectionObserver, String ipAddress, String port) throws IOException {
        this(tcpConnectionObserver, new Socket(ipAddress, Integer.parseInt(port)));
    }

    public TCPConnection(TCPConnectionObserver observer, Socket socket) throws IOException {
        this.observer = observer;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        rThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    observer.connectionReady(TCPConnection.this);
                    while (!rThread.isInterrupted()) {
                        observer.receiveString(TCPConnection.this, in.readLine());
                    }
                } catch (NullPointerException n) {

                } catch (Exception e) {
                    observer.tcpException(TCPConnection.this, e);
                } finally {
                    observer.tcpDisconnect(TCPConnection.this);
                }

            }
        });
        rThread.start();
    }

    public synchronized void sendMsg(String message) {
        try {
//            if (message.equalsIgnoreCase("/exit")) {
//                disconnect();
//            } else if
            if (!message.isEmpty()) {
                out.write(message + "\r\n");
                out.flush();
            }
        } catch (IOException e) {
            observer.tcpException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        rThread.interrupt();
        try {
            this.socket.close();
        } catch (IOException e) {
            observer.tcpException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}
