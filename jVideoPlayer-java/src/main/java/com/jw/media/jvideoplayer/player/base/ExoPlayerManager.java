package com.jw.media.jvideoplayer.player.base;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.Surface;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.lib.provider.ContextProvider;

import java.io.File;
import java.util.Map;

import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * Created by Joyce.wang on 2024/3/11 14:28
 *
 * @Description IjkExoMediaPlayer manager (EXO Player)
 */
class ExoPlayerManager implements IPlayerManager {
    private static Logger logger = LoggerFactory.getLogger(ExoPlayerManager.class.getName());

    private IjkExoMediaPlayer mediaPlayer;
    private Surface surface;
    @Override
    public IMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void initVideoPlayer(Context context, InitializationPlayerConfig config) {
        mediaPlayer = new IjkExoMediaPlayer(context);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
//            mediaPlayer.setLooping(config.isLooping());//no support
            //set url
            Uri uri = Uri.parse(config.getUrl());
            setDataSource(uri, config.getHeaders());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the data source for the media player based on the URI and headers.
     * This method handles different URI schemes and Android versions for compatibility.
     *
     * @param uri     The URI of the video to play.
     * @param headers Optional headers to include in the request.
     */
    private void setDataSource(Uri uri,  Map<String,String> headers) throws Exception {
        String scheme = uri.getScheme();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (TextUtils.isEmpty(scheme) || scheme.equalsIgnoreCase("file"))) {
            IMediaDataSource dataSource = new FileMediaDataSource(new File(uri.toString()));
            mediaPlayer.setDataSource(dataSource);
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mediaPlayer.setDataSource(ContextProvider.getContext(), uri, headers);
        } else {
            mediaPlayer.setDataSource(uri.toString());
        }
    }

    @Override
    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
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
            return mediaPlayer.getVideoWidth();
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
        if (surface == null && mediaPlayer != null) {
            mediaPlayer.setSurface(null);
        } else if (surface != null) {
            this.surface = surface;
            if (mediaPlayer != null && surface.isValid()) {
                mediaPlayer.setSurface(surface);
            }
        }
    }

    @Override
    public void setNeedMute(boolean needMute) {
        if (mediaPlayer != null) {
            if (needMute) {
                mediaPlayer.setVolume(0, 0);
            } else {
                mediaPlayer.setVolume(1, 1);
            }
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
            mediaPlayer.setSurface(null);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
