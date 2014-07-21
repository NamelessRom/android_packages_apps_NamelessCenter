/*
 * <!--
 *    Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * -->
 */

package org.namelessrom.center.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Make interactions with files easier
 */
public class FileUtils {

    public static void writeToFile(final File file, final String content) throws Exception {
        if (file == null) throw new Exception("File is null!");
        if (file.exists()) {
            file.delete();
        }
        file.getParentFile().mkdirs();

        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            bw.write(content);
        } finally {
            if (bw != null) bw.close();
            if (fw != null) fw.close();
        }
    }

    public static String readFromFile(final File file) throws Exception {
        if (file == null) throw new Exception("File is null!");

        final StringBuilder sb = new StringBuilder();
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        } finally {
            if (br != null) br.close();
            if (fr != null) fr.close();
        }

        return sb.toString();
    }

}
