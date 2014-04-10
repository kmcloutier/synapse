package com.integpg.synapse;

import com.integpg.synapse.actions.WaitAction;
import com.integpg.synapse.actions.Action;
import com.integpg.synapse.actions.SendAction;
import com.integpg.synapse.actions.OutputAction;
import com.integpg.synapse.triggers.Trigger;
import com.integpg.synapse.triggers.TimerTrigger;
import com.integpg.synapse.devices.RawEthernetDevice;
import com.integpg.logger.FileLogger;
import com.integpg.synapse.actions.CompositeAction;
import com.integpg.synapse.devices.Device;
import com.integpg.system.ArrayUtils;
import com.integpg.system.MessagePump;
import com.integpg.system.SystemMsg;
import com.integpg.tcp.AsciiConnection;
import com.integpg.tcp.MessageReceivedEvent;
import com.integpg.tcp.TcpConnectionListener;
import com.integpg.tcp.server.TcpServer;
import com.integpg.tcp.server.TcpServerEvent;
import com.integpg.tcp.server.TcpServerListener;
import com.integpg.utils.StringUtils;
import java.io.File;
import java.net.Socket;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Json;
import java.util.NoSuchElementException;



public class Synapse implements TcpConnectionListener {
    
    public static Synapse INSTANCE;
    
    private static final int APP_ID = 3000;
    
    private MessagePump _msgPump = new MessagePump();
    private Hashtable _clientConnections = new Hashtable();
    
    private Hashtable _devices = new Hashtable();
    private Hashtable _actions = new Hashtable();
    private Hashtable _triggers = new Hashtable();
    
    private String _string;
    private int _pos;
    
    
    
    public static void main(String[] args) {
        FileLogger.setAppName("Synapse");
        FileLogger.vital("***** ***** *****");
        FileLogger.vital("Synapse v." + AssemblyInfo.getVersion());
        FileLogger.setDebugLevel(FileLogger.ALL);
        
        
        INSTANCE = new Synapse();
        INSTANCE.init();
    }
    
    
    
    private void init() {
        String jsonString = "{"
                + "\"Actions\":["
                + "{\"ID\":\"1111\",\"Type\":\"WaitAction\",\"Duration\":1500},"
                + "{\"ID\":\"a65c\",\"Type\":\"OutputAction\",\"Action\":\"Close Pulse\",\"Channel\":1},"
                + "{\"ID\":\"dead\",\"Type\":\"OutputAction\",\"Action\":\"Close Pulse\",\"Channel\":2},"
                + "{\"ID\":\"babe\",\"Type\":\"OutputAction\",\"Action\":\"Close Pulse\",\"Channel\":3},"
                + "{\"ID\":\"feed\",\"Type\":\"OutputAction\",\"Action\":\"Close Pulse\",\"Channel\":4},"
                + "{\"ID\":\"deaf\",\"Type\":\"OutputAction\",\"Action\":\"Close Pulse\",\"Channel\":5},"
                + "{\"ID\":\"eee3\",\"Type\":\"OutputAction\",\"Action\":\"Close Pulse\",\"Channel\":6},"
                + "{\"ID\":\"9874\",\"Type\":\"OutputAction\",\"Action\":\"Close Pulse\",\"Channel\":7},"
                + "{\"ID\":\"bbbb\",\"Type\":\"OutputAction\",\"Action\":\"Close Pulse\",\"Channel\":8},"
                + "{\"ID\":\"cea3\",\"Type\":\"CompositeAction\",\"Actions\":[\"a65c\",\"1111\",\"dead\",\"1111\",\"babe\",\"1111\",\"feed\"]},"
                + "{\"ID\":\"cea4\",\"Type\":\"CompositeAction\",\"Actions\":[\"deaf\",\"eee3\",\"9874\",\"bbbb\"]},"
                + "{\"ID\":\"cea6\",\"Type\":\"CompositeAction\",\"Actions\":[\"bbbb\",\"9874\",\"eee3\",\"deaf\"]},"
                + "{\"ID\":\"cea5\",\"Type\":\"CompositeAction\",\"Actions\":[\"cea3\",\"cea4\",\"1111\",\"cea6\",\"1111\",\"cea4\",\"1111\",\"cea6\"]}"
                + "]"
                + "}";
        System.out.println(jsonString);


        // load json config
        File jsonConfigFile = new File("synapse.json");
        Json json = new Json(jsonString);
        json.save(jsonConfigFile);


//        Json actionJson = new Json();
//        actionJson.put("Type", "Action");
//        actionJson.put("Action", "Close Pulse");
//        actionJson.put("Channel", 1);
//
//        json = new Json();
//        json.put("Actions", new Json[]{actionJson});
////        json.save(jsonConfigFile);
//        System.out.println(json.toString());
        if (jsonConfigFile.exists()) {
            json = new Json(jsonConfigFile);
            System.out.println(json);
            
            
            Action.LoadActions(json);
            
            
            Json[] actions = (Json[]) json.get("Actions");
            for (int i = 0; i < actions.length; i++) {
                try {
                    Json actionJson = actions[i];
                    System.out.println(actionJson);
                    
                    
                    String actionType = (String) actionJson.get("Type");
                    System.out.println(actionType);
                    
                    
                    if (actionType.equalsIgnoreCase("waitaction")) {
                        WaitAction action = new WaitAction(actionJson);
//                        action.execute();
                    } //
                    else if (actionType.equalsIgnoreCase("outputaction")) {
                        OutputAction action = new OutputAction(actionJson);
//                        action.execute();
                    } //
                    else if (actionType.equalsIgnoreCase("compositeaction")) {
                        CompositeAction action = new CompositeAction(actionJson);
//                        action.execute();
                    }
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
            
            try {
                Action.execute("cea5");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        
        initMessagePump();


        // start tcp server
        startTcpServer();
        
        
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    
    
    private void startTcpServer() {
        TcpServer tcpServer = new TcpServer("Control Server", 9100);
        tcpServer.setTcpServerListener(new TcpServerListener() {
            
            
            public void clientConnected(TcpServerEvent evt) {
                Socket socket = evt.getSocket();
                AsciiConnection client = new AsciiConnection(socket, 0);
                client.setListener(INSTANCE);
                client.init();
            }
            
        });
        tcpServer.start();
    }

    //

    
    private String getNextToken(String delim, boolean skipDelim) throws Exception {
        int pos;
        do {
            pos = _string.indexOf(delim, _pos);
//            System.out.println(_pos + " " + pos);
            if (pos == -1) {
                if (_pos < _string.length()) pos = _string.length();
                else throw new NoSuchElementException();
            }
            if (pos > _pos) break;
            _pos++;
        } while (true);
        
        String token = _string.substring(_pos, pos);
        _pos = pos;
        if (skipDelim) _pos += delim.length();
        token = token.trim();
        System.out.println(token);
        return token;
    }
    
    
    
    private String getName() throws Exception {
        String name = getNextToken(":", true);
        if (name == null) throw new Exception("Invalid Name");
        return name.trim();
    }
    
    
    
    private void updateItem(String description) {
        try {
//            _string = description;
//            _pos = 0;
//
//
//            String name = getName();
//
//
//            Trigger trigger = null;
//            while (true) {
//                String token = getNextToken(" ", true);
//                if (token == null) throw new Exception("Invalid Token");
//
//                if (token.equalsIgnoreCase("raw_ethernet")) {
//                    String ipAddress = getNextToken(" ", true);
//                    String port = getNextToken(" ", true);
//
//                    System.out.println(name + " " + token + " " + ipAddress + ":" + port);
//
//
//                    RawEthernetDevice red = new RawEthernetDevice(ipAddress, Integer.parseInt(port));
//                    _devices.put(name, red);
//
//                    break;
//
//                } else if (token.equalsIgnoreCase("wait")) {
//                    String duration = getNextToken(" ", true);
//                    if (duration == null) throw new Exception("Invalid Duration");
//                    double d = Double.valueOf(duration).doubleValue();
//
//                    WaitAction wa = new WaitAction(d);
//                    _actions.put(name, wa);
//
//                    break;
//
//                } else if (token.equalsIgnoreCase("toggle")) {
//                    String channel = getNextToken(" ", true);
//                    if (channel == null) throw new Exception("Invalid Channel");
//
//                    System.out.println(name + " " + token + " " + channel);
//
//
//                    OutputAction oa = new OutputAction("t" + channel.charAt(4));
//                    _actions.put(name, oa);
//
//                    break;
//
//                } else if (token.equalsIgnoreCase("pulse")) {
//                    String channel = getNextToken(" ", true);
//                    if (channel == null) throw new Exception("Invalid Channel");
//
//                    System.out.println(name + " " + token + " " + channel);
//
//
//                    OutputAction oa = new OutputAction("cp" + channel.charAt(4) + "=1000");
//                    _actions.put(name, oa);
//
//                    break;
//
//                } else if (token.equalsIgnoreCase("on")) {
//
//                    String channel = getNextToken(" ", true);
//                    if (channel == null) throw new Exception("Invalid Channel");
//
//                    System.out.println(name + " " + token + " " + channel);
//
//
//                    break;
//
//                } else if (token.equalsIgnoreCase("every")) {
//                    String interval = getNextToken(" ", true);
//                    if (interval == null) throw new Exception("Invalid Duration");
//                    double d = Double.valueOf(interval).doubleValue();
//
//                    String timeType = getNextToken(" ", true);
//
//
//                    String doToken = getNextToken(" ", true);
//
//
//                    String doName = getNextToken("\"", true);
//                    System.out.println("do name: " + doName);
//
//
//                    System.out.println(name + " " + token + " " + interval + " " + timeType + " " + doName);
//
//
//                    if (_triggers.containsKey(name)) {
//                        Trigger a = (Trigger) _triggers.get(name);
//                        if (a instanceof TimerTrigger) {
//                            ((TimerTrigger) a).cancel();
//                        }
//                    }
//
//                    trigger = new TimerTrigger(d, doName);
//                    trigger.start();
//
//
//                    _triggers.put(name, trigger);
//                    break;
//
//                } else if (token.equalsIgnoreCase("send")) {
//                    String to = getNextToken(" ", true);
//
//                    String deviceName = getNextToken(":", true);
//
//                    String dataString = description.substring(_pos).trim();
//
//                    System.out.println(name + " " + deviceName + " : " + dataString);
//
//                    SendAction sa = new SendAction(deviceName, dataString);
//                    _actions.put(name, sa);
//
//                    break;
//
//                } else {
//                    throw new Exception("Unknown action");
//                }
//
//            }

        } catch (Exception ex) {
            FileLogger.error("Error updating action: " + ex.getMessage());
        }
    }
    
    
    
    private void sendItems() {
        String[] items = new String[_actions.size()];
        Enumeration e = _actions.keys();
        int i = 0;
        while (e.hasMoreElements()) {
            items[i++] = (String) e.nextElement();
        }
        
        Json json = new Json();
        json.put("Message", "action-items");
        json.put("Items", items);
        
        
        items = new String[_triggers.size()];
        e = _triggers.keys();
        i = 0;
        while (e.hasMoreElements()) {
            items[i++] = (String) e.nextElement();
        }
        
        json = new Json();
        json.put("Message", "action-items");
        json.put("Items", items);
        
    }
    
    
    
    public void trigger(String actionName) {
        String[] actions = StringUtils.split(actionName, ",");
        for (int i = 0; i < actions.length; i++) {
            try {
                actionName = actions[i].trim();
                System.out.println("Trigger: " + actionName);
                if (!_actions.containsKey(actionName)) throw new Exception("Action does not exist");
                Action action = (Action) _actions.get(actionName);
                action.execute();
            } catch (Exception ex) {
                FileLogger.error("Error triggering action " + actionName + ": " + ex.getMessage());
            }
        }
    }



    // MESSAGE PUMP ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Handle the Message pump
     */
    private void initMessagePump() {
        FileLogger.vital("Init message pump");
        Thread thd = new Thread(new Runnable() {
            public void run() {
                _msgPump.open();
                
                
                try {
                    FileLogger.vital("Message pump ready");
                    // run the message pump forever
                    while (true) {
                        // read a message from the message pump.  we only care about types for this application id.
                        SystemMsg msg = _msgPump.getMessage(APP_ID);

                        // we have received a message meant for us.  now process it and respond accordingly
                        ProcessSystemMsg(msg);
                    } // while true
                } catch (Exception ex) {
                    FileLogger.error("Error processing message pump: " + ex.toString());
                }
            }
        });
        thd.setDaemon(true);
        thd.start();
    }



    /**
     * Here we process what was received on the message pump
     *
     * @param msg
     */
    private void ProcessSystemMsg(SystemMsg msg) {
        Json json = null;


        // get the source information
        byte[] sender = new byte[6];
        ArrayUtils.arraycopy(msg.msg, 0, sender, 0, 6);
        String client = getClientString(sender);
        
        
        if (!_clientConnections.containsKey(client)) {
            _clientConnections.put(client, sender);
        }


        // get a string from the message received
        String message = new String(msg.msg, 6, msg.msg.length - 6);
        
        try {
            json = new Json(message);


            // parse the message that was received
        } catch (Exception ex) {
            try {
//                sendErrorResponse(sender, 0, new Exception("Error parsing json: " + message));
            } catch (Exception ex2) {
                FileLogger.error("Error sending error response to message pump: " + ex2.getMessage());
            }
            
            FileLogger.error("Unable to get JSON from message received");
            return;
        }


        // make sure we have a valid json object
        if (json == null) return;
        // if we do print it
        else System.out.println(client + " -> " + json);


        // get the message type
        String messageType = (String) (json.get("Message"));


        // check to see what type of message was received and call the appropriet function to deal with it
        if (messageType.equals("update-item")) updateItem((String) (json.get("Value")));
        else if (messageType.equals("execute-item")) trigger((String) (json.get("Value")));
        else if (messageType.equals("get-actions")) getActions(sender);
    }
    
    
    
    private String getClientString(byte[] sender) {
        String ipAddress = sender[0] + "." + sender[1] + "." + sender[2] + "." + sender[3];
        int port = ArrayUtils.getShort(sender, 4) & 0xffff;
        return ipAddress + ":" + port;
    }
    
    
    
    public Device getDevice(String deviceName) throws Exception {
        System.out.println("get " + deviceName);
        System.out.println(_devices.size() + " devices");
        if (!_devices.containsKey(deviceName)) throw new Exception("Device does not exist");
        return (Device) _devices.get(deviceName);
    }
    
    
    
    public void connectionEstablished(EventObject evt) {
        FileLogger.info("Client Connection Established");
    }
    
    
    
    public void connectionClosed(EventObject evt) {
        FileLogger.info("Client Connection Closed");
    }
    
    
    
    public void messageReceived(MessageReceivedEvent evt) {
        String command = (String) evt.getMessage();
        
        
        FileLogger.debug("command: " + command);
    }
    
    
    
    private void getActions(byte[] sender) {
        try {
            sendResponseMessage(sender, Action.ActionJson.toString().getBytes());
        } catch (Exception ex) {
            FileLogger.error(null, "Error getting actions: " + ex.getMessage());
        }
    }
    
    
    
    private void sendResponseMessage(byte[] sender, byte[] payload) throws Exception {
        try {
            SystemMsg msg = new SystemMsg();
            msg.type = APP_ID + 1;
            msg.msg = new byte[6 + payload.length];
            ArrayUtils.arraycopy(sender, 0, msg.msg, 0, 6);
            ArrayUtils.arraycopy(payload, 0, msg.msg, 6, payload.length);

            // post our response
            _msgPump.postMessage(msg);
        } catch (Exception ex) {
            throw new Exception("Error sending response to message pump: " + ex.toString());
        }
    }
}
