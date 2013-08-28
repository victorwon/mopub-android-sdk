package com.mopub.mobileads.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import com.mopub.mobileads.MraidVideoPlayerActivity;
import com.mopub.mobileads.Utils;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Environment.MEDIA_MOUNTED;

public class MraidUtils {
    public static final String ANDROID_CALENDAR_CONTENT_TYPE = "vnd.android.cursor.item/event";

    public static boolean isTelAvailable(Context context) {
        Intent telIntent = new Intent(Intent.ACTION_DIAL);
        telIntent.setData(Uri.parse("tel:"));

        return Utils.deviceCanHandleIntent(context, telIntent);
    }

    public static boolean isSmsAvailable(Context context) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("sms:"));

        return Utils.deviceCanHandleIntent(context, smsIntent);
    }

    public static boolean isStorePictureSupported(Context context) {
        return MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && context.checkCallingOrSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isCalendarAvailable(Context context) {
        Intent calendarIntent = new Intent(Intent.ACTION_INSERT).setType(ANDROID_CALENDAR_CONTENT_TYPE);

        return VersionCode.currentApiLevel().isAtLeast(VersionCode.ICE_CREAM_SANDWICH)
                && Utils.deviceCanHandleIntent(context, calendarIntent);
    }

    public static boolean isInlineVideoAvailable(Context context) {
        Intent mraidVideoIntent = new Intent(context, MraidVideoPlayerActivity.class);

        return Utils.deviceCanHandleIntent(context, mraidVideoIntent);
    }
}
