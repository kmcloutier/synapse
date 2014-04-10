package com.integpg.logger;

import com.integpg.datetime.JniorDateFormat;
import com.integpg.system.JANOS;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Vector;



public class FileLogger implements Runnable {
    
    public static final int ALL = 0xff;
    public static final int DEBUG = 16;
    public static final int INFO = 8;
    public static final int VITAL = 4;
    public static final int WARN = 2;
    public static final int ERROR = 1;
    private static int LEVEL = (VITAL << 1) - 1; // | WARN; // | INFO;
    private static final String SPACES = "                                                  ";
    private static final Vector _entries = new Vector();
    //
    private static String APP_NAME;
    private static String APP_FILENAME;
    private static FileLogger _logger = null;
    private static boolean _includeThreadName = false;
    
    
    
    static {
        ((File) new File("/flash/logs")).mkdir();
    }
    
    
    
    public static void setAppName(String appName) {
        APP_NAME = appName;
        APP_FILENAME = appName.toLowerCase() + ".log";
    }
    
    
    
    public static void setDebugLevel(int level) {
//        System.out.println(level);
//        System.out.println(level << 1);
        LEVEL = (level << 1) - 1;
//        System.out.println(LEVEL);

        if (LEVEL >= DEBUG) {
            vital("Debug Level Set to DEBUG");
        } else if (LEVEL >= INFO) {
            vital("Debug Level Set to INFO");
        } else if (LEVEL >= VITAL) {
            vital("Debug Level Set to IMPORTANT");
        } else if (LEVEL >= WARN) {
            vital("Debug Level Set to WARN");
        } else {
            vital("Debug Level Set to ERROR");
        }
        
        vital("To run the application under a different debug level use the following commandline:");
        vital("     java flash/xchange.jar [-debug,-info]");
    }
    
    
    
    public static void setIncludeThreadName(boolean b) {
        _includeThreadName = b;
    }
    
    
    
    public static void debug(Object obj) {
        log(APP_FILENAME, obj, DEBUG);
    }
    
    
    
    public static void debug(String filename, Object obj) {
        log(filename, obj, DEBUG);
    }
    
    
    
    public static void info(Object obj) {
        log(APP_FILENAME, obj, INFO);
    }
    
    
    
    public static void info(String filename, Object obj) {
        log(filename, obj, INFO);
    }
    
    
    
    public static void vital(Object obj) {
        log(APP_FILENAME, obj, VITAL);
    }
    
    
    
    public static void vital(String filename, Object obj) {
        log(filename, obj, VITAL);
    }
    
    
    
    public static void warn(Object obj) {
        log(APP_FILENAME, obj, WARN);
    }
    
    
    
    public static void warn(String filename, Object obj) {
        log(filename, obj, WARN);
    }
    
    
    
    public static void error(Object obj) {
        log(APP_FILENAME, obj, ERROR);
    }
    
    
    
    public static void error(String filename, Object obj) {
        log(filename, obj, ERROR);
    }
    
    
    
    private static void log(String filename, Object message, int level) {
        if ((level & LEVEL) == 0 || message == null) {
            return;
        }
        
        
        StringBuffer sb = new StringBuffer();
//            System.out.println(level + " " + LEVEL + " " + (level & LEVEL));
        sb.setLength(0);
//                _sb.append(JANOS.uptimeMillis());
//                _sb.append("  ");
        switch (level) {
            case DEBUG:
                sb.append("DEBUG  ");
                break;
            case INFO:
                sb.append("INFO   ");
                break;
            case VITAL:
                sb.append("VITAL  ");
                break;
            case WARN:
                sb.append("WARN   ");
                break;
            case ERROR:
                sb.append("ERROR  ");
                break;
        }
        
        if (_includeThreadName) {
            sb.append(Thread.currentThread().getName());
            
            if (sb.length() < (_includeThreadName ? 50 : 10)) {
                sb.append(SPACES.substring(0, ((_includeThreadName ? 50 : 10) - sb.length())));
            }
        }
        
        sb.append(message.toString());
        
        String s = sb.toString();
        System.out.println(s);
        log(filename, s);
    }
    
    
    
    public static void log(String filename, String text) {
        try {
            if (filename == null && APP_FILENAME == null) {
                throw new RuntimeException("Default application filename has not been set");
            }
            
            LogEntry le = new LogEntry(filename, System.currentTimeMillis(), text);
            synchronized (_entries) {
                _entries.addElement(le);
//                System.out.println(_entries.capacity());
//            System.out.println("log element count: " + _entries.size() + " " + _entries.capacity());
                _entries.notifyAll();
//            System.out.println("log entry added: " + text);

                if (_logger == null) {
                    start();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    
    private static void start() {
        if (_logger == null) {
//            System.out.println("create new logger");
            _logger = new FileLogger();
            Thread thd = new Thread(_logger, "Logger");
            thd.start();
        }
    }
    
    
    
    public static boolean isPendingWrites() {
        return !_entries.isEmpty();
    }
    
    
    
    public void run() {
//        System.out.println("logger running");
        while (true) {
            LogEntry le = null;
            
            synchronized (_entries) {
                while (_entries.isEmpty()) {
                    try {
                        _entries.wait(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                } // while entries is empty

                if (!_entries.isEmpty()) {
                    le = (LogEntry) (_entries.elementAt(0));
                }
            } // synchronized

            if (le != null) {
//                System.out.println("logger entry available");

                FileOutputStream out = null;
                try {
                    File f = new File(le.getFilename());
                    if (f.exists()) {
                        // if the file will exceed our limit then move to a backup location in flash
                        if ((f.length() + JniorDateFormat.DATE_STAMP_LENGTH_WITH_COMMA + le.getText().length() + 2) > 32 * 1024) {
                            doBackup(le);
                        }
                    }
                    
                    out = new FileOutputStream(le.getFilename(), true);
                    
                    out.write(JniorDateFormat.getDateStampBytes(le.getTimestamp()), 0, JniorDateFormat.DATE_STAMP_LENGTH_WITH_COMMA);
                    out.write((le.getText() + "\r\n").getBytes());
                    out.close();

                    // we successfully loged the entry.  now remove it.
                    _entries.removeElementAt(0);
                    le = null;
//            System.out.println("log element count: " + _entries.size() + " " + _entries.capacity());
                } catch (IOException ex) {
                    JANOS.syslog("Unable to log \"" + le.getText() + "\" to " + le.getFilename() + ": " + ex.getMessage());
                } finally {
                    try {
                        if (out != null) out.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } // not empty
        } // forever
    }
    
    
    
    private void doBackup(LogEntry le) {
        File f = new File(le.getFilename());
        int index = le.getFilename().lastIndexOf(".");
        String name = le.getFilename().substring(0, index);
        String newFileName = "/flash/logs/" + APP_NAME;

        // make sure backup directory exists
        if (!new File(newFileName).exists()) {
            ((File) new File(newFileName)).mkdir();
        }
        
        newFileName += "/" + name + "_" + getFileTimeStamp() + ".log";
        System.out.println("backup log to " + newFileName);
        copyfile(f, new File(newFileName));
        f.delete();

        // get all of the logs that are associated with the file.
        File flashLogs = new File("/flash/logs");
        String[] files = flashLogs.list();
        Vector fileNames = new Vector();
        int count = 1;
        for (int i = 0; i < files.length; i++) {
//            System.out.println(count + " " + files[i]);
            fileNames.addElement(files[i]);
            count++;
        }

        // remove the 17th oldest file.  this allows each application to have a rolling 16 backup
        // logs.  since the number is per application this means that you will not get the same
        // number of each file.  files that are written to more often will have a higher number of
        // backups in the backup location
        while (fileNames.size() > 16) {
            String fileName = (String) (fileNames.elementAt(0));
            (new File("/flash/logs/" + fileName)).delete();
            fileNames.removeElementAt(0);
        }
    }
    
    
    
    private static String getFileTimeStamp() {
        StringBuffer sb = new StringBuffer(12);
        Calendar cal = Calendar.getInstance();
        
        int rMonth = cal.get(Calendar.MONTH);
        int rDay = cal.get(Calendar.DAY_OF_MONTH);
        int rYear = cal.get(Calendar.YEAR);
        int rHour = cal.get(Calendar.HOUR_OF_DAY);
        int rMinute = cal.get(Calendar.MINUTE);
        
        sb.append(rYear);
        sb.append(String.valueOf(rMonth + 101).substring(1));
        sb.append(String.valueOf(rDay + 100).substring(1));
        sb.append(String.valueOf(rHour + 100).substring(1));
        sb.append(String.valueOf(rMinute + 100).substring(1));
        
        return sb.toString();
    }
    
    
    
    private static void copyfile(File srFile, File dtFile) {
        try {
//            System.out.println("Copy " + srFile + " to " + dtFile);

            InputStream in = new FileInputStream(srFile);

            //For Overwrite the file.
            OutputStream out = new FileOutputStream(dtFile);
            
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            
            buf = null;
//            System.out.println("File copied.");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
//            ExceptionHandler.logException(0, ex.getMessage() + " in the specified directory.", ex);
        } catch (IOException ex) {
            ex.printStackTrace();
//            ExceptionHandler.logException(0, "Error copying " + srFile + " to " + dtFile, ex);
        }
    }
}
