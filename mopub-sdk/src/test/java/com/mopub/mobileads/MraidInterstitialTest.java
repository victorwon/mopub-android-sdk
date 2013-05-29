package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;

import java.util.HashMap;
import java.util.Map;

import static com.mopub.mobileads.AdFetcher.MRAID_HTML_DATA;
import static com.mopub.mobileads.BaseActivity.SOURCE_KEY;
import static com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import static com.mopub.mobileads.MoPubErrorCode.MRAID_LOAD_ERROR;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf_;

@RunWith(SdkTestRunner.class)
public class MraidInterstitialTest {
    private MraidInterstitial subject;
    private CustomEventInterstitialListener interstitialListener;
    private Map<String,Object> localExtras;
    private Map<String,String> serverExtras;
    private Context context;
    private static final String INPUT_HTML_DATA = "%3Chtml%3E%3C%2Fhtml%3E";
    private static final String EXPECTED_HTML_DATA = "<html></html>";

    @Before
    public void setUp() throws Exception {
        subject = new MraidInterstitial();
        context = new Activity();
        interstitialListener = mock(CustomEventInterstitialListener.class);
        localExtras = new HashMap<String, Object>();
        serverExtras = new HashMap<String, String>();
        serverExtras.put(MRAID_HTML_DATA, INPUT_HTML_DATA);
    }

    @Test
    public void loadBanner_withNonActivityContext_shouldNotifyInterstialFailed() throws Exception {
        subject.loadInterstitial(Robolectric.application, interstitialListener, localExtras, serverExtras);

        verify(interstitialListener).onInterstitialFailed(MRAID_LOAD_ERROR);
        verify(interstitialListener, never()).onInterstitialLoaded();
    }

    @Test
    public void loadBanner_withMalformedServerExtras_shouldNotifyInterstitialFailed() throws Exception {
        serverExtras.remove(MRAID_HTML_DATA);
        subject.loadInterstitial(context, interstitialListener, localExtras, serverExtras);

        verify(interstitialListener).onInterstitialFailed(MRAID_LOAD_ERROR);
        verify(interstitialListener, never()).onInterstitialLoaded();
    }

    @Test
    public void loadInterstitial_shouldNotifyInterstitialLoaded() throws Exception {
        subject.loadInterstitial(context, interstitialListener, localExtras, serverExtras);

        verify(interstitialListener).onInterstitialLoaded();
    }

    @Test
    public void showInterstitial_shouldStartActivityWithIntent() throws Exception {
        subject.loadInterstitial(context, interstitialListener, localExtras, serverExtras);
        subject.showInterstitial();

        ShadowActivity shadowActivity = shadowOf_(context);
        Intent intent = shadowActivity.getNextStartedActivityForResult().intent;

        assertThat(intent.getComponent().getPackageName()).isEqualTo("com.mopub.mobileads");
        assertThat(intent.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidActivity");
        assertThat(intent.getExtras().get(SOURCE_KEY)).isEqualTo(EXPECTED_HTML_DATA);
    }
}
