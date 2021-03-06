/**
 * Copyright 2014 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.internal.schedulers;

import java.util.concurrent.TimeUnit;

import rx.disposables.*;
import rx.internal.disposables.BooleanDisposable;
import rx.schedulers.Scheduler;

/**
 * Executes work immediately on the current thread.
 */
public final class ImmediateScheduler implements Scheduler {
    static final ImmediateScheduler INSTANCE = new ImmediateScheduler();

    public static ImmediateScheduler instance() {
        return INSTANCE;
    }
    
    /* package accessible for unit tests */ImmediateScheduler() {
    }

    @Override
    public Worker createWorker() {
        return new InnerImmediateScheduler();
    }

    private final class InnerImmediateScheduler implements Scheduler.Worker {

        final BooleanDisposable innerSubscription = new BooleanDisposable();

        @Override
        public Disposable schedule(Runnable action, long delayTime, TimeUnit unit) {
            // since we are executing immediately on this thread we must cause this thread to sleep
            long execTime = now() + unit.toMillis(delayTime);

            return schedule(new SleepingRunnable(action, this, execTime));
        }

        @Override
        public Disposable schedule(Runnable action) {
            action.run();
            return Disposable.DISPOSED;
        }

        @Override
        public void dispose() {
            innerSubscription.dispose();
        }

        @Override
        public boolean isDisposed() {
            return innerSubscription.isDisposed();
        }

    }

}
