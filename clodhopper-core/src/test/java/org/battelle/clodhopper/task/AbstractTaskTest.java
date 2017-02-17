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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
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
public class AbstractTaskTest {
    
    public AbstractTaskTest() {
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
     * Test of addTaskListener method, of class AbstractTask.
     */
    @Test
    public void testAddTaskListener() throws InterruptedException, ExecutionException {
        System.out.println("addTaskListener");
        TestTaskListener l = new TestTaskListener();
        final String s = "a string to capitalize";
        AbstractTask<String> instance = new CapitalizeStringTask(s);
        instance.addTaskListener(l);
        Thread t = new Thread(instance);
        t.start();
        t.join();
        assertThat(l.taskBegunCount(), is(1));
        assertThat(l.taskEndedCount(), is(1));
        assertThat(instance.get(), is(s.toUpperCase()));
    }

    /**
     * Test of removeTaskListener method, of class AbstractTask.
     */
    @Test
    public void testRemoveTaskListener() throws InterruptedException {
        System.out.println("removeTaskListener");
        TestTaskListener l = new TestTaskListener();
        AbstractTask instance = new CapitalizeStringTask("a string to capitalize");
        instance.addTaskListener(l);
        instance.removeTaskListener(l);
        Thread t = new Thread(instance);
        t.start();
        t.join();
        assertThat(l.taskBegunCount(), is(0));
        assertThat(l.taskEndedCount(), is(0));
    }

    /**
     * Test of setProgressEndpoints method, of class AbstractTask.
     */
    @Test
    public void testSetProgressEndpoints() throws InterruptedException {
        System.out.println("setProgressEndpoints");
        TestTaskListener l = new TestTaskListener();
        AbstractTask instance = new CapitalizeStringTask("a string to capitalize");
        instance.setProgressEndpoints(0.25, 0.75);
        instance.addTaskListener(l);
        Thread t = new Thread(instance);
        t.start();
        t.join();
        assertThat(l.minProgress(), is(0.25));
        assertThat(l.maxProgress(), is(0.75));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProgressEndpointBeginLessThanZero() {
        AbstractTask instance = new CapitalizeStringTask("a string to capitalize");
        instance.setProgressEndpoints(-0.25, 1.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetProgressEndpointEndLessThanBegin() {
        AbstractTask instance = new CapitalizeStringTask("a string to capitalize");
        instance.setProgressEndpoints(0.75, 0.5);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetProgressEndpointsWithNaNs() {
        AbstractTask instance = new CapitalizeStringTask("a string to capitalize");
        instance.setProgressEndpoints(Double.NaN, Double.NaN);
    }

    /**
     * Test of getBeginProgress method, of class AbstractTask.
     */
    @Test
    public void testGetBeginAndEndProgress() {
        System.out.println("getBeginProgress/getEndProgress");
        AbstractTask instance = new CapitalizeStringTask("a string to capitalize");
        instance.setProgressEndpoints(0.25, 0.75);
        assertThat(instance.getBeginProgress(), is(0.25));
        assertThat(instance.getEndProgress(), is(0.75));
    }
    
    @Test(expected=TimeoutException.class)
    public void testGetWithNoStartOfTask() throws InterruptedException, ExecutionException, TimeoutException {
        AbstractTask<String> instance = new CapitalizeStringTask("a string to capitalize");
        String result = instance.get(1L, TimeUnit.MILLISECONDS);
    }

//    /**
//     * Test of isPaused method, of class AbstractTask.
//     */
//    @Test
//    public void testIsPaused() {
//        System.out.println("isPaused");
//        AbstractTask instance = new AbstractTaskImpl();
//        boolean expResult = false;
//        boolean result = instance.isPaused();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of pause method, of class AbstractTask.
//     */
//    @Test
//    public void testPause() {
//        System.out.println("pause");
//        AbstractTask instance = new AbstractTaskImpl();
//        instance.pause();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of play method, of class AbstractTask.
//     */
//    @Test
//    public void testPlay() {
//        System.out.println("play");
//        AbstractTask instance = new AbstractTaskImpl();
//        instance.play();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of reset method, of class AbstractTask.
//     */
//    @Test
//    public void testReset() {
//        System.out.println("reset");
//        AbstractTask instance = new AbstractTaskImpl();
//        instance.reset();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of run method, of class AbstractTask.
//     */
//    @Test
//    public void testRun() {
//        System.out.println("run");
//        AbstractTask instance = new AbstractTaskImpl();
//        instance.run();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of cancel method, of class AbstractTask.
//     */
//    @Test
//    public void testCancel() {
//        System.out.println("cancel");
//        boolean mayInterruptIfRunning = false;
//        AbstractTask instance = new AbstractTaskImpl();
//        boolean expResult = false;
//        boolean result = instance.cancel(mayInterruptIfRunning);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isCancelled method, of class AbstractTask.
//     */
//    @Test
//    public void testIsCancelled() {
//        System.out.println("isCancelled");
//        AbstractTask instance = new AbstractTaskImpl();
//        boolean expResult = false;
//        boolean result = instance.isCancelled();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isDone method, of class AbstractTask.
//     */
//    @Test
//    public void testIsDone() {
//        System.out.println("isDone");
//        AbstractTask instance = new AbstractTaskImpl();
//        boolean expResult = false;
//        boolean result = instance.isDone();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of finishWithError method, of class AbstractTask.
//     */
//    @Test
//    public void testFinishWithError() {
//        System.out.println("finishWithError");
//        String errorMsg = "";
//        AbstractTask instance = new AbstractTaskImpl();
//        instance.finishWithError(errorMsg);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getErrorMessage method, of class AbstractTask.
//     */
//    @Test
//    public void testGetErrorMessage() {
//        System.out.println("getErrorMessage");
//        AbstractTask instance = new AbstractTaskImpl();
//        String expResult = "";
//        String result = instance.getErrorMessage();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getError method, of class AbstractTask.
//     */
//    @Test
//    public void testGetError() {
//        System.out.println("getError");
//        AbstractTask instance = new AbstractTaskImpl();
//        Throwable expResult = null;
//        Throwable result = instance.getError();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getProgress method, of class AbstractTask.
//     */
//    @Test
//    public void testGetProgress() {
//        System.out.println("getProgress");
//        AbstractTask instance = new AbstractTaskImpl();
//        double expResult = 0.0;
//        double result = instance.getProgress();
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of doTask method, of class AbstractTask.
//     */
//    @Test
//    public void testDoTask() throws Exception {
//        System.out.println("doTask");
//        AbstractTask instance = new AbstractTaskImpl();
//        Object expResult = null;
//        Object result = instance.doTask();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isBegun method, of class AbstractTask.
//     */
//    @Test
//    public void testIsBegun() {
//        System.out.println("isBegun");
//        AbstractTask instance = new AbstractTaskImpl();
//        boolean expResult = false;
//        boolean result = instance.isBegun();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isEnded method, of class AbstractTask.
//     */
//    @Test
//    public void testIsEnded() {
//        System.out.println("isEnded");
//        AbstractTask instance = new AbstractTaskImpl();
//        boolean expResult = false;
//        boolean result = instance.isEnded();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTaskOutcome method, of class AbstractTask.
//     */
//    @Test
//    public void testGetTaskOutcome() {
//        System.out.println("getTaskOutcome");
//        AbstractTask instance = new AbstractTaskImpl();
//        TaskOutcome expResult = null;
//        TaskOutcome result = instance.getTaskOutcome();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of postMessage method, of class AbstractTask.
//     */
//    @Test
//    public void testPostMessage() {
//        System.out.println("postMessage");
//        String msg = "";
//        AbstractTask instance = new AbstractTaskImpl();
//        instance.postMessage(msg);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of postProgress method, of class AbstractTask.
//     */
//    @Test
//    public void testPostProgress() {
//        System.out.println("postProgress");
//        double progress = 0.0;
//        AbstractTask instance = new AbstractTaskImpl();
//        instance.postProgress(progress);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of checkForCancel method, of class AbstractTask.
//     */
//    @Test
//    public void testCheckForCancel() {
//        System.out.println("checkForCancel");
//        AbstractTask instance = new AbstractTaskImpl();
//        instance.checkForCancel();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    public class CapitalizeStringTask extends AbstractTask<String> {

        private final String s;
        
        CapitalizeStringTask(String s) {
            this.s = s;
        }
        
        public String doTask() throws Exception {
            super.postProgress(super.getBeginProgress());
            StringBuilder sb = new StringBuilder();
            final int len = s.length();
            for (int i=0; i<len; i++) {
                char c = s.charAt(i);
                sb.append(Character.toUpperCase(c));
            }
            super.postProgress(super.getEndProgress());
            return sb.toString();
        }

        @Override
        public String taskName() {
            return "Test task that capitalizes a string";
        }
    }
    
    public static class TestTaskListener implements TaskListener {

        private AtomicInteger taskBegunCount = new AtomicInteger();
        private double minProgress = Double.POSITIVE_INFINITY;
        private double maxProgress = Double.NEGATIVE_INFINITY;
        private AtomicInteger taskEndedCount = new AtomicInteger();
        
        public int taskBegunCount() {
            return taskBegunCount.get();
        }
        
        public int taskEndedCount() {
            return taskEndedCount.get();
        }
        
        public double minProgress() {
            return minProgress;
        }
        
        public double maxProgress() {
            return maxProgress;
        }
        
        @Override
        public void taskBegun(TaskEvent e) {
            taskBegunCount.incrementAndGet();
        }

        @Override
        public void taskMessage(TaskEvent e) {
        }

        @Override
        public void taskProgress(TaskEvent e) {
            final double p = e.getProgress();
            if (p < minProgress) {
                minProgress = p;
            }
            if (p > maxProgress) {
                maxProgress = p;
            }
        }

        @Override
        public void taskPaused(TaskEvent e) {
        }

        @Override
        public void taskResumed(TaskEvent e) {
        }

        @Override
        public void taskEnded(TaskEvent e) {
            taskEndedCount.incrementAndGet();
        }
        
    }
    
}
