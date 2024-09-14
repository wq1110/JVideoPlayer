package com.jw.media.jvideoplayer.cache.okhttp;

/**
 * Created by Joyce.wang on 2024/9/11 8:59
 *
 * @Description TODO
 */
public interface IFetchResponseListener {
    //通过一条请求获取contentLength
    void onContentLength(long contentLength);
}
