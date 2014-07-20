package org.namelessrom.center.utils;

import android.text.TextUtils;

import org.namelessrom.center.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class for checking md5 hashes of files
 */
public class MD5 {

    private static final String TAG = "MD5";

    public static boolean checkMD5(final String md5, final File updateFile) {
        if (TextUtils.isEmpty(md5) || updateFile == null) {
            Logger.e(TAG, "MD5 string empty or updateFile null");
            return false;
        }

        final String calculatedDigest = calculateMD5(updateFile);
        if (calculatedDigest == null) {
            Logger.e(TAG, "calculatedDigest null");
            return false;
        }

        Logger.v(TAG, "Calculated digest: " + calculatedDigest);
        Logger.v(TAG, "Provided digest: " + md5);

        return calculatedDigest.equalsIgnoreCase(md5);
    }

    public static String calculateMD5(final File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Logger.e(TAG, "Exception while getting digest: " + e.getMessage());
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Logger.e(TAG, "Exception while getting FileInputStream: " + e.getMessage());
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            final BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Logger.e(TAG, "Exception on closing MD5 input stream: " + e.getMessage());
            }
        }
    }

}
