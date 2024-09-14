package com.jw.media.jvideoplayer.player.play;

/**
 * Created by Joyce.wang on 2024/9/12 11:26
 *
 * @Description TODO
 */
public class PlayConstant {
    public static final class Error {
        public static final int ERROR_CODE_NO_SUPPORT = 10000;//不支持使用
        public static final int ERROR_CODE_NO_READY = 10001;//未准备好
        public static final int ERROR_CODE_PARAM_ERROR = 10002;//参数有问题
        public static final int ERROR_CODE_PLAY_SOURCE_NOT_EXIST = 10003;//播放资源不存在
        public static final int ERROR_CODE_QUERY_PLAY_SOURCE_FAIL = 10004;//请求播放资源失败
        public static final int ERROR_CODE_PROXY_SERVICE_ERROR = 10005;//proxy跟引擎建立service失败
        public static final int ERROR_CODE_NO_TRAILER_URL = 10006;//trailer url不存在
        public static final int ERROR_CODE_NOT_SUPPORTED_ROTATION = 10007;//剧集还不支持轮播
        public static final int ERROR_CODE_QUERY_VIDEO_DETAIL_FAIL = 10008;//请求影片详情信息失败
        public static final int ERROR_CODE_DETAIL_META_NOT_EXIST = 10009;//请求影片详情信息不存在
        public static final int ERROR_CODE_PLAY_COMPLETED_FOR_ROTATION = 10010;//轮播最后一部电影结束
        public static final int ERROR_CODE_SERVICE_EXPIRATION = 10011;//服务到期
        public static final int ERROR_CODE_USER_NOT_ACTIVATED = 10012;//用户未激活
    }

    public static final class Message {
        private static final int MESSAGE_BASE = 0x9000;
        public static final int MESSAGE_UPDATE_PROGRESS = MESSAGE_BASE + 1;
        public static final int MESSAGE_HIDE_GESTURE_BOX = MESSAGE_BASE + 2;
        public static final int MESSAGE_SHOW_BOX = MESSAGE_BASE + 3;//展示所有box控件（包含顶部box，右边box，底部box，中间box， 不包含loading box）
        public static final int MESSAGE_HIDE_BOX = MESSAGE_BASE + 4;//隐藏所有Box控件（包含顶部box，右边box，底部box，中间box， 不包含loading box）
        public static final int MESSAGE_SHOW_LOADING = MESSAGE_BASE + 5;//展示loading
        public static final int MESSAGE_HIDE_LOADING = MESSAGE_BASE + 6;//隐藏loading
        public static final int MESSAGE_SEEK_NEW_POSITION = MESSAGE_BASE + 7;
        public static final int MESSAGE_HIDE_CENTER_BOX = MESSAGE_BASE + 8;
        public static final int MESSAGE_CONFIGURATION_CHANGED_PORTRAOT =  MESSAGE_BASE + 9;
        public static final int MESSAGE_CONFIGURATION_CHANGED_LANDSCAPE =  MESSAGE_BASE + 10;
        public static final int MESSAGE_UPDATE_SUBTITLE = MESSAGE_BASE + 11;//更新字幕
        public static final int MESSAGE_CHANGE_PLAY_STATUS = MESSAGE_BASE + 12;
        public static final int MESSAGE_SHOW_MEIDA_PLAY = MESSAGE_BASE + 13;//展示视频中间的播放按钮
        public static final int MESSAGE_HIDE_MEDIA_PLAY = MESSAGE_BASE + 14;//隐藏视频中间的播放按钮
        public static final int MESSAGE_UPDATE_LOADING_EXTRA_INFO = MESSAGE_BASE + 15;
        public static final int MESSAGE_SHOW_NEXT_EPISODE = MESSAGE_BASE + 16;//展示next episode 按钮
        public static final int MESSAGE_HIDE_NEXT_EPISODE = MESSAGE_BASE + 17;//隐藏next episode 按钮
        public static final int MESSAGE_RESET_PLAYER = MESSAGE_BASE + 18;//重置播放
        public static final int MESSAGE_ENTER_FULL_SCREEN_PLAY = MESSAGE_BASE + 19;//全屏播放
        public static final int MESSAGE_EXIT_FULL_SCREEN_PLAY = MESSAGE_BASE + 20;//退出全屏播放
        public static final int MESSAGE_UPDATE_MEDIA_TITLE = MESSAGE_BASE + 21;//更新title
    }
}
