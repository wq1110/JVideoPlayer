package com.jw.media.jvideoplayer.mvx.mvvm;

import android.widget.EditText;

/**
 * Created by Joyce.wang on 2024/9/11 16:01
 *
 * @Description TODO
 */
public class Converter {
    public static String dateToString(EditText view, long oldValue,
                                      long value) {
        // Converts long to String.
        return String.valueOf(oldValue) + String.valueOf(value);
    }

    public static long stringToDate(EditText view, String oldValue,
                                    String value) {
        // Converts String to long.
        return Long.valueOf(oldValue) + Long.valueOf(value);
    }
}
