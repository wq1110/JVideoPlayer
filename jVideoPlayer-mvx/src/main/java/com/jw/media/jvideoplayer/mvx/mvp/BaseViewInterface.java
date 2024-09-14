package com.jw.media.jvideoplayer.mvx.mvp;

import android.app.Activity;

import com.jw.media.jvideoplayer.lib.callback.ICallBack;
import com.jw.media.jvideoplayer.mvx.base.LayoutAsyncSupported;

/**
 * Created by Joyce.wang on 2024/9/13 14:29
 *
 * @Description View的基本接口
 */
public interface BaseViewInterface extends LayoutAsyncSupported {
    /********************************************OP******************************************************************/
    int OP_BACK = 0x888;


    /**
     * Loading系列
     */
    void showLoading();

    void hideLoading();

    /**
     * 返回上一层
     */
    void back();

    /**
     * 页面消失
     */
    void dismiss();

    /**
     * 返回登陆页面重新登入
     */
    //  void backToLogIn();

    void showErrorTypeToast(String msg);

    void showTipTypeToast(String msg);

    void showToast(String msg);

    void addLifeListener(LifeListener listener);

    @SuppressWarnings("unused")
    void removeLifeListener(LifeListener listener);

    void notifyOnClickBack(ICallBack<ICallBack<Boolean>> cb);

    /**
     * 提供Activity, 不管是Framgent还是Activity, 都能提供
     *
     * @return
     */
    Activity provideActivity();

    /**
     * 第一次可见时 do sth
     *
     * @author Damon
     */
    void doWhenFirstTimeInSight(Runnable runnable);

    void exitApp();

    /**
     * UI层重置
     */
    void reset();

    /**
     * Debug信息提示
     */
    void showDebugContent(String content);
}