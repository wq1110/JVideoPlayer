package com.jw.media.jvideoplayer.mvx.mvp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.jw.media.jvideoplayer.lib.callback.ICallBack;
import com.jw.media.jvideoplayer.mvx.R;
import com.jw.media.jvideoplayer.mvx.base.BaseDialogFragment;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Joyce.wang on 2024/9/13 14:33
 *
 * @Description TODO
 */
public abstract class MVPBaseDialog extends BaseDialogFragment implements BaseViewInterface {
    private final ArrayList<LifeListener> lifeListeners = new ArrayList<>();

    //创建Present
    private void createPresenter() {
        PresenterCenter.createPresenter(this);
    }

    public void back() {
        dismiss();
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

    public void onViewCreated() {
        super.onViewCreated();
        for (LifeListener listener : lifeListeners) {
            listener.onViewCreated();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createPresenter();
        for (LifeListener listener : lifeListeners) {
            listener.onCreate(savedInstanceState, getArguments() != null ? getArguments() : new Bundle());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        for (LifeListener listener : lifeListeners) {
            listener.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        for (LifeListener listener : lifeListeners) {
            listener.onPause();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        for (LifeListener listener : lifeListeners) {
            listener.onStart();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        for (LifeListener listener : lifeListeners) {
            listener.onStop();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        for (LifeListener listener : lifeListeners) {
            listener.onSave(outState);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        for (LifeListener listener : lifeListeners) {
            listener.onRestore(savedInstanceState);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < lifeListeners.size(); i++) {
            LifeListener listener = lifeListeners.get(i);
            listener.onDestroy();
            lifeListeners.remove(listener);
            i--;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            for (LifeListener listener : lifeListeners) {
                listener.onStop();
            }
        } else {
            for (LifeListener listener : lifeListeners) {
                listener.onStart();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (LifeListener listener : lifeListeners) {
            listener.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public Activity provideActivity() {
        return getActivity();
    }

    @Override
    public void notifyOnClickBack(ICallBack<ICallBack<Boolean>> cb) {

    }

    @Override
    public void reset() {

    }

    @Override
    public void doWhenFirstTimeInSight(Runnable runnable) {
        doAfterViewReady(new Runnable() {
            @Override
            public void run() {
                getView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        getView().getViewTreeObserver().removeOnPreDrawListener(this);
                        runnable.run();
                        return false;
                    }
                });
            }
        });
    }

    protected void notifyUIAction(String action, Object... uiArgs) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                BasePresenter presenter = PresenterCenter.fetchBindPresenter(this);
                if (presenter != null) {
                    UIEventsCenter uiEventsCenter =  BasePresenter.PresentersManagerGlobal.getInstance().getUIEventsCenter(presenter);
                    if (uiEventsCenter != null)  uiEventsCenter.notifyEvent(action, uiArgs);
                }
            });
        }
    }
}

