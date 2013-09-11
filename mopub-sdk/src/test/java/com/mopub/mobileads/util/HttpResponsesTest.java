package com.mopub.mobileads.util;

import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class HttpResponsesTest {
    private static final String NAME_KEY = "name";
    private static final String INTEGER_KEY = "integer";
    private TestHttpResponseWithHeaders response;

    @Before
    public void setup() {
        response = new TestHttpResponseWithHeaders(200, "all is well");
    }

    @Test
    public void extractBooleanHeader_whenValueIsZero_shouldReturnFalse() throws Exception {
        response.addHeader(NAME_KEY, "0");
        assertThat(HttpResponses.extractBooleanHeader(response, NAME_KEY, false)).isFalse();

        response.addHeader(NAME_KEY, "0");
        assertThat(HttpResponses.extractBooleanHeader(response, NAME_KEY, true)).isFalse();
    }

    @Test
    public void extractBooleanHeader_whenValueIsOne_shouldReturnTrue() throws Exception {
        response.addHeader(NAME_KEY, "1");
        assertThat(HttpResponses.extractBooleanHeader(response, NAME_KEY, false)).isTrue();

        response.addHeader(NAME_KEY, "1");
        assertThat(HttpResponses.extractBooleanHeader(response, NAME_KEY, true)).isTrue();
    }

    @Test
    public void extractBooleanHeader_shouldReturnDefaultValue() throws Exception {
        // no header added to response

        assertThat(HttpResponses.extractBooleanHeader(response, NAME_KEY, false)).isFalse();
        assertThat(HttpResponses.extractBooleanHeader(response, NAME_KEY, true)).isTrue();
    }

    @Test
    public void extractIntegerHeader_shouldReturnIntegerValue() throws Exception {
        response.addHeader(INTEGER_KEY, "10");
        assertThat(HttpResponses.extractIntegerHeader(response, INTEGER_KEY)).isEqualTo(10);

        response.addHeader(INTEGER_KEY, "0");
        assertThat(HttpResponses.extractIntegerHeader(response, INTEGER_KEY)).isEqualTo(0);

        response.addHeader(INTEGER_KEY, "-2");
        assertThat(HttpResponses.extractIntegerHeader(response, INTEGER_KEY)).isEqualTo(-2);
    }

    @Test
    public void extractIntegerHeader_withDoubleValue_shouldTruncateValue() throws Exception {
        response.addHeader(INTEGER_KEY, "3.14");
        assertThat(HttpResponses.extractIntegerHeader(response, INTEGER_KEY)).isEqualTo(3);

        response.addHeader(INTEGER_KEY, "-3.14");
        assertThat(HttpResponses.extractIntegerHeader(response, INTEGER_KEY)).isEqualTo(-3);
    }

    @Test
    public void extractIntegerHeader_whenNoHeaderPresent_shouldReturnNull() throws Exception {
        // no header added to response
        assertThat(HttpResponses.extractIntegerHeader(response, INTEGER_KEY)).isNull();

        response.addHeader(INTEGER_KEY, null);
        assertThat(HttpResponses.extractIntegerHeader(response, INTEGER_KEY)).isNull();
    }

    @Test
    public void extractIntegerHeader_withNonsenseStringValue_shouldReturnNull() throws Exception {
        response.addHeader(INTEGER_KEY, "llama!!guy");
        assertThat(HttpResponses.extractIntegerHeader(response, INTEGER_KEY)).isNull();
    }

    @Test
    public void extractIntHeader_withInvalidHeader_shouldUseDefaultValue() throws Exception {
        response.addHeader(INTEGER_KEY, "5");
        assertThat(HttpResponses.extractIntHeader(response, INTEGER_KEY, 10)).isEqualTo(5);

        response.addHeader(INTEGER_KEY, "five!");
        assertThat(HttpResponses.extractIntHeader(response, INTEGER_KEY, 10)).isEqualTo(10);
    }
}
