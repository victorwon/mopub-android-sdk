package com.mopub.mobileads.test.support;

import com.mopub.mobileads.CustomEventBannerAdapter;
import com.mopub.mobileads.MoPubView;
import com.mopub.mobileads.factories.CustomEventBannerAdapterFactory;

import static org.mockito.Mockito.mock;

public class TestCustomEventBannerAdapterFactory extends CustomEventBannerAdapterFactory {
    private static CustomEventBannerAdapter instance = mock(CustomEventBannerAdapter.class);
    private static MoPubView moPubView;
    private static String className;
    private static String classData;

    public static CustomEventBannerAdapter getSingletonMock() {
        return instance;
    }

    @Override
    protected CustomEventBannerAdapter internalCreate(MoPubView moPubView, String className, String classData) {
        this.moPubView = moPubView;
        this.className = className;
        this.classData = classData;
        return instance;
    }

    public static MoPubView getLatestMoPubView() {
        return moPubView;
    }

    public static String getLatestClassName() {
        return className;
    }

    public static String getLatestClassData() {
        return classData;
    }

    public static void reset() {
        moPubView = null;
        className = null;
        classData = null;
    }
}
