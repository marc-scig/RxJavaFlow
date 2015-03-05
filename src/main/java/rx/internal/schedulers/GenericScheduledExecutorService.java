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

import java.util.concurrent.*;

import rx.schedulers.Scheduler;

/**
 * A default {@link ScheduledExecutorService} that can be used for scheduling actions when a {@link Scheduler} implementation doesn't have that ability.
 * <p>
 * For example if a {@link Scheduler} is given an {@link Executor} or {{@link ExecutorService} instead of {@link ScheduledExecutorService}.
 * <p>
 * NOTE: No actual work should be done on tasks submitted to this executor. Submit a task with the appropriate delay which then in turn invokes
 * the work asynchronously on the appropriate {@link Scheduler} implementation. This means for example that you would not use this approach
 * along with {@link TrampolineScheduler} or {@link ImmediateScheduler}.
 */
public final class GenericScheduledExecutorService {

    private static final String THREAD_NAME_PREFIX = "RxScheduledExecutorPool-";
    private static final RxThreadFactory THREAD_FACTORY = new RxThreadFactory(THREAD_NAME_PREFIX);
    
    private final static GenericScheduledExecutorService INSTANCE = new GenericScheduledExecutorService();
    private final ScheduledExecutorService executor;

    private GenericScheduledExecutorService() {
        int count = Runtime.getRuntime().availableProcessors();
        if (count > 4) {
            count = count / 2;
        }
        // we don't need more than 8 to handle just scheduling and doing no work
        if (count > 8) {
            count = 8;
        }
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(count, THREAD_FACTORY);
        if (!NewThreadWorker.tryEnableCancelPolicy(exec)) {
            if (exec instanceof ScheduledThreadPoolExecutor) {
                NewThreadWorker.registerExecutor((ScheduledThreadPoolExecutor)exec);
            }
        }
        executor = exec;
    }

    /**
     * See class Javadoc for information on what this is for and how to use.
     * 
     * @return {@link ScheduledExecutorService} for generic use.
     */
    public static ScheduledExecutorService getInstance() {
        return INSTANCE.executor;
    }
}