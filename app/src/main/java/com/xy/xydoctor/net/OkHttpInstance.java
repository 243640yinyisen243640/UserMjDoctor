package com.xy.xydoctor.net;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;


public class OkHttpInstance {

    private static OkHttpClient okHttpClient;


    public static synchronized OkHttpClient createInstance() {
        if (okHttpClient == null) {
            synchronized (OkHttpInstance.class) {
                if (okHttpClient == null) {
                    //HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(new HttpLoggerInterceptor());
                    //logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(60, TimeUnit.SECONDS)      //设置连接超时
                            .readTimeout(60, TimeUnit.SECONDS)         //设置读超时
                            .writeTimeout(60, TimeUnit.SECONDS)        //设置写超时
                            .retryOnConnectionFailure(true)            //是否自动重连
                            //.addNetworkInterceptor(logInterceptor)
                            //.addInterceptor(new HttpHeaderInterceptor())
                            .build();
                }
            }
        }
        return okHttpClient;
    }

    public static synchronized OkHttpClient getInstance() {
        return okHttpClient;
    }
}
