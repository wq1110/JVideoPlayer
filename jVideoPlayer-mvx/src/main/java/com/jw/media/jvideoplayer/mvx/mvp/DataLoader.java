package com.jw.media.jvideoplayer.mvx.mvp;

import com.jw.media.jvideoplayer.lib.callback.ICallBack;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Joyce.wang on 2024/9/13 14:29
 *
 * @Description 数据加载器（拉取 获取 重置 线程安全）
 */
abstract public class DataLoader {
    private Object result;
    private boolean isLoading;
    private int times = -1;
    private AtomicBoolean lock = new AtomicBoolean();
    private ICallBack<Object> mListener;
    private ICallBack<Throwable> mListenerError;

    public int fetchResultTimes() {
        return times;
    }

    public boolean hasWorkedBefore() {
        return times != -1;
    }

    abstract void onDataLoaded(Object result);

    private void start(ICallBack<Object> cb, ICallBack<Throwable> cbError) {
        while (true) {
            if (lock.compareAndSet(false, true)) {
                isLoading = true;
                result = null;
                mListener = cb;
                times = 0;
                mListenerError = cbError;
                lock.set(false);
                proceed(new ICallBack<Object>() {
                    @Override
                    public void call(Object t) {
                        while (true) {
                            if (lock.compareAndSet(false, true)) {
                                result = t;
                                isLoading = false;
                                onDataLoaded(result);
                                if (mListener != null) {
                                    try {
                                        mListener.call(result);
                                    } catch (Exception e) {
                                        lock.set(false);
                                        throw e;
                                    } finally {
                                        times++;
                                        mListener= null;
                                    }
                                }
                                lock.set(false);
                                break;
                            }
                        }
                    }
                }, new ICallBack<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        while (true) {
                            if (lock.compareAndSet(false, true)) {
                                isLoading = false;
                                result = e;
                                onDataLoaded(result);
                                if (mListenerError != null) {
                                    try {
                                        mListenerError.call((Throwable) result);
                                    }catch (Exception ee) {
                                        lock.set(false);
                                        throw ee;
                                    }finally {
                                        times++;
                                        mListenerError= null;
                                    }
                                }
                                lock.set(false);
                                break;
                            }
                        }
                    }
                });
                break;
            }
        }
    }

    public void get(boolean reset, ICallBack<Object> cb, ICallBack<Throwable> cbError) {
        while (true) {
            if (lock.compareAndSet(false, true)) {
                if (reset) result = null;
                if (isLoading) {
                    mListener = cb;
                    mListenerError = cbError;
                    lock.set(false);
                } else if (result == null){
                    lock.set(false);
                    start(cb, cbError);
                }else {
                    times++;
                    lock.set(false);
                    if (result instanceof Throwable) {
                        if (cbError != null) cbError.call((Throwable) result);
                    }else {
                        try {
                            if (cb != null) cb.call(result);
                        }catch (Exception e) {
                            if (cbError != null) cbError.call(e);
                        }
                    }
                }
                break;
            }
        }

    }

    public abstract void proceed(ICallBack<Object> successCb, ICallBack<Throwable> errorCb);
}
