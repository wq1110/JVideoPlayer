package com.jw.media.jvideoplayer.mvx.mvp;

/**
 * Created by Joyce.wang on 2024/9/13 14:26
 *
 * @Description TODO
 */
public class APIResultBean<T> {
    private boolean success;
    private String errorMessage; // 错误信息
    private String errorCode; // 错误码
    public T data;

    public T getData() {
        return data;
    }

    public APIResultBean<T> setData(T data) {
        this.data = data;
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public APIResultBean<T> setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public APIResultBean<T> setErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private APIResultBean<T> setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }
}
