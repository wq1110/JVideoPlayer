package com.jw.media.jvideoplayer.player.play;

import com.jw.media.jvideoplayer.mvx.mvp.APIDataListener;

/**
 * Created by Joyce.wang on 2024/9/13 10:39
 *
 * @Description TODO
 */
public class ExceptionUtil {
    public static  <T> void handleError(APIDataListener<T> listener, int errorCode, String slaReason){
        if(listener != null){
            listener.onError(new PlayException(errorCode));
        }
    }

    public static  <T> void handleError(APIDataListener<T> listener, Throwable throwable, String slaReason){
        if(listener != null){
            listener.onError(throwable);
        }
    }
}