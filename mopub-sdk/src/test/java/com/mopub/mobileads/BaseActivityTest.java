package com.mopub.mobileads;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import org.fest.assertions.api.ANDROID;
import org.junit.Ignore;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLocalBroadcastManager;

import static com.mopub.mobileads.BaseActivity.ACTION_INTERSTITIAL_DISMISS;
import static com.mopub.mobileads.BaseActivity.ACTION_INTERSTITIAL_SHOW;
import static com.mopub.mobileads.BaseActivity.HTML_INTERSTITIAL_INTENT_FILTER;
import static com.mopub.mobileads.resource.Drawables.INTERSTITIAL_CLOSE_BUTTON_NORMAL;
import static com.mopub.mobileads.resource.Drawables.INTERSTITIAL_CLOSE_BUTTON_PRESSED;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;

@Ignore
public class BaseActivityTest {
    public static final String EXPECTED_SOURCE = "expected source";

    protected BaseActivity subject;
    protected BroadcastReceiver broadcastReceiver;

    public void setup() {
        broadcastReceiver = mock(BroadcastReceiver.class);
    }

    @Test
    public void onCreate_shouldSetContentView() throws Exception {
        subject.onCreate(null);

        assertThat(getContentView(subject).getChildCount()).isEqualTo(2);
    }

    @Test
    public void onCreate_shouldCreateView() throws Exception {
        subject.onCreate(null);

        View adView = getContentView(subject).getChildAt(0);

        assertThat(adView).isNotNull();
    }

    @Test
    public void onCreate_shouldBroadcastInterstitialShow() throws Exception {
        Intent expectedIntent = new Intent(ACTION_INTERSTITIAL_SHOW);
        ShadowLocalBroadcastManager.getInstance(subject).registerReceiver(broadcastReceiver, HTML_INTERSTITIAL_INTENT_FILTER);

        subject.onCreate(null);

        verify(broadcastReceiver).onReceive(eq(subject), eq(expectedIntent));
    }

    @Test
    public void onCreate_shouldShowInterstitialCloseButton() throws Exception {
        subject.onCreate(null);

        ImageButton closeButton = getCloseButton();

        Robolectric.clickOn(closeButton);

        ANDROID.assertThat(subject).isFinishing();
    }

    @Test
    public void onCreate_shouldMakeCloseButtonVisible() throws Exception {
        subject.onCreate(null);

        ImageButton closeButton = getCloseButton();

        ANDROID.assertThat(closeButton).isVisible();
        StateListDrawable states = (StateListDrawable) closeButton.getDrawable();

        int[] unpressedState = new int[] {-android.R.attr.state_pressed};
        assertThat(shadowOf(states).getDrawableForState(unpressedState))
                .isEqualTo(INTERSTITIAL_CLOSE_BUTTON_NORMAL.decodeImage(new Activity()));
        int[] pressedState = new int[] {android.R.attr.state_pressed};
        assertThat(shadowOf(states).getDrawableForState(pressedState))
                .isEqualTo(INTERSTITIAL_CLOSE_BUTTON_PRESSED.decodeImage(new Activity()));
    }

    @Test
    public void canShowAndHideTheCloseButton() throws Exception {
        subject.onCreate(null);
        ANDROID.assertThat(getCloseButton()).isVisible();

        subject.hideInterstitialCloseButton();
        ANDROID.assertThat(getCloseButton()).isInvisible();

        subject.showInterstitialCloseButton();
        ANDROID.assertThat(getCloseButton()).isVisible();
    }

    @Test
    public void onDestroy_shouldCleanUpContentView() throws Exception {
        subject.onCreate(null);
        subject.onDestroy();

        assertThat(getContentView(subject).getChildCount()).isEqualTo(0);
    }

    @Test
    public void onDestroy_shouldBroadcastInterstitialDismiss() throws Exception {
        Intent expectedIntent = new Intent(ACTION_INTERSTITIAL_DISMISS);
        ShadowLocalBroadcastManager.getInstance(subject).registerReceiver(broadcastReceiver, HTML_INTERSTITIAL_INTENT_FILTER);

        subject.onCreate(null);
        subject.onDestroy();

        verify(broadcastReceiver).onReceive(eq(subject), eq(expectedIntent));
    }

    protected ImageButton getCloseButton() {
        return (ImageButton) getContentView(subject).getChildAt(1);
    }

    protected RelativeLayout getContentView(BaseActivity subject) {
        return (RelativeLayout) ((ViewGroup) subject.findViewById(android.R.id.content)).getChildAt(0);
    }

    protected void resetMockedView(View view) {
        reset(view);
        stub(view.getLayoutParams()).toReturn(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
    }
}
