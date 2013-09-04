package com.mopub.mobileads;


import android.app.Activity;
import android.os.Build;
import android.webkit.WebSettings;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static com.mopub.mobileads.util.VersionCode.ECLAIR_MR1;
import static com.mopub.mobileads.util.VersionCode.FROYO;
import static com.mopub.mobileads.util.VersionCode.JELLY_BEAN_MR2;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class BaseWebViewTest {

    private Activity context;
    private BaseWebView subject;

    @Before
    public void setup() {
        context = new Activity();
    }

    @Test
    public void beforeFroyo_shouldDisablePluginsByDefault() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ECLAIR_MR1.getApiLevel());
        subject = new BaseWebView(context);

        WebSettings webSettings = subject.getSettings();
        assertThat(webSettings.getPluginsEnabled()).isFalse();

        subject.enablePlugins(true);
        assertThat(webSettings.getPluginsEnabled()).isTrue();
    }

    @Test
    public void froyoAndAfter_shouldDisablePluginsByDefault() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", FROYO.getApiLevel());
        subject = new BaseWebView(context);

        WebSettings webSettings = subject.getSettings();
        assertThat(webSettings.getPluginState()).isEqualTo(WebSettings.PluginState.OFF);

        subject.enablePlugins(true);
        assertThat(webSettings.getPluginState()).isEqualTo(WebSettings.PluginState.ON);
    }

    @Test
    public void jellyBeanMr2AndAfter_shouldPass() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", JELLY_BEAN_MR2.getApiLevel());
        subject = new BaseWebView(context);

        subject.enablePlugins(true);

        // pass
    }
}
