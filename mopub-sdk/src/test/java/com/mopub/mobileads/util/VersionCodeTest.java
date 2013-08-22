package com.mopub.mobileads.util;

import android.os.Build;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static com.mopub.mobileads.util.VersionCode.*;
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
