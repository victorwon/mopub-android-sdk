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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

@RunWith(SdkTestRunner.class)
public class UtilsTest {

    private Map<String,String> expectedMap;

    @Before
    public void setup() {
        expectedMap = new HashMap<String, String>();
    }

    @Test
    public void jsonStringToMap_shouldParseJson() throws Exception {
        expectedMap.put("key", "value");
        expectedMap.put("other_key", "other_value");

        String json = "{\"key\":\"value\",\"other_key\":\"other_value\"}";
        Map map = Utils.jsonStringToMap(json);
        assertThat(map).isEqualTo(expectedMap);
    }

    @Test
    public void jsonStringToMap_whenStringIsNull_shouldReturnEmptyMap() throws Exception {
        Map map = Utils.jsonStringToMap(null);
        assertThat(map).isEqualTo(expectedMap);
    }

    @Test
    public void jsonStringToMap_whenStringIsEmpty_shouldReturnEmptyMap() throws Exception {
        Map map = Utils.jsonStringToMap("");
        assertThat(map).isEqualTo(expectedMap);
    }

    @Test
    public void mapToJsonString_followedByJsonStringToMap_shouldReturnSameMap() throws Exception {
        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("key", "value");
        inputMap.put("other_key", "other_value");

        Map<String, String> outputMap = Utils.jsonStringToMap(Utils.mapToJsonString(inputMap));
        assertThat(outputMap).isEqualTo(inputMap);
    }

    @Test
    public void mapToJsonString_shouldReturnValidMap() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");

        String expectedJson = "{\"key\":\"value\"}";
        String actualJson = Utils.mapToJsonString(map);
        assertThat(actualJson).isEqualTo(expectedJson);
    }

    @Test
    public void mapToJsonString_whenMapIsEmpty_shouldReturnEmptyJson() throws Exception {
        String expectedJson = "{}";
        assertThat(Utils.mapToJsonString(new HashMap<String, String>())).isEqualTo(expectedJson);
    }

    @Test
    public void mapToJsonString_whenMapIsNull_shouldReturnEmptyJson() throws Exception {
        String expectedJson = "{}";
        assertThat(Utils.mapToJsonString(null)).isEqualTo(expectedJson);
    }

    @Test
    public void deviceCanHandleIntent_whenActivityCanResolveIntent_shouldReturnTrue() throws Exception {
        Context context = mock(Context.class);
        PackageManager packageManager = mock(PackageManager.class);

        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
        resolveInfos.add(new ResolveInfo());

        stub(context.getPackageManager()).toReturn(packageManager);
        Intent specificIntent = new Intent();
        specificIntent.setData(Uri.parse("specificIntent:"));

        stub(packageManager.queryIntentActivities(eq(specificIntent), eq(0))).toReturn(resolveInfos);

        assertThat(Utils.deviceCanHandleIntent(context, specificIntent)).isTrue();
    }

    @Test
    public void deviceCanHandleIntent_whenActivityCanNotResolveIntent_shouldReturnFalse() throws Exception {
        Context context = mock(Context.class);
        PackageManager packageManager = mock(PackageManager.class);

        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
        resolveInfos.add(new ResolveInfo());

        stub(context.getPackageManager()).toReturn(packageManager);
        Intent specificIntent = new Intent();
        specificIntent.setData(Uri.parse("specificIntent:"));

        Intent otherIntent = new Intent();
        otherIntent.setData(Uri.parse("other:"));
        stub(packageManager.queryIntentActivities(eq(specificIntent), eq(0))).toReturn(resolveInfos);

        assertThat(Utils.deviceCanHandleIntent(context, otherIntent)).isFalse();
    }
}
