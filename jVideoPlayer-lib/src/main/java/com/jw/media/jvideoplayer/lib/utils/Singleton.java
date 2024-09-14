package com.jw.media.jvideoplayer.lib.utils;

/**
 * Created by Joyce.wang on 2024/9/12 13:35
 *
 * @Description TODO
 */
public abstract class Singleton<T> {
    private volatile T mInstance;

    protected abstract T create();

    public final T get() {
        synchronized (this) {
            if (mInstance == null) {
                mInstance = create();
            }
            return mInstance;
        }
    }

    public final void clear(){
        synchronized (this) {
            mInstance = null;
        }
    }
}
