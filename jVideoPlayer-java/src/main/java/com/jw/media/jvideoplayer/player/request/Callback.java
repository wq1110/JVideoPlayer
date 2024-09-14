package com.jw.media.jvideoplayer.player.request;

/**
 * Created by Joyce.wang on 2024/9/13 9:15
 *
 * @Description TODO
 */
public interface Callback<T> {
    void onSuccess(T t);
    void onBusinessFail(int code, String message, Throwable throwable);
    void onError(Throwable throwable);
}
