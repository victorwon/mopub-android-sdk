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

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class TaskTrackerTest {

    private TaskTracker taskTracker;

    @Before
    public void setUp() throws Exception {
        taskTracker = new TaskTracker();
    }

    @Test
    public void newTaskStarted_shouldIncrementIdsFromNegativeOne() throws Exception {
        assertThat(taskTracker.getCurrentTaskId()).isEqualTo(-1);

        taskTracker.newTaskStarted();

        assertThat(taskTracker.getCurrentTaskId()).isEqualTo(0);
    }

    @Test
    public void isMostCurrentTask_onFirstTask_whenSecondTaskIsCompleted_shouldBeFalse() throws Exception {
        taskTracker.newTaskStarted();
        taskTracker.newTaskStarted();
        taskTracker.markTaskCompleted(taskTracker.getCurrentTaskId());

        assertThat(taskTracker.isMostCurrentTask(0)).isFalse();
    }

    @Test
    public void isMostCurrentTask_onFirstTask_whenSecondTaskIsNotCompleted_shouldBeTrue() throws Exception {
        taskTracker.newTaskStarted();
        taskTracker.newTaskStarted();

        assertThat(taskTracker.isMostCurrentTask(0)).isTrue();
    }

    @Test
    public void mostCurrentTaskIsLastCompletedTaskOrLater() throws Exception {
        taskTracker.newTaskStarted();
        taskTracker.newTaskStarted();
        taskTracker.newTaskStarted();
        taskTracker.markTaskCompleted(1);

        assertThat(taskTracker.isMostCurrentTask(0)).isFalse();
        assertThat(taskTracker.isMostCurrentTask(1)).isTrue();
        assertThat(taskTracker.isMostCurrentTask(2)).isTrue();
    }

    @Test
    public void markTaskCompleted_shouldKeepTrackOfMostCurrentTaskRegardlessOfCompletionOrder() throws Exception {
        taskTracker.newTaskStarted();
        taskTracker.newTaskStarted();
        taskTracker.newTaskStarted();
        taskTracker.markTaskCompleted(1);
        taskTracker.markTaskCompleted(0);

        assertThat(taskTracker.isMostCurrentTask(0)).isFalse();
        assertThat(taskTracker.isMostCurrentTask(1)).isTrue();
    }
}
