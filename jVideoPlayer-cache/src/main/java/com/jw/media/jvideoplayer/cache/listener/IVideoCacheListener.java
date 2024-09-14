package com.jw.media.jvideoplayer.cache.listener;

import com.jw.media.jvideoplayer.cache.model.VideoCacheInfo;

/**
 * Created by Joyce.wang on 2024/9/11 8:56
 *
 * @Description TODO
 */
public interface IVideoCacheListener {
    void onCacheStart(VideoCacheInfo cacheInfo);

    void onCacheProgress(VideoCacheInfo cacheInfo);

    void onCacheError(VideoCacheInfo cacheInfo, int errorCode);

    void onCacheForbidden(VideoCacheInfo cacheInfo);

    void onCacheFinished(VideoCacheInfo cacheInfo);
}
