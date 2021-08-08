package dev.jpcode.eccore.util;

public class Util {
    static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if(version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if(dot != -1) { version = version.substring(0, dot); }
        }
        int versionNum = 0;
        try {
            versionNum = Integer.parseInt(version.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return versionNum;
    }

}
