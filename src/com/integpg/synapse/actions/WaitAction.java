package com.integpg.synapse.actions;

import java.util.Json;



public class WaitAction extends Action {

    private int _duration;



    public WaitAction(Json json) {
        _duration = Integer.valueOf((String) json.get("Duration")).intValue();


        ActionHash.put((String) json.get("ID"), this);
    }



    public void execute() throws Exception {
        try {
            Thread.sleep(_duration);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}
