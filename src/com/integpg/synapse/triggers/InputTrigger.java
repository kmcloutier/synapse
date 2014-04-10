package com.integpg.synapse.triggers;

import com.integpg.io.InputMonitor;
import com.integpg.io.InputsChangedListener;
import com.integpg.logger.FileLogger;
import java.io.IOException;
import java.util.EventObject;



public class InputTrigger extends Trigger implements InputsChangedListener {

    private int _channel;



    public InputTrigger(int channel) {
        _channel = channel;


        // init the Input Monitor
        InputMonitor.addListener(this);
        InputMonitor.start();
    }



    public void start() throws IOException {
    }



    public void cancel() throws IOException {
    }



    protected void trigger() {
        FileLogger.info("Trigger Input Monitor for Channel: " + _channel);
    }



    public void inputChanged(EventObject event) {
        try {
            int changedInputs = InputMonitor.ChangedInputs;
            int inputStates = InputMonitor.InputStates;


            if ((changedInputs & (1 << (_channel - 1))) != 0 && (inputStates & (1 << (_channel - 1))) != 0) {
                trigger();
            }
        } catch (Exception ex) {
            FileLogger.error("Error during input changed listener: " + ex.getMessage());
        }
    }

}
