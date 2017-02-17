/*
 * Copyright 2016 rande.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.battelle.clodhopper.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rande
 */
public class ProgressHandlerTest {
    
    public ProgressHandlerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of setMinProgressIncrement method, of class ProgressHandler.
     */
    @Test
    public void testWithBeginAndEndWithSteps() throws InterruptedException {
        
        // Choose non-default endpoints.
        final double beginProgress = 0.2;
        final double endProgress = 0.7;
        final int numSteps = 25;
        
        TestTask testTask = new TestTask(beginProgress, endProgress, numSteps);
        
        final AtomicReference<Double> beginReceived = new AtomicReference<>();
        final AtomicReference<Double> endReceived = new AtomicReference<>();
      
        final AtomicInteger numProgressMsgs = new AtomicInteger();
        
        testTask.addTaskListener(new TaskAdapter() {
            @Override
            public void taskProgress(TaskEvent evt) {
                beginReceived.compareAndSet(null, evt.getProgress());
                endReceived.accumulateAndGet(evt.getProgress(), (v1, v2) -> {
                    double val1 = v1 != null ? v1 : 0.0;
                    double val2 = v2 != null ? v2 : 0.0;
                    return Math.max(val1, val2);
                });
                numProgressMsgs.incrementAndGet();
            }
        });
        
        Thread t = new Thread(testTask);
        t.start();
        t.join();
        
        assertEquals(beginProgress, beginReceived.get(), 0.0);
        assertEquals(endProgress, endReceived.get(), 0.0);
    }

    /**
     * Test of setMinTimeIncrement method, of class ProgressHandler.
     */
    @Test
    public void testSetMinTimeIncrement() throws InterruptedException {
        
        // 10 steps...
        TestTask testTask = new TestTask(0.0, 1.0, 10);
        // 100 msecs total means 10 msecs per step.
        testTask.setTotalTimeToDoTask(100L, TimeUnit.MILLISECONDS);
        // But set 25 msecs as the min time increment between progress postings.
        testTask.getProgressHandler().setMinTimeIncrement(25L);
        // Have to make the min progress increment large, so time will be the deciding
        // factor.
        testTask.getProgressHandler().setMinProgressIncrement(5.0);
        
        final AtomicReference<Long> lastProgressReceived = new AtomicReference<>();
        final List<Long> timeBetweenPosts = new ArrayList<>();
        
        testTask.addTaskListener(new TaskAdapter() {
            @Override
            public void taskProgress(TaskEvent evt) {
                long now = System.currentTimeMillis();
                Long lastTimeReceived = lastProgressReceived.getAndSet(now);
                if (lastTimeReceived != null) {
                    Long timeBetween = now - lastTimeReceived;
                    timeBetweenPosts.add(timeBetween);
                }
            }
        });
        
        Thread t = new Thread(testTask);
        t.start();
        t.join();
        
        for (int i=0; i<timeBetweenPosts.size()-1; i++) {
            assertTrue("timeBetweenPosts(" + i + ") is too large", timeBetweenPosts.get(i) >= 25L);
        }
    }

    /**
     * Test of postMessage method, of class ProgressHandler.
     */
    @Test
    public void testPostMessage() throws InterruptedException {

        final int steps = 100;
        TestTask testTask = new TestTask(0.0, 1.0, steps);

        List<String> messages = new ArrayList<>();
        
        testTask.addTaskListener(new TaskAdapter() {
            @Override
            public void taskMessage(TaskEvent evt) {
                messages.add(evt.getMessage());
            }
        });
        
        Thread t = new Thread(testTask);
        t.start();
        t.join();
        
        // TestTask is written to post one message per step.
        // AbstractTask posts one message after doTask() completes.
        assertThat(messages.size(), is(equalTo(steps + 1)));
    }

    /**
     * Test of postFraction method, of class ProgressHandler.
     */
    @Test
    public void testPostFraction() throws InterruptedException {
        
        // 10 steps...
        TestTask testTask = new TestTask(0.0, 1.0, 10);
        // So it'll call postFraction() on the progress handler with values
        // 0.1, 0.2, 0.3, ..., 1.0
        testTask.setPostFractionalProgress(true);
        
        final List<Double> progressReceived = new ArrayList<>();
        
        testTask.addTaskListener(new TaskAdapter() {
            @Override
            public void taskProgress(TaskEvent evt) {
                progressReceived.add(evt.getProgress());
            }
        });
        
        Thread t = new Thread(testTask);
        t.start();
        t.join();

        assertTrue(progressReceived.size() >= 10);
        
        // The last 2 progresses are the same, so leave off the last one.
        for (int i=1; i<progressReceived.size() - 1; i++) {
            assertEquals(0.1, progressReceived.get(i) - progressReceived.get(i-1), 1.0e-10);
        }
    }

    /**
     * Test of postSteps method, of class ProgressHandler.
     */
    @Test
    public void testPostSteps() throws InterruptedException {

        final List<Integer> stepsToPost = Arrays.asList(2, 4, 5, 7, 3, 2, 6);
        final Integer totalSteps = stepsToPost.stream().mapToInt(n -> n.intValue()).sum();

        final List<Double> expectedProgress = new ArrayList<>();
        stepsToPost.forEach(n -> {
            double d = ((double) n.intValue())/totalSteps;
            if (expectedProgress.size() > 0) {
                d += expectedProgress.get(expectedProgress.size() - 1);
            }
            expectedProgress.add(d);
        });

        Task<Void> task = new AbstractTask<Void> () {
            @Override
            protected Void doTask() throws Exception {
                ProgressHandler ph = new ProgressHandler(this, 0.0, 1.0, totalSteps);
                // postBegin() and postEnd() each send progress events.
                ph.postBegin();
                // Then one is send for each list entry.
                stepsToPost.forEach(ph::postSteps);
                ph.postEnd();
                return null;
            }
            @Override
            public String taskName() {
                return "testPostSteps() task";
            }
        };
        
        final List<Double> progressReceived = new ArrayList<>();
        
        task.addTaskListener(new TaskAdapter() {
            @Override
            public void taskProgress(TaskEvent evt) {
                progressReceived.add(evt.getProgress());
            }
        });
        
        Thread t = new Thread(task);
        t.start();
        t.join();
        
        assertThat(progressReceived.size(), is(equalTo(expectedProgress.size() + 2)));
        for (int i=0; i<expectedProgress.size(); i++) {
            // progressReceived starts with 0. expectedProgress doesn't.
            assertEquals(expectedProgress.get(i), progressReceived.get(i+1), 1.0e-10);
        }
    }

    /**
     * Test of postIndeterminate method, of class ProgressHandler.
     */
    @Test
    public void testPostIndeterminate() throws InterruptedException {

        final int loops = 10;
        
        Task<Void> task = new AbstractTask<Void> () {
            @Override
            protected Void doTask() throws Exception {
                ProgressHandler ph = new ProgressHandler(this, 0.0, 1.0);
                ph.postBegin();
                for (int i=0; i<loops; i++) {
                    ph.postIndeterminate();
                }
                ph.postEnd();
                return null;
            }
            @Override
            public String taskName() {
                return "testPostIndeterminate() task";
            }
        };
        
        final AtomicInteger numIndeterminates = new AtomicInteger();
        task.addTaskListener(new TaskAdapter() {
           @Override
           public void taskProgress(TaskEvent evt) {
               if (evt.getProgress() == -1.0) {
                    numIndeterminates.incrementAndGet();
               }
           }
        });
        
        Thread t = new Thread(task);
        t.start();
        t.join();
        
        // Should've only gotten 1 even though there were 10 loops! 
        assertThat(numIndeterminates.get(), is(equalTo(1)));
    }

//    /**
//     * Test of subsection method, of class ProgressHandler.
//     */
//    @Test
//    public void testSubsection_double() {
//        System.out.println("subsection");
//        double fraction = 0.0;
//        ProgressHandler instance = null;
//        instance.subsection(fraction);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of subsection method, of class ProgressHandler.
//     */
//    @Test
//    public void testSubsection_double_int() {
//        System.out.println("subsection");
//        double fraction = 0.0;
//        int steps = 0;
//        ProgressHandler instance = null;
//        instance.subsection(fraction, steps);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of subsection method, of class ProgressHandler.
//     */
//    @Test
//    public void testSubsection_int() {
//        System.out.println("subsection");
//        int steps = 0;
//        ProgressHandler instance = null;
//        instance.subsection(steps);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of subsection method, of class ProgressHandler.
//     */
//    @Test
//    public void testSubsection_0args() {
//        System.out.println("subsection");
//        ProgressHandler instance = null;
//        instance.subsection();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of postBegin method, of class ProgressHandler.
//     */
//    @Test
//    public void testPostBegin() {
//        System.out.println("postBegin");
//        ProgressHandler instance = null;
//        instance.postBegin();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCurrentProgress method, of class ProgressHandler.
//     */
//    @Test
//    public void testGetCurrentProgress() {
//        System.out.println("getCurrentProgress");
//        ProgressHandler instance = null;
//        double expResult = 0.0;
//        double result = instance.getCurrentProgress();
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    
    static class TestTask extends AbstractTask<Boolean> {

        private final int numSteps;
        private final ProgressHandler progressHandler;
        private long totalTime;
        private TimeUnit timeUnit;
        private boolean postFractionalProgress;
        
        TestTask(final double beginProgress, final double endProgress, int numSteps) {
            super.setProgressEndpoints(beginProgress, endProgress);
            this.numSteps = numSteps;
            this.progressHandler = new ProgressHandler(this, numSteps);
        }
        
        public void setTotalTimeToDoTask(long totalTime, TimeUnit timeUnit) {
            this.totalTime = totalTime;
            this.timeUnit = timeUnit;
        }
        
        public void setPostFractionalProgress(boolean b) {
            postFractionalProgress = b;
        }
        
        public ProgressHandler getProgressHandler() {
            return progressHandler;
        }
        
        @Override
        protected Boolean doTask() throws Exception {
            long timePerStep = 0L;
            if (this.totalTime > 0L) {
                long totalTimeMs = this.timeUnit.toMillis(this.totalTime);
                timePerStep = totalTimeMs/numSteps;
            }
            progressHandler.postBegin();
            for (int i=0; i<numSteps; i++) {
                progressHandler.postMessage("on loop " + i);
                if (timePerStep > 0L) {
                    Thread.sleep(timePerStep);
                }
                if (postFractionalProgress) {
                    progressHandler.postFraction(((double) (i+1))/numSteps);
                } else {
                    progressHandler.postStep();
                }
            }
            // This sends a progress event.
            progressHandler.postEnd();
            return Boolean.TRUE;
        }

        @Override
        public String taskName() {
            return "TestTask";
        }
        
    }
}
