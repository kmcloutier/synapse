package com.integpg.tcp;

import com.integpg.logger.FileLogger;
import java.net.Socket;



public class AsciiConnection extends TcpConnection {

    private int _readTimeout;



    public AsciiConnection(Socket socket, int readTimeout) {
        super(socket);


        _readTimeout = readTimeout;
    }



    protected String getName() {
        return "Ascii Client";
    }



    protected void handleConnectionLoop() {
        try {
            out.write("hello".getBytes());


            // set the read timeout if non zero
            if (_readTimeout != 0) _socket.setSoTimeout(_readTimeout);


            while (_socket != null) {
                int readByte = getByte();

                String command = processAscii(readByte);

                if (_listener != null) {
                    _listener.messageReceived(new MessageReceivedEvent(this, command));
                }
            }
        } catch (Exception ex) {
            FileLogger.error("Error in client connection loop: " + ex.getMessage());
            close();
        }
    }

}
