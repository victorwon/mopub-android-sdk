/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.mobileads;


import android.app.Activity;
import android.os.Build;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowWebView;

import static com.mopub.mobileads.util.VersionCode.ECLAIR_MR1;
import static com.mopub.mobileads.util.VersionCode.FROYO;
import static com.mopub.mobileads.util.VersionCode.JELLY_BEAN_MR2;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;

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

    @Test
    public void destroy_shouldRemoveSelfFromParent_beforeCallingDestroy() throws Exception {
        subject = new BaseWebView(context);
        ViewGroup parent = mock(ViewGroup.class);
        ShadowWebView shadow = shadowOf(subject);
        shadow.setMyParent(parent);

        subject.destroy();

        verify(parent).removeView(eq(subject));
        assertThat(shadow.wasDestroyCalled()).isTrue();
    }
}
