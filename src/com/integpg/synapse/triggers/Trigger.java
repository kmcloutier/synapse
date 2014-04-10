package com.integpg.synapse.triggers;

import java.io.IOException;



public abstract class Trigger {

    public abstract void start() throws IOException;



    public abstract void cancel() throws IOException;



    protected abstract void trigger();
}
