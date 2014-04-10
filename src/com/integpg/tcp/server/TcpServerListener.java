package com.integpg.tcp.server;

public interface TcpServerListener {

    public void clientConnected(TcpServerEvent evt);
}
