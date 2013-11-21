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
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class MraidBrowserControllerTest {
    private MraidBrowserController subject;
    private MraidView view;
    private Context context;
    private MraidView.OnOpenListener onOpenListener;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
        view = new MraidView(context, null);
        onOpenListener = mock(MraidView.OnOpenListener.class);
        view.setOnOpenListener(onOpenListener);

        subject = new MraidBrowserController(view);
    }

    @Test
    public void open_withApplicationUrl_shouldStartNewIntent() throws Exception {
        String applicationUrl = "amzn://blah";
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(applicationUrl)), new ResolveInfo());

        subject.open(applicationUrl);

        Intent startedIntent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedIntent).isNotNull();
        assertThat(startedIntent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(startedIntent.getComponent()).isNull();
    }

    @Test
    public void open_withHttpApplicationUrl_shouldStartMraidBrowser() throws Exception {
        String applicationUrl = "http://blah";

        subject.open(applicationUrl);

        Intent startedIntent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedIntent).isNotNull();
        assertThat(startedIntent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(startedIntent.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
    }

    @Test
    public void open_withApplicationUrlThatCantBeHandled_shouldDefaultToMraidBrowser() throws Exception {
        String applicationUrl = "canthandleme://blah";

        subject.open(applicationUrl);

        Intent startedIntent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedIntent).isNotNull();
        assertThat(startedIntent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(startedIntent.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
        assertThat(startedIntent.getStringExtra("extra_url")).isEqualTo(applicationUrl);
    }

    @Test
    public void open_withHttpApplicationUrl_shouldCallMraidListenerOnOpenCallback() throws Exception {
        String applicationUrl = "http://blah";
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(applicationUrl)), new ResolveInfo());

        subject.open(applicationUrl);

        verify(onOpenListener).onOpen(eq(view));
    }

    @Test
    public void open_withApplicationUrl_shouldCallMraidListenerOnOpenCallback() throws Exception {
        String applicationUrl = "app://blah";

        subject.open(applicationUrl);

        verify(onOpenListener).onOpen(eq(view));
    }
}
