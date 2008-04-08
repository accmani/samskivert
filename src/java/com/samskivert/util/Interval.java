//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
// 
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.util;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import static com.samskivert.Log.log;

/**
 * An interface for doing operations after some delay. Allows expiration to occur on a specific
 * thread, and guarantees that any queued expiration will not run if the Interval has since been
 * cancelled or rescheduled.
 */
public abstract class Interval
{
    /**
     * An interface that will be implemented by the runnable posted to a RunQueue that can be used
     * to retrieve the original Interval.
     */
    public static interface RunBuddy extends Runnable
    {
        /**
         * Retrieve the Interval that is responsible for posting this RunBuddy to a RunQueue. Most
         * likely used to call toString() on the Interval for logging purposes.
         */
        public Interval getInterval ();
    }

    /**
     * This may be removed.
     *
     * @deprecated
     */
    @Deprecated
    public static void resetTimer ()
    {
    }

    /**
     * Create a simple interval that does not use a RunQueue to run the {@link #expired} method.
     */
    public Interval ()
    {
        this(null);
    }

    /**
     * Create an Interval that uses the specified {@link RunQueue} to run the {@link #expired}
     * method. If null is supplied the interval will be run directly on the timer thread.
     */
    public Interval (RunQueue runQueue)
    {
        _runQueue = runQueue;
    }

    /**
     * The main method where your interval should do its work.
     */
    public abstract void expired ();

    /**
     * Schedule the interval to execute once, after the specified delay.  Supersedes any previous
     * schedule that this Interval may have had.
     */
    public final void schedule (long delay)
    {
        schedule(delay, 0L);
    }

    /**
     * Schedule the interval to execute repeatedly, with the same delay.  Supersedes any previous
     * schedule that this Interval may have had.
     */
    public final void schedule (long delay, boolean repeat)
    {
        schedule(delay, repeat ? delay : 0L);
    }

    /**
     * Schedule the interval to execute repeatedly with the specified initial delay and repeat
     * delay.  Supersedes any previous schedule that this Interval may have had.
     */
    public final void schedule (long initialDelay, long repeatDelay)
    {
        cancel();
        TimerTask task = new IntervalTask();
        _task = task;

        // try twice to schedule the task- see comment inside the catch
        for (int tryCount = 0; tryCount < 2; tryCount++) {
            try {
                if (repeatDelay == 0L) {
                    _timer.schedule(task, initialDelay);
                } else {
                    _timer.scheduleAtFixedRate(task, initialDelay, repeatDelay);
                }
                return;

            } catch (IllegalStateException ise) {
                // Timer.schedule will only throw this if the TimerThead was shut down.
                // This may happen automatically in Applets, so we need to create a new
                // Timer now. Note that in a multithreaded environment it may be possible
                // to have more than one Timer, but that shouldn't be an issue.
                if (tryCount == 0) {
                    _timer = createTimer();

                } else {
                    // the second time through? Throw it!
                    throw ise;
                }
            }
        }
    }

    /**
     * Cancel the current schedule, and ensure that any expirations that are queued up but have not
     * yet run do not run.
     */
    public final void cancel ()
    {
        TimerTask task = _task;
        if (task != null) {
            _task = null;
            task.cancel();
        }
    }

    /**
     * Safely expire the interval.
     */
    protected final void safelyExpire (TimerTask task)
    {
        // only expire the interval if the task is still valid
        if (_task == task) {
            try {
                expired();
            } catch (Throwable t) {
                log.log(Level.WARNING, "Interval broken in expired() " + this, t);
            }

        } else {
            // If the task has been defanged, we go ahead and try cancelling it again. The reason
            // for this is that it's possible to have a runaway task if two threads call schedule()
            // and cancel() at the same time.
            // 1) ThreadA calls cancel() and gets a handle on taskA, yields.
            // 2) ThreadB calls schedule(), gets a handle on taskA, cancel()s, which sets _task to
            //    null, then sets up taskB, returns.
            // 3) ThreadA resumes, sets _task to null and re-cancels taskA.  taskB is now an active
            //    TimerTask but is not referenced anywhere.  In case this is taskB, we cancel it so
            //    that it doesn't ineffectually expire repeatedly until the JVM exists.
            task.cancel();
        }
    }

    protected static Timer createTimer ()
    {
        return new Timer(/*JDK1.5 "samskivert Interval Timer",*/ true);
    }

    /**
     * The task that schedules actually runs the interval.
     */
    protected class IntervalTask extends TimerTask
    {
        // documentation inherited
        public void run () {
            if (_runQueue == null) {
                safelyExpire(this);

            } else {
                if (_runner == null) { // lazy initialize _runner
                    _runner = new RunBuddy() {
                        public void run () {
                            safelyExpire(IntervalTask.this);
                        }

                        public Interval getInterval () {
                            return Interval.this;
                        }

                        public String toString () {
                            return Interval.this.toString();
                        }
                    };
                }
                try {
                    _runQueue.postRunnable(_runner);
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to execute interval on run-queue " +
                            "[queue=" + _runQueue + ", interval=" + Interval.this + "].", e);
                }
            }
        }

        /** If we are using a RunQueue, the Runnable we post to it. */
        protected RunBuddy _runner;
    }

    /** If non-null, the RunQueue used to run the expired() method for each Interval. */
    protected RunQueue _runQueue;

    /** The task that actually schedules our execution with the static Timer. */
    protected volatile TimerTask _task;

    /** The daemon timer used to schedule all intervals. */
    protected static Timer _timer = createTimer();
}
