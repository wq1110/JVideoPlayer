package com.jw.media.jvideoplayer.player.play;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.jw.media.jvideoplayer.player.base.PlayerStatus;
import com.jw.media.jvideoplayer.player.base.VideoPlayer;
import com.jw.media.jvideoplayer.player.controller.IMediaController;
import com.jw.media.jvideoplayer.player.model.PlayParam;
import com.jw.media.jvideoplayer.player.request.MediaRequest;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Joyce.wang on 2024/9/13 9:09
 *
 * @Description 播放功能对外接口
 */
public interface IPlay {
    /**
     *@description 设置播放view
     *@return
     */
    void setVideoView(@NonNull VideoPlayer videoPlayer);

    /**
     * @description 设置播放控制view
     * @return
     */
    void setMediaController(@NonNull IMediaController mediaController);

    /**
     * @description 开始播放
     * @param playparam #PlayParam #toPlayTrailer(String url) 播放Trailer
     *
     * @return
     */
    MediaRequest<Boolean> startPlay(@NonNull PlayParam playparam);

    /**
     *@description 播放下一集
     *@return
     */
    MediaRequest<Void> nextEpisode(Activity activity);

    /**
     * @description 恢复播放
     * @return
     */
    void resumePlay();

    /**
     * @description 暂停播放
     * @return
     */
    void pause();

    /**
     * @description seek播放
     * @param position seek的位置
     * @return
     */
    void seekTo(int position);

    /**
     * @description 停止播放
     * @return
     */
    void stop();

    /**
     *@description 设置静音
     *@param needMute
     *@return
     */
    void setNeedMute(boolean needMute);

    /**
     * @description 设置Aspect Ratio模式
     * @param aspectRatioMode
     * @return
     */
    void setAspectRatioMode(int aspectRatioMode);

    /**
     * @description 是否在播放中（不包含暂停，暂停会返回false）
     * @return
     */
    boolean isPlaying();

    /**
     * @description 获取视频播放总时长
     * @return
     */
    long getDuration();

    /**
     * @description 获取当前播放位置
     * @param
     * @return
     */
    long getCurrentPosition();

    /**
     * @description 获取当前播放状态
     * @param
     * @return
     */
    PlayerStatus getCurrentPlayState();

    /**
     * @description 获取视频缓冲百分比
     * @return
     */
    int getBufferPercentage();

    /**
     * @description 获取当前视频显示比例类型
     * @return
     */
    int getCurrentAspectRatioMode();

    /**
     * @description 注册起播成功监听 listener
     * @param l
     * @return
     */
    void setOnPreparedListener(IMediaPlayer.OnPreparedListener l);

    /**
     * @description 设置播放缓冲监听
     * @param l
     * @return
     */
    void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener l);

    /**
     * @description 注册播放器error监听 listener
     * @param l
     * @return
     */
    void setOnErrorListener(IMediaPlayer.OnErrorListener l);

    /**
     * @description 注册播放器完成监听 listener
     * @param l
     * @return
     */
    void setOnCompletionListener(IMediaPlayer.OnCompletionListener l);

    /**
     * @description 注册播放器info event监听 listener
     * @param l
     * @return
     */
    void setOnInfoListener(IMediaPlayer.OnInfoListener l);

    /**
     * @description 注册播放器seek完成监听 listener
     * @param l
     * @return
     */
    void setOnSeekCompleteListener(IMediaPlayer.OnSeekCompleteListener l);
}
