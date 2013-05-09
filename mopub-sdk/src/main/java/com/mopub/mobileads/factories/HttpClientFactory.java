package com.mopub.mobileads.factories;

import org.apache.http.impl.client.DefaultHttpClient;

public class HttpClientFactory {
    private static HttpClientFactory instance = new HttpClientFactory();

    public static void setInstance(HttpClientFactory factory) {
        instance = factory;
    }

    public static DefaultHttpClient create() {
        return instance.internalCreate();
    }

    protected DefaultHttpClient internalCreate() {
        return new DefaultHttpClient();
    }
}
