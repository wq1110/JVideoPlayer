package com.jw.media.jvideoplayer.cache.listener;

import com.jw.media.jvideoplayer.cache.common.VideoCacheException;
import com.jw.media.jvideoplayer.cache.m3u8.M3U8;
import com.jw.media.jvideoplayer.cache.model.VideoCacheInfo;

/**
 * Created by Joyce.wang on 2024/9/11 8:57
 *
 * @Description TODO
 */
public interface IVideoInfoParsedListener {
    //M3U8视频解析成功
    void onM3U8ParsedFinished(M3U8 m3u8, VideoCacheInfo cacheInfo);

    //M3U8视频解析失败
    void onM3U8ParsedFailed(VideoCacheException exception, VideoCacheInfo cacheInfo);

    //M3U8视频是直播
    void onM3U8LiveCallback(VideoCacheInfo cacheInfo);

    //非M3U8视频解析成功
    void onNonM3U8ParsedFinished(VideoCacheInfo cacheInfo);

    //非M3U8视频解析失败
    void onNonM3U8ParsedFailed(VideoCacheException exception, VideoCacheInfo cacheInfo);
}
