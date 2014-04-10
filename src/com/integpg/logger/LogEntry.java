package com.integpg.logger;

public class LogEntry {

    private String _filename;
    private long _timestamp;
    private String _text;

    public LogEntry(String filename, long timestamp, String text) {
        _filename = filename;
        _timestamp = timestamp;
        _text = text;
    }

    public String getFilename() {
        return _filename;
    }

    public long getTimestamp() {
        return _timestamp;
    }

    public String getText() {
        return _text;
    }
}
