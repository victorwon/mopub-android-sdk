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

import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class JsonTest {
    private Map<String,String> expectedMap;

    @Before
    public void setUp() throws Exception {
        expectedMap = new HashMap<String, String>();
    }

    @Test
    public void jsonStringToMap_shouldParseJson() throws Exception {
        expectedMap.put("key", "value");
        expectedMap.put("other_key", "other_value");

        String json = "{\"key\":\"value\",\"other_key\":\"other_value\"}";
        Map<String, String> map = Json.jsonStringToMap(json);
        assertThat(map).isEqualTo(expectedMap);
    }

    @Test
    public void jsonStringToMap_whenStringIsNull_shouldReturnEmptyMap() throws Exception {
        Map<String, String> map = Json.jsonStringToMap(null);
        assertThat(map).isEqualTo(expectedMap);
    }

    @Test
    public void jsonStringToMap_whenStringIsEmpty_shouldReturnEmptyMap() throws Exception {
        Map<String, String> map = Json.jsonStringToMap("");
        assertThat(map).isEqualTo(expectedMap);
    }

    @Test
    public void mapToJsonString_followedByJsonStringToMap_shouldReturnSameMap() throws Exception {
        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("key", "value");
        inputMap.put("other_key", "other_value");

        Map<String, String> outputMap = Json.jsonStringToMap(Json.mapToJsonString(inputMap));
        assertThat(outputMap).isEqualTo(inputMap);
    }

    @Test
    public void mapToJsonString_shouldReturnValidMap() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");

        String expectedJson = "{\"key\":\"value\"}";
        String actualJson = Json.mapToJsonString(map);
        assertThat(actualJson).isEqualTo(expectedJson);
    }

    @Test
    public void mapToJsonString_whenMapIsEmpty_shouldReturnEmptyJson() throws Exception {
        String expectedJson = "{}";
        assertThat(Json.mapToJsonString(new HashMap<String, String>())).isEqualTo(expectedJson);
    }

    @Test
    public void mapToJsonString_whenMapIsNull_shouldReturnEmptyJson() throws Exception {
        String expectedJson = "{}";
        assertThat(Json.mapToJsonString(null)).isEqualTo(expectedJson);
    }

    @Test
    public void jsonArrayToStringArray_withMultipleValidParameters_shouldReturnCorrespondingStringArray() throws Exception {
        String jsonString = "[\"hi\",\"dog\",\"goat\"]";

        String[] expected = {"hi", "dog", "goat"};

        assertThat(Json.jsonArrayToStringArray(jsonString)).isEqualTo(expected);
    }

    @Test
    public void jsonArrayToStringArray_withMultipleValidParameters_withSingleQuotes_shouldReturnCorrespondingStringArray() throws Exception {
        String jsonString = "['hi','dog','goat']";

        String[] expected = {"hi", "dog", "goat"};

        assertThat(Json.jsonArrayToStringArray(jsonString)).isEqualTo(expected);
    }

    @Test
    public void jsonArrayToStringArray_withMultipleValidParameters_withNoQuotes_shouldReturnCorrespondingStringArray() throws Exception {
        String jsonString = "[hi,dog,goat]";

        String[] expected = {"hi", "dog", "goat"};

        assertThat(Json.jsonArrayToStringArray(jsonString)).isEqualTo(expected);
    }

    @Test
    public void jsonArrayToStringArray_withNullInput_shouldReturnEmptyStringArray() throws Exception {
        String[] result = Json.jsonArrayToStringArray(null);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    public void jsonArrayToStringArray_withEmptyJsonArray_shouldReturnEmptyStringArray() throws Exception {
        String[] result = Json.jsonArrayToStringArray("[]");

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    public void jsonArrayToStringArray_withEmptyString_shouldReturnEmptyStringArray() throws Exception {
        String[] result = Json.jsonArrayToStringArray("");

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    public void jsonArrayToStringArray_withMalformedMalicousString_shouldReturnEmptyStringArray() throws Exception {
        String[] result = Json.jsonArrayToStringArray("} die");

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    public void jsonArrayToStringArray_whenMalformed_shouldReturnEmptyStringArray() throws Exception {
        String jsonString = "[cool,guy,crew";

        String[] result = Json.jsonArrayToStringArray(jsonString);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    public void jsonArrayToStringArray_withLotsOfEmptySpace_shouldReturnStringArrayWithoutSpaces() throws Exception {
        String jsonString = "        [    \"  hi\",\"do g\",\"goat  \"]";
        String[] expected = {"  hi", "do g", "goat  "};

        String[] result = Json.jsonArrayToStringArray(jsonString);

        assertThat(result).isEqualTo(expected);
    }
}
