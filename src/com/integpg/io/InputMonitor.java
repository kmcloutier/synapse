package com.integpg.io;

import com.integpg.logger.FileLogger;
import com.integpg.system.JANOS;
import java.util.EventObject;
import java.util.Vector;



public class InputMonitor implements Runnable {

    private static Thread _thd = null;

    private static Vector _listeners = new Vector();

    public static int ChangedInputs, InputStates;



    public static void addListener(InputsChangedListener listener) {
        _listeners.addElement(listener);
        FileLogger.vital("Input Monitor listener added");
    }



    public static void start() {
        if (_thd == null) {
            _thd = new Thread(new InputMonitor());
            _thd.setDaemon(true);
            _thd.start();
        }
    }



    public void run() {
        FileLogger.vital("Input Monitor started");


        int lastInputStates = JANOS.getInputStates();


        try {
            // run forever
            while (true) {
                // get input states
                InputStates = JANOS.getInputStates();

                // get what channels have changed
                ChangedInputs = InputStates ^ lastInputStates;

                // if there has been a state change then alert our listeners
                if (ChangedInputs != 0) {
                    lastInputStates = InputStates;

                    for (int i = 0; i < _listeners.size(); i++) {
                        InputsChangedListener listener = (InputsChangedListener) (_listeners.elementAt(i));
                        listener.inputChanged(new EventObject(this));
                    }
                }

                Thread.sleep(100);
            } // while true
        } catch (Exception ex) {
            FileLogger.error("Error in input state monitor: " + ex.getMessage());
        }
    }
}
