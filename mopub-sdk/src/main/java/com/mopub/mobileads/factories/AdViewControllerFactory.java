package com.mopub.mobileads.factories;

import android.content.Context;
import android.util.Log;
import com.mopub.mobileads.AdViewController;
import com.mopub.mobileads.MoPubView;

import java.lang.reflect.Constructor;

import static com.mopub.mobileads.util.VersionCode.ECLAIR_MR1;
import static com.mopub.mobileads.util.VersionCode.currentApiLevel;

public class AdViewControllerFactory {
    protected static AdViewControllerFactory instance = new AdViewControllerFactory();

    @Deprecated // for testing
    public static void setInstance(AdViewControllerFactory factory) {
        instance = factory;
    }

    public static AdViewController create(Context context, MoPubView moPubView) {
        return instance.internalCreate(context, moPubView);
    }

    protected AdViewController internalCreate(Context context, MoPubView moPubView) {
        if (currentApiLevel().isBelow(ECLAIR_MR1)) {
            return new AdViewController(context, moPubView);
        }

        Class<?> HTML5AdViewClass = null;
        try {
            HTML5AdViewClass = Class.forName("com.mopub.mobileads.HTML5AdView");
        } catch (ClassNotFoundException e) {
            return new AdViewController(context, moPubView);
        }

        try {
            Constructor<?> constructor = HTML5AdViewClass.getConstructor(Context.class, MoPubView.class);
            return (AdViewController) constructor.newInstance(context, moPubView);
        } catch (Exception e) {
            Log.e("MoPub", "Could not load HTML5AdView.");
        }

        return new AdViewController(context, moPubView);
    }
}
