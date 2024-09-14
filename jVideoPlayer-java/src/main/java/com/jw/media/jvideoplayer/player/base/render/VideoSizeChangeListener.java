package com.jw.media.jvideoplayer.player.base.render;

/**
 * Created by Joyce.wang on 2024/3/11 17:37
 *
 * @Description Provides information about the dimensions of a video.
 */
public interface VideoSizeChangeListener {

    int getCurrentVideoWidth();

    int getCurrentVideoHeight();

    int getVideoSarNum();

    int getVideoSarDen();

    int getAspectRatioMode();
}
