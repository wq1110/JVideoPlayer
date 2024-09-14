package com.jw.media.jvideoplayer.player.base;

import android.content.Context;
import android.view.Surface;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Joyce.wang on 2024/3/11 14:19
 *
 * @Description Manages Player
 */
interface IPlayerManager {
    IMediaPlayer getMediaPlayer();

    /**
     * Initializes the video player with necessary configurations.
     *
     * @param context The application context.
     * @param config  Initialization configurations for the player.
     */
    void initVideoPlayer(Context context,InitializationPlayerConfig config);

    void start();

    void stop();

    void pause();

    int getVideoWidth();

    int getVideoHeight();
    /**
      * 是否在播放中（不包含暂停，暂停返回false）
     */
    boolean isPlaying();

    void seekTo(long position);

    long getCurrentPosition();

    long getDuration();

    /**
     * 视频横向采样数值（像素点数）
     */
    int getVideoSarNum();

    /**
     * 视频纵向采样数值（像素点数）
     */
    int getVideoSarDen();

    //设置渲染显示
    void setDisplay(Surface surface);

    /**
     *@description Sets whether the video should be muted.
     *
     *@param needMute True to mute, false to unmute.
     */
    void setNeedMute(boolean needMute);

    /**
     * Sets the volume levels for left and right channels.
     *
     * @param leftVolume  Left channel volume (0.0 - 1.0).
     * @param rightVolume Right channel volume (0.0 - 1.0).
     */
    void setVolume(float leftVolume, float rightVolume);

    //Releases the rendering surface.（释放渲染）
    void releaseSurface();

    //释放内核
    void release();
}
