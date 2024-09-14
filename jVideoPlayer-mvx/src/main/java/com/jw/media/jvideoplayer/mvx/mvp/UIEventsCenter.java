package com.jw.media.jvideoplayer.mvx.mvp;

import android.util.Log;

import com.jw.media.jvideoplayer.lib.anotation.ViewCallback;
import com.jw.media.jvideoplayer.lib.callback.ICallBack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
/**
 * Created by Joyce.wang on 2024/9/13 14:34
 *
 * @Description TODO
 */
class UIEventsCenter {
    private final ArrayList<Disposable> mSubscriptions = new ArrayList<>();
    private final HashMap<String, EventItem> mEventItems = new HashMap<>();

    void notifyEvent(String action, Object[] args) {
        EventItem eventItem = mEventItems.get(action);
        if (eventItem != null) {
            try {
                Object arg = null;
                if (args != null) {
                    if (args.length == 1) arg = args[0];
                    else if (args.length > 1) arg = args;
                }
                eventItem.onNext.accept(arg);
            } catch (Exception e) {
                Log.w(UIEventsCenter.class.getSimpleName(), "", e);
            }
        }
    }

    void startRunning(BasePresenter presenter) {
        if (mEventItems.isEmpty()) {
            pharseViewCallbackFunc(presenter);
        }

        for (Map.Entry<String, EventItem> entry : mEventItems.entrySet()) {
            buildUIEvent(entry.getValue().onSubscribe, entry.getValue().onNext);
        }
    }

    void stopRunning() {
        for (Disposable subscription : mSubscriptions) {
            subscription.dispose();
        }
        mSubscriptions.clear();
    }

    private void getAllInterfaceMethods(Class<?> clz, List<Method> list) {
        list.addAll(Arrays.asList(clz.getDeclaredMethods()));
        Class<?>[] ifss = clz.getInterfaces();
        for (Class<?> ifs : ifss) {
            getAllInterfaceMethods(ifs, list);
        }
    }

    private void pharseViewCallbackFunc(BasePresenter presenter) {
        ArrayList<Method> methods = new ArrayList<>();
        getAllInterfaceMethods(presenter.getView().getClass(), methods);
        for (Method method : methods) {
            if (method.getAnnotation(ViewCallback.class) != null) {
                ICallBack cb = presenter.mapViewMethod(method.getName());
                if (cb != null) {
                    BaseOnSubcribe onSubscribe = new BaseOnSubcribe<>(method, presenter.getView());
                    Consumer onNext = o -> {
                        try {
                            cb.call(o);
                        } catch (Exception e) {
                            presenter.onUnExceptedError(e);
                        }
                    };
                    EventItem eventItem = new EventItem(onSubscribe, onNext, method.getName());
                    mEventItems.put(eventItem.actionName, eventItem);
                }
            }
        }
    }

    private <T> void  buildUIEvent(ObservableSource<T> onSubscribe, Consumer<T> onNext) {
        Disposable subscription = Observable.unsafeCreate(onSubscribe).subscribe(onNext);
        mSubscriptions.add(subscription);
    }


    static class EventItem {
        String actionName;
        ObservableSource onSubscribe;
        Consumer onNext;

        EventItem(ObservableSource onSubscribe, Consumer onNext, String action) {
            this.onSubscribe = onSubscribe;
            this.onNext = onNext;
            this.actionName = action;
        }
    }

    static class BaseOnSubcribe<T> implements ObservableSource<T> {
        ICallBack<T> mCbWapper;
        Method mCallbackMethod;
        Object mTarget;

        BaseOnSubcribe(Method viewCallBackFun, Object target) {
            if (viewCallBackFun != null) {
                mCallbackMethod = viewCallBackFun;
                mCallbackMethod.setAccessible(true);
                mTarget = target;
            }
        }

        @Override
        public void subscribe(Observer<? super T> observer) {
            if (mTarget == null) return;
            mCbWapper = new ICallBack<T>() {
                @Override
                public void call(T t) {
                    observer.onNext(t);
                }
            };
            try {
                if (mCallbackMethod.getParameterTypes().length == 1 && mCallbackMethod.getParameterTypes()[0] == ICallBack.class) {
                    mCallbackMethod.invoke(mTarget, mCbWapper);
                }
            } catch (Exception e) {
                Log.w(UIEventsCenter.class.getSimpleName(), "", e);
            }
        }
    }
}