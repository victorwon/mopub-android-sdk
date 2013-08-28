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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
