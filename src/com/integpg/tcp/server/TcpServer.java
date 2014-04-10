package com.integpg.tcp.server;

import com.integpg.logger.FileLogger;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;



/**
 *
 * @author Kevin
 */
public class TcpServer
        implements Runnable {

    private String _name;
    private TcpServerListener _listener = null;
    private ServerSocket svrSocket;
    private Socket socket;
    private int _port;
    private Thread theServer;
    private boolean bServerRun = true;



    /**
     * Creates a new instance of TcpServer
     */
    public TcpServer(String name, int port) {
        _name = name;
        _port = port;
    }



    public void setTcpServerListener(TcpServerListener listener) {
        _listener = listener;
    }



    public void start() {
        /**
         * try to listen on the port. if we cant, that means another instance is. gracefully exit
         */
        try {
            svrSocket = new ServerSocket(_port);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to start the server: " + ex.getMessage());
        }

        try {
            // Start the server in background
            theServer = new Thread(this, _name + " " + _port);
            theServer.setDaemon(true);
            theServer.start();
        } catch (RuntimeException ex) {
            throw new RuntimeException("Unable to start the server: " + ex.getMessage());
        }
    }



    public void stop() throws IOException {
        bServerRun = false;
        svrSocket.close();

        try {
            theServer.interrupt();
            theServer.join();
        } catch (InterruptedException ex) {
            FileLogger.error("Error stopping server socket: " + ex.getMessage());
        }
    }



    public void run() {
        try {
            FileLogger.vital("Listening for clients on port " + _port + ".");

            // Listen for a Client connections
            while (bServerRun) { /* server runs forever */

                try {
                    socket = svrSocket.accept();

                    if (_listener != null) {
                        _listener.clientConnected(new TcpServerEvent(this, socket));
                    }

                } catch (IOException ex) {
                    FileLogger.error("Error accepting server socket connection: " + ex.getMessage());
                    bServerRun = false;
                } catch (Exception ex) {
                    FileLogger.error("Error accepting server socket connection: " + ex.getMessage());
                }

                System.gc();
            } /* server runs forever */

        } /* An Exception is normally thrown here if we are stopping the service
         *  so the event is logged ONLY if the server is still supposed to be running.
         */ catch (Exception ex) {
            FileLogger.error("Error accepting server socket connection: " + ex.getMessage());
            bServerRun = false;
        }

        try {
            svrSocket.close();
        } catch (IOException ex) {
            FileLogger.error("Error closing server socket: " + ex.getMessage());
        }
        svrSocket = null;
    }
}
