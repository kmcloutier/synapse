package com.integpg.synapse;



public class AssemblyInfo {

    private static final String VERSION = "1./*++*/0./****/0./*//*/72";  // 3/26/2014



    public static String getVersion() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < VERSION.length(); i++) {
            char c = VERSION.charAt(i);
            if (c != '+' && c != '/' && c != '*') sb.append(c);
        }
        return sb.toString();
    }
}
