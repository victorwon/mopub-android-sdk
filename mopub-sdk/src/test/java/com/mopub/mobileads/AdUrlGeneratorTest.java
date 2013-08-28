package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowNetworkInfo;
import org.robolectric.shadows.ShadowTelephonyManager;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.net.ConnectivityManager.*;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UNKNOWN;
import static com.mopub.mobileads.AdUrlGenerator.MoPubNetworkType;
import static com.mopub.mobileads.util.Strings.isEmpty;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class AdUrlGeneratorTest {

    private AdUrlGenerator subject;
    private static final String TEST_UDID = "20b013c721c";
    private String expectedUdidSha;
    private Configuration configuration;
    private ShadowTelephonyManager shadowTelephonyManager;
    private ShadowConnectivityManager shadowConnectivityManager;
    private Activity context;

    @Before
    public void setup() {
        context = new Activity();
        shadowOf(context).grantPermissions(ACCESS_NETWORK_STATE);
        subject = new AdUrlGenerator(context);
        Settings.Secure.putString(application.getContentResolver(), Settings.Secure.ANDROID_ID, TEST_UDID);
        expectedUdidSha = Utils.sha1(TEST_UDID);
        configuration = application.getResources().getConfiguration();
        shadowTelephonyManager = shadowOf((TelephonyManager) application.getSystemService(Context.TELEPHONY_SERVICE));
        shadowConnectivityManager = shadowOf((ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE));
    }

    @Test
    public void generateAdUrl_shouldIncludeMinimumFields() throws Exception {
        String expectedAdUrl = new AdUrlBuilder(expectedUdidSha).build();

        String adUrl = generateMinimumUrlString();

        assertThat(adUrl).isEqualTo(expectedAdUrl);
    }

    @Test
    public void generateAdUrl_shouldRunMultipleTimes() throws Exception {
        String expectedAdUrl = new AdUrlBuilder(expectedUdidSha).build();

        String adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(expectedAdUrl);
        adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(expectedAdUrl);
    }

    @Test
    public void generateAdUrl_shouldIncludeAllFields() throws Exception {
        final String expectedAdUrl = new AdUrlBuilder(expectedUdidSha)
                .withAdUnitId("adUnitId")
                .withQuery("key%3Avalue")
                .withLatLon("20.1%2C30.0", "1")
                .withMcc("123")
                .withMnc("456")
                .withCountryIso("expected%20country")
                .withCarrierName("expected%20carrier")
                .withExternalStoragePermission(false)
                .build();

        shadowTelephonyManager.setNetworkOperator("123456");
        shadowTelephonyManager.setNetworkCountryIso("expected country");
        shadowTelephonyManager.setNetworkOperatorName("expected carrier");

        Location location = new Location("");
        location.setLatitude(20.1);
        location.setLongitude(30.0);
        location.setAccuracy(1.23f); // should get rounded to "1"

        String adUrl = subject
                .withAdUnitId("adUnitId")
                .withKeywords("key:value")
                .withLocation(location)
                .generateUrlString("ads.mopub.com");

        assertThat(adUrl).isEqualTo(expectedAdUrl);
    }

    @Test
    public void generateAdUrl_shouldRecognizeOrientation() throws Exception {
        configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
        assertThat(generateMinimumUrlString()).contains("&o=l");
        configuration.orientation = Configuration.ORIENTATION_PORTRAIT;
        assertThat(generateMinimumUrlString()).contains("&o=p");
        configuration.orientation = Configuration.ORIENTATION_SQUARE;
        assertThat(generateMinimumUrlString()).contains("&o=s");
    }

    @Test
    public void generateAdUrl_shouldHandleFunkyNetworkOperatorCodes() throws Exception {
        AdUrlBuilder urlBuilder = new AdUrlBuilder(expectedUdidSha);

        shadowTelephonyManager.setNetworkOperator("123456");
        String adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(urlBuilder.withMcc("123").withMnc("456").build());

        shadowTelephonyManager.setNetworkOperator("12345");
        adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(urlBuilder.withMcc("123").withMnc("45").build());

        shadowTelephonyManager.setNetworkOperator("1234");
        adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(urlBuilder.withMcc("123").withMnc("4").build());

        shadowTelephonyManager.setNetworkOperator("123");
        adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(urlBuilder.withMcc("123").withMnc("").build());

        shadowTelephonyManager.setNetworkOperator("12");
        adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(urlBuilder.withMcc("12").withMnc("").build());
    }

    @Test
    public void generateAdurl_whenOnCDMA_shouldGetOwnerStringFromSimCard() throws Exception {
        AdUrlBuilder urlBuilder = new AdUrlBuilder(expectedUdidSha);
        shadowTelephonyManager.setPhoneType(TelephonyManager.PHONE_TYPE_CDMA);
        shadowTelephonyManager.setSimState(TelephonyManager.SIM_STATE_READY);
        shadowTelephonyManager.setNetworkOperator("123456");
        shadowTelephonyManager.setSimOperator("789012");
        String adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(urlBuilder.withMcc("789").withMnc("012").build());
    }

    @Test
    public void generateAdurl_whenSimNotReady_shouldDefaultToNetworkOperator() throws Exception {
        AdUrlBuilder urlBuilder = new AdUrlBuilder(expectedUdidSha);
        shadowTelephonyManager.setPhoneType(TelephonyManager.PHONE_TYPE_CDMA);
        shadowTelephonyManager.setSimState(TelephonyManager.SIM_STATE_ABSENT);
        shadowTelephonyManager.setNetworkOperator("123456");
        shadowTelephonyManager.setSimOperator("789012");
        String adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(urlBuilder.withMcc("123").withMnc("456").build());
    }

    @Test
    public void generateAdUrl_shouldSetNetworkType() throws Exception {
        AdUrlBuilder urlBuilder = new AdUrlBuilder(expectedUdidSha);
        String adUrl;

        shadowConnectivityManager.setActiveNetworkInfo(createNetworkInfo(TYPE_DUMMY));
        adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(urlBuilder.withNetworkType(MoPubNetworkType.UNKNOWN).build());

        shadowConnectivityManager.setActiveNetworkInfo(createNetworkInfo(TYPE_ETHERNET));
        adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(urlBuilder.withNetworkType(MoPubNetworkType.ETHERNET).build());

        shadowConnectivityManager.setActiveNetworkInfo(createNetworkInfo(TYPE_WIFI));
        adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(urlBuilder.withNetworkType(MoPubNetworkType.WIFI).build());

        // bunch of random mobile types just to make life more interesting
        shadowConnectivityManager.setActiveNetworkInfo(createNetworkInfo(TYPE_MOBILE));
        adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(urlBuilder.withNetworkType(MoPubNetworkType.MOBILE).build());

        shadowConnectivityManager.setActiveNetworkInfo(createNetworkInfo(TYPE_MOBILE_DUN));
        adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(urlBuilder.withNetworkType(MoPubNetworkType.MOBILE).build());

        shadowConnectivityManager.setActiveNetworkInfo(createNetworkInfo(TYPE_MOBILE_HIPRI));
        adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(urlBuilder.withNetworkType(MoPubNetworkType.MOBILE).build());

        shadowConnectivityManager.setActiveNetworkInfo(createNetworkInfo(TYPE_MOBILE_MMS));
        adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(urlBuilder.withNetworkType(MoPubNetworkType.MOBILE).build());

        shadowConnectivityManager.setActiveNetworkInfo(createNetworkInfo(TYPE_MOBILE_SUPL));
        adUrl = generateMinimumUrlString();
        assertThat(adUrl).isEqualTo(urlBuilder.withNetworkType(MoPubNetworkType.MOBILE).build());
    }

    @Test
    public void generateAdUrl_whenNoNetworkPermission_shouldGenerateUnknownNetworkType() throws Exception {
        AdUrlBuilder urlBuilder = new AdUrlBuilder(expectedUdidSha);

        shadowOf(context).denyPermissions(ACCESS_NETWORK_STATE);
        shadowConnectivityManager.setActiveNetworkInfo(createNetworkInfo(TYPE_MOBILE));

        String adUrl = generateMinimumUrlString();

        assertThat(adUrl).isEqualTo(urlBuilder.withNetworkType(MoPubNetworkType.UNKNOWN).build());
    }

    @Test
    public void generateAdUrl_shouldTolerateNullActiveNetwork() throws Exception {
        AdUrlBuilder urlBuilder = new AdUrlBuilder(expectedUdidSha);
        shadowConnectivityManager.setActiveNetworkInfo(null);

        String adUrl = generateMinimumUrlString();

        assertThat(adUrl).isEqualTo(urlBuilder.withNetworkType(MoPubNetworkType.UNKNOWN).build());
    }

    private NetworkInfo createNetworkInfo(int type) {
        return ShadowNetworkInfo.newInstance(null,
                type,
                NETWORK_TYPE_UNKNOWN, true, true);
    }

    private String generateMinimumUrlString() {
        return subject.generateUrlString("ads.mopub.com");
    }

    private static class AdUrlBuilder {
        private String expectedUdidSha;
        private String adUnitId = "";
        private String query = "";
        private String latLon = "";
        private String locationAccuracy = "";
        private String mnc = "";
        private String mcc = "";
        private String countryIso = "";
        private String carrierName = "";
        private MoPubNetworkType networkType = MoPubNetworkType.MOBILE;
        private int externalStoragePermission;

        public AdUrlBuilder(String expectedUdidSha) {
            this.expectedUdidSha = expectedUdidSha;
        }

        public String build() {
            return "http://ads.mopub.com/m/ad" +
                    "?v=6" +
                    paramIfNotEmpty("id", adUnitId) +
                    "&nv=" + MoPub.SDK_VERSION +
                    "&udid=sha%3A" + expectedUdidSha +
                    paramIfNotEmpty("q", query) +
                    (isEmpty(latLon) ? "" : "&ll=" + latLon + "&lla=" + locationAccuracy) +
                    "&z=-0700" +
                    "&o=u" +
                    "&sc_a=1.0" +
                    "&mr=1" +
                    paramIfNotEmpty("mcc", mcc) +
                    paramIfNotEmpty("mnc", mnc) +
                    paramIfNotEmpty("iso", countryIso) +
                    paramIfNotEmpty("cn", carrierName) +
                    "&ct=" + networkType +
                    "&av=1.0" +
                    "&android_perms_ext_storage=" + externalStoragePermission;
        }

        public AdUrlBuilder withAdUnitId(String adUnitId) {
            this.adUnitId = adUnitId;
            return this;
        }

        public AdUrlBuilder withQuery(String query) {
            this.query = query;
            return this;
        }

        public AdUrlBuilder withLatLon(String latLon, String locationAccuracy) {
            this.latLon = latLon;
            this.locationAccuracy = locationAccuracy;
            return this;
        }

        public AdUrlBuilder withMcc(String mcc) {
            this.mcc = mcc;
            return this;
        }

        public AdUrlBuilder withMnc(String mnc) {
            this.mnc = mnc;
            return this;
        }

        public AdUrlBuilder withCountryIso(String countryIso) {
            this.countryIso = countryIso;
            return this;
        }

        public AdUrlBuilder withCarrierName(String carrierName) {
            this.carrierName = carrierName;
            return this;
        }

        public AdUrlBuilder withNetworkType(MoPubNetworkType networkType) {
            this.networkType = networkType;
            return this;
        }

        public AdUrlBuilder withExternalStoragePermission(boolean enabled) {
            this.externalStoragePermission = enabled ? 1 : 0;
            return this;
        }

        private String paramIfNotEmpty(String key, String value) {
            if (isEmpty(value)) {
                return "";
            } else {
                return "&" + key + "=" + value;
            }
        }
    }
}
