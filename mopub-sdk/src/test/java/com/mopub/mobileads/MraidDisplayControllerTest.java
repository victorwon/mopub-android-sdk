package com.mopub.mobileads;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestMraidViewFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SdkTestRunner.class)
public class MraidDisplayControllerTest {

    private MraidView mraidView;
    private MraidDisplayController subject;
    private View rootView;
    private FrameLayout contentView;
    private MoPubView moPubView;
    private FrameLayout adContainerLayout;
    private RelativeLayout expansionLayout;
    private FrameLayout placeholderView;

    @Before
    public void setup() {
        mraidView = TestMraidViewFactory.getSingletonMock();
        moPubView = mock(MoPubView.class);
        rootView = mock(View.class);
        contentView = mock(FrameLayout.class);
        adContainerLayout = mock(FrameLayout.class);
        expansionLayout = mock(RelativeLayout.class);
        placeholderView = mock(FrameLayout.class);

        stub(mraidView.getContext()).toReturn(new Activity());
        when(mraidView.getParent()).thenReturn(moPubView).thenReturn(null);
        stub(mraidView.getRootView()).toReturn(rootView);
        stub(rootView.findViewById(eq(android.R.id.content))).toReturn(contentView);
        stub(contentView.getContext()).toReturn(new Activity());

        subject = new TestMraidDisplayController(mraidView, null, null);
    }

    @Test
    public void initialization_shouldSetupStartingState() throws Exception {
        assertThat(subject.getMraidView()).isSameAs(mraidView);
    }

    @Test
    public void expand_shouldSwapWithPlaceholderView() throws Exception {
        stub(moPubView.getChildAt(eq(0))).toReturn(mraidView);
        subject.expand(null, 320, 50, false, false);

        verify(moPubView).addView(any(FrameLayout.class), eq(0), any(ViewGroup.LayoutParams.class));
        verify(moPubView).removeView(eq(mraidView));
        verify(adContainerLayout, times(2)).addView(any(ImageView.class), any(FrameLayout.LayoutParams.class));
    }

    @Test
    public void close_shouldUnexpandView() throws Exception {
        subject.expand(null, 320, 50, false, false);
        stub(placeholderView.getParent()).toReturn(moPubView);

        subject.close();

        verify(adContainerLayout).removeAllViewsInLayout();
        verify(expansionLayout).removeAllViewsInLayout();
        verify(contentView).removeView(eq(expansionLayout));
        verify(moPubView).addView(eq(mraidView), any(int.class));
        verify(moPubView).removeView(eq(placeholderView));
        verify(moPubView).invalidate();
    }

    private class TestMraidDisplayController extends MraidDisplayController {
        public TestMraidDisplayController(MraidView mraidView, MraidView.ExpansionStyle expStyle,
                                          MraidView.NativeCloseButtonStyle buttonStyle) {
            super(mraidView, expStyle, buttonStyle);
        }

        @Override
        FrameLayout createAdContainerLayout() {
            return adContainerLayout;
        }

        @Override
        RelativeLayout createExpansionLayout() {
            return expansionLayout;
        }

        @Override
        FrameLayout createPlaceholderView() {
            return placeholderView;
        }
    }
}
