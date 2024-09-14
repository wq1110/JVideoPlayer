package com.jw.media.jvideoplayer.cache.common;

/**
 * Created by Joyce.wang on 2024/9/11 8:50
 *
 * @Description 视频缓存异常统一类
 */
public class VideoCacheException extends Exception {
    private String mMsg;

    public VideoCacheException(String message) {
        super(message);
        mMsg = message;
    }

    public VideoCacheException(String message, Throwable cause) {
        super(message, cause);
        mMsg = message;
    }

    public VideoCacheException(Throwable cause) {
        super(cause);
    }

    public String getMsg() {
        return mMsg;
    }
}
