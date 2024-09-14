package com.jw.media.jvideoplayer.player.request;

import io.reactivex.Observable;

/**
 * Created by Joyce.wang on 2024/9/13 9:14
 *
 * @Description TODO
 */
public interface MediaRequest<T> {
    /**
     * 发起请求 Normal Callback Code
     *
     * @param callback 回调
     */
    void proceed(Callback<T> callback);

    /**
     * 串行发起请求   Serial Code
     *
     * @return
     */
    T proceed() throws RuntimeException;

    /**
     * 发起请求 RxJava Stream Code
     * {@link Callback#onBusinessFail(int, String, Throwable)} 的 Throwable 会转移到 onError
     *
     * @return Observable
     */
    Observable<T> proceedWithRx();
}
