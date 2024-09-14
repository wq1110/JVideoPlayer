package com.jw.media.jvideoplayer.mvx.mvp;

import android.os.Looper;
import android.util.Log;

import com.jw.media.jvideoplayer.lib.callback.IAction;
import com.jw.media.jvideoplayer.lib.callback.ICallBack;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Joyce.wang on 2024/9/13 14:30
 *
 * @Description 支持预加载的 API层
 */
abstract public class DataPreLoadAPIImpl extends BaseAPIImpl {
    public static boolean debug = false;
    private HashMap<String, Request> requests = new HashMap<>();

    public DataPreLoadAPIImpl() {
    }


    abstract protected IAction<Object> fetchDataReal(Object[] args, String tag, boolean isPreload);


    protected void fetchData(Object[] args, String tag, boolean pre, APIDataListener listener) {
        String tagMixArgs = null;
        if (args != null) {
            StringBuilder tagBuilder = new StringBuilder(tag == null ? "" : tag);
            for (Object arg : args) {
                if (arg != null) {
                    if (arg instanceof List) {
                        for (Object obj : (List) arg) {
                            tagBuilder.append("-");
                            tagBuilder.append(obj);
                        }
                    } else {
                        tagBuilder.append("-");
                        tagBuilder.append(arg);
                    }
                }
            }
            tagMixArgs = tagBuilder.toString();
        }

        tagMixArgs = tagMixArgs == null ? tag : tagMixArgs;
        Request request = requests.get(tagMixArgs);
        if (request == null) {
            request = new Request(tag, tagMixArgs);
            requests.put(tagMixArgs, request);
        }

        request.fetch(args, pre, new ICallBack<Object>() {
            @Override
            public void call(Object o) {
                if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                    executeInWorkThread(() -> {
                        try {
                            successOnListener(listener, o);
                        } catch (Exception e) {
                            errorOnListener(listener, e);
                        }
                    });
                } else
                    successOnListener(listener, o);
            }
        }, new ICallBack<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                    executeInWorkThread(() -> errorOnListener(listener, throwable));
                } else
                    errorOnListener(listener, throwable);
            }
        });
    }

    private ICallBack<String> onDataLoadedCb;

    public void setOnDataLoadedCb(ICallBack<String> onDataLoadedCb) {
        this.onDataLoadedCb = onDataLoadedCb;
    }

    protected void onDataLoaded(String tag, Object result) {
        if (onDataLoadedCb != null) onDataLoadedCb.call(tag);
    }

    private class Request {
        private DataLoader dataLoader;
        private String tag;
        private String debugTag;
        private IAction<Object> action;

        public Request(String tag, String debugTag) {
            this.tag = tag;
            this.debugTag = debugTag;
            dataLoader = new DataLoader() {
                @Override
                void onDataLoaded(Object result) {
                    DataPreLoadAPIImpl.this.onDataLoaded(tag, result);
                }

                @Override
                public void proceed(ICallBack<Object> successCb, ICallBack<Throwable> errorCb) {
                    request(new APIRequestTask(Request.this.tag, true, () -> {
                        Object result;
                        if (action != null) {
                            result = action.get();
                        } else
                            result = null;

                        successCb.call(result);
                    }, new APIDataListener() {
                        @Override
                        public void onSuccess(Object result) {
                        }

                        @Override
                        public void onError(Throwable exception) {
                            errorCb.call(exception);
                        }
                    }));
                }
            };
        }

        public void fetch(Object[] args, boolean pre, ICallBack<Object> cb, ICallBack<Throwable> cbError) {
            action = fetchDataReal(args, Request.this.tag, pre);
            assert action != null : "action must be no null";
            if (pre) {
                //预加载, 拉取数据, 但是不获取数据
                dataLoader.get(true, null, null);
                logs("预加载, 拉取数据, 但是不获取数据");
            } else if (!dataLoader.hasWorkedBefore() || dataLoader.fetchResultTimes() >= 1) {
                //从没开始过 或者 预加载过, 获取次数在一次以上, 重新拉取数据
                dataLoader.get(true, cb, cbError);
                if (debug) {
                    if (!dataLoader.hasWorkedBefore()) logs("从没开始过, 重新拉取数据");
                    if (dataLoader.fetchResultTimes() >= 1) logs("预加载过, 获取次数在一次以上, 重新拉取数据");
                }
                logs("从没开始过 或者 预加载过, 获取次数在一次以上, 重新拉取数据");
            } else {
                //预加载过, 获取数据不重新拉取
                dataLoader.get(false, cb, cbError);
                logs("预加载过, 获取数据不重新拉取");
            }
        }

        private void logs(String content) {
            if (debug)
                Log.v(DataPreLoadAPIImpl.class.getSimpleName(), debugTag + ":" + content);
        }
    }
}

