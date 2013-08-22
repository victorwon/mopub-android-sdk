package com.mopub.mobileads;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/*
 * This class enables the MoPub SDK to deliver targeted ads from Facebook via MoPub Marketplace
 * (MoPub's real-time bidding ad exchange) as part of a test program. This class sends an identifier
 * to Facebook so Facebook can select the ad MoPub will serve in your app through MoPub Marketplace.
 * If this class is removed from the SDK, your application will not receive targeted ads from
 * Facebook.
 */

public class FacebookKeywordProvider {
    private static final Uri ID_URL = Uri.parse("content://com.facebook.katana.provider.AttributionIdProvider");
    private static final String ID_COLUMN_NAME = "aid";
    private static final String ID_PREFIX = "FBATTRID:";

    public static String getKeyword(Context context) {
        Cursor cursor = null;

        try {
            String projection[] = {ID_COLUMN_NAME};
            cursor = context.getContentResolver().query(ID_URL, projection, null, null, null);
            
            if (cursor == null || !cursor.moveToFirst()) {
                return null;
            }
            
            String attributionId = cursor.getString(cursor.getColumnIndex(ID_COLUMN_NAME));
            
            if (attributionId == null || attributionId.length() == 0) {
                return null;
            }
            
            return ID_PREFIX + attributionId;
        } catch (Exception exception) {
            Log.d("MoPub", "Unable to retrieve FBATTRID: " + exception.toString());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
