package com.mopub.mobileads;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class FacebookKeywordProviderTest {

    private Context context;
    private ContentResolver contentResolver;
    private Cursor cursor;
    private FacebookKeywordProvider subject;

    @Before
    public void setUp() throws Exception {
        subject = new FacebookKeywordProvider();
        context = mock(Context.class);
        contentResolver = mock(ContentResolver.class);
        cursor = mock(Cursor.class);

        stub(context.getContentResolver()).toReturn(contentResolver);
        stub(contentResolver.query(
                any(Uri.class),
                any(String[].class),
                anyString(),
                any(String[].class),
                anyString())).toReturn(cursor);
    }

    @Test
    public void getKeyword_shouldCloseCursor() throws Exception {
        subject.getKeyword(context);

        verify(cursor).close();
    }
}
