package com.jw.media.jvideoplayer.cache.listener;

import com.jw.media.jvideoplayer.cache.model.VideoRange;

/**
 * Created by Joyce.wang on 2024/9/11 8:56
 *
 * @Description TODO
 */
public interface IMp4CacheThreadListener {
    void onCacheFailed(VideoRange range, Exception e);

    //缓存文件的进度
    void onCacheProgress(VideoRange range, long cachedSize, float speed, float percent);

    //当前range 文件缓存完全
    void onCacheRangeCompleted(VideoRange range);

    void onCacheCompleted(VideoRange range);
}
