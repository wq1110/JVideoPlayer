package com.jw.media.jvideoplayer.player.youtube;

import android.text.TextUtils;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.mvx.mvp.APIDataListener;
import com.jw.media.jvideoplayer.mvx.mvp.BaseAPIImpl;

/**
 * Created by Joyce.wang on 2024/9/13 14:24
 *
 * @Description TODO
 */
public class YoutubeRemoteDataSource extends BaseAPIImpl {
    private static Logger logger = LoggerFactory.getLogger(YoutubeRemoteDataSource.class.getSimpleName());

    public void fetchTrailerUrl(String source, String sourceKey, APIDataListener<String> listener) {
        request(new APIRequestTask("fetchTrailerUrl", true, new Runnable() {
            @Override
            public void run() {
                try {
                    String realUrl = null;
                    String playUrl = YouTubeData.getYouTubeVideoId(sourceKey);
                    //使用youtube解析地址播放
                    logger.i("start youtube parse");
                    try {
                        realUrl = YouTubeData.getRealYouTubeUrl(playUrl);
                        logger.d("realUrl:%s", realUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    logger.i("realUrl:%s", realUrl);

                    if (TextUtils.isEmpty(realUrl)) {
                        logger.e("fetch trailer url error, source: %s, playUrl: %s", source, playUrl);
                    }

                    if (listener != null) {
                        listener.onSuccess(realUrl);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onError(e);
                    }
                }
            }
        }, listener));
    }
}
