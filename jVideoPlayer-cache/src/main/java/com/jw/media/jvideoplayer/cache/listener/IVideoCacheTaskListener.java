package com.jw.media.jvideoplayer.cache.listener;

import java.util.Map;

/**
 * Created by Joyce.wang on 2024/9/11 8:56
 *
 * @Description TODO
 */
public interface IVideoCacheTaskListener {
    void onTaskStart();

    void onTaskProgress(float percent, long cachedSize, float speed);

    void onM3U8TaskProgress(float percent, long cachedSize, float speed, Map<Integer, Long> tsLengthMap);

    void onTaskFailed(Exception e);

    void onTaskCompleted(long totalSize);

    void onM3U8TaskCompleted(long totalSize);

    void onVideoSeekComplete();
}
