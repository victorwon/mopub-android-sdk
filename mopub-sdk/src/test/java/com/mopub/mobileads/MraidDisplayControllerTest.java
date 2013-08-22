package com.mopub.mobileads;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.CalendarContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.mopub.mobileads.test.support.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowToast;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static com.mopub.mobileads.MraidCommandRegistry.*;
import static com.mopub.mobileads.MraidCommandStorePicture.MIME_TYPE_HEADER;
import static com.mopub.mobileads.MraidVideoPlayerActivityTest.assertVideoPlayerActivityStarted;
import static com.mopub.mobileads.util.VersionCode.ECLAIR;
import static com.mopub.mobileads.util.VersionCode.ICE_CREAM_SANDWICH;
import static java.io.File.separator;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class MraidDisplayControllerTest {
    private static final String IMAGE_URI_VALUE = "file://tmp/expectedFile.jpg";
    private static final int TIME_TO_PAUSE_FOR_NETWORK = 300;
    private static final String VIDEO_URL = "http://video";
    public static final String FAKE_IMAGE_DATA = "imageFileData";
    public static final String CALENDAR_START_TIME = "2013-08-14T20:00:00-0000";

    private MraidView mraidView;
    private MraidDisplayController subject;
    private View rootView;
    private FrameLayout contentView;
    private MoPubView moPubView;
    private FrameLayout adContainerLayout;
    private RelativeLayout expansionLayout;
    private FrameLayout placeholderView;
    private File expectedFile;
    private File pictureDirectory;
    private File fileWithoutExtension;
    private TestHttpResponseWithHeaders response;
    private Map<String, String> params;

    @Before
    public void setup() {
        mraidView = TestMraidViewFactory.getSingletonMock();
        moPubView = mock(MoPubView.class);
        rootView = mock(View.class);
        contentView = mock(FrameLayout.class);
        adContainerLayout = mock(FrameLayout.class);
        expansionLayout = mock(RelativeLayout.class);
        placeholderView = mock(FrameLayout.class);
        params = new HashMap<String, String>();

        resetMockMraidView();
        stub(rootView.findViewById(eq(android.R.id.content))).toReturn(contentView);
        stub(contentView.getContext()).toReturn(new Activity());

        subject = new TestMraidDisplayController(mraidView, null, null);

        FileUtils.copyFile("etc/expectedFile.jpg", "/tmp/expectedFile.jpg");
        expectedFile = new File(Environment.getExternalStorageDirectory(), "Pictures" + separator + "expectedFile.jpg");
        pictureDirectory = new File(Environment.getExternalStorageDirectory(), "Pictures");
        fileWithoutExtension = new File(pictureDirectory, "file");
    }

    @Test
    public void initialization_shouldSetupStartingState() throws Exception {
        assertThat(subject.getMraidView()).isSameAs(mraidView);
    }

    @Test
    public void initializeJavaScriptState_shouldSetMraidSupportsProperties() throws Exception {
        verify(mraidView).fireChangeEventForProperty(any(MraidProperty.class));

        subject.initializeJavaScriptState();
        verify(mraidView).fireChangeEventForProperty(isA(MraidSupportsProperty.class));
    }

    @Test
    public void initializeSupportedFunctionsProperty_whenPhoneIsAvailable_shouldReportPhoneAvailable() throws Exception {
        Robolectric.packageManager.setSystemFeature(PackageManager.FEATURE_TELEPHONY, true);
        Robolectric.getShadowApplication().grantPermissions(Manifest.permission.CALL_PHONE);

        resetMockMraidView();
        subject.initializeSupportedFunctionsProperty();

        MraidSupportsProperty mraidSupportsProperty = captureMraidSupportProperties();

        assertThat(mraidSupportsProperty.toJsonPair()).contains("tel: true");
    }

    @Test
    public void initializeSupportedFunctionsProperty_whenNoPhone_shouldReportPhoneNotAvailable() throws Exception {
        Robolectric.packageManager.setSystemFeature(PackageManager.FEATURE_TELEPHONY, false);
        Robolectric.getShadowApplication().grantPermissions(Manifest.permission.CALL_PHONE);

        resetMockMraidView();
        subject.initializeSupportedFunctionsProperty();

        MraidSupportsProperty mraidSupportsProperty = captureMraidSupportProperties();

        assertThat(mraidSupportsProperty.toJsonPair()).contains("tel: false");
    }

    @Test
    public void initializeSupportedFunctionsProperty_whenPhonePermissionNotGranted_shouldReportPhoneNotAvailable() throws Exception {
        Robolectric.packageManager.setSystemFeature(PackageManager.FEATURE_TELEPHONY, true);
        Robolectric.getShadowApplication().denyPermissions(Manifest.permission.CALL_PHONE);

        resetMockMraidView();
        subject.initializeSupportedFunctionsProperty();

        MraidSupportsProperty mraidSupportsProperty = captureMraidSupportProperties();

        assertThat(mraidSupportsProperty.toJsonPair()).contains("tel: false");
    }

    @Test
    public void initializeSupportedFunctionsProperty_whenSmsIsAvailable_shouldReportSmsAvailable() throws Exception {
        Robolectric.packageManager.setSystemFeature(PackageManager.FEATURE_TELEPHONY, true);
        Robolectric.getShadowApplication().grantPermissions(Manifest.permission.SEND_SMS);

        resetMockMraidView();
        subject.initializeSupportedFunctionsProperty();

        MraidSupportsProperty mraidSupportsProperty = captureMraidSupportProperties();

        assertThat(mraidSupportsProperty.toJsonPair()).contains("sms: true");
    }

    @Test
    public void initializeSupportedFunctionsProperty_whenNoPhone_shouldReportSmsNotAvailable() throws Exception {
        Robolectric.packageManager.setSystemFeature(PackageManager.FEATURE_TELEPHONY, false);
        Robolectric.getShadowApplication().grantPermissions(Manifest.permission.SEND_SMS);

        resetMockMraidView();
        subject.initializeSupportedFunctionsProperty();

        MraidSupportsProperty mraidSupportsProperty = captureMraidSupportProperties();

        assertThat(mraidSupportsProperty.toJsonPair()).contains("sms: false");
    }

    @Test
    public void initializeSupportedFunctionsProperty_whenSmsPermissionNotGranted_shouldReportSmsNotAvailable() throws Exception {
        Robolectric.packageManager.setSystemFeature(PackageManager.FEATURE_TELEPHONY, true);
        Robolectric.getShadowApplication().denyPermissions(Manifest.permission.SEND_SMS);

        resetMockMraidView();
        subject.initializeSupportedFunctionsProperty();

        MraidSupportsProperty mraidSupportsProperty = captureMraidSupportProperties();

        assertThat(mraidSupportsProperty.toJsonPair()).contains("sms: false");
    }

    @Test
    public void initializeSupportedFunctionsProperty_shouldReportCalendarInlineVideoAndStorePictureAvailable() throws Exception {
        resetMockMraidView();
        subject.initializeSupportedFunctionsProperty();

        MraidSupportsProperty mraidSupportsProperty = captureMraidSupportProperties();

        assertThat(mraidSupportsProperty.toJsonPair()).contains("calendar: true");
        assertThat(mraidSupportsProperty.toJsonPair()).contains("inlineVideo: true");
        assertThat(mraidSupportsProperty.toJsonPair()).contains("storePicture: true");
    }

    @Test
    public void expand_shouldSwapWithPlaceholderView() throws Exception {
        stub(moPubView.getChildAt(eq(0))).toReturn(mraidView);
        subject.expand(null, 320, 50, false, false);

        verify(moPubView).addView(any(FrameLayout.class), eq(0), any(ViewGroup.LayoutParams.class));
        verify(moPubView).removeView(eq(mraidView));
        verify(adContainerLayout, times(2)).addView(any(ImageView.class), any(FrameLayout.LayoutParams.class));
    }

    @Test
    public void close_shouldUnexpandView() throws Exception {
        subject.expand(null, 320, 50, false, false);
        stub(placeholderView.getParent()).toReturn(moPubView);

        subject.close();

        verify(adContainerLayout).removeAllViewsInLayout();
        verify(expansionLayout).removeAllViewsInLayout();
        verify(contentView).removeView(eq(expansionLayout));
        verify(moPubView).addView(eq(mraidView), any(int.class));
        verify(moPubView).removeView(eq(placeholderView));
        verify(moPubView).invalidate();
    }

    @Test
    public void showUserDownloadImageAlert_withActivityContext_shouldDisplayAlertDialog() throws Exception {
        stub(mraidView.getContext()).toReturn(new Activity());
        subject = new TestMraidDisplayController(mraidView, null, null);
        response = new TestHttpResponseWithHeaders(200, FAKE_IMAGE_DATA);

        subject.showUserDownloadImageAlert(IMAGE_URI_VALUE);

        AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(alertDialog);

        assertThat(alertDialog.isShowing());

        assertThat(shadowAlertDialog.getTitle()).isEqualTo("Save Image");
        assertThat(shadowAlertDialog.getMessage()).isEqualTo("Download image to Picture gallery?");
        assertThat(shadowAlertDialog.isCancelable());

        assertThat(alertDialog.getButton(BUTTON_POSITIVE).hasOnClickListeners());
        assertThat(alertDialog.getButton(BUTTON_NEGATIVE)).isNotNull();
    }

    @Test
    public void showUserDownloadImageAlert_whenOkayClicked_shouldDownloadImage() throws Exception {
        stub(mraidView.getContext()).toReturn(new Activity());
        subject = new TestMraidDisplayController(mraidView, null, null);
        response = new TestHttpResponseWithHeaders(200, FAKE_IMAGE_DATA);
        Robolectric.addPendingHttpResponse(response);

        subject.showUserDownloadImageAlert(IMAGE_URI_VALUE);

        ShadowAlertDialog.getLatestAlertDialog().getButton(BUTTON_POSITIVE).performClick();
        ThreadUtils.pause(TIME_TO_PAUSE_FOR_NETWORK);

        assertThat(expectedFile.exists()).isTrue();
        assertThat(expectedFile.length()).isEqualTo(FAKE_IMAGE_DATA.length());
    }

    @Test
    public void showUserDownloadImageAlert_whenCancelClicked_shouldDismissDialog() throws Exception {
        stub(mraidView.getContext()).toReturn(new Activity());
        subject = new TestMraidDisplayController(mraidView, null, null);
        response = new TestHttpResponseWithHeaders(200, FAKE_IMAGE_DATA);

        subject.showUserDownloadImageAlert(IMAGE_URI_VALUE);

        AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(alertDialog);

        alertDialog.getButton(BUTTON_NEGATIVE).performClick();
        assertThat(shadowAlertDialog.hasBeenDismissed()).isTrue();

        assertThat(expectedFile.exists()).isFalse();
        assertThat(expectedFile.length()).isEqualTo(0);
    }

    @Test
    public void showUserDownloadImageAlert_withAppContext_shouldToastAndDownloadImage() throws Exception {
        stub(mraidView.getContext()).toReturn(Robolectric.application);
        subject = new TestMraidDisplayController(mraidView, null, null);
        response = new TestHttpResponseWithHeaders(200, FAKE_IMAGE_DATA);
        Robolectric.addPendingHttpResponse(response);

        assertThat(ShadowToast.getLatestToast()).isNull();

        subject.showUserDownloadImageAlert(IMAGE_URI_VALUE);
        ThreadUtils.pause(TIME_TO_PAUSE_FOR_NETWORK);

        assertThat(ShadowToast.getLatestToast()).isNotNull();

        assertThat(expectedFile.exists()).isTrue();
        assertThat(expectedFile.length()).isEqualTo(FAKE_IMAGE_DATA.length());
    }

    @Test
    public void showUserDownloadImageAlert_withMimeTypeAndNoFileExtension_shouldSavePictureWithMimeType() throws Exception {
        String fileNameWithNoExtension = "https://www.somewhere.com/images/blah/file";

        assertThatMimeTypeWasAddedCorrectly(
                fileNameWithNoExtension,
                "image/jpg",
                "file.jpg",
                ".jpg");
    }

    @Test
    public void showUserDownloadImageAlert_withMultipleContentTypesAndNoFileExtension_shouldSavePictureWithMimeType() throws Exception {
        String fileNameWithNoExtension = "https://www.somewhere.com/images/blah/file";

        assertThatMimeTypeWasAddedCorrectly(
                fileNameWithNoExtension,
                "text/html; image/png",
                "file.png",
                ".png");
    }

    @Test
    public void showUserDownloadImageAlert_withMimeTypeAndFileExtension_shouldSavePictureWithFileExtension() throws Exception {
        String fileNameWithExtension = "https://www.somewhere.com/images/blah/file.extension";

        assertThatMimeTypeWasAddedCorrectly(
                fileNameWithExtension,
                "image/extension",
                "file.extension",
                ".extension");

        assertThat((expectedFile.getName()).endsWith(".extension.extension")).isFalse();
    }

    @Test
    public void showUserDownloadImageAlert_withHttpUri_shouldRequestPictureFromNetwork() throws Exception {
        response = new TestHttpResponseWithHeaders(200, "OK");
        downloadImageForPendingResponse("https://www.google.com/images/srpr/logo4w.png", response);

        HttpUriRequest latestRequest = (HttpUriRequest) Robolectric.getLatestSentHttpRequest();
        assertThat(latestRequest.getURI()).isEqualTo(URI.create("https://www.google.com/images/srpr/logo4w.png"));
    }

    @Test
    public void orientationBroadcastReceiver_whenUnregistered_shouldIgnoreOnReceive() throws Exception {
        Intent intent = mock(Intent.class);
        stub(intent.getAction()).toReturn("some bogus action which we hope never to see");
        Context context = new Activity();
        MraidDisplayController.OrientationBroadcastReceiver receiver = subject.new OrientationBroadcastReceiver();
        receiver.register(context);

        receiver.unregister();
        receiver.onReceive(context, intent);

        verify(intent, never()).getAction();
    }

    @Test
    public void showVideo_shouldStartVideoPlayerActivity() throws Exception {
        subject.showVideo(VIDEO_URL);

        assertVideoPlayerActivityStarted(VIDEO_URL);
    }

    @Test
    public void getCurrentPosition_shouldFireErrorEvent() throws Exception {
        resetMockMraidView();

        subject.getCurrentPosition();

        verify(mraidView).fireErrorEvent(eq(MRAID_JAVASCRIPT_COMMAND_GET_CURRENT_POSITION), any(String.class));
    }

    @Test
    public void getDefaultPosition_shouldFireErrorEvent() throws Exception {
        resetMockMraidView();

        subject.getDefaultPosition();

        verify(mraidView).fireErrorEvent(eq(MRAID_JAVASCRIPT_COMMAND_GET_DEFAULT_POSITION), any(String.class));
    }

    @Test
    public void getMaxSize_shouldFireErrorEvent() throws Exception {
        resetMockMraidView();

        subject.getMaxSize();

        verify(mraidView).fireErrorEvent(eq(MRAID_JAVASCRIPT_COMMAND_GET_MAX_SIZE), any(String.class));
    }
    @Test
    public void getScreenSize_shouldFireErrorEvent() throws Exception {
        resetMockMraidView();

        subject.getScreenSize();

        verify(mraidView).fireErrorEvent(eq(MRAID_JAVASCRIPT_COMMAND_GET_SCREEN_SIZE), any(String.class));
    }

    @Test
    public void createCalendarEvent_withMinimumValidParams_onICS_shouldCreateEventIntent() throws Exception {
        setupCalendarParams();

        subject.createCalendarEvent(params);

        verify(mraidView, never()).fireErrorEvent(eq(MRAID_JAVASCRIPT_COMMAND_CREATE_CALENDAR_EVENT), any(String.class));

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();

        assertThat(intent.getType()).isEqualTo("vnd.android.cursor.item/event");
        assertThat(intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(intent.getStringExtra(CalendarContract.Events.TITLE)).isNotNull();
        assertThat(intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, -1)).isNotEqualTo(-1);
    }

    @Test
    public void createCalendarEvent_withDailyRecurrence_shouldCreateCalendarIntent() throws Exception {
        setupCalendarParams();
        params.put("frequency", "daily");

        subject.createCalendarEvent(params);

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getType()).isEqualTo("vnd.android.cursor.item/event");
        assertThat(intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(intent.getStringExtra(CalendarContract.Events.RRULE)).isEqualTo("FREQ=DAILY;");
    }

    @Test
    public void createCalendarEvent_withDailyRecurrence_withInterval_shouldCreateCalendarIntent() throws Exception {
        setupCalendarParams();
        params.put("frequency", "daily");
        params.put("interval", "2");

        subject.createCalendarEvent(params);

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getStringExtra(CalendarContract.Events.RRULE)).isEqualTo("FREQ=DAILY;INTERVAL=2;");
    }

    @Test
    public void createCalendarEvent_withWeeklyRecurrence_shouldCreateCalendarIntent() throws Exception {
        setupCalendarParams();
        params.put("frequency", "weekly");

        subject.createCalendarEvent(params);

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getStringExtra(CalendarContract.Events.RRULE)).isEqualTo("FREQ=WEEKLY;");
    }

    @Test
    public void createCalendarEvent_withWeeklyRecurrence_withInterval_withOutWeekday_shouldCreateCalendarIntent() throws Exception {
        setupCalendarParams();
        params.put("frequency", "weekly");
        params.put("interval", "7");

        subject.createCalendarEvent(params);

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getStringExtra(CalendarContract.Events.RRULE)).isEqualTo("FREQ=WEEKLY;INTERVAL=7;");
    }

    @Test
    public void createCalendarEvent_withWeeklyRecurrence_onAllWeekDays_shouldCreateCalendarIntent() throws Exception {
        setupCalendarParams();
        params.put("frequency", "weekly");
        params.put("daysInWeek", "0,1,2,3,4,5,6");

        subject.createCalendarEvent(params);

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getStringExtra(CalendarContract.Events.RRULE)).isEqualTo("FREQ=WEEKLY;BYDAY=SU,MO,TU,WE,TH,FR,SA;");
    }

    @Test
    public void createCalendarEvent_withWeeklyRecurrence_onDuplicateWeekDays_shouldCreateCalendarIntent() throws Exception {
        setupCalendarParams();
        params.put("frequency", "weekly");
        params.put("daysInWeek", "3,2,3,3,7,0");

        subject.createCalendarEvent(params);

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getStringExtra(CalendarContract.Events.RRULE)).isEqualTo("FREQ=WEEKLY;BYDAY=WE,TU,SU;");
    }

    @Test
    public void createCalendarEvent_withWeeklyRecurrence_withInterval_withWeekDay_shouldCreateCalendarIntent() throws Exception {
        setupCalendarParams();
        params.put("frequency", "weekly");
        params.put("interval", "1");
        params.put("daysInWeek", "1");

        subject.createCalendarEvent(params);

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getStringExtra(CalendarContract.Events.RRULE)).isEqualTo("FREQ=WEEKLY;INTERVAL=1;BYDAY=MO;");
    }

    @Test
    public void createCalendarEvent_withDailyRecurrence_withWeeklyRecurrence_withMonthlyOccurence_shouldCreateDailyCalendarIntent() throws Exception {
        setupCalendarParams();
        params.put("frequency", "daily");
        params.put("frequency", "daily");
        params.put("frequency", "daily");
        params.put("interval", "2");
        params.put("daysInWeek", "1");

        subject.createCalendarEvent(params);

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getStringExtra(CalendarContract.Events.RRULE)).isEqualTo("FREQ=DAILY;INTERVAL=2;");
    }


    @Test
    public void createCalendarEvent_withMonthlyRecurrence_withOutInterval_shouldCreateCalendarIntent() throws Exception {
        setupCalendarParams();
        params.put("frequency", "monthly");

        subject.createCalendarEvent(params);

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getStringExtra(CalendarContract.Events.RRULE)).isEqualTo("FREQ=MONTHLY;");
    }

    @Test
    public void createCalendarEvent_withMonthlyRecurrence_withInterval_shouldCreateCalendarIntent() throws Exception {
        setupCalendarParams();
        params.put("frequency", "monthly");
        params.put("interval", "2");

        subject.createCalendarEvent(params);

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getStringExtra(CalendarContract.Events.RRULE)).isEqualTo("FREQ=MONTHLY;INTERVAL=2;");
    }

    @Test
    public void createCalendarEvent_withMonthlyRecurrence_withOutInterval_withDaysOfMonth_shouldCreateCalendarIntent() throws Exception {
        setupCalendarParams();
        params.put("frequency", "monthly");
        params.put("daysInMonth", "2,-15");

        subject.createCalendarEvent(params);

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getStringExtra(CalendarContract.Events.RRULE)).isEqualTo("FREQ=MONTHLY;BYMONTHDAY=2,-15;");
    }

    @Test
    public void createCalendarEvent_withMonthlyRecurrence_withInvalidDaysOfMonth_shouldNotCreateCalendarIntent() throws Exception {
        setupCalendarParams();
        params.put("frequency", "monthly");
        params.put("daysInMonth", "55");

        subject.createCalendarEvent(params);

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();

        assertThat(intent).isNull();
        assertThat(ShadowLog.getLogs().size()).isEqualTo(1);
    }

    @Test
    public void createCalendarEvent_withWeeklyRecurrence_withInvalidDaysOfWeek_shouldNotCreateCalendarIntent() throws Exception {
        setupCalendarParams();
        params.put("frequency", "weekly");
        params.put("daysInWeek", "-1,20");

        subject.createCalendarEvent(params);

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();

        assertThat(intent).isNull();
        assertThat(ShadowLog.getLogs().size()).isEqualTo(1);
    }

    @Test
    public void createCalendarEvent_onPreICSDevice_shouldFireErrorEvent() throws Exception {
        resetMockMraidView();
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ECLAIR.getApiLevel());

        subject.createCalendarEvent(params);

        verify(mraidView).fireErrorEvent(eq(MRAID_JAVASCRIPT_COMMAND_CREATE_CALENDAR_EVENT), any(String.class));
    }

    @Test
    public void createCalendarEvent_withInvalidDate_shouldFireErrorEvent() throws Exception {
        resetMockMraidView();
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ICE_CREAM_SANDWICH.getApiLevel());
        params.put("start", "2013-08-14T09:00.-08:00");
        params.put("description", "Some Event");

        subject.createCalendarEvent(params);

        verify(mraidView).fireErrorEvent(eq(MRAID_JAVASCRIPT_COMMAND_CREATE_CALENDAR_EVENT), any(String.class));
    }

    @Test
    public void createCalendarEvent_withMissingParameters_shouldFireErrorEvent() throws Exception {
        resetMockMraidView();
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ICE_CREAM_SANDWICH.getApiLevel());
        //it needs a start time
        params.put("description", "Some Event");

        subject.createCalendarEvent(params);

        verify(mraidView).fireErrorEvent(eq(MRAID_JAVASCRIPT_COMMAND_CREATE_CALENDAR_EVENT), any(String.class));
    }

    @Test
    public void createCalendarEvent_withNullDate_shouldFireErrorEvent() throws Exception {
        resetMockMraidView();
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ICE_CREAM_SANDWICH.getApiLevel());
        params.put("start", null);
        params.put("description", "Some Event");

        subject.createCalendarEvent(params);

        verify(mraidView).fireErrorEvent(eq(MRAID_JAVASCRIPT_COMMAND_CREATE_CALENDAR_EVENT), any(String.class));
    }

    @Test
    public void createCalendarEvent_withValidParamsAllExceptRecurrence_onICS_shouldCreateEventIntent() throws Exception {
        setupCalendarParams();
        params.put("location", "my house");
        params.put("end", "2013-08-14T22:01:01-0000");
        params.put("summary", "some description actually");
        params.put("transparency", "transparent");

        subject.createCalendarEvent(params);

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();

        assertThat(intent.getType()).isEqualTo("vnd.android.cursor.item/event");
        assertThat(intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(intent.getStringExtra(CalendarContract.Events.TITLE)).isNotNull();
        assertThat(intent.getStringExtra(CalendarContract.Events.DESCRIPTION)).isNotNull();
        assertThat(intent.getStringExtra(CalendarContract.Events.EVENT_LOCATION)).isNotNull();
        assertThat(intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, -1)).isNotEqualTo(-1);
        assertThat(intent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, -1)).isNotEqualTo(-1);
        assertThat(intent.getIntExtra(CalendarContract.Events.AVAILABILITY, -1)).isEqualTo(CalendarContract.Events.AVAILABILITY_FREE);
    }

    private void resetMockMraidView() {
        reset(mraidView);
        stub(mraidView.getContext()).toReturn(new Activity());
        when(mraidView.getParent()).thenReturn(moPubView).thenReturn(null);
        stub(mraidView.getRootView()).toReturn(rootView);
    }

    private MraidSupportsProperty captureMraidSupportProperties() {
        ArgumentCaptor<MraidSupportsProperty> propertiesCaptor = ArgumentCaptor.forClass(MraidSupportsProperty.class);
        verify(mraidView).fireChangeEventForProperty(propertiesCaptor.capture());
        return propertiesCaptor.getValue();
    }

    private void downloadImageForPendingResponse(String uri, HttpResponse response){
        Robolectric.addPendingHttpResponse(response);

        stub(mraidView.getContext()).toReturn(Robolectric.application);
        subject = new TestMraidDisplayController(mraidView, null, null);
        subject.showUserDownloadImageAlert(uri);

        ThreadUtils.pause(TIME_TO_PAUSE_FOR_NETWORK);
    }

    private void assertThatMimeTypeWasAddedCorrectly(String originalFileName, String contentType, String expectedFileName, String expectedExtension) {
        expectedFile = new File(pictureDirectory, expectedFileName);
        response = new TestHttpResponseWithHeaders(200, FAKE_IMAGE_DATA);
        response.addHeader(MIME_TYPE_HEADER, contentType);

        downloadImageForPendingResponse(originalFileName, response);

        assertThat(expectedFile.exists()).isTrue();
        assertThat(expectedFile.getName()).endsWith(expectedExtension);
        assertThat(fileWithoutExtension.exists()).isFalse();
    }


    private void setupCalendarParams() {
        resetMockMraidView();
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ICE_CREAM_SANDWICH.getApiLevel());
        params.put("description", "Some Event");
        params.put("start", CALENDAR_START_TIME);
    }


    private class TestMraidDisplayController extends MraidDisplayController {
        public TestMraidDisplayController(MraidView mraidView, MraidView.ExpansionStyle expStyle,
                                          MraidView.NativeCloseButtonStyle buttonStyle) {
            super(mraidView, expStyle, buttonStyle);
        }

        @Override
        FrameLayout createAdContainerLayout() {
            return adContainerLayout;
        }

        @Override
        RelativeLayout createExpansionLayout() {
            return expansionLayout;
        }

        @Override
        FrameLayout createPlaceholderView() {
            return placeholderView;
        }
    }
}
