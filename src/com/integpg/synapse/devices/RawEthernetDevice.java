package com.integpg.synapse.devices;

import com.integpg.logger.FileLogger;
import java.io.IOException;
import java.net.Socket;



public class RawEthernetDevice extends Device {

    private String _ipAddress;
    private int _port;



    public RawEthernetDevice(String ipAddress, int port) {
        _ipAddress = ipAddress;
        _port = port;
    }



    public void send(byte[] bytes) {
        try {
            System.out.println("Send to " + _ipAddress + ":" + _port);


            Socket socket = new Socket(_ipAddress, _port);
            socket.getOutputStream().write(bytes);
            socket.close();
        } catch (IOException ex) {
            FileLogger.error("Error sending to raw ethernet device: " + ex.getMessage());
        }
    }
}
