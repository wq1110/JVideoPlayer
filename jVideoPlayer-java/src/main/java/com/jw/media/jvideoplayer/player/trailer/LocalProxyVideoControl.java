package com.jw.media.jvideoplayer.player.trailer;

import com.jw.media.jvideoplayer.cache.VideoProxyCacheManager;
import com.jw.media.jvideoplayer.cache.common.VideoParams;
import com.jw.media.jvideoplayer.cache.listener.IVideoCacheListener;
import com.jw.media.jvideoplayer.cache.model.VideoCacheInfo;
import com.jw.media.jvideoplayer.cache.utils.ProxyCacheUtils;
import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.player.base.VideoPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joyce.wang on 2023/4/8.
 */
public class LocalProxyVideoControl {
    private static Logger logger = LoggerFactory.getLogger(LocalProxyVideoControl.class.getSimpleName());

    private VideoPlayer mVideoPlayer;
    private String mVideoUrl;
    private String mSourceId = "";

    public void setVideoView(VideoPlayer videoPlayer) {
        mVideoPlayer = videoPlayer;
    }

    //开始cache任务
    public void startRequestVideoInfo(String sourceId, String videoUrl, Map<String, String> headers, Map<String, Object> extraParams) {
        mVideoUrl = videoUrl;//待请求的url
        mSourceId = sourceId;

        String md5 = ProxyCacheUtils.computeMD5(sourceId);

        VideoProxyCacheManager.getInstance().addCacheListener(sourceId, mVideoUrl, mListener);//添加缓存listener,有开始缓存、缓存进度更新、缓存失败、缓存成功的回调
        VideoProxyCacheManager.getInstance().setPlayingUrlMd5(md5);
        VideoProxyCacheManager.getInstance().startRequestVideoInfo(sourceId, videoUrl, headers, extraParams);
    }

    public void pauseLocalProxyTask() {
        VideoProxyCacheManager.getInstance().pauseCacheTask(mSourceId);
    }

    public void resumeLocalProxyTask() {
        VideoProxyCacheManager.getInstance().resumeCacheTask(mSourceId);
    }

    public void seekToCachePosition(long position) {
        if (mVideoPlayer != null) {
            long totalDuration = mVideoPlayer.getDuration();
            if (totalDuration > 0) {
                float percent = position * 1.0f / totalDuration;
                VideoProxyCacheManager.getInstance().seekToCacheTaskFromClient(mSourceId, percent);
            }
        }
    }

    public void releaseLocalProxyResources() {
        VideoProxyCacheManager.getInstance().stopCacheTask(mSourceId);   //停止视频缓存任务
        VideoProxyCacheManager.getInstance().releaseProxyReleases(mSourceId);
    }

    //cache task progress listener
    private IVideoCacheListener mListener = new IVideoCacheListener() {
        @Override
        public void onCacheStart(VideoCacheInfo cacheInfo) {
            logger.d("onCacheStart");
        }

        @Override
        public void onCacheProgress(VideoCacheInfo cacheInfo) {
            Map<String, Object> params = new HashMap<>();
            params.put(VideoParams.PERCENT, cacheInfo.getPercent());
            params.put(VideoParams.CACHE_SIZE, cacheInfo.getCachedSize());
        }

        @Override
        public void onCacheError(VideoCacheInfo cacheInfo, int errorCode) {
            logger.d("onCacheError");
        }

        @Override
        public void onCacheForbidden(VideoCacheInfo cacheInfo) {

        }

        @Override
        public void onCacheFinished(VideoCacheInfo cacheInfo) {
            Map<String, Object> params = new HashMap<>();
            params.put(VideoParams.PERCENT, 100f);
            params.put(VideoParams.TOTAL_SIZE, cacheInfo.getTotalSize());
        }
    };
}
