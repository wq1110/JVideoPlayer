package com.jw.media.jvideoplayer.player.base;

import android.view.Surface;

import java.util.Map;

/**
 * Created by Joyce.wang on 2024/3/11 16:13
 *
 * @Description Bridge interface between a unified player manager and a basic video player view (VideoPlayer).
 */
interface VideoPlayerBridge {
    /**
     * Starts preparing for playback.
     *
     * @param url     The playback URL.
     * @param headers Headers for the request.
     * @param loop    Whether to loop the playback.
     * @param speed   the playback speed.
     */
    void prepare(final String url, final Map<String, String> headers,
                 boolean loop, float speed, boolean isAudioOnly);

    /**
     * Gets the current player core.
     *
     * @return The current {@link IPlayerManager} instance.
     */
    IPlayerManager getPlayer();

    void start();

    void stop();

    void pause();

    int getVideoWidth();

    int getVideoHeight();

    boolean isPlaying();

    void seekTo(long time);

    long getCurrentPosition();

    long getDuration();

    int getVideoSarNum();

    int getVideoSarDen();

    void setAspectRatioMode(int aspectRatioMode);

    int getAspectRatioMode();

    //释放播放器
    void releaseMediaPlayer();

    void setSelfVideoSizeFlag(boolean selfVideoSizeFlag);

    void setCurrentVideoSize(int videoWidth, int videoHeight);

    int getCurrentVideoWidth();

    int getCurrentVideoHeight();

    void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen);

    int getCurrentVideoSarNum();

    int getCurrentVideoSarDen();

    /**
     * Sets whether the video should be muted.
     */
    void setNeedMute(boolean needMute);

    /**
     * Sets the display surface for video rendering.
     */
    void setDisplay(Surface holder);

    /**
     * Releases the display surface.
     */
    void releaseSurface(Surface surface);

    MediaPlayerListener getMediaPlayerListener();

    void setMediaPlayerListener(MediaPlayerListener listener);
}
