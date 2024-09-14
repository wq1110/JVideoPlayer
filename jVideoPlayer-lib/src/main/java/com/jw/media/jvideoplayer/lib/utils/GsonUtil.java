package com.jw.media.jvideoplayer.lib.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Joyce.wang on 2024/9/13 13:53
 *
 * @Description TODO
 */
public class GsonUtil {
    private static GsonBuilder getGsonBuilder() {
        return new GsonBuilder().serializeNulls()
                .setDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public static Gson getGson() {
        return getGsonBuilder().create();
    }
}
