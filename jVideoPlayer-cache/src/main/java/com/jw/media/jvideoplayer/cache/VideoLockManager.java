package com.jw.media.jvideoplayer.cache;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Joyce.wang on 2024/9/11 14:16
 *
 * @Description TODO
 */
public class VideoLockManager {
    private static volatile VideoLockManager sInstance = null;
    private Map<String, Object> mLockMap = new ConcurrentHashMap<>();

    public static VideoLockManager getInstance() {
        if (sInstance == null) {
            synchronized (VideoLockManager.class) {
                if (sInstance == null) {
                    sInstance = new VideoLockManager();
                }
            }
        }
        return sInstance;
    }

    public synchronized Object getLock(@NonNull String md5) {
        Object lock = mLockMap.get(md5);
        if (lock == null) {
            lock = new Object();
            mLockMap.put(md5, lock);
        }
        return lock;
    }

    public synchronized void removeLock(@NonNull String md5) {
        mLockMap.remove(md5);
    }
}
