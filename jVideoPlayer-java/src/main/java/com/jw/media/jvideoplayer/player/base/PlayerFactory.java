package com.jw.media.jvideoplayer.player.base;

/**
 * Created by Joyce.wang on 2024/3/11 15:47
 *
 * @Description 播放器内核工厂，根据对应的播放器管理类，创建对应的播放器管理对象
 *              A factory class responsible for creating instances of IPlayerManager.
 */
public class PlayerFactory {
    private static Class<? extends IPlayerManager> sPlayerManagerClass;

    public static void setPlayManager(Class<? extends IPlayerManager> playerManagerClass) {
        sPlayerManagerClass = playerManagerClass;
    }

    public static IPlayerManager getPlayManager() {
        if (sPlayerManagerClass == null) {
            sPlayerManagerClass = IjkPlayerManager.class;
        }
        try {
            return sPlayerManagerClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
