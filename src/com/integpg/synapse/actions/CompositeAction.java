package com.integpg.synapse.actions;

import com.integpg.logger.FileLogger;
import java.io.IOException;
import java.util.Json;



public class CompositeAction extends Action {

    private String[] _actions;



    public CompositeAction(Json json) {
        _actions = (String[]) json.get("Actions");


        ActionHash.put((String) json.get("ID"), this);
    }



    public void execute() throws IOException {
        Thread thd = new Thread(new Runnable() {
            public void run() {
                FileLogger.debug("Executing Composite Action in " + Thread.currentThread().getName());


                for (int i = 0; i < _actions.length; i++) {
                    try {
                        Action.execute(_actions[i]);
                    } catch (Exception ex) {
                        FileLogger.error("Error executing action: " + ex.getMessage());
                    }
                }
            }
        });
        thd.setDaemon(true);
        thd.start();
    }

}
