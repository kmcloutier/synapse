package com.integpg.tcp;

import java.util.EventObject;

public class MessageReceivedEvent extends EventObject {

    private Object _msg;

    public MessageReceivedEvent(Object source, Object msg){
        super (source);
        _msg = msg;
    }

    public Object getMessage() {
        return _msg;
    }
}
