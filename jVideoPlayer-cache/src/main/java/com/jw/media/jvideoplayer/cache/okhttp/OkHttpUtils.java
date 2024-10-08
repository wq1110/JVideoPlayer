package com.jw.media.jvideoplayer.cache.okhttp;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Joyce.wang on 2024/9/11 9:01
 *
 * @Description okhttp全局管理类（全局唯一的实例）
 */
public class OkHttpUtils {
    private static Logger logger = LoggerFactory.getLogger(OkHttpUtils.class.getName());

    private OkHttpUtils() {}

    public static OkHttpClient getInstance() {
        return Singleton.INSTANCE.getInstance();
    }

    private enum Singleton {
        INSTANCE;

        private OkHttpClient mClient;

        Singleton() {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(60 * 1000L, TimeUnit.MILLISECONDS);
            builder.readTimeout(60 * 1000L, TimeUnit.MILLISECONDS);
            builder.followRedirects(false);
            builder.followSslRedirects(false);
            ConnectionPool connectionPool = new ConnectionPool(50, 5 * 60, TimeUnit.SECONDS);
            builder.connectionPool(connectionPool);
            mClient = builder.build();
        }

        public OkHttpClient getInstance() { return mClient; }
    }

    public static OkHttpClient createOkHttpClient(String url, long readTimeout, long connTimeout, boolean ignoreCert, IHttpPipelineListener listener) {
        OkHttpClient.Builder builder = OkHttpUtils.getInstance().newBuilder();
        builder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
        builder.connectTimeout(connTimeout, TimeUnit.MILLISECONDS);
        builder.eventListener(new OkHttpEventListener(url, listener));
        if (HttpUrl.parse(url).isHttps() && ignoreCert) {
            trustCert(builder);
        }
        return builder.build();
    }

    /**
     * okhttp 信任证书
     * @param builder
     */
    private static void trustCert(OkHttpClient.Builder builder) {
        X509TrustManager trustManager = new CustomTrustManager();
        SSLSocketFactory sslSocketFactory = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            logger.w("Create SSLSocketFactory failed");
        }
        if (trustManager != null && sslSocketFactory != null) {
            builder.sslSocketFactory(sslSocketFactory, trustManager);
        }
        HostnameVerifier hostnameVerifier = (hostname, session) -> true;
        builder.hostnameVerifier(hostnameVerifier);
    }

    public static Request.Builder createRequestBuilder(String url, Map<String, String> headers, boolean isHeadRequest) {
        Request.Builder requestBuilder;
        if (isHeadRequest) {
            requestBuilder = new Request.Builder().url(url).head();
        } else {
            requestBuilder = new Request.Builder().url(url);
        }
        if (headers != null) {
            Iterator iterator = headers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return requestBuilder;
    }
}
