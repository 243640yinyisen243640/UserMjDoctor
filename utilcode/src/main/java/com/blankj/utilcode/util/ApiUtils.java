package com.blankj.utilcode.util;


import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2019/07/09
 *     desc  : utils about api
 * </pre>
 */
public final class ApiUtils {

    private static final String TAG = "ApiUtils";

    private Map<Class, BaseApi> mApiMap = new ConcurrentHashMap<>();
    private Map<Class, Class> mInjectApiImplMap = new HashMap<>();

    private ApiUtils() {
        init();
    }

    /**
     * Get api.
     *
     * @param apiClass The class of api.
     * @param <T>      The type.
     * @return the api
     */
    public static <T extends BaseApi> T getApi(@NonNull final Class<T> apiClass) {
        return getInstance().getApiInner(apiClass);
    }

    public static void register(Class<? extends BaseApi> implClass) {
        getInstance().registerImpl(implClass);
    }

    public static String toString_() {
        return getInstance().toString();
    }

    private static ApiUtils getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * It'll be injected the implClasses who have {@link ApiUtils.Api} annotation
     * by function of {@link ApiUtils#registerImpl} when execute transform task.
     */
    private void init() {/*inject*/}

    private void registerImpl(Class implClass) {
        mInjectApiImplMap.put(implClass.getSuperclass(), implClass);
    }

    @Override
    public String toString() {
        return "ApiUtils: " + mInjectApiImplMap;
    }

    private <Result> Result getApiInner(Class apiClass) {
        BaseApi api = mApiMap.get(apiClass);
        if (api != null) {
            return (Result) api;
        }
        synchronized (apiClass) {
            api = mApiMap.get(apiClass);
            if (api != null) {
                return (Result) api;
            }
            Class implClass = mInjectApiImplMap.get(apiClass);
            if (implClass != null) {
                try {
                    api = (BaseApi) implClass.newInstance();
                    mApiMap.put(apiClass, api);
                    return (Result) api;
                } catch (Exception ignore) {
                    Log.e(TAG, "The <" + implClass + "> has no parameterless constructor.");
                    return null;
                }
            } else {
                Log.e(TAG, "The <" + apiClass + "> doesn't implement.");
                return null;
            }
        }
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.CLASS)
    public @interface Api {
        boolean isMock() default false;
    }

    private static class LazyHolder {
        private static final ApiUtils INSTANCE = new ApiUtils();
    }

    public static class BaseApi {
    }
}