package com.mopub.mobileads.util;

import android.os.AsyncTask;
import android.os.Build;

import com.mopub.mobileads.test.support.SdkTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static com.mopub.mobileads.util.VersionCode.HONEYCOMB_MR2;
import static com.mopub.mobileads.util.VersionCode.ICE_CREAM_SANDWICH;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class AsyncTasksTest {

    private AsyncTask<String, ?, ?> asyncTask;

    @Before
    public void setUp() throws Exception {
        asyncTask = spy(new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                return null;
            }
        });
    };

    @Test
    public void safeExecuteOnExecutor_beforeICS_shouldCallExecuteWithParams() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", HONEYCOMB_MR2.getApiLevel());

        AsyncTasks.safeExecuteOnExecutor(asyncTask, "hello");

        verify(asyncTask).execute(eq("hello"));
    }

    @Test
    public void safeExecutorOnExecutor_beforeICS_withNullParam_shouldCallExecute() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", HONEYCOMB_MR2.getApiLevel());

        AsyncTasks.safeExecuteOnExecutor(asyncTask, (String) null);

        verify(asyncTask).execute(eq((String) null));
    }

    @Test
    public void safeExecutorOnExecutor_beforeICS_withNullAsyncTask_shouldThrowIllegalArgumentException() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", HONEYCOMB_MR2.getApiLevel());

        try {
            AsyncTasks.safeExecuteOnExecutor(null, "hello");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            // pass
        }
    }

    @Test
    public void safeExecuteOnExecutor_atLeastICS_shouldCallExecuteWithParamsWithExecutor() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ICE_CREAM_SANDWICH.getApiLevel());

        AsyncTasks.safeExecuteOnExecutor(asyncTask, "goodbye");

        verify(asyncTask).executeOnExecutor(eq(THREAD_POOL_EXECUTOR), eq("goodbye"));
    }

    @Test
    public void safeExecutorOnExecutor_atLeastICS_withNullParam_shouldCallExecuteWithParamsWithExecutor() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ICE_CREAM_SANDWICH.getApiLevel());

        AsyncTasks.safeExecuteOnExecutor(asyncTask, (String) null);

        verify(asyncTask).executeOnExecutor(eq(THREAD_POOL_EXECUTOR), eq((String) null));

    }


    @Test
    public void safeExecutorOnExecutor_atLeastICS_withNullAsyncTask_shouldThrowIllegalArgumentException() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ICE_CREAM_SANDWICH.getApiLevel());

        try {
            AsyncTasks.safeExecuteOnExecutor(null, "hello");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            // pass
        }
    }
}
