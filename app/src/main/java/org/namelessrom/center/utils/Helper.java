package org.namelessrom.center.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * A helper class which helps me helping you
 */
public class Helper {

    public static boolean isNameless() { return existsInBuildProp("ro.nameless.version"); }

    public static boolean isNamelessDebug() { return existsInBuildProp("ro.nameless.debug=1"); }

    public static boolean existsInBuildProp(final String filter) {
        final File f = new File("/system/build.prop");
        BufferedReader bufferedReader = null;
        if (f.exists() && f.canRead()) {
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    if (s.contains(filter)) return true;
                }
            } catch (Exception whoops) {
                return false;
            } finally {
                try {
                    if (bufferedReader != null) bufferedReader.close();
                } catch (Exception ignored) { }
            }
        }
        return false;
    }

}
