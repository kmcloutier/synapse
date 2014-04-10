package com.integpg.tcp;

import com.integpg.logger.FileLogger;
import com.integpg.system.JANOS;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.EventObject;



public abstract class TcpConnection {

    public String _filename;
    protected Socket _socket;
    private InputStream in;
    public OutputStream out;
    protected TcpConnectionListener _listener;
    private String _ipAddress;

    byte[] _sBytes = new byte[4096];
    int _sBytePos = 0;
    private byte[] inbuf = new byte[512];
    private int inbufPos, inbufLen;
    private String _terminationString = "\r\n";
    private int _terminationOffset = 0;
    private int count = 0;



    public TcpConnection(Socket socket) {
        _socket = socket;
        _ipAddress = _socket.getInetAddress().getHostAddress() + ":" + _socket.getPort();
        
        
          try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException ex) {
            FileLogger.error("Error in TCP Connection <init>: " + ex.getMessage());
        }
    }



    public Socket getSocket() {
        return _socket;
    }



    public String getIpAddress() {
        return _ipAddress;
    }



    protected abstract String getName();



    public void setListener(TcpConnectionListener listener) {
        _listener = listener;
    }



    public void init() {
        Thread thd = new Thread(new Runnable() {

            public void run() {
                handleConnection();
            }
        }, getIpAddress() + " handle " + getName());
        thd.setDaemon(true);
        thd.start();
    }



    protected abstract void handleConnectionLoop();



    private void handleConnection() {
        if (_listener != null) {
            _listener.connectionEstablished(new EventObject(this));
        }


        try {
            handleConnectionLoop();
        } catch (Exception ex) {
            FileLogger.error("Error handling socket: " + ex.getMessage());
        }

        close();
    }



    protected void close() {
        if (_socket != null) {
            try {
                _socket.close();
                _socket = null;

                if (_listener != null) {
                    _listener.connectionClosed(new EventObject(this));
                }
            } catch (IOException ex) {
                FileLogger.error("Error closing socket: " + ex.getMessage());
            }
        }
    }



    protected String processAscii(int readByte) throws Exception {
        // make local copy to increase speed
        String terminationString = _terminationString;
        _sBytePos = 0;

        try {
            char terminationChar = terminationString.charAt(_terminationOffset);
            while (true) {
                if (_sBytes.length == _sBytePos) {
                    byte[] sBytes = new byte[_sBytes.length + 64];
                    System.arraycopy(_sBytes, 0, sBytes, 0, count);
                    _sBytes = sBytes;
                }
                _sBytes[_sBytePos++] = (byte) readByte;


                if (readByte == terminationChar) {
                    _terminationOffset++;
                    if (_terminationOffset == terminationString.length()) {
                        break;
                    }
                    terminationChar = terminationString.charAt(_terminationOffset);
                } else {
                    _terminationOffset = 0;
                }

                // get next byte
                if (inbufPos == inbufLen) {
                    readByte = getByte();
                } else {
                    readByte = (inbuf[inbufPos++] & 0xff);
                }
            }

        } catch (IOException ex) {
            throw ex;
        }


        _terminationOffset = 0;
        String s;
        try {
            s = new String(_sBytes, 0, _sBytePos - terminationString.length());  //_asciiStringBuffer.toString().substring(0, _asciiStringBuffer.length() - terminationString.length());
        } catch (Exception ex) {
            System.out.println(Thread.currentThread().getName() + " " + new String(_sBytes, 0, _sBytePos) + " " + _sBytePos + " " + _terminationString.length());
            throw ex;
        }
        return s;
    }



    protected int getByte() throws IOException {
//        ensureHeartbeat();
//        System.out.println(inbufPos + " " + inbufLen + " " + inbuf[inbufPos]);

        if (inbufPos == inbufLen) {
            if (in == null) throw new IOException("Input Stream is null");


            long start = JANOS.uptimeMillis();
            try {
//                System.out.println(Thread.currentThread().getName() + " read from socket");
                inbufLen = in.read(inbuf, 0, inbuf.length);
            } catch (SocketTimeoutException ex) {
                if (JANOS.uptimeMillis() - start < 30000) {
                    FileLogger.error("Incorrect Socket Timeout");
                }
                throw ex;
            } finally {
                inbufPos = 0;
            }

//            System.out.println(Thread.currentThread().getName() + " bytes read: " + inbufLen + " total bytes read: " + totalBytesRead);
            if (inbufLen == -1) {
                throw new EOFException("Client Disconnected");
            }
        }

        if (inbufPos != inbufLen) {
//            totalBytesProvided++;
//            System.out.println(Thread.currentThread().getName() + " total bytes provided " + totalBytesProvided + " " + inbufPos + " " + inbufLen);
            return (inbuf[inbufPos++] & 0xff);
        }
        throw new IOException("no bytes available");
    }
}
