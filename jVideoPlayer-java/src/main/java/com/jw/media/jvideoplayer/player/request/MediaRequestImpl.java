package com.jw.media.jvideoplayer.player.request;

import com.jw.media.jvideoplayer.lib.async.ThreadsBox;
import com.jw.media.jvideoplayer.lib.rxjava.StreamController;

import java.util.concurrent.atomic.AtomicBoolean;
import io.reactivex.Observable;

/**
 * Created by Joyce.wang on 2024/9/13 9:18
 *
 * @Description TODO
 */
public abstract class MediaRequestImpl<T> implements MediaRequest<T> {
    Callback<T> mCallback;
    StreamController<T> streamController = new StreamController<>();
    AtomicBoolean started = new AtomicBoolean(false);
    T result;
    Throwable throwable;

    protected abstract void run();

    public MediaRequestImpl<T> getVoDRequestImpl() {
        return this;
    }

    void proceedInternal() {
        if (started.compareAndSet(false, true)) {
            try {
                run();
            } catch (Exception e) {
                notifyException(e);
            }
        } else
            throw new RuntimeException("Request already started");
    }

    public void notifySuccess(T result) {
        this.result = result;
        if (mCallback != null) {
            mCallback.onSuccess(result);
        } else
            streamController.push(result);
    }

    public void notifyBusinessFail(int code, String msg, Throwable throwable) {
        this.throwable = throwable;
        if (mCallback != null) {
            mCallback.onBusinessFail(code, msg, throwable);
        } else
            streamController.error(throwable);
    }

    public void notifyFail(int errCode, String errMsg) {
        if (errCode == -1) {
            notifyException(new RuntimeException(errMsg));
        } else {
            notifyBusinessFail(errCode, errMsg, null);
        }
    }

    public void handleResponse(ResponseStatus response) {
        if (response.getCode() == 200) {
            if (response instanceof ResponseData) {
                notifySuccess((T) ((ResponseData<?>) response).getData());
            } else
                notifySuccess(null);
        } else {
            notifyFail(response.getCode(), response.getMsg());
        }
    }

    public void handleError(Throwable e) {
        if (e instanceof ServerException) {
            ServerException serverException = (ServerException) e;
            ResponseStatus responseStatus = serverException.getResponseStatus();
            notifyFail(responseStatus.getCode(), responseStatus.getMsg());
        } else {
            notifyException(e);
        }
    }

    public void notifyException(Throwable e) {
        this.throwable = e;
        if (mCallback != null) {
            mCallback.onError(e);
        } else
            streamController.error(e);
    }

    @Override
    public void proceed(Callback<T> callback) {
        mCallback = callback;
        proceedInternal();
    }

    @Override
    public T proceed() {
        return proceedWithRx().blockingFirst();
    }

    @Override
    public Observable<T> proceedWithRx() {
        return streamController.stream().doOnSubscribe(disposable -> {
            ThreadsBox.getDefaultMainHandler().post(() -> proceedInternal());
        });
    }
}
