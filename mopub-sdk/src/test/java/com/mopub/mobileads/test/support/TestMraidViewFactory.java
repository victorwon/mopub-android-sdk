package com.mopub.mobileads.test.support;

import android.content.Context;
import com.mopub.mobileads.MraidActivity;
import com.mopub.mobileads.MraidView;
import com.mopub.mobileads.factories.MraidViewFactory;

import static org.mockito.Mockito.mock;

public class TestMraidViewFactory extends MraidViewFactory{

    public static final MraidView instance = mock(MraidView.class);

    public static MraidView getSingletonMock() {
        return instance;
    }

    @Override
    protected MraidView internalCreate(Context context) {
        return instance;
    }

    @Override
    protected MraidView internalCreate(MraidActivity mraidActivity, MraidView.ExpansionStyle expansionStyle, MraidView.NativeCloseButtonStyle buttonStyle, MraidView.PlacementType placementType) {
        return instance;
    }
}
