package com.jw.media.jvideoplayer.lib.callback;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by Joyce.wang on 2024/9/10 17:21
 *
 * @Description 通用单数据回调
 */
public abstract class ICallBack<T> {
    ICallBack<T> base;
    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    //执行体
    public abstract void call(T t);

    public ICallBack() {
    }

    private ICallBack(ICallBack<T> base) {
        this.base = base;
    }

    //一定在UI线程中执行
    public ICallBack<T> inUIThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) return this;
        return new ICallBack<T>(this) {
            @Override
            public void call(final T t) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        base.call(t);
                        base = null;
                    }
                });
            }
        };
    }
}
