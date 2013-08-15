package com.mopub.mobileads;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowLocalBroadcastManager;

import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_CLICK;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_DISMISS;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_FAIL;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_SHOW;
import static com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_INVALID_STATE;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class EventForwardingBroadcastReceiverTest {

    private CustomEventInterstitialListener customEventInterstitialListener;
    private EventForwardingBroadcastReceiver subject;
    private Activity context;

    @Before
    public void setUp() throws Exception {
        customEventInterstitialListener = mock(CustomEventInterstitialListener.class);
        subject = new EventForwardingBroadcastReceiver(customEventInterstitialListener);
        context = new Activity();
    }

    @Test
    public void onReceive_whenActionInterstitialFail_shouldNotifyListener() throws Exception {
        Intent intent = new Intent(ACTION_INTERSTITIAL_CLICK);

        subject.onReceive(context, intent);

        verify(customEventInterstitialListener).onInterstitialClicked();
    }

    @Test
    public void onReceive_whenActionInterstitialShow_shouldNotifyListener() throws Exception {
        Intent intent = new Intent(ACTION_INTERSTITIAL_FAIL);

        subject.onReceive(context, intent);

        verify(customEventInterstitialListener).onInterstitialFailed(eq(NETWORK_INVALID_STATE));
    }


    @Test
    public void onReceive_whenActionInterstitialDismiss_shouldNotifyListener() throws Exception {
        Intent intent = new Intent(ACTION_INTERSTITIAL_DISMISS);

        subject.onReceive(context, intent);

        verify(customEventInterstitialListener).onInterstitialDismissed();
    }

    @Test
    public void onReceive_whenActionInterstitialClick_shouldNotifyListener() throws Exception {
        Intent intent = new Intent(ACTION_INTERSTITIAL_CLICK);

        subject.onReceive(context, intent);

        verify(customEventInterstitialListener).onInterstitialClicked();
    }

    @Test
    public void onReceiver_whenCustomEventInterstitialListenerIsNull_shouldNotBlowUp() throws Exception {
        Intent intent = new Intent(ACTION_INTERSTITIAL_SHOW);

        subject = new EventForwardingBroadcastReceiver(null);
        subject.onReceive(context, intent);

        // pass
    }

    @Test
    public void register_shouldEnableReceivingBroadcasts() throws Exception {
        subject.register(context);
        Intent intent = new Intent(ACTION_INTERSTITIAL_SHOW);
        ShadowLocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        verify(customEventInterstitialListener).onInterstitialShown();
    }

    @Test
    public void unregister_shouldDisableReceivingBroadcasts() throws Exception {
        subject.register(context);

        subject.unregister();
        Intent intent = new Intent(ACTION_INTERSTITIAL_SHOW);
        ShadowLocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        verify(customEventInterstitialListener, never()).onInterstitialShown();
    }

    @Test
    public void unregister_whenNotRegistered_shouldNotBlowUp() throws Exception {
        subject.unregister();

        // pass
    }

    @Test
    public void unregister_shouldNotLeakTheContext() throws Exception {
        subject.register(context);
        subject.unregister();

        LocalBroadcastManager.getInstance(context).registerReceiver(subject, BaseInterstitialActivity.HTML_INTERSTITIAL_INTENT_FILTER);
        subject.unregister();

        // Unregister shouldn't know the context any more and so should not have worked
        Intent intent = new Intent(ACTION_INTERSTITIAL_SHOW);
        ShadowLocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        verify(customEventInterstitialListener).onInterstitialShown();
    }
}
