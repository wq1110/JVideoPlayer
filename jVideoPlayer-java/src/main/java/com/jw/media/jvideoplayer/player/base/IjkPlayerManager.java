package com.jw.media.jvideoplayer.player.base;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.lib.preferences.CommonPreference;
import com.jw.media.jvideoplayer.lib.provider.ContextProvider;

import java.io.File;

import tv.danmaku.ijk.media.player.BuildConfig;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkLibLoader;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * Created by Joyce.wang on 2024/3/11 15:00
 *
 * @Description IjkMediaPlayer manager (IJK Player)
 */
class IjkPlayerManager implements IPlayerManager {
    private static Logger logger = LoggerFactory.getLogger(IjkPlayerManager.class.getName());
    private IjkMediaPlayer mediaPlayer;
    private IjkLibLoader ijkLibLoader;
    private Surface surface;
    private boolean usingMediaCodec = true;//使用编解码器硬编码还是软编码，true：硬编码 false：软编码
    private boolean usingOpenSLES = false;//音频播放是否使用openSL，true：使用openSL false： 使用AudioTrack
    @Override
    public IMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void initVideoPlayer(Context context, InitializationPlayerConfig config) {
        mediaPlayer = (ijkLibLoader == null) ? new IjkMediaPlayer() : new IjkMediaPlayer(ijkLibLoader);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // Set IJK log level consistently
        if (BuildConfig.DEBUG) {
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_INFO);
        } else {
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_INFO);
        }

        mediaPlayer.setOnNativeInvokeListener(new IjkMediaPlayer.OnNativeInvokeListener() {
            @Override
            public boolean onNativeInvoke(int i, Bundle bundle) {
                return true;
            }
        });

        String url = config.getUrl();

        // Configure IJK options
        try {
            //设置是否开启 Mediacodec 硬解，1 为硬解，0 使用 FFmpeg 软解
            if (usingMediaCodec) {
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 1);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-avc", 1);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-all-videos", 1);
            } else {
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 0);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-avc", 0);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-all-videos", 0);
            }

            //设置音频播放是否使用 openSL，1：使用 openSL，0：使用 AudioTrack
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", usingOpenSLES ? 1 : 0);

            //设置视频显示格式
            String pixelFormat = "";
            if (TextUtils.isEmpty(pixelFormat)) {
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
            } else {
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", pixelFormat);
            }

            //audiotrack 阻塞自动恢复配置
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-auto-restart-audio",
                    CommonPreference.getInstance().getBoolean(PlayerConstant.ENABLE_AUTO_RESTART_AUDIO_KEY, true) ? 1 : 0);

            // 设置 cpu 处理不过来的时候的丢帧帧数（跳帧处理），默认为 0，参数范围是 [-1, 120]
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);

            //在资源准备好后是否自动播放, 1：自动播放，0：准备好后不自动播放
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);

            //设置是否开启变调，配合 setSpeed 实现变速播放
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);

            //设置是否开启环路过滤，0 为开启，画面质量高，解码开销大，48 为关闭，画面质量稍差，解码开销小
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);

            //参考： https://ffmpeg.org/ffmpeg-protocols.html
            // ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect_at_eof", 1);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect_streamed", 1);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-naked-csd", 1);

            //设置是否开启精准 seek，默认关闭
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
            //设置Buffering是否每收到一帧数据就轮询判断buffering是否结束（判断buffering结束有一系列条件，会按一定的时间间隔轮询判断）
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fast-check-buffering", 1);
            //用于hls协议，把音频的语言参数传递出来。如果有显示语言要求的，建议加上
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "copy_substream_metadata", 1);

            //设置播放前的探测时长，单位：微秒
            long analyzeDuration = CommonPreference.getInstance().getLong(PlayerConstant.PLAYER_ANALYZE_DURATION_KEY, PlayerConstant.PLAYER_ANALYZE_DURATION_DEFAULT);
            if (analyzeDuration > 0) {
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", analyzeDuration);
            }

            if (config.isAudioOnly()) {
                //设置是否禁掉视频 1：禁掉视频只播音频 0：播放视频
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "vn", 1);
            }
            //用于dash协议，默认的ffmpeg只支持一个period，但是流如果发生中断，录制回放时可能出现多个period。这个配置就是将所有的period合成一个
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "merge-period", 1);

            mediaPlayer.setLooping(config.isLooping());
            if (config.getSpeed() != 1 && config.getSpeed() > 0) {
                mediaPlayer.setSpeed(config.getSpeed());
            }

            //set url
            Uri uri = Uri.parse(url);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public IjkLibLoader getIjkLibLoader() {
        return ijkLibLoader;
    }

    public void setIjkLibLoader(IjkLibLoader ijkLibLoader) {
        this.ijkLibLoader = ijkLibLoader;
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
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
