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

import android.os.Build;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static com.mopub.mobileads.util.VersionCode.BASE;
import static com.mopub.mobileads.util.VersionCode.CUR_DEVELOPMENT;
import static com.mopub.mobileads.util.VersionCode.DONUT;
import static com.mopub.mobileads.util.VersionCode.FROYO;
import static com.mopub.mobileads.util.VersionCode.ICE_CREAM_SANDWICH;
import static com.mopub.mobileads.util.VersionCode.JELLY_BEAN;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class VersionCodeTest {
    @Test
    public void currentApiLevel_shouldReflectActualApiLevel() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", 4);
        assertThat(VersionCode.currentApiLevel()).isEqualTo(DONUT);

        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", 8);
        assertThat(VersionCode.currentApiLevel()).isEqualTo(FROYO);

        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", 14);
        assertThat(VersionCode.currentApiLevel()).isEqualTo(ICE_CREAM_SANDWICH);
    }

    @Test
    public void currentApiLevel_whenUnknownApiLevel_shouldReturnCurDevelopment() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", 900);
        assertThat(VersionCode.currentApiLevel()).isEqualTo(CUR_DEVELOPMENT);
    }

    @Test
    public void isAtMost_shouldCompareVersions() throws Exception {
        assertThat(ICE_CREAM_SANDWICH.isAtMost(BASE)).isFalse();
        assertThat(ICE_CREAM_SANDWICH.isAtMost(JELLY_BEAN)).isTrue();
        assertThat(ICE_CREAM_SANDWICH.isAtMost(ICE_CREAM_SANDWICH)).isTrue();
    }

    @Test
    public void isAtLeast_shouldCompareVersions() throws Exception {
        assertThat(ICE_CREAM_SANDWICH.isAtLeast(BASE)).isTrue();
        assertThat(ICE_CREAM_SANDWICH.isAtLeast(JELLY_BEAN)).isFalse();
        assertThat(ICE_CREAM_SANDWICH.isAtLeast(ICE_CREAM_SANDWICH)).isTrue();
    }

    @Test
    public void isBelow_shouldCompareVersions() throws Exception {
        assertThat(ICE_CREAM_SANDWICH.isBelow(BASE)).isFalse();
        assertThat(ICE_CREAM_SANDWICH.isBelow(JELLY_BEAN)).isTrue();
        assertThat(ICE_CREAM_SANDWICH.isBelow(ICE_CREAM_SANDWICH)).isFalse();
    }
}
