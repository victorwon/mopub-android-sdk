package com.mopub.mobileads.factories;

import android.content.Context;
import com.mopub.mobileads.MraidActivity;
import com.mopub.mobileads.MraidView;
import com.mopub.mobileads.MraidView.NativeCloseButtonStyle;

public class MraidViewFactory {
    protected static MraidViewFactory instance = new MraidViewFactory();

    @Deprecated // for testing
    public static void setInstance(MraidViewFactory factory) {
        instance = factory;
    }

    public static MraidView create(Context context) {
        return instance.internalCreate(context);
    }

    public static MraidView create(
            MraidActivity mraidActivity,
            MraidView.ExpansionStyle expansionStyle,
            NativeCloseButtonStyle buttonStyle,
            MraidView.PlacementType placementType) {
        return instance.internalCreate(mraidActivity, expansionStyle, buttonStyle, placementType);
    }

    protected MraidView internalCreate(Context context) {
        return new MraidView(context);
    }

    protected MraidView internalCreate(MraidActivity mraidActivity, MraidView.ExpansionStyle expansionStyle, NativeCloseButtonStyle buttonStyle, MraidView.PlacementType placementType) {
        return new MraidView(mraidActivity, expansionStyle, buttonStyle, placementType);
    }
}
