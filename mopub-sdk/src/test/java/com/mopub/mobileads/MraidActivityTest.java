package com.mopub.mobileads;

import android.content.ComponentName;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestMraidViewFactory;
import org.fest.assertions.api.ANDROID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLocalBroadcastManager;

import static com.mopub.mobileads.BaseActivity.ACTION_INTERSTITIAL_DISMISS;
import static com.mopub.mobileads.BaseActivity.HTML_INTERSTITIAL_INTENT_FILTER;
import static com.mopub.mobileads.BaseActivity.SOURCE_KEY;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class MraidActivityTest extends BaseActivityTest {

    private MraidView mraidView;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Intent mraidActivityIntent = createMraidActivityIntent(EXPECTED_SOURCE);
        mraidView = TestMraidViewFactory.getSingletonMock();
        resetMockedView(mraidView);
        subject = Robolectric.buildActivity(MraidActivity.class).withIntent(mraidActivityIntent).create().get();
        resetMockedView(mraidView);
    }

    @Test
    public void onCreate_shouldSetupAnMraidView() throws Exception {
        subject.onCreate(null);

        assertThat(getContentView(subject).getChildAt(0)).isSameAs(mraidView);
        verify(mraidView).setOnReadyListener(any(MraidView.OnReadyListener.class));
        verify(mraidView).setOnCloseButtonStateChange(any(MraidView.OnCloseButtonStateChangeListener.class));
        verify(mraidView).setOnCloseListener(any(MraidView.OnCloseListener.class));

        verify(mraidView).loadHtmlData(EXPECTED_SOURCE);
    }

    @Test
    public void onDestroy_DestroyMraidView() throws Exception {
        Intent expectedIntent = new Intent(ACTION_INTERSTITIAL_DISMISS);
        ShadowLocalBroadcastManager.getInstance(subject).registerReceiver(broadcastReceiver, HTML_INTERSTITIAL_INTENT_FILTER);

        subject.onCreate(null);
        subject.onDestroy();

        verify(broadcastReceiver).onReceive(eq(subject), eq(expectedIntent));
        verify(mraidView).destroy();
        assertThat(getContentView(subject).getChildCount()).isEqualTo(0);
    }

    @Test
    public void getAdView_shouldSetupOnReadyListener() throws Exception {
        subject.onCreate(null);
        resetMockedView(mraidView);
        ArgumentCaptor<MraidView.OnReadyListener> captor = ArgumentCaptor.forClass(MraidView.OnReadyListener.class);
        View actualAdView = subject.getAdView();

        assertThat(actualAdView).isSameAs(mraidView);
        verify(mraidView).setOnReadyListener(captor.capture());

        subject.hideInterstitialCloseButton();
        captor.getValue().onReady(null);
        ImageButton closeButton = (ImageButton) getContentView(subject).getChildAt(1);
        assertThat(closeButton).isNotNull();
    }

    @Test
    public void getAdView_shouldSetupOnCloseButtonStateChangeListener() throws Exception {
        subject.onCreate(null);
        resetMockedView(mraidView);
        ArgumentCaptor<MraidView.OnCloseButtonStateChangeListener> captor = ArgumentCaptor.forClass(MraidView.OnCloseButtonStateChangeListener.class);
        View actualAdView = subject.getAdView();

        assertThat(actualAdView).isSameAs(mraidView);
        verify(mraidView).setOnCloseButtonStateChange(captor.capture());
        MraidView.OnCloseButtonStateChangeListener listener = captor.getValue();

        ANDROID.assertThat(getCloseButton()).isVisible();

        listener.onCloseButtonStateChange(null, false);
        ANDROID.assertThat(getCloseButton()).isNotVisible();

        listener.onCloseButtonStateChange(null, true);
        ANDROID.assertThat(getCloseButton()).isVisible();
    }

    @Test
    public void getAdView_shouldSetupOnCloseListener() throws Exception {
        subject.onCreate(null);
        resetMockedView(mraidView);
        ArgumentCaptor<MraidView.OnCloseListener> captor = ArgumentCaptor.forClass(MraidView.OnCloseListener.class);
        View actualAdView = subject.getAdView();

        assertThat(actualAdView).isSameAs(mraidView);
        verify(mraidView).setOnCloseListener(captor.capture());

        captor.getValue().onClose(null, null);

        ANDROID.assertThat(subject).isFinishing();
    }

    private Intent createMraidActivityIntent(String expectedSource) {
        Intent mraidActivityIntent = new Intent();
        mraidActivityIntent.setComponent(new ComponentName("", ""));
        mraidActivityIntent.putExtra(SOURCE_KEY, expectedSource);
        return mraidActivityIntent;
    }
}
