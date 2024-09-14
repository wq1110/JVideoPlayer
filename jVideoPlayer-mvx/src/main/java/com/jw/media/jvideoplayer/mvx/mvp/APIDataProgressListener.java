package com.jw.media.jvideoplayer.mvx.mvp;

/**
 * Created by Joyce.wang on 2024/9/13 14:26
 *
 * @Description TODO
 */
public interface APIDataProgressListener<T> extends APIDataListener<T> {
    void onProgress(int progress);
}