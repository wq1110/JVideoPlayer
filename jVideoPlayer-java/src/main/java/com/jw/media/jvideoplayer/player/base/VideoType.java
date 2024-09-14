package com.jw.media.jvideoplayer.player.base;

/**
 * Created by Joyce.wang on 2024/3/11 10:32
 *
 * @Description TODO
 */
public class VideoType {
    // 播放器类型
    public static final int PV_PLAYER__IjkExoMediaPlayer = 0;
    public static final int PV_PLAYER__AndroidMediaPlayer = 1;
    public static final int PV_PLAYER__IjkMediaPlayer = 2;

    public static final int MODE_NORMAL = 10;//普通模式
    public static final int MODE_FULL_SCREEN = 11;//全屏模式
    public static final int MODE_SMALL_WINDOW = 12;//小窗口模式


    public final static int AR_ASPECT_FIT_PARENT = 0; // without clip, 自适应屏幕等比例缩放，不变型（保证画面完整显示且画面贴近窗口，但不一定能铺满窗口，可能一边留有黑边）
    public final static int AR_ASPECT_FILL_PARENT = 1; // may clip 填充，不变型，切割画面（等比例缩放铺满窗口，画面可能会被裁掉）
    public final static int AR_ASPECT_WRAP_CONTENT = 2;//自适应 等比例缩小（保证画面完整显示），不进行等比例放大（当视频大小小于屏幕宽高时，以视频宽高为基础展示）
    public final static int AR_MATCH_PARENT = 3;//拉伸（非等比例缩放铺满窗口，画面可能会变形）
    public final static int AR_16_9_FIT_PARENT = 4;//16:9
    public final static int AR_4_3_FIT_PARENT = 5;//4:3

    public static final int RENDER_SURFACE_VIEW = 1;//渲染类型，SurfaceView
    public static final int RENDER_TEXTURE_VIEW = 2;//渲染类型，TextureView

    //audiotrack block error
    public static final int FFP_MSG_ERROR_997 = 997;
    public static final int FFP_MSG_ERROR_998 = 998;

    public static final int MEDIA_INFO_PLAYER_TYPE = 1000001;

    public static final int ERROR_TRAILER_URL_NOT_EXIST = 5000;
    public static final int ERROR_TRAILER_PARSE_URL_EXCEPTION = 5001;

    public static final String TYPE_SOURCE_YOUTUBE = "youtube";
}
