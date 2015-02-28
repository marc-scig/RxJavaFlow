/**
 * Copyright 2015 David Karnok
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rxjf.internal.operators;

import rxjf.Flow.Subscriber;
import rxjf.schedulers.Scheduler;

/**
 * 
 */
public final class OperatorObserveOn<T> implements Operator<T, T> {
    final Scheduler scheduler;
    public OperatorObserveOn(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
    @Override
    public Subscriber<? super T> apply(Subscriber<? super T> child) {
        // TODO Auto-generated method stub
        return null;
    }
}