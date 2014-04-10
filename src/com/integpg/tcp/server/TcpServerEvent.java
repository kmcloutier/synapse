package com.integpg.tcp.server;

import java.net.Socket;
import java.util.EventObject;

public class TcpServerEvent extends EventObject {

    private Socket _socket;

    public TcpServerEvent(Object source, Socket socket) {
        super(source);

        _socket = socket;
    }

    public Socket getSocket() {
        return _socket;
    }
}
