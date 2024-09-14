package com.jw.media.jvideoplayer.mvx.mvp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.jw.media.jvideoplayer.lib.callback.ICallBack;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Joyce.wang on 2024/9/13 14:27
 *
 * @Description Presenter基类
 */
public abstract class BasePresenter<T extends BaseViewInterface> implements LifeListener, Handler.Callback {

    T mViewRef;

    private final Handler mHandler = new Handler(this);

    private final ArrayList<APIError> mErrors = new ArrayList<>();

    private boolean isVisible = false;
    //ViewRef Destory会被置空, IO回调时间点可能会在Destory之后
    //用于当ViewRef为空时, 代替ViewRef来接收Method Invoked,防止空指针crash
    private T proxyView;
    //切换主线程
    private T proxyToMainThread;

    private T proxyCheckViewReady;

    private Class mViewInterfaceClz;

    /********************************************MSG***************************************************************/
    protected static final int MSG_RUNNABLE = 0x444;
    protected static final int MSG_GPS_TIMEOUT = 0x443;
    protected static final int MSG_ERROR = 0x442;
    protected static final int MSG_INVAILD = 0x445;

    abstract protected boolean handleMessageSafe(Message msg);

    @Override
    public final boolean handleMessage(Message msg) {
        if (!isAttachedView()) {
            return true;
        }

        try {
            if (handleMessageSafe(msg)) return true;
        } catch (Exception ignore) {
        }


        switch (msg.what) {
            case MSG_RUNNABLE:
                if (msg.obj != null && msg.obj instanceof Runnable) {
                    try {
                        ((Runnable) msg.obj).run();
                    } catch (Exception ignore) {
                        onUnExceptedError(ignore);
                    }
                }
                return true;
            case MSG_ERROR:
                if (msg.obj != null) {

                }
                return true;
        }
        return false;
    }

    protected Handler getHandler() {
        return mHandler;
    }


    protected final void runUI(Runnable runnable) {
        getHandler().sendMessage(getHandler().obtainMessage(MSG_RUNNABLE, runnable));
    }

    public BasePresenter(T mViewRef) {
        super();
        attachView(mViewRef);
    }


    private void attachView(T view) {
        mViewRef = view;
        try {
            Class<?>[] interfaces = mViewRef.getClass().getInterfaces();
            if (interfaces.length == 0) {
                throw new Exception(mViewRef.getClass().getSimpleName() + " 没有实现任何View接口");
            }
            mViewInterfaceClz = interfaces[0];
            proxyView = (T) Proxy.newProxyInstance(mViewRef.getClass().getClassLoader(), interfaces, (proxy, method, args) -> {
                Class returnType = method.getReturnType();
                if (returnType == boolean.class) {
                    return false;
                } else if (returnType == int.class) {
                    return -1;
                } else {
                    return null;
                }
            });

            proxyToMainThread = (T) Proxy.newProxyInstance(mViewRef.getClass().getClassLoader(), interfaces, (proxy, method, args) -> {
                Class returnType = method.getReturnType();
                if (returnType == void.class) {
                    BasePresenter.this.runUI(() -> {
                        try {
                            method.invoke(proxyCheckViewReady, args);
                        } catch (Exception e) {
                            onUnExceptedError(e);
                        }
                    });
                    return null;
                }
                return method.invoke(mViewRef, args);
            });

            proxyCheckViewReady = (T) Proxy.newProxyInstance(mViewRef.getClass().getClassLoader(), interfaces, (proxy, method, args) -> {
                Class returnType = method.getReturnType();
                if (returnType == void.class) {
                    if (!mViewRef.viewReady()) {
                        mViewRef.doAfterViewReady(() -> {
                            try {
                                method.invoke(mViewRef, args);
                            } catch (Exception e) {
                                onUnExceptedError(e);
                            }
                        });
                        return null;
                    }
                }
                return method.invoke(mViewRef, args);
            });
        } catch (Exception e) {
            onUnExceptedError(e);
        }

        mViewRef.addLifeListener(this);

        if (isViewPresenter()) {
            PresentersManagerGlobal.getInstance().addPresenter(this);
        }

    }

    protected void onUnExceptedError(Throwable e) {
        Log.w(getClass().getSimpleName(), "", e);
    }

    protected boolean isViewPresenter() {
        return true;
    }

    protected boolean isAttachedView() {
        return mViewRef != null;
    }

    protected boolean isViewVisible() {
        return isVisible;
    }

    protected T getView() {
        if (mViewRef == null && proxyView != null) {
            return proxyView;
        }

        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            return proxyToMainThread;
        }

        return proxyCheckViewReady;
    }


    //解绑View
    private void detachView() {
        if (mViewRef != null) {
            mViewRef.removeLifeListener(this);
            mViewRef = null;
        }
        if (isViewPresenter()) PresentersManagerGlobal.getInstance().removePresenter(this);
    }

    private void executePendingError() {
        for (APIError error : mErrors) {
            getHandler().sendMessage(getHandler().obtainMessage(MSG_ERROR, error));
        }
        mErrors.clear();
    }

    @Override
    public void onCreate(Bundle savedInstanceState, Bundle initData) {
        isVisible = true;
        if (savedInstanceState != null) {
        }
    }

    @Override
    public void onViewCreated() {
    }

    @Override
    public void onStart() {
        isVisible = true;
        getView().doAfterViewReady(() -> {
            getView().notifyOnClickBack(new ICallBack<ICallBack<Boolean>>() {
                @Override
                public void call(ICallBack<Boolean> o) {
                    onClickBack(o);
                }
            });
            if (isViewPresenter()) PresentersManagerGlobal.getInstance().getUIEventsCenter(BasePresenter.this).startRunning(BasePresenter.this);
        });
    }

    @Override
    public void onStop() {
        isVisible = false;
        if (isViewPresenter()) PresentersManagerGlobal.getInstance().getUIEventsCenter(BasePresenter.this).stopRunning();
    }

    @Override
    public void onDestroy() {
        detachView();
    }

    @Override
    public void onResume() {
        executePendingError();
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onRestore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
        }
    }

    @Override
    public void onSave(Bundle savedInstanceState) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    protected void onClickBack(ICallBack<Boolean> o) {
        o.call(false);
        getView().back();
    }

    abstract protected ICallBack mapViewMethod(String methodName);

    protected final String getString(int resId) {
        if (getView().provideActivity() == null) return "";
        return getView().provideActivity().getString(resId);
    }

    protected final String getString(int resId, Object... formatArgs) {
        if (getView().provideActivity() == null) return "";
        return getView().provideActivity().getString(resId, formatArgs);
    }

    protected final <T extends BasePresenter> ArrayList<T> getPresenter(Class<T> searchClz) {
        return PresentersManagerGlobal.getInstance().getPresenter(searchClz);
    }

    /**
     * 页面之间 传参
     * 参数传入 入口
     * Really Careful: invoke time is before child 's <init>
     *
     * @param isInit 是否为初始化参数
     * @param args   参数
     */
    abstract protected void onArgs(boolean isInit, ArgumentsMap args);

    /**
     * 针对页面进行传参
     */
    protected final void postArg(Class<? extends BaseViewInterface> targetViewClz, ArgumentsMap args) {
        PresentersManagerGlobal.getInstance().postArguments(targetViewClz, args);
    }

    /**
     * 针对页面进行传递 初始化参数 一次性
     */
    protected final void postInitArg(Class<? extends BaseViewInterface> targetViewClz, ArgumentsMap args) {
        PresentersManagerGlobal.getInstance().postInitArguments(targetViewClz, args);
    }

    protected final void clearInitArg(Class<? extends BaseViewInterface> targetViewClz) {
        PresentersManagerGlobal.getInstance().clearInitArguments(targetViewClz);
    }

    /**
     * 获取一个用来传递参数的Map
     *
     * @return
     */
    protected final ArgumentsMap obtainArgsMap() {
        ArgumentsMap map = new ArgumentsMap();
        try {
            map.put(ArgumentsMap.KEY_CALLER_VIEW_CLASS, mViewInterfaceClz);
        } catch (Exception e) {
        }
        return map;
    }


    static final class PresentersManagerGlobal {
        private final HashMap<BasePresenter, UIEventsCenter> mPresenter_UIEventCenters = new HashMap<>();
        private final HashMap<Class<BasePresenter>, List<BasePresenter>> mPClz_Presenters = new HashMap<>();
        private final HashMap<Class<BaseViewInterface>, Queue<ArgumentsMap>> mInitArgs = new HashMap<>();
        private final HashMap<Class<BaseViewInterface>, Queue<BasePresenter>> mVClz_Presenters = new HashMap<>();

        public static PresentersManagerGlobal getInstance() {
            return SingleTonInnerClass.INSTANCE;
        }

        static class SingleTonInnerClass {
            final static PresentersManagerGlobal INSTANCE = new PresentersManagerGlobal();
        }

        void postInitArguments(Class<? extends BaseViewInterface> viewClz, ArgumentsMap args) {
            if (args == null || viewClz == null) return;
            getQueueOfPresenterInitArgsByView(viewClz).offer(args);
        }

        void clearInitArguments(Class<? extends BaseViewInterface> viewClz) {
            if (viewClz == null) return;
            getQueueOfPresenterInitArgsByView(viewClz).clear();
        }

        void postArguments(Class<? extends BaseViewInterface> viewClz, ArgumentsMap args) {
            if (args == null || viewClz == null) return;
            Iterator<BasePresenter> iterator = getPresentersByView(viewClz).iterator();

            while (iterator.hasNext()) {
                BasePresenter presenter = iterator.next();
                presenter.onArgs(false, args);
            }

        }

        <T extends BasePresenter> ArrayList<T> getPresenter(Class<T> searchClz) {
            List<BasePresenter> tmp = getCollectionOfPresenter(searchClz);
            ArrayList<BasePresenter> presenters = new ArrayList<>(tmp);
            ArrayList<Class> clzs = new ArrayList<>(mPClz_Presenters.keySet());
            clzs.remove(searchClz);

            for (Class clz : clzs) {
                Class parentClz = clz.getSuperclass();
                do {
                    if (parentClz.equals(searchClz)) {
                        presenters.addAll(mPClz_Presenters.get(clz));
                    }
                    parentClz = parentClz.getSuperclass();
                } while (parentClz != Object.class);
            }

            return (ArrayList<T>) presenters;
        }

        private void removePresenter(final BasePresenter presenter) {
            getCollectionOfPresenter(presenter.getClass()).remove(presenter);
            getQueueOfPresenterByView(presenter.mViewInterfaceClz).remove(presenter);
            UIEventsCenter uiEventsCenter = mPresenter_UIEventCenters.remove(presenter);

        }

        private void addPresenter(final BasePresenter presenter) {
            Class viewClz = presenter.mViewInterfaceClz;
            try {
                Queue<ArgumentsMap> argsQueue = mInitArgs.get(viewClz);
                if (argsQueue == null || argsQueue.isEmpty()) {
                    for (Class<BaseViewInterface> vClass : mInitArgs.keySet()) {
                        if (vClass.isAssignableFrom(viewClz)) {
                            argsQueue = mInitArgs.get(vClass);
                            break;
                        }
                    }
                }

                if (argsQueue != null && argsQueue.size() > 0) {
                    presenter.onArgs(true, argsQueue.poll());
                }


            } catch (Exception e) {
                Log.w(getClass().getSimpleName(), "", e);
            }

            try {
                getQueueOfPresenterByView(viewClz).offer(presenter);
                getCollectionOfPresenter(presenter.getClass()).add(presenter);
            } catch (Exception e) {
                Log.w(getClass().getSimpleName(), "", e);
            }

            mPresenter_UIEventCenters.put(presenter, new UIEventsCenter());
        }

        public UIEventsCenter getUIEventsCenter(BasePresenter presenter) {
            if (presenter == null) return null;
            return mPresenter_UIEventCenters.get(presenter);
        }


        private static final List<Object> nulls = Arrays.asList(null, null, null, null, null);

        private Queue<ArgumentsMap> getQueueOfPresenterInitArgsByView(Class viewClz) {
            Queue<ArgumentsMap> args = mInitArgs.get(viewClz);
            if (args == null) {
                args = new LinkedList<>();
                mInitArgs.put(viewClz, args);
            }
            return args;
        }

        private Queue<BasePresenter> getQueueOfPresenterByView(Class viewClz) {
            Queue<BasePresenter> presenters = mVClz_Presenters.get(viewClz);
            if (presenters == null) {
                presenters = new LinkedList<>();
                mVClz_Presenters.put(viewClz, presenters);
            }
            return presenters;
        }

        private Queue<BasePresenter> getPresentersByView(Class viewClz) {
            Queue<BasePresenter> presenters = new LinkedList<>();
            presenters.addAll(getQueueOfPresenterByView(viewClz));
            for (Class clz : mVClz_Presenters.keySet()) {
                if (clz != viewClz && viewClz.isAssignableFrom(clz)) {
                    presenters.addAll(getQueueOfPresenterByView(clz));
                }
            }
            return presenters;
        }

        private List<BasePresenter> getCollectionOfPresenter(Class presenterClz) {
            List<BasePresenter> presentersForClz = mPClz_Presenters.get(presenterClz);
            if (presentersForClz == null) {
                presentersForClz = new ArrayList<>();
                mPClz_Presenters.put(presenterClz, presentersForClz);
            }

            presentersForClz.removeAll(nulls);
            if (presentersForClz.size() == 0) {
                presentersForClz.add(null);
            }
            return presentersForClz;
        }
    }


    public static class ArgumentsMap {
        private final HashMap<String, Object> mMap = new HashMap<>();
        public static final String KEY_CALLER_VIEW_CLASS = "KEY_CALLER_VIEW_CLASS";

        public Class<? extends BaseViewInterface> postFrom() {
            return (Class<? extends BaseViewInterface>) get(KEY_CALLER_VIEW_CLASS);
        }

        public Object get(String key) {
            return mMap.get(key);
        }

        public ArgumentsMap put(String key, Object obj) {
            mMap.put(key, obj);
            return this;
        }

        public ArgumentsMap putAll(Map<String, Object> map) {
            mMap.putAll(map);
            return this;
        }

        public boolean containsKey(String key) {
            return mMap.containsKey(key);
        }
    }
}
