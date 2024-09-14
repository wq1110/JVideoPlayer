package com.jw.media.jvideoplayer.player;

import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * Created by Joyce.wang on 2024/9/13 13:13
 *
 * @Description TODO
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SdkServiceManager {
    final static SparseArray<Object> mServices = new SparseArray<>();
    static {

    }

    public static <T> T getService(@NonNull Class<T> clazz) {
        return (T) mServices.get(clazz.hashCode());
    }

    public static <T> void registerService(@NonNull Class<T> clazz, Object service) {
        mServices.put(clazz.hashCode(), service);
    }
}
