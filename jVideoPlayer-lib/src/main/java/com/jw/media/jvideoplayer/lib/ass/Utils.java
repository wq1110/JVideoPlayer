package com.jw.media.jvideoplayer.lib.ass;

import java.io.File;
import java.io.FileOutputStream;

class Utils {
    static void storeToCache(File dir, byte[] data, String fileName) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(dir, fileName));
            fileOutputStream.write(data);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
