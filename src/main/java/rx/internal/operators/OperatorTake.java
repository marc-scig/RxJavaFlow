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

package rx.internal.operators;

import rx.Flow.Subscriber;
import rx.Flow.Subscription;
import rx.Observable.Operator;
import rx.internal.subscriptions.AbstractSubscription;
import rx.subscribers.AbstractSubscriber;

/**
 * 
 */
public final class OperatorTake<T> implements Operator<T, T> {
    final long n;
    public OperatorTake(long n) {
        this.n = n;
    }
    @Override
    public Subscriber<? super T> apply(Subscriber<? super T> t) {
        if (n == 0) {
            AbstractSubscription.setEmptyOn(t);
            t.onComplete();
            return AbstractSubscriber.cancelled();
        }
        long r = n;
        return new Subscriber<T>() {
            boolean done;
            Subscription s;
            long remaining = r;
            @Override
            public void onSubscribe(Subscription subscription) {
                s = subscription;
                t.onSubscribe(subscription);
            }
            @Override
            public void onNext(T item) {
                if (!done) {
                    if (remaining > 0) {
                        t.onNext(item);
                        remaining--;
                    }
                    if (remaining <= 0) {
                        done = true;
                        t.onComplete();
                        s.cancel();
                    }
                }
            }
            @Override
            public void onError(Throwable throwable) {
                if (!done) {
                    t.onError(throwable);
                }
            }
            @Override
            public void onComplete() {
                if (!done) {
                    done = true;
                    t.onComplete();
                }
            }
        };
    }
}
