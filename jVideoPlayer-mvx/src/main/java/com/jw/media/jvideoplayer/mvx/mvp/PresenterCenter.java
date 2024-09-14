package com.jw.media.jvideoplayer.mvx.mvp;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Joyce.wang on 2024/9/13 14:34
 *
 * @Description TODO
 */
public class PresenterCenter {
    private static HashMap<Class<? extends BaseViewInterface>, Class<? extends BasePresenter>> mClzV_ClzP = new HashMap<>();

    public static void register(MVPInfoFiller filler) {
        filler.fill(mClzV_ClzP);
    }

    static BasePresenter createPresenter(BaseViewInterface viewImpl) {
        Pair<Class<? extends BaseViewInterface>, Class<? extends BasePresenter>> relation = getRelationFromViewImpl(viewImpl);
        if (relation != null) {
            try {
                return relation.second.getDeclaredConstructor(relation.first).newInstance(viewImpl);
            } catch (Exception e) {
                Log.w(PresenterCenter.class.getSimpleName(), viewImpl != null ? viewImpl.getClass().getSimpleName(): "" + " ", e);
            }
        }
        return null;
    }

    private static Pair<Class<? extends BaseViewInterface>, Class<? extends BasePresenter>> getRelationFromViewImpl(BaseViewInterface viewImpl) {
        try {
            Class tmp = viewImpl.getClass();
            ArrayList<Class> interfacesClz = new ArrayList<>();
            while (tmp != null) {
                Class[] interfaces = tmp.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    interfacesClz.add(interfaces[i]);
                }
                tmp = tmp.getSuperclass();
            }

            Class presenterClz = null;
            Class viewIFClz = null;
            for (Class interfaceClz : interfacesClz) {
                presenterClz = mClzV_ClzP.get(interfaceClz);
                if (presenterClz != null) {
                    viewIFClz = interfaceClz;
                    break;
                }
            }
            return new Pair<Class<? extends BaseViewInterface>, Class<? extends BasePresenter>>(viewIFClz, presenterClz);
        } catch (Exception e) {
            Log.w(PresenterCenter.class.getSimpleName(), "", e);
        }
        return null;
    }

    public static BasePresenter fetchBindPresenter(BaseViewInterface viewImpl) {
        Pair<Class<? extends BaseViewInterface>, Class<? extends BasePresenter>> relation = getRelationFromViewImpl(viewImpl);
        if (relation != null) {
            List<? extends BasePresenter> presenters = BasePresenter.PresentersManagerGlobal.getInstance().getPresenter(relation.second);
            for (BasePresenter presenter : presenters) {
                if (presenter != null && presenter.mViewRef == viewImpl) {
                    return presenter;
                }
            }
        }
        return null;
    }


    public static <T extends BaseViewInterface> void postArg(Class<T> viewInterface, Map<String, Object> args) {
        if (args == null || viewInterface == null) return;
        BasePresenter.ArgumentsMap argumentsMap = new BasePresenter.ArgumentsMap();
        argumentsMap.putAll(args);
        BasePresenter.PresentersManagerGlobal.getInstance().postArguments(viewInterface, argumentsMap);
    }
}