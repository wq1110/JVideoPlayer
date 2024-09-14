package com.jw.media.jvideoplayer.cache.listener;

import com.jw.media.jvideoplayer.cache.common.VideoCacheException;
import com.jw.media.jvideoplayer.cache.m3u8.M3U8;
import com.jw.media.jvideoplayer.cache.model.VideoCacheInfo;

/**
 * Created by Joyce.wang on 2024/9/11 8:57
 *
 * @Description TODO
 */
public abstract class VideoInfoParsedListener implements IVideoInfoParsedListener {
    @Override
    public void onM3U8ParsedFinished(M3U8 m3u8, VideoCacheInfo cacheInfo) {

    }

    @Override
    public void onM3U8ParsedFailed(VideoCacheException exception, VideoCacheInfo cacheInfo) {

    }

    @Override
    public void onM3U8LiveCallback(VideoCacheInfo cacheInfo) {

    }

    @Override
    public void onNonM3U8ParsedFinished(VideoCacheInfo cacheInfo) {

    }

    @Override
    public void onNonM3U8ParsedFailed(VideoCacheException exception, VideoCacheInfo cacheInfo) {

    }
}
