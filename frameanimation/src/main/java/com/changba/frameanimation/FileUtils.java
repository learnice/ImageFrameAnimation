package com.changba.frameanimation;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author HeXuebin on 2021/1/26.
 */
public class FileUtils {

    public static byte[] readFileToMem(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        File file = new File(fileName);
        if (!file.exists()) {
            return null;
        }
        byte[] buffer;
        try (FileInputStream fis = new FileInputStream(file)) {
            int streamLength = fis.available();
            buffer = new byte[streamLength];
            int readLen = fis.read(buffer);
            if (readLen == streamLength) {
                return buffer;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readResToMem(int resId, Context context) {
        byte[] buffer;
        try (InputStream is = context.getResources().openRawResource(resId)) {
            int streamLength = is.available();
            buffer = new byte[streamLength];
            int readLen = is.read(buffer);
            if (readLen == streamLength) {
                return buffer;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
