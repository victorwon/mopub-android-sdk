package com.mopub.mobileads.util;

import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class StringsTest {
    @Test
    public void isEmpty_shouldReturnValidResponse() throws Exception {
        assertThat(Strings.isEmpty("")).isTrue();

        assertThat(Strings.isEmpty("test")).isFalse();

        assertThat(Strings.isEmpty(null)).isFalse();
    }
}
