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

package com.mopub.mobileads.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Environment;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowEnvironment;

import java.util.*;

import static com.mopub.mobileads.util.VersionCode.HONEYCOMB_MR2;
import static com.mopub.mobileads.util.VersionCode.ICE_CREAM_SANDWICH;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

@RunWith(SdkTestRunner.class)
public class MraidsTest {
    Context context;

    @Before
    public void setup() {
        context = new Activity();
    }

    @Test
    public void isTelAvailable_whenCanAcceptIntent_shouldReturnTrue() throws Exception {
        context = createMockContextWithSpecificIntentData("tel", null, null, "android.intent.action.DIAL");

        assertThat(Mraids.isTelAvailable(context)).isTrue();
    }

    @Test
    public void isTelAvailable_whenCanNotAcceptIntent_shouldReturnFalse() throws Exception {
        context = createMockContextWithSpecificIntentData("", null, null, "android.intent.action.DIAL");

        assertThat(Mraids.isTelAvailable(context)).isFalse();
    }

    @Test
    public void isSmsAvailable_whenCanAcceptIntent_shouldReturnTrue() throws Exception {
        context = createMockContextWithSpecificIntentData("sms", null, null, "android.intent.action.VIEW");

        assertThat(Mraids.isSmsAvailable(context)).isTrue();
    }

    @Test
    public void isSmsAvailable_whenCanNotAcceptIntent_shouldReturnFalse() throws Exception {
        context = createMockContextWithSpecificIntentData("", null, null, "android.intent.action.VIEW");

        assertThat(Mraids.isSmsAvailable(context)).isFalse();
    }

    @Test
    public void isStorePictureAvailable_whenPermissionDeclaredAndMediaMounted_shouldReturnTrue() throws Exception {
        Robolectric.getShadowApplication().grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);

        assertThat(Mraids.isStorePictureSupported(context)).isTrue();
    }

    @Test
    public void isStorePictureAvailable_whenPermissionDenied_shouldReturnFalse() throws Exception {
        Robolectric.getShadowApplication().denyPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);

        assertThat(Mraids.isStorePictureSupported(context)).isFalse();
    }

    @Test
    public void isStorePictureAvailable_whenMediaUnmounted_shouldReturnFalse() throws Exception {
        Robolectric.getShadowApplication().grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_UNMOUNTED);

        assertThat(Mraids.isStorePictureSupported(context)).isFalse();
    }

    @Test
    public void isCalendarAvailable_whenApiLevelICS_shouldReturnTrue() throws Exception {
        context = createMockContextWithSpecificIntentData(null, null, Mraids.ANDROID_CALENDAR_CONTENT_TYPE, "android.intent.action.INSERT");
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ICE_CREAM_SANDWICH.getApiLevel());

        assertThat(Mraids.isCalendarAvailable(context)).isTrue();
    }

    @Test
    public void isCalendarAvailable_whenApiLevelBelowICS_shouldReturnFalse() throws Exception {
        context = createMockContextWithSpecificIntentData(null, null, Mraids.ANDROID_CALENDAR_CONTENT_TYPE, "android.intent.action.INSERT");
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", HONEYCOMB_MR2.getApiLevel());

        assertThat(Mraids.isCalendarAvailable(context)).isFalse();
    }

    @Test
    public void isCalendarAvailable_whenApiLevelICSButCanNotAcceptIntent_shouldReturnFalse() throws Exception {
        context = createMockContextWithSpecificIntentData(null, null, "vnd.android.cursor.item/NOPE", "android.intent.action.INSERT");
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ICE_CREAM_SANDWICH.getApiLevel());

        assertThat(Mraids.isCalendarAvailable(context)).isFalse();
    }

    @Test
    public void isInlineVideoAvailable_whenCanAcceptMraidVideoPlayerActivityIntent_shouldReturnTrue() throws Exception {
        context = createMockContextWithSpecificIntentData(null, "com.mopub.mobileads.MraidVideoPlayerActivity", null, null);

        assertThat(Mraids.isInlineVideoAvailable(context)).isTrue();
    }

    @Test
    public void isInlineVideoAvailable_whenCanNotAcceptMraidVideoPlayerActivityIntent_shouldReturnFalse() throws Exception {
        context = createMockContextWithSpecificIntentData(null, "com.mopub.mobileads.DO_NOT_ACCEPT", null, null);

        assertThat(Mraids.isInlineVideoAvailable(context)).isFalse();
    }

    public static Context createMockContextWithSpecificIntentData(final String scheme, final String componentName, final String type, final String action) {
        Context context = mock(Context.class);
        PackageManager packageManager = mock(PackageManager.class);

        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
        resolveInfos.add(new ResolveInfo());

        stub(context.getPackageManager()).toReturn(packageManager);

        BaseMatcher intentWithSpecificData = new BaseMatcher() {
            // check that the specific intent has the special data, i.e. "tel:", or a component name, or string type, based on a particular data

            @Override
            public boolean matches(Object item) {
                if (item != null && item instanceof Intent ){
                    boolean result = action != null || type != null || componentName != null || scheme != null;
                    if (action != null) {
                        if (((Intent) item).getAction() != null) {
                            result = result && action.equals(((Intent) item).getAction());
                        }
                    }

                    if (type != null) {
                        if (((Intent) item).getType() != null) {
                            result = result && type.equals(((Intent) item).getType());
                        }
                    }

                    if (componentName != null) {
                        if (((Intent) item).getComponent() != null) {
                            result = result && componentName.equals(((Intent) item).getComponent().getClassName());
                        }
                    }

                    if (scheme != null) {
                        if (((Intent) item).getData() != null) {
                            result = result && scheme.equals(((Intent) item).getData().getScheme());
                        }
                    }
                    return result;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {

            }
        };

        // It is okay to query with specific intent or nothing, because by default, none of the query would normally any resolveInfo anyways
        stub(packageManager.queryIntentActivities((Intent) argThat(intentWithSpecificData), eq(0))).toReturn(resolveInfos);
        return context;
    }
}
