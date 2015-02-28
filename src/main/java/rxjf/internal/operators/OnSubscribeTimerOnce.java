/**
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package rxjf.internal.operators;

import java.util.concurrent.TimeUnit;

import rxjf.Flow.Subscriber;
import rxjf.schedulers.Scheduler;
import rxjf.subscribers.CancellableSubscriber;

/**
 * Timer that emits a single 0L and completes after the specified time.
 * @see <a href='http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.timer.aspx'>MSDN Observable.Timer</a>
 */
public final class OnSubscribeTimerOnce implements OnSubscribe<Long> {
    final long time;
    final TimeUnit unit;
    final Scheduler scheduler;

    public OnSubscribeTimerOnce(long time, TimeUnit unit, Scheduler scheduler) {
        this.time = time;
        this.unit = unit;
        this.scheduler = scheduler;
    }

    @Override
    public void accept(final Subscriber<? super Long> child) {
        Scheduler.Worker worker = scheduler.createWorker();
        CancellableSubscriber<? super Long> cs = CancellableSubscriber.wrap(child);
        cs.add(worker);
        worker.schedule(() -> {
            try {
                cs.onNext(0L);
            } catch (Throwable t) {
                cs.onError(t);
                return;
            }
            cs.onComplete();
        }, time, unit);
    }
    
}