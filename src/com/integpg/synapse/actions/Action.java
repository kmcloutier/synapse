package com.integpg.synapse.actions;

import java.util.Hashtable;
import java.util.Json;



public abstract class Action {

    public static Json ActionJson;
    public static Hashtable ActionHash = new Hashtable();



    public static void LoadActions(Json actionJson) {
        ActionJson = actionJson;
    }



    public abstract void execute() throws Exception;



    public static void execute(String actionId) throws Exception {
        if (!ActionHash.containsKey(actionId)) throw new Exception("Action not found for: " + actionId);


        // get the action based on the ID
        Action action = (Action) ActionHash.get(actionId);
        action.execute();
    }
}
