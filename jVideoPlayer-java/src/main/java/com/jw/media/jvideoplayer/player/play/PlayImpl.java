package com.jw.media.jvideoplayer.player.play;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.jw.media.jvideoplayer.mvx.mvp.APIDataListener;
import com.jw.media.jvideoplayer.player.base.PlayerStatus;
import com.jw.media.jvideoplayer.player.base.VideoPlayer;
import com.jw.media.jvideoplayer.player.controller.IMediaController;
import com.jw.media.jvideoplayer.player.model.PlayParam;
import com.jw.media.jvideoplayer.player.request.MediaRequest;
import com.jw.media.jvideoplayer.player.request.MediaRequestImpl;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Joyce.wang on 2024/9/13 9:56
 *
 * @Description TODO
 */
public class PlayImpl implements IPlay {
    @Override
    public void setVideoView(@NonNull VideoPlayer videoPlayer) {
        PlayRepository.getInstance().setVideoView(videoPlayer);
    }

    @Override
    public void setMediaController(@NonNull IMediaController mediaController) {
        PlayRepository.getInstance().setMediaController(mediaController);
    }

    @Override
    public MediaRequest<Boolean> startPlay(@NonNull PlayParam playparam) {
        return new MediaRequestImpl<Boolean>() {
            @Override
            protected void run() {
                PlayRepository.getInstance().startPlay(playparam, new APIDataListener<Boolean>() {

                    @Override
                    public void onSuccess(Boolean result) {
                        notifySuccess(result);
                    }

                    @Override
                    public void onError(Throwable exception) {
                        PlayRepository.getInstance().clean(false);
                        notifyException(exception);
                    }
                });
            }
        };
    }

    @Override
    public MediaRequest<Void> nextEpisode(Activity activity) {
        return new MediaRequestImpl<Void>() {
            @Override
            protected void run() {
                PlayRepository.getInstance().nextEpisode(activity, new APIDataListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        notifySuccess(result);
                    }

                    @Override
                    public void onError(Throwable exception) {
                        notifyException(exception);
                    }
                });
            }
        };
    }

    @Override
    public void resumePlay() {
        PlayRepository.getInstance().resumePlay();
    }

    @Override
    public void pause() {
        PlayRepository.getInstance().pause();
    }

    @Override
    public void seekTo(int position) {
        PlayRepository.getInstance().seekTo(position);
    }

    @Override
    public void stop() {
        PlayRepository.getInstance().stop();
    }

    @Override
    public void setNeedMute(boolean needMute) {
        PlayRepository.getInstance().setNeedMute(needMute);
    }

    @Override
    public void setAspectRatioMode(int aspectRatioMode) {
        PlayRepository.getInstance().setAspectRatioMode(aspectRatioMode);
    }

    @Override
    public boolean isPlaying() {
        return PlayRepository.getInstance().isPlaying();
    }

    @Override
    public long getDuration() {
        return PlayRepository.getInstance().getDuration();
    }

    @Override
    public long getCurrentPosition() {
        return PlayRepository.getInstance().getCurrentPosition();
    }

    @Override
    public PlayerStatus getCurrentPlayState() {
        return PlayRepository.getInstance().getCurrentPlayState();
    }

    @Override
    public int getBufferPercentage() {
        return PlayRepository.getInstance().getBufferPercentage();
    }

    @Override
    public int getCurrentAspectRatioMode() {
        return PlayRepository.getInstance().getCurrentAspectRatioMode();
    }

    @Override
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        PlayRepository.getInstance().setOnPreparedListener(l);
    }

    @Override
    public void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener l) {
        PlayRepository.getInstance().setOnBufferingUpdateListener(l);
    }

    @Override
    public void setOnErrorListener(IMediaPlayer.OnErrorListener l) {
        PlayRepository.getInstance().setOnErrorListener(l);
    }

    @Override
    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {
        PlayRepository.getInstance().setOnCompletionListener(l);
    }

    @Override
    public void setOnInfoListener(IMediaPlayer.OnInfoListener l) {
        PlayRepository.getInstance().setOnInfoListener(l);
    }

    @Override
    public void setOnSeekCompleteListener(IMediaPlayer.OnSeekCompleteListener l) {
        PlayRepository.getInstance().setOnSeekCompleteListener(l);
    }
}
