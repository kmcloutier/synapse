package com.integpg.synapse.triggers;

import com.integpg.synapse.Synapse;
import com.integpg.system.JANOS;
import java.io.IOException;



public class TimerTrigger extends Trigger implements Runnable {

    private Thread _thd = null;
    private boolean _cancel = false;

    private long _nextTrigger = Long.MAX_VALUE;
    private double _interval;
    private String _doName;



    public TimerTrigger(double interval, String doName) {
        _interval = interval;
        _doName = doName;
        _nextTrigger = (long) (JANOS.uptimeMillis() + _interval * 1000);
    }



    public void start() throws IOException {
        if (_thd == null) {
            _thd = new Thread(this);
            _thd.setDaemon(true);
            _thd.start();
        }
    }



    public void cancel() throws IOException {
        if (_thd != null) _thd.interrupt();
        _cancel = true;
    }



    protected void trigger() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }



    public void run() {
        while (!_cancel) {
            try {
                long sleepTime = _nextTrigger - JANOS.uptimeMillis();
                if (sleepTime > 0) Thread.sleep(sleepTime);
                _nextTrigger += (long) (_interval * 1000);
            } catch (InterruptedException ex) {
                break;
            }

//            System.out.println(JANOS.uptimeMillis() + "   Timer Triggered " + _doName);
            Synapse.INSTANCE.trigger(_doName);
        }
    }

}
