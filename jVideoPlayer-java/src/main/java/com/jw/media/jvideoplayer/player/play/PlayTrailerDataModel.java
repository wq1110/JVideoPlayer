package com.jw.media.jvideoplayer.player.play;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.jw.media.jvideoplayer.cache.VideoProxyCacheManager;
import com.jw.media.jvideoplayer.cache.utils.ProxyCacheUtils;
import com.jw.media.jvideoplayer.cache.utils.ThreadUtils;
import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.mvx.mvp.APIDataListener;
import com.jw.media.jvideoplayer.player.base.VideoType;
import com.jw.media.jvideoplayer.player.model.PlayParam;
import com.jw.media.jvideoplayer.player.trailer.LocalProxyVideoControl;
import com.jw.media.jvideoplayer.player.youtube.TrailerSourceType;
import com.jw.media.jvideoplayer.player.youtube.YouTubeData;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Joyce.wang on 2024/9/13 13:37
 *
 * @Description TODO
 */
public class PlayTrailerDataModel extends ItemDataModel<PlayRepository> {
    private static Logger logger = LoggerFactory.getLogger(PlayTrailerDataModel.class.getSimpleName());
    public ConcurrentHashMap<String, String> mUrlMaps = new ConcurrentHashMap<>();
    public LocalProxyVideoControl mLocalProxyVideoControl;

    public PlayTrailerDataModel(@NonNull PlayRepository dataModel) {
        super(dataModel);
        mLocalProxyVideoControl = new LocalProxyVideoControl();
    }

    public void playTrailer(PlayParam.PlayTrailerParam param, APIDataListener<Boolean> listener) {
        String url = param.url;

        if (TextUtils.isEmpty(url)) {
            logger.e("play banner error, url: %s", url);
            ExceptionUtil.handleError(listener, PlayConstant.Error.ERROR_CODE_PARAM_ERROR, null);
            return;
        }
        mDataModel.mPlayLiveData.setUrl(url);
        mDataModel.mPlayLiveData.setLandscape(param.isLandscape);
        mDataModel.mPlayLiveData.setTrailer(true);
        processPlayTrailerWithUrl(url, listener);
    }

    private void processPlayTrailerWithUrl(String url, APIDataListener<Boolean> listener) {
        // YouTube视频处理
        if (YouTubeData.isYouTubeUrl(url)) {
            String sourceKey = YouTubeData.getYoutubeWebKey(url);
            processYouTubeVideo(VideoType.TYPE_SOURCE_YOUTUBE, sourceKey, listener);
            return;
        }

        //非YouTube视频处理
        TrailerSourceType type = TrailerSourceType.UNKNOWN;
        String cachePath = getCachePath(url, type);
        if (!TextUtils.isEmpty(cachePath)) {
            ThreadUtils.runOnUiThread(() -> {
                preparePlay(url, cachePath, type);
            }, 500);
            return;
        }

        ThreadUtils.runOnUiThread(() -> {
            preparePlay(url, url, type);
        }, 500);
    }

    private void processYouTubeVideo(String source, String sourceKey, APIDataListener<Boolean> listener) {
        TrailerSourceType type = TrailerSourceType.YOUTUBE;
        String cachePath = getCachePath(sourceKey, type);
        if (!TextUtils.isEmpty(cachePath)) {
            //有缓存，播放缓存
            ThreadUtils.runOnUiThread(() -> {
                preparePlay(sourceKey, cachePath, type);
            }, 500);
            return;
        }
        if (mUrlMaps.containsKey(sourceKey) && !TextUtils.isEmpty(mUrlMaps.get(sourceKey))) {
            ThreadUtils.runOnUiThread(() -> {
                preparePlay(sourceKey, mUrlMaps.get(sourceKey), type);
            }, 500);
            return;
        }

        mDataModel.mYoutubeRemoteDataSource.fetchTrailerUrl(source, sourceKey, new APIDataListener<String>() {
            @Override
            public void onSuccess(String realUrl) {
                if (TextUtils.isEmpty(realUrl)) {
                    logger.e("fetch trailer url, url not exist.");
                    mDataModel.clean(false);
                    ExceptionUtil.handleError(listener, PlayConstant.Error.ERROR_CODE_NO_TRAILER_URL, null);
                    return;
                }
                if (!TextUtils.isEmpty(sourceKey)) {
                    if (!mUrlMaps.containsKey(sourceKey)) {
                        mUrlMaps.put(sourceKey, realUrl);
                    } else {
                        mUrlMaps.remove(sourceKey);
                        mUrlMaps.put(sourceKey, realUrl);
                    }
                }
                preparePlay(sourceKey, realUrl, type);
                if (listener != null) listener.onSuccess(true);
            }

            @Override
            public void onError(Throwable exception) {
                logger.e("fetch trailer url fail.");
                exception.printStackTrace();
                mDataModel.clean(false);
                ExceptionUtil.handleError(listener, PlayConstant.Error.ERROR_CODE_NO_TRAILER_URL, null);
            }
        });
    }

    private String getCachePath(String url, TrailerSourceType type) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String cachePath = null;
        if (type == TrailerSourceType.UNKNOWN) {
            cachePath = VideoProxyCacheManager.getInstance().getTrailerCacheUrl(url);
        } else {
            String sourceId = ProxyCacheUtils.getSourceId(YouTubeData.getYoutubeWebKey(url), VideoType.TYPE_SOURCE_YOUTUBE);
            cachePath = VideoProxyCacheManager.getInstance().getTrailerCacheUrl(sourceId);
        }
        return cachePath;
    }

    //不需要去解析youtube或者向自主网关请求播放地址
    private void preparePlay(String url, String realPlayUrl, TrailerSourceType type) {
        if (type == TrailerSourceType.UNKNOWN) {
            String sourceId = realPlayUrl;
            String playUrl = ProxyCacheUtils.getProxyUrl(sourceId, realPlayUrl, null, null);
            mLocalProxyVideoControl.startRequestVideoInfo(sourceId, realPlayUrl, null, null);
            mDataModel.mVideoPlayer.setVideoUrl(playUrl);
        } else {
            String sourceId = ProxyCacheUtils.getSourceId(YouTubeData.getYoutubeWebKey(url), VideoType.TYPE_SOURCE_YOUTUBE);
            String playUrl = ProxyCacheUtils.getProxyUrl(sourceId, realPlayUrl, null, null);
            mLocalProxyVideoControl.startRequestVideoInfo(sourceId, realPlayUrl, null, null);
            mDataModel.mVideoPlayer.setVideoUrl(playUrl);
        }
        mDataModel.resumePlay();
    }
}
