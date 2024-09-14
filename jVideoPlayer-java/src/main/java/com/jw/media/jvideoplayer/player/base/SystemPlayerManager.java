package com.jw.media.jvideoplayer.player.base;

import android.content.Context;
import android.media.AudioManager;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.Surface;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.lib.provider.ContextProvider;

import java.io.File;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * Created by Joyce.wang on 2024/3/11 14:48
 *
 * @Description AndroidMediaPlayer manager (System Player)
 */
class SystemPlayerManager implements IPlayerManager {
    private static Logger logger = LoggerFactory.getLogger(SystemPlayerManager.class.getName());
    private Context context;

    private AndroidMediaPlayer mediaPlayer;

    private Surface surface;

    private boolean isReleased;

    private boolean isPlaying = false;
    @Override
    public IMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void initVideoPlayer(Context context, InitializationPlayerConfig config) {
        this.context = context.getApplicationContext();
        mediaPlayer = new AndroidMediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        isReleased = false;
        try {
            //set url
            Uri uri = Uri.parse(config.getUrl());
            String scheme = uri.getScheme();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    (TextUtils.isEmpty(scheme) || scheme.equalsIgnoreCase("file"))) {
                IMediaDataSource dataSource = new FileMediaDataSource(new File(uri.toString()));
                mediaPlayer.setDataSource(dataSource);
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mediaPlayer.setDataSource(ContextProvider.getContext(), uri, config.getHeaders());
            } else {
                mediaPlayer.setDataSource(uri.toString());
            }
            mediaPlayer.setLooping(config.isLooping());
            if (config.getSpeed() != 1 && config.getSpeed() > 0) {
                setSpeed(config.getSpeed());
            }
        } catch (Exception e) {
            logger.e("Error initializing video player.");
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    @Override
    public int getVideoWidth() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoWidth();
        }
        return 0;
    }

    @Override
    public int getVideoHeight() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoHeight();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public void seekTo(long position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    @Override
    public long getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public int getVideoSarNum() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoSarNum();
        }
        return 0;
    }

    @Override
    public int getVideoSarDen() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoSarDen();
        }
        return 0;
    }

    @Override
    public void setDisplay(Surface surface) {
        if (surface == null && mediaPlayer != null && !isReleased) {
            mediaPlayer.setSurface(null);
        } else if (surface != null) {
            this.surface = surface;
            if (mediaPlayer != null && surface.isValid() && !isReleased) {
                mediaPlayer.setSurface(surface);
            }
            if (!isPlaying) {
                pause();
            }
        }
    }

    @Override
    public void setNeedMute(boolean needMute) {
        try {
            if (mediaPlayer != null && !isReleased) {
                if (needMute) {
                    mediaPlayer.setVolume(0, 0);
                } else {
                    mediaPlayer.setVolume(1, 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setVolume(float left, float right) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(left, right);
        }
    }

    @Override
    public void releaseSurface() {
        if (surface != null) {
            //surface.release();
            surface = null;
        }
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            isReleased = true;
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void setSpeed(float speed) {
        if (isReleased || mediaPlayer == null
                || mediaPlayer.getInternalMediaPlayer() == null || !mediaPlayer.isPlayable()) {
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed(speed);
                mediaPlayer.getInternalMediaPlayer().setPlaybackParams(playbackParams);
            } else {
                logger.w("Setting playback speed is not supported on this Android version.");
            }
        } catch (Exception e) {
            logger.e("Error setting playback speed.");
            e.printStackTrace();
        }
    }
}
