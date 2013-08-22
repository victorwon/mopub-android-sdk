package com.mopub.mobileads;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
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

import static com.mopub.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_DISMISS;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_SHOW;
import static com.mopub.mobileads.BaseInterstitialActivity.HTML_INTERSTITIAL_INTENT_FILTER;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class MraidActivityTest extends BaseInterstitialActivityTest {

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
        verify(mraidView).setMraidListener(any(MraidView.MraidListener.class));
        verify(mraidView).setOnCloseButtonStateChange(any(MraidView.OnCloseButtonStateChangeListener.class));

        verify(mraidView).loadHtmlData(EXPECTED_SOURCE);
    }

    @Test
    public void onCreate_shouldBroadcastInterstitialShow() throws Exception {
        Intent expectedIntent = new Intent(ACTION_INTERSTITIAL_SHOW);
        ShadowLocalBroadcastManager.getInstance(subject).registerReceiver(broadcastReceiver, HTML_INTERSTITIAL_INTENT_FILTER);

        subject.onCreate(null);

        verify(broadcastReceiver).onReceive(eq(subject), eq(expectedIntent));
    }

    @Test
    public void onCreate_whenICS_shouldSetHardwareAcceleratedFlag() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", 14);

        subject.onCreate(null);

        boolean hardwareAccelerated = shadowOf(subject.getWindow()).getFlag(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        assertThat(hardwareAccelerated).isTrue();
    }

    @Test
    public void onCreate_whenPreICS_shouldNotSetHardwareAcceleratedFlag() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", 13);

        subject.onCreate(null);

        boolean hardwareAccelerated = shadowOf(subject.getWindow()).getFlag(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        assertThat(hardwareAccelerated).isFalse();
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
        ArgumentCaptor<MraidView.MraidListener> captor = ArgumentCaptor.forClass(MraidView.MraidListener.class);
        View actualAdView = subject.getAdView();

        assertThat(actualAdView).isSameAs(mraidView);
        verify(mraidView).setMraidListener(captor.capture());

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
        ArgumentCaptor<MraidView.MraidListener> captor = ArgumentCaptor.forClass(MraidView.MraidListener.class);
        View actualAdView = subject.getAdView();

        assertThat(actualAdView).isSameAs(mraidView);
        verify(mraidView).setMraidListener(captor.capture());

        captor.getValue().onClose(null, null);

        ANDROID.assertThat(subject).isFinishing();
    }

    @Test
    public void onPause_shouldOnPauseMraidView() throws Exception {
        subject.onCreate(null);
        ((MraidActivity)subject).onPause();

        verify(mraidView).onPause();
    }

    @Test
    public void onResume_shouldResumeMraidView() throws Exception {
        subject.onCreate(null);
        ((MraidActivity)subject).onPause();
        ((MraidActivity)subject).onResume();

        verify(mraidView).onResume();
    }

    private Intent createMraidActivityIntent(String expectedSource) {
        Intent mraidActivityIntent = new Intent();
        mraidActivityIntent.setComponent(new ComponentName("", ""));
        mraidActivityIntent.putExtra(HTML_RESPONSE_BODY_KEY, expectedSource);
        return mraidActivityIntent;
    }
}
