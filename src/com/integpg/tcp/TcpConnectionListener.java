package com.integpg.tcp;

import java.util.EventObject;

public interface TcpConnectionListener {

    public void connectionEstablished(EventObject evt);

    public void connectionClosed(EventObject evt);

    public void messageReceived(MessageReceivedEvent evt);
}
