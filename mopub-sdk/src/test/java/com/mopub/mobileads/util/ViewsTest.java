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

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class ViewsTest {
    private Context context;
    private View subject;
    private RelativeLayout parent;

    @Before
    public void setup() {
        context = new Activity();
        subject = new View(context);
        parent = new RelativeLayout(context);
    }

    @Test
    public void removeFromParent_shouldRemoveViewFromParent() throws Exception {
        assertThat(parent.getChildCount()).isEqualTo(0);

        parent.addView(subject);
        assertThat(parent.getChildCount()).isEqualTo(1);
        assertThat(subject.getParent()).isEqualTo(parent);

        Views.removeFromParent(subject);

        assertThat(parent.getChildCount()).isEqualTo(0);
        assertThat(subject.getParent()).isNull();
    }

    @Test
    public void removeFromParent_withMultipleChildren_shouldRemoveCorrectChild() throws Exception {
        parent.addView(new TextView(context));

        assertThat(parent.getChildCount()).isEqualTo(1);

        parent.addView(subject);

        assertThat(parent.getChildCount()).isEqualTo(2);

        Views.removeFromParent(subject);
        assertThat(parent.getChildCount()).isEqualTo(1);

        assertThat(parent.getChildAt(0)).isInstanceOf(TextView.class);
    }

    @Test
    public void removeFromParent_whenViewIsNull_shouldPass() throws Exception {
        Views.removeFromParent(null);

        // pass
    }

    @Test
    public void removeFromParent_whenViewsParentIsNull_shouldPass() throws Exception {
        assertThat(subject.getParent()).isNull();

        Views.removeFromParent(subject);

        // pass
    }
}
