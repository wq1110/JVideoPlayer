package com.jw.media.jvideoplayer.mvx.mvp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jw.media.jvideoplayer.lib.callback.ICallBack;
import com.jw.media.jvideoplayer.mvx.R;
import com.jw.media.jvideoplayer.mvx.base.BaseActivity;
import com.jw.media.jvideoplayer.mvx.base.BaseFragment;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Joyce.wang on 2024/9/13 14:33
 *
 * @Description TODO
 */
public abstract class MVPBaseFragment extends BaseFragment implements BaseViewInterface {
    private final ArrayList<LifeListener> lifeListeners = new ArrayList<>();


    //创建Present
    private void createPresenter() {
        PresenterCenter.createPresenter(this);
    }

    public void back() {
        dismiss();
    }

    @Override
    public void dismiss() {

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        if (!isAdded()) return;

        if (hidden) {
            onStop();
        } else {
            onStart();
        }

        for (Fragment fg : getChildFragmentManager().getFragments()) {
            fg.onHiddenChanged(hidden);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isAdded()) return;

        dispatchUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            onStart();
        } else {
            onStop();
        }
    }
    protected void dispatchUserVisibleHint(boolean isVisibleToUser) {
        for (Fragment fg : getChildFragmentManager().getFragments()) {
            fg.setUserVisibleHint(isVisibleToUser);
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
    public void showErrorTypeToast(String msg) {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showErrorTypeToast(msg);
        }
    }

    @Override
    public void showTipTypeToast(String msg) {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showTipTypeToast(msg);
        }
    }

    @Override
    public void showToast(String msg) {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showToast(msg);
        }
    }


    @Override
    public void exitApp() {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).exitApp();
        }
    }

    @Override
    public void showLoading() {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showLoading();
        }
    }

    @Override
    public void hideLoading() {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).hideLoading();
        }
    }

    @Override
    public void reset() {
        try {
            FragmentManager fm = getParentFragmentManager();
            int ContainerId = ((View) getView().getParent()).getId();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(ContainerId, this.getClass().newInstance());
            ft.commitNowAllowingStateLoss();
        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), "", e);
        }
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
                    UIEventsCenter uiEventsCenter = BasePresenter.PresentersManagerGlobal.getInstance().getUIEventsCenter(presenter);
                    if (uiEventsCenter != null) uiEventsCenter.notifyEvent(action, uiArgs);
                }
            });
        }
    }

    boolean backHandle;
    private ICallBack<ICallBack<Boolean>> mBackCb;
    ICallBack<Boolean> cb = new ICallBack<Boolean>() {
        @Override
        public void call(Boolean aBoolean) {
            backHandle = aBoolean;
        }
    };

    @Override
    public void notifyOnClickBack(ICallBack<ICallBack<Boolean>> cb) {
        mBackCb = cb;
    }

    @Override
    protected boolean handleBackPress() {
        mBackCb.call(cb);
        return backHandle;
    }
}