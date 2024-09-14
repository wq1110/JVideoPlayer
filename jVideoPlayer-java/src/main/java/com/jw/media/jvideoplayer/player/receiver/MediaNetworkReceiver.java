package com.jw.media.jvideoplayer.player.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.player.listener.OnConnectionChangeListener;

public class MediaNetworkReceiver extends BroadcastReceiver {
    private static Logger logger = LoggerFactory.getLogger(MediaNetworkReceiver.class.getSimpleName());

    private OnConnectionChangeListener listener;
    private OnConnectionChangeListener.ConnectionType type = OnConnectionChangeListener.ConnectionType.UNKNOWN;

    public MediaNetworkReceiver(OnConnectionChangeListener listener) {
        this.listener = listener;
    }

    public OnConnectionChangeListener.ConnectionType getType() {
        return type;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        logger.i("VodMediaReceiver onReceive....");
        if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
            logger.e("system language change");
            if (listener != null) listener.onLocaleChange();
        } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            OnConnectionChangeListener.ConnectionType newType = OnConnectionChangeListener.ConnectionType.UNKNOWN;
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo == null) {
                /** 没有任何网络 */
                logger.d("no active network");
                newType =  OnConnectionChangeListener.ConnectionType.NONE;
            } else {
                if(!networkInfo.isConnected()) {
                    logger.d("no connected network");
                    newType =  OnConnectionChangeListener.ConnectionType.NONE;
                } else {
                    logger.d("active network is:%d state:%b:%b", networkInfo.getType(), networkInfo.isConnectedOrConnecting(), networkInfo.isConnected());
                    if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                        /** 以太网网络 */
                        newType = OnConnectionChangeListener.ConnectionType.ETHERNET;
                    } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        /** wifi网络，当激活时，默认情况下，所有的数据流量将使用此连接 */
                        newType = OnConnectionChangeListener.ConnectionType.WIFI;
                    } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        /** mobile网络 */
                        newType = OnConnectionChangeListener.ConnectionType.MOBILE;
                    }
                }

            }

            if(null != listener && newType != type) {
                type = newType;
                listener.onConnectionChange(type);
            }
        }
    }
}
