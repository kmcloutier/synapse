package com.integpg.utils;

import com.integpg.system.JANOS;

public class StringUtils {

    public static String[] split(String str, String delim) {
        return split(str, new String[]{delim}, 0);
    }

    public static String[] split(String str, String delim, int index) {
        return split(str, new String[]{delim}, index);
    }

    public static String[] split(String str, String[] delim, int index) {
        int startIndex = index;

        int[] delimPosArray = new int[delim.length];
        for (int delimIndex = 0; delimIndex < delimPosArray.length; delimIndex++) {
            delimPosArray[delimIndex] = str.indexOf(delim[delimIndex]);
//            System.out.println("Delim Index for " + delim[delimIndex] + " is " + delimPosArray[delimIndex]);
        }



//        System.out.println("Split " + str.substring(index));

        long start = JANOS.uptimeMillis();
        int delimPos = 0, count = 0;

        // get the number of string elements we should end up with
        while (true) {
            int nextDelimIndex = -1, nextDelimPos = Integer.MAX_VALUE;
            for (int delimIndex = 0; delimIndex < delimPosArray.length; delimIndex++) {
                if (delimPosArray[delimIndex] < nextDelimPos) {
                    nextDelimIndex = delimIndex;
                    nextDelimPos = delimPosArray[delimIndex];
                }
            }

            if (nextDelimIndex == -1 || (delimPos = str.indexOf(delim[nextDelimIndex], index)) == -1) {
                break;
            }

            count++;
            index = delimPos + delim[nextDelimIndex].length();
            delimPosArray[nextDelimIndex] = str.indexOf(delim[nextDelimIndex], index);
            if (delimPosArray[nextDelimIndex] == -1) {
                delimPosArray[nextDelimIndex] = str.length();
            }
//            System.out.println("Delim Index for " + delim[nextDelimIndex] + " is " + delimPosArray[nextDelimIndex]);

            if (index == str.length()) {
                count++;
            }
        }

        if (index != str.length()) {
            count++;
        }


        for (int delimIndex = 0; delimIndex < delimPosArray.length; delimIndex++) {
            delimPosArray[delimIndex] = str.indexOf(delim[delimIndex]);
//            System.out.println("Delim Index for " + delim[delimIndex] + " is " + delimPosArray[delimIndex]);
        }



        // go through and load our string array
        String[] arr = new String[count];
        int i = 0;
        index = startIndex;
        while (true) {
            int nextDelimIndex = -1, nextDelimPos = Integer.MAX_VALUE;
            for (int delimIndex = 0; delimIndex < delimPosArray.length; delimIndex++) {
                if (delimPosArray[delimIndex] < nextDelimPos) {
                    nextDelimIndex = delimIndex;
                    nextDelimPos = delimPosArray[delimIndex];
                }
            }

            if (nextDelimIndex == -1 || (delimPos = str.indexOf(delim[nextDelimIndex], index)) == -1) {
                break;
            }

            arr[i++] = str.substring(index, delimPos);
            index = delimPos + delim[nextDelimIndex].length();
            delimPosArray[nextDelimIndex] = str.indexOf(delim[nextDelimIndex], index);
            if (delimPosArray[nextDelimIndex] == -1) {
                delimPosArray[nextDelimIndex] = str.length();
            }
//            System.out.println("Delim Index for " + delim[nextDelimIndex] + " is " + delimPosArray[nextDelimIndex]);

            if (index == str.length()) {
                arr[i++] = "";
            }
        }

        if (index != str.length()) {
            arr[i] = str.substring(index);
        }

        long elapsed = JANOS.uptimeMillis() - start;
//        System.out.println("split took " + elapsed);

        // return
        return arr;
    }
//
//    public static String[] split2(String str, String delim) {
//        System.out.println("Split " + str);
//
//        long start = JANOS.uptimeMillis();
//        Vector v = new Vector(48);
//
//        int index = 0, delimPos;
//        while ((delimPos = str.indexOf(delim, index)) != -1) {
//            v.addElement(str.substring(index, delimPos));
//            index = delimPos + delim.length();
//
//            if (index == str.length()) {
//                v.addElement("");
//            }
//        }
//
//        if (index != str.length()) {
//            v.addElement(str.substring(index));
//        }
//
//        String[] arr = new String[v.size()];
//        for (int i = 0; i < v.size(); i++) {
//            arr[i] = (String) (v.elementAt(i));
//        }
//
//        long elapsed = JANOS.uptimeMillis() - start;
//        System.out.println("split took " + elapsed + ".");
//
//        return arr;
//    }

       public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, 0, bytes.length);
    }

    public static String bytesToHex(byte[] bytes, int offset, int len) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        int v, i = 0;
        for (int j = offset; j < offset + len; j++) {
            v = bytes[j] & 0xFF;
            hexChars[i++] = hexArray[v >>> 4];
            hexChars[i++] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
