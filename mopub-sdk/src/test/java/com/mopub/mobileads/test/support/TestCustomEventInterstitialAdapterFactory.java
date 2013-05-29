package com.mopub.mobileads.test.support;

import com.mopub.mobileads.CustomEventInterstitialAdapter;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.factories.CustomEventInterstitialAdapterFactory;

import static org.mockito.Mockito.mock;

public class TestCustomEventInterstitialAdapterFactory extends CustomEventInterstitialAdapterFactory{
    private static CustomEventInterstitialAdapter instance = mock(CustomEventInterstitialAdapter.class);
    private static MoPubInterstitial latestMoPubInterstitial;
    private static String latestClassName;
    private static String latestClassData;

    public static CustomEventInterstitialAdapter getSingletonMock() {
        return instance;
    }

    public static MoPubInterstitial getLatestMoPubInterstitial() {
        return latestMoPubInterstitial;
    }

    public static String getLatestClassName() {
        return latestClassName;
    }

    public static String getLatestClassData() {
        return latestClassData;
    }

    @Override
    protected CustomEventInterstitialAdapter internalCreate(MoPubInterstitial moPubInterstitial, String className, String classData) {
        latestMoPubInterstitial = moPubInterstitial;
        latestClassName = className;
        latestClassData = classData;
        return instance;
    }
}
