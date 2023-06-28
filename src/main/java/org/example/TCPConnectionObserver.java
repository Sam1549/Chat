package org.example;

public interface TCPConnectionObserver {
    // соединение запущено и готово к работе
    void connectionReady(TCPConnection tcpConnection);

    // соединение приняло входящую строчку
    void receiveString(TCPConnection tcpConnection, String value);

    // соединение разорвано
    void tcpDisconnect(TCPConnection tcpConnection);

    // исключения
    void tcpException(TCPConnection tcpConnection, Exception e);

    // Присвоить ник
    void tcpSetName(TCPConnection tcpConnection, String name);


}

