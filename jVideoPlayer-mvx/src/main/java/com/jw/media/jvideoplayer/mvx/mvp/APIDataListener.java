package com.jw.media.jvideoplayer.mvx.mvp;

/**
 * Created by Joyce.wang on 2024/9/13 14:25
 *
 * @Description API接口 监听
 */
public interface APIDataListener<T> {
    void onSuccess(T result);
    void onError(Throwable exception);
}