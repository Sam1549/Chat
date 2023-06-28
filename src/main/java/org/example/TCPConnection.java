package org.example;

import java.io.*;
import java.net.Socket;


public class TCPConnection {
    private final Socket socket;
    private final Thread rThread;
    private final TCPConnectionObserver observer;
    private final BufferedReader in;
    private final BufferedWriter out;

    private String name;


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
                    observer.tcpSetName(TCPConnection.this, in.readLine());
                    while (!rThread.isInterrupted()) {
                        observer.receiveString(TCPConnection.this, in.readLine());
                    }
                } catch (NullPointerException ignored) {

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
