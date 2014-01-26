package com.mopub.simpleadsdemo;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class Utils {
    public static final String LOGTAG = "MoPub Demo";

    private Utils() {}

    public static void validateAdUnitId(String adUnitId) throws IllegalArgumentException {
        if (adUnitId == null) {
            throw new IllegalArgumentException("Invalid Ad Unit ID: null ad unit.");
        } else if (adUnitId.length() == 0) {
            throw new IllegalArgumentException("Invalid Ad Unit Id: empty ad unit.");
        } else if (adUnitId.length() > 256) {
            throw new IllegalArgumentException("Invalid Ad Unit Id: length too long.");
        } else if (!isAlphaNumeric(adUnitId)) {
            throw new IllegalArgumentException("Invalid Ad Unit Id: contains non-alphanumeric characters.");
        }
    }

    public static void hideSoftKeyboard(final EditText editText) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static boolean isAlphaNumeric(String input) {
        return input.matches("^[a-zA-Z0-9-_]*$");
    }

    public static void logToast(Context context, String message) {
        Log.d(LOGTAG, message);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
