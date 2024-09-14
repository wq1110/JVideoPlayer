package com.jw.media.jvideoplayer.mvx.base;

/**
 * Created by Joyce.wang on 2024/9/11 15:55
 *
 * @Description TODO
 */
public interface LayoutAsyncSupported {
    //组件的View是否已经准备好
    boolean viewReady();
    //View准备好为前提 do sth
    void doAfterViewReady(Runnable runnable);
}
