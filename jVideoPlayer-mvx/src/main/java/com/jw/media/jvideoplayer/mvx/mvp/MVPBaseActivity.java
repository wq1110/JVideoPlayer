package com.jw.media.jvideoplayer.mvx.mvp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.jw.media.jvideoplayer.lib.callback.ICallBack;
import com.jw.media.jvideoplayer.mvx.R;
import com.jw.media.jvideoplayer.mvx.base.BaseActivity;

import java.util.ArrayList;

/**
 * Created by Joyce.wang on 2024/9/13 14:33
 *
 * @Description TODO
 */
public abstract class MVPBaseActivity extends BaseActivity implements BaseViewInterface {
    private final ArrayList<LifeListener> lifeListeners = new ArrayList<>();

    private ICallBack<ICallBack<Boolean>> mBackCb;
    boolean backConsumed = false;

    @Override
    public void notifyOnClickBack(ICallBack<ICallBack<Boolean>> cb) {
        mBackCb = cb;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mBackCb != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                backConsumed = false;
                ICallBack<Boolean> cb = new ICallBack<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        backConsumed = aBoolean;
                    }
                };
                handleBackPress(cb);
            }
            return backConsumed || super.dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    protected void handleBackPress(ICallBack<Boolean> cb) {
        mBackCb.call(cb);
    }

    @Override
    public void back() {
        dismiss();
    }

    @Override
    public void dismiss() {
        finish();
    }

    //创建Present
    protected void createPresenter() {
        PresenterCenter.createPresenter(this);
    }

    @Override
    public void addLifeListener(LifeListener listener) {
        if (listener != null) {
            lifeListeners.add(listener);
        }
    }

    @Override
    public void removeLifeListener(LifeListener listener) {
        if (listener != null) {
            lifeListeners.remove(listener);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createPresenter();
        for (LifeListener listener : lifeListeners) {
            try {
                listener.onCreate(savedInstanceState, getIntent().getExtras() == null ? new Bundle() : getIntent().getExtras());
            } catch (Exception e) {
                Log.w(getClass().getSimpleName(), "", e);
            }
        }
    }

    protected void onViewCreated(View view) {
        super.onViewCreated(view);
        for (LifeListener listener : lifeListeners) {
            listener.onViewCreated();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (LifeListener listener : lifeListeners) {
            listener.onResume();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        for (LifeListener listener : lifeListeners) {
            listener.onPause();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        for (LifeListener listener : lifeListeners) {
            listener.onStart();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        for (LifeListener listener : lifeListeners) {
            listener.onStop();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (LifeListener listener : lifeListeners) {
            listener.onDestroy();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (LifeListener listener : lifeListeners) {
            listener.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        for (LifeListener listener : lifeListeners) {
            listener.onSave(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        for (LifeListener listener : lifeListeners) {
            listener.onRestore(savedInstanceState);
        }
    }

    @Override
    public Activity provideActivity() {
        return this;
    }

    @Override
    public void doWhenFirstTimeInSight(Runnable runnable) {
        doAfterViewReady(new Runnable() {
            @Override
            public void run() {
                getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        getWindow().getDecorView().getViewTreeObserver().removeOnPreDrawListener(this);
                        runnable.run();
                        return false;
                    }
                });
            }
        });
    }

    @Override
    public void reset() {
        recreate();
    }

    protected void notifyUIAction(String action, Object... uiArgs) {
        runOnUiThread(() -> {
            BasePresenter presenter = PresenterCenter.fetchBindPresenter(this);
            if (presenter != null) {
                UIEventsCenter uiEventsCenter = BasePresenter.PresentersManagerGlobal.getInstance().getUIEventsCenter(presenter);
                if (uiEventsCenter != null) uiEventsCenter.notifyEvent(action, uiArgs);
            }
        });
    }
}
