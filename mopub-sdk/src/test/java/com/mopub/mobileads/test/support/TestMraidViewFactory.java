package com.mopub.mobileads.test.support;

import android.content.Context;
import com.mopub.mobileads.MraidActivity;
import com.mopub.mobileads.MraidView;
import com.mopub.mobileads.factories.MraidViewFactory;

import static org.mockito.Mockito.mock;

public class TestMraidViewFactory extends MraidViewFactory {
    private final MraidView mockMraidView = mock(MraidView.class);

    public static MraidView getSingletonMock() {
        return getTestFactory().mockMraidView;
    }

    private static TestMraidViewFactory getTestFactory() {
        return (TestMraidViewFactory) instance;
    }

    @Override
    protected MraidView internalCreate(Context context) {
        return mockMraidView;
    }

    @Override
    protected MraidView internalCreate(MraidActivity mraidActivity, MraidView.ExpansionStyle expansionStyle, MraidView.NativeCloseButtonStyle buttonStyle, MraidView.PlacementType placementType) {
        return mockMraidView;
    }
}
