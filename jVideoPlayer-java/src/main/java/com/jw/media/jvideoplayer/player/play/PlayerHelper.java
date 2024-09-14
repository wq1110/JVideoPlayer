package com.jw.media.jvideoplayer.player.play;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.lib.preferences.CommonPreference;
import com.jw.media.jvideoplayer.player.base.VideoType;
import com.jw.media.jvideoplayer.player.listener.OnPlayerTypeCallback;

/**
 * Created by Joyce.wang on 2024/3/12 18:10
 *
 * @Description 播放器帮助类
 */
public class PlayerHelper {
    private static Logger logger = LoggerFactory.getLogger(PlayerHelper.class.getSimpleName());

    public static final String KEY_LAST_PLAYER_TYPE = "key_last_player_type";//记录上一次播放类型
    private int defaultPlayerType = VideoType.PV_PLAYER__IjkMediaPlayer;//默认播放器类型
    private int defaultTailerPlayerType = VideoType.PV_PLAYER__IjkExoMediaPlayer;//默认预告片播放器类型

    private volatile static PlayerHelper instance;

    private OnPlayerTypeCallback callback;

    private PlayerHelper() {
    }

    public static PlayerHelper getInstance() {
        if (instance == null) {
            synchronized(PlayerHelper.class) {
                if (instance == null) {
                    instance = new PlayerHelper();
                }
            }
        }
        return instance;
    }

    public static String getPlayerTypeName(int playerType) {
        switch (playerType) {
            case VideoType.PV_PLAYER__AndroidMediaPlayer:
                return "AndroidMediaPlayer";
            case VideoType.PV_PLAYER__IjkExoMediaPlayer:
                return "IjkExoPlayer";
            case VideoType.PV_PLAYER__IjkMediaPlayer:
                return "IjkMediaPlayer";
            default:
                return "Unknown";
        }
    }

    public void setOnPlayerTypeCallback(OnPlayerTypeCallback callback) {
        this.callback = callback;
    }

    public int getPlayerType(boolean isTrailer) {
        if (isTrailer) {
            return defaultTailerPlayerType;
        } else {
            //获取配置/设置的播放器类型
            int currentPlayerType = callback != null ? callback.onPlayerType() : -1;

            if (currentPlayerType == -1) {
                //获取上一次播放的播放器类型
                return CommonPreference.getInstance().getInt(KEY_LAST_PLAYER_TYPE, defaultPlayerType);
            } else {
                CommonPreference.getInstance().putInt(KEY_LAST_PLAYER_TYPE, currentPlayerType);
                return currentPlayerType;
            }
        }
    }
}
