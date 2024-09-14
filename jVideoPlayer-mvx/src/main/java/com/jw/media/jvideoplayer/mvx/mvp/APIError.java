package com.jw.media.jvideoplayer.mvx.mvp;

/**
 * Created by Joyce.wang on 2024/9/13 14:26
 *
 * @Description API接口 错误类
 */
public abstract class APIError extends Throwable {
    abstract String getErrorCode();

    abstract String getErrorMessage();
}
