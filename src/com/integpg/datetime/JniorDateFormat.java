package com.integpg.datetime;

import com.integpg.system.ArrayUtils;
import com.integpg.system.JANOS;
import java.util.Calendar;
import java.util.Date;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author kcloutier
 */
public class JniorDateFormat {

    /* So it turns out that working with the Date() object and Calendar() is very inefficient.
     *  It takes like a 1/2 second to create one of these. This causes I/O logging to be
     *  rediculously slow. So... the following two methods cache date processing while we
     *  manually present the seconds detail. This works much better.
     *
     * The approach is this... A string datestamp is created once a minute that includes
     *  everything except the current second and millisecond information. In the interim
     *  we only calculate and append the seconds and milliseconds. This pushes any Date()
     *  object calculations off to only once per minute and the datestamp can be
     *  quickly presented.
     */
    private static long tRef = 0;
    public static long nextMinute = 0;
    private static byte[] _dateStampBytes = "  /  /     :  :  .       , ".getBytes();
    public static int DATE_STAMP_LENGTH = _dateStampBytes.length - 2;
    public static int DATE_STAMP_LENGTH_WITH_COMMA = _dateStampBytes.length;
    private static byte[] _buf = new byte[8];
    public static long _bootTime = System.currentTimeMillis() - JANOS.uptimeMillis();
    public static long _lastTime = 0;

    static {
        ArrayUtils.arraycopy(Calendar.getInstance().getTimeZone().getID().getBytes(), 0, _dateStampBytes, 22, 3);
    }

    public synchronized static long getTime() {
        long time = _bootTime + JANOS.uptimeMillis();
        // make sure we never return a time earlier than the last time
        if (time < _lastTime) {
            System.out.println("Time is old");
//            Logger.logEntry("Time is old");
            return _lastTime;
        }
        return (_lastTime = time);
    }

//    public synchronized static byte[] getDateStampBytes() {
//        return getDateStampBytes(0);
//    }

    public synchronized static byte[] getDateStampBytes(long time) {
        if (time <= 0) {
            time = getTime();
        }

        if (tRef > time || time > nextMinute) {
            InitRefBytes(time);
        }

        int delta = (int) (time - tRef);
        int rSecond = delta / 1000;
        int rMillis = delta - 1000 * rSecond;

        stuffInt(_buf, rSecond + 100, 0, false);
        ArrayUtils.arraycopy(_buf, 1, _dateStampBytes, 15, 2);
        stuffInt(_buf, rMillis + 1000, 0, false);
        ArrayUtils.arraycopy(_buf, 1, _dateStampBytes, 18, 3);

        return _dateStampBytes;
    }

    private static void InitRefBytes(long time) {
        _bootTime = System.currentTimeMillis() - JANOS.uptimeMillis();
//        System.out.println("Set init refs " + _bootTime);

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(time));

        int rMonth = cal.get(Calendar.MONTH);
        int rDay = cal.get(Calendar.DAY_OF_MONTH);
        int rYear = cal.get(Calendar.YEAR);
        int rHour = cal.get(Calendar.HOUR_OF_DAY);
        int rMinute = cal.get(Calendar.MINUTE);
        int rSecond = cal.get(Calendar.SECOND);
        int rMillis = cal.get(Calendar.MILLISECOND);

        tRef = time - (1000 * rSecond + rMillis);
        nextMinute = time + 60000 - (time % 60000);

        stuffInt(_buf, rMonth + 101, 0, false);
        ArrayUtils.arraycopy(_buf, 1, _dateStampBytes, 0, 2);
        stuffInt(_buf, rDay + 100, 0, false);
        ArrayUtils.arraycopy(_buf, 1, _dateStampBytes, 3, 2);
        stuffInt(_buf, rYear, 0, false);
        ArrayUtils.arraycopy(_buf, 2, _dateStampBytes, 6, 2);
        stuffInt(_buf, rHour + 100, 0, false);
        ArrayUtils.arraycopy(_buf, 1, _dateStampBytes, 9, 2);
        stuffInt(_buf, rMinute + 100, 0, false);
        ArrayUtils.arraycopy(_buf, 1, _dateStampBytes, 12, 2);
    }

    private static void stuffInt(byte[] array, int value, int dec, boolean rjustified) {
        ArrayUtils.arrayFill(array, 0, array.length, (byte) ' ');

        if (value == 0) {
            array[0] = (byte) '0';
            return;
        }

        int dig;
        int offset = 0;
        boolean leading = true;

        if (value < 0) {
            array[offset++] = (byte) '-';
            value = -value;
            rjustified = false;
        }

        for (dig = 1000000; dig >= 1; dig /= 10) {
            if (leading && value < dig) {
                if (rjustified) {
                    offset++;
                }
                continue;
            }

            array[offset] = (byte) '0';
            while (value >= dig) {
                array[offset]++;
                value -= dig;
            }
            offset++;
            leading = false;
        }

        for (dig = dec; dig > 0; dig--) {
            array[offset] = array[offset - 1];
            if (array[offset] == ' ') {
                array[offset] = '0';
            }
            offset--;

            if (dig == 1) {
                array[offset] = (byte) '.';
                if (array[offset - 1] == ' ') {
                    array[offset - 1] = '0';
                }
            }
        }
    }
//    public synchronized static String getDateStamp() {
//        long time = System.currentTimeMillis();
//
//        if (sPrefix == null || tRef > time || time > nextMinute)
//        {
//            InitRefs(time);
//        }
//
//        int delta = (int) (time - tRef);
//        int rSecond = delta / 1000;
//        int rMillis = delta - 1000 * rSecond;
//
//        return sPrefix
//                + Integer.toString(rSecond + 100).substring(1) + "."
//                + Integer.toString(rMillis + 1000).substring(1);
//    }
//
//    private static void InitRefs(long time) {
////        System.out.println("Set init refs");
//
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(new Date(time));
//
//        int rMonth = cal.get(Calendar.MONTH);
//        int rDay = cal.get(Calendar.DAY_OF_MONTH);
//        int rYear = cal.get(Calendar.YEAR);
//        int rHour = cal.get(Calendar.HOUR_OF_DAY);
//        int rMinute = cal.get(Calendar.MINUTE);
//        int rSecond = cal.get(Calendar.SECOND);
//        int rMillis = cal.get(Calendar.MILLISECOND);
//
//        tRef = time - (1000 * rSecond + rMillis);
//        nextMinute = time + 60000 - (time % 60000);
//
//        StringBuffer buf = new StringBuffer(32);
//        buf.append(Integer.toString(rMonth + 101).substring(1));  // month
//        buf.append("/");
//        buf.append(Integer.toString(rDay + 100).substring(1));    // day
//        buf.append("/");
//        buf.append(Integer.toString(rYear).substring(2));         // year
//        buf.append(" ");
//        buf.append(Integer.toString(rHour + 100).substring(1));   // hour
//        buf.append(":");
//        buf.append(Integer.toString(rMinute + 100).substring(1)); // minute
//        buf.append(":");
//        sPrefix = buf.toString();
//    }
}
