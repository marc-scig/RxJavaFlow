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
package rx.internal.operators;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.*;

import org.junit.*;
import org.mockito.InOrder;

import rx.Flow.Subscriber;
import rx.*;
import rx.Observable.OnSubscribe;
import rx.exceptions.*;
import rx.internal.subscriptions.AbstractSubscription;
import rx.subscribers.*;

public class OperatorMergeDelayErrorTest {

    @Ignore // FIXME the new merge stops sources if they error out so this test fails always
    @Test
    public void testErrorDelayed1() {
        final Observable<String> o1 = Observable.create(new TestErrorObservable("four", null, "six")); // we expect to lose "six" from the source (and it should never be sent by the source since onError was called
        final Observable<String> o2 = Observable.create(new TestErrorObservable("one", "two", "three"));

        Observable<String> m = Observable.mergeDelayError(o1, o2);
        
        @SuppressWarnings("unchecked")
        Subscriber<String> stringObserver = mock(Subscriber.class);
        TestSubscriber<String> ts = new TestSubscriber<>(stringObserver);
        
        m.subscribe(ts);

        verify(stringObserver, times(1)).onError(any(NullPointerException.class));
        verify(stringObserver, never()).onComplete();
        verify(stringObserver, times(1)).onNext("one");
        verify(stringObserver, times(1)).onNext("two");
        verify(stringObserver, times(1)).onNext("three");
        verify(stringObserver, times(1)).onNext("four");
        verify(stringObserver, times(0)).onNext("five");
        // despite not expecting it ... we don't do anything to prevent it if the source Observable keeps sending after onError
        verify(stringObserver, times(1)).onNext("six");
    }

    @Ignore // FIXME the new merge stops sources if they error out so this test fails always
    @Test
    public void testErrorDelayed2() {
        final Observable<String> o1 = Observable.create(new TestErrorObservable("one", "two", "three"));
        final Observable<String> o2 = Observable.create(new TestErrorObservable("four", null, "six")); // we expect to lose "six" from the source (and it should never be sent by the source since onError was called
        final Observable<String> o3 = Observable.create(new TestErrorObservable("seven", "eight", null));
        final Observable<String> o4 = Observable.create(new TestErrorObservable("nine"));

        Observable<String> m = Observable.mergeDelayError(o1, o2, o3, o4);
        
        @SuppressWarnings("unchecked")
        Subscriber<String> stringObserver = mock(Subscriber.class);
        TestSubscriber<String> ts = new TestSubscriber<>(stringObserver);

        m.subscribe(ts);

        verify(stringObserver, times(1)).onError(any(NullPointerException.class));
        verify(stringObserver, never()).onComplete();
        verify(stringObserver, times(1)).onNext("one");
        verify(stringObserver, times(1)).onNext("two");
        verify(stringObserver, times(1)).onNext("three");
        verify(stringObserver, times(1)).onNext("four");
        verify(stringObserver, times(0)).onNext("five");
        // despite not expecting it ... we don't do anything to prevent it if the source Observable keeps sending after onError
        verify(stringObserver, times(1)).onNext("six");
        verify(stringObserver, times(1)).onNext("seven");
        verify(stringObserver, times(1)).onNext("eight");
        verify(stringObserver, times(1)).onNext("nine");
    }

    @Test
    public void testErrorDelayed3() {
        final Observable<String> o1 = Observable.create(new TestErrorObservable("one", "two", "three"));
        final Observable<String> o2 = Observable.create(new TestErrorObservable("four", "five", "six"));
        final Observable<String> o3 = Observable.create(new TestErrorObservable("seven", "eight", null));
        final Observable<String> o4 = Observable.create(new TestErrorObservable("nine"));

        Observable<String> m = Observable.mergeDelayError(o1, o2, o3, o4);

        @SuppressWarnings("unchecked")
        Subscriber<String> stringObserver = mock(Subscriber.class);
        TestSubscriber<String> ts = new TestSubscriber<>(stringObserver);

        m.subscribe(ts);

        verify(stringObserver, times(1)).onError(any(NullPointerException.class));
        verify(stringObserver, never()).onComplete();
        verify(stringObserver, times(1)).onNext("one");
        verify(stringObserver, times(1)).onNext("two");
        verify(stringObserver, times(1)).onNext("three");
        verify(stringObserver, times(1)).onNext("four");
        verify(stringObserver, times(1)).onNext("five");
        verify(stringObserver, times(1)).onNext("six");
        verify(stringObserver, times(1)).onNext("seven");
        verify(stringObserver, times(1)).onNext("eight");
        verify(stringObserver, times(1)).onNext("nine");
    }

    @Test
    public void testErrorDelayed4() {
        final Observable<String> o1 = Observable.create(new TestErrorObservable("one", "two", "three"));
        final Observable<String> o2 = Observable.create(new TestErrorObservable("four", "five", "six"));
        final Observable<String> o3 = Observable.create(new TestErrorObservable("seven", "eight"));
        final Observable<String> o4 = Observable.create(new TestErrorObservable("nine", null));

        Observable<String> m = Observable.mergeDelayError(o1, o2, o3, o4);

        @SuppressWarnings("unchecked")
        Subscriber<String> stringObserver = mock(Subscriber.class);
        TestSubscriber<String> ts = new TestSubscriber<>(stringObserver);

        m.subscribe(ts);

        verify(stringObserver, times(1)).onError(any(NullPointerException.class));
        verify(stringObserver, never()).onComplete();
        verify(stringObserver, times(1)).onNext("one");
        verify(stringObserver, times(1)).onNext("two");
        verify(stringObserver, times(1)).onNext("three");
        verify(stringObserver, times(1)).onNext("four");
        verify(stringObserver, times(1)).onNext("five");
        verify(stringObserver, times(1)).onNext("six");
        verify(stringObserver, times(1)).onNext("seven");
        verify(stringObserver, times(1)).onNext("eight");
        verify(stringObserver, times(1)).onNext("nine");
    }

    @Test
    public void testErrorDelayed4WithThreading() {
        final TestAsyncErrorObservable o1 = new TestAsyncErrorObservable("one", "two", "three");
        final TestAsyncErrorObservable o2 = new TestAsyncErrorObservable("four", "five", "six");
        final TestAsyncErrorObservable o3 = new TestAsyncErrorObservable("seven", "eight");
        // throw the error at the very end so no onComplete will be called after it
        final TestAsyncErrorObservable o4 = new TestAsyncErrorObservable("nine", null);

        Observable<String> m = Observable.mergeDelayError(Observable.create(o1), Observable.create(o2), Observable.create(o3), Observable.create(o4));

        @SuppressWarnings("unchecked")
        Subscriber<String> stringObserver = mock(Subscriber.class);
        TestSubscriber<String> ts = new TestSubscriber<>(stringObserver);

        m.subscribe(ts);

        try {
            o1.t.join();
            o2.t.join();
            o3.t.join();
            o4.t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        verify(stringObserver, times(1)).onNext("one");
        verify(stringObserver, times(1)).onNext("two");
        verify(stringObserver, times(1)).onNext("three");
        verify(stringObserver, times(1)).onNext("four");
        verify(stringObserver, times(1)).onNext("five");
        verify(stringObserver, times(1)).onNext("six");
        verify(stringObserver, times(1)).onNext("seven");
        verify(stringObserver, times(1)).onNext("eight");
        verify(stringObserver, times(1)).onNext("nine");
        verify(stringObserver, times(1)).onError(any(NullPointerException.class));
        verify(stringObserver, never()).onComplete();
    }

    @Ignore // FIXME the new merge stops sources if they error out so this test fails always
    @Test
    public void testCompositeErrorDelayed1() {
        final Observable<String> o1 = Observable.create(new TestErrorObservable("four", null, "six")); // we expect to lose "six" from the source (and it should never be sent by the source since onError was called
        final Observable<String> o2 = Observable.create(new TestErrorObservable("one", "two", null));

        Observable<String> m = Observable.mergeDelayError(o1, o2);
        
        @SuppressWarnings("unchecked")
        Subscriber<String> stringObserver = mock(Subscriber.class);
        TestSubscriber<String> ts = new TestSubscriber<>(stringObserver);

        m.subscribe(ts);

        verify(stringObserver, times(1)).onError(any(CompositeException.class));
        verify(stringObserver, never()).onComplete();
        verify(stringObserver, times(1)).onNext("one");
        verify(stringObserver, times(1)).onNext("two");
        verify(stringObserver, times(0)).onNext("three");
        verify(stringObserver, times(1)).onNext("four");
        verify(stringObserver, times(0)).onNext("five");
        // despite not expecting it ... we don't do anything to prevent it if the source Observable keeps sending after onError
        verify(stringObserver, times(1)).onNext("six");
    }

    @Test
    public void testCompositeErrorDelayed2() {
        final Observable<String> o1 = Observable.create(new TestErrorObservable("four", null, "six")); // we expect to lose "six" from the source (and it should never be sent by the source since onError was called
        final Observable<String> o2 = Observable.create(new TestErrorObservable("one", "two", null));

        Observable<String> m = Observable.mergeDelayError(o1, o2);
        CaptureObserver w = new CaptureObserver();
        m.subscribe(w);

        assertNotNull(w.e);
        if (w.e instanceof CompositeException) {
            assertEquals(2, ((CompositeException) w.e).getExceptions().size());
            w.e.printStackTrace();
        } else {
            fail("Expecting CompositeException");
        }

    }

    /**
     * The unit tests below are from OperationMerge and should ensure the normal merge functionality is correct.
     */

    @Test
    public void testMergeObservableOfObservables() {
        final Observable<String> o1 = Observable.create(new TestSynchronousObservable());
        final Observable<String> o2 = Observable.create(new TestSynchronousObservable());

        Observable<Observable<String>> observableOfObservables = Observable.create(new Observable.OnSubscribe<Observable<String>>() {

            @Override
            public void accept(Subscriber<? super Observable<String>> subscriber) {
                // simulate what would happen in an observable
                AbstractSubscription.setEmptyOn(subscriber);
                subscriber.onNext(o1);
                subscriber.onNext(o2);
                subscriber.onComplete();
            }

        });
        Observable<String> m = Observable.mergeDelayError(observableOfObservables);
        
        @SuppressWarnings("unchecked")
        Subscriber<String> stringObserver = mock(Subscriber.class);
        TestSubscriber<String> ts = new TestSubscriber<>(stringObserver);

        m.subscribe(ts);

        verify(stringObserver, never()).onError(any(Throwable.class));
        verify(stringObserver, times(1)).onComplete();
        verify(stringObserver, times(2)).onNext("hello");
    }

    @Test
    public void testMergeArray() {
        final Observable<String> o1 = Observable.create(new TestSynchronousObservable());
        final Observable<String> o2 = Observable.create(new TestSynchronousObservable());

        Observable<String> m = Observable.mergeDelayError(o1, o2);
        
        @SuppressWarnings("unchecked")
        Subscriber<String> stringObserver = mock(Subscriber.class);
        TestSubscriber<String> ts = new TestSubscriber<>(stringObserver);

        m.subscribe(ts);

        verify(stringObserver, never()).onError(any(Throwable.class));
        verify(stringObserver, times(2)).onNext("hello");
        verify(stringObserver, times(1)).onComplete();
    }

    @Test
    public void testMergeList() {
        final Observable<String> o1 = Observable.create(new TestSynchronousObservable());
        final Observable<String> o2 = Observable.create(new TestSynchronousObservable());
        List<Observable<String>> listOfObservables = new ArrayList<>();
        listOfObservables.add(o1);
        listOfObservables.add(o2);

        Observable<String> m = Observable.mergeDelayError(Observable.from(listOfObservables));
        
        @SuppressWarnings("unchecked")
        Subscriber<String> stringObserver = mock(Subscriber.class);
        TestSubscriber<String> ts = new TestSubscriber<>(stringObserver);

        m.subscribe(ts);

        verify(stringObserver, never()).onError(any(Throwable.class));
        verify(stringObserver, times(1)).onComplete();
        verify(stringObserver, times(2)).onNext("hello");
    }

    @Test
    public void testMergeArrayWithThreading() {
        final TestASynchronousObservable o1 = new TestASynchronousObservable();
        final TestASynchronousObservable o2 = new TestASynchronousObservable();

        Observable<String> m = Observable.mergeDelayError(Observable.create(o1), Observable.create(o2));
        
        @SuppressWarnings("unchecked")
        Subscriber<String> stringObserver = mock(Subscriber.class);
        TestSubscriber<String> ts = new TestSubscriber<>(stringObserver);

        m.subscribe(ts);

        try {
            o1.t.join();
            o2.t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        verify(stringObserver, never()).onError(any(Throwable.class));
        verify(stringObserver, times(2)).onNext("hello");
        verify(stringObserver, times(1)).onComplete();
    }

    @Test(timeout=1000L)
    public void testSynchronousError() {
        final Observable<Observable<String>> o1 = Observable.error(new RuntimeException("unit test"));

        final CountDownLatch latch = new CountDownLatch(1);
        Observable.mergeDelayError(o1).subscribe(new AbstractSubscriber<String>() {
            @Override
            public void onComplete() {
                fail("Expected onError path");
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }

            @Override
            public void onNext(String s) {
                fail("Expected onError path");
            }
        });

        try {
            latch.await();
        } catch (InterruptedException ex) {
            fail("interrupted");
        }
    }

    private static class TestSynchronousObservable implements Observable.OnSubscribe<String> {

        @Override
        public void accept(Subscriber<? super String> subscriber) {
            AbstractSubscription.setEmptyOn(subscriber);
            subscriber.onNext("hello");
            subscriber.onComplete();
        }
    }

    private static class TestASynchronousObservable implements Observable.OnSubscribe<String> {
        Thread t;

        @Override
        public void accept(final Subscriber<? super String> subscriber) {
            t = new Thread(() -> {
                AbstractSubscription.setEmptyOn(subscriber);
                subscriber.onNext("hello");
                subscriber.onComplete();
            });
            t.start();
        }
    }

    private static class TestErrorObservable implements Observable.OnSubscribe<String> {

        String[] valuesToReturn;

        TestErrorObservable(String... values) {
            valuesToReturn = values;
        }

        @Override
        public void accept(Subscriber<? super String> subscriber) {
            AbstractSubscription.setEmptyOn(subscriber);
            boolean errorThrown = false;
            for (String s : valuesToReturn) {
                if (s == null) {
                    System.out.println("throwing exception");
                    subscriber.onError(new NullPointerException());
                    errorThrown = true;
                    // purposefully not returning here so it will continue calling onNext
                    // so that we also test that we handle bad sequences like this
                } else {
                    subscriber.onNext(s);
                }
            }
            if (!errorThrown) {
                subscriber.onComplete();
            }
        }
    }

    private static class TestAsyncErrorObservable implements Observable.OnSubscribe<String> {

        String[] valuesToReturn;

        TestAsyncErrorObservable(String... values) {
            valuesToReturn = values;
        }

        Thread t;

        @Override
        public void accept(final Subscriber<? super String> subscriber) {
            t = new Thread(() -> {
                AbstractSubscription.setEmptyOn(subscriber);
                for (String s : valuesToReturn) {
                    if (s == null) {
                        System.out.println("throwing exception");
                        try {
                            Thread.sleep(100);
                        } catch (Throwable e) {

                        }
                        subscriber.onError(new NullPointerException());
                        return;
                    } else {
                        subscriber.onNext(s);
                    }
                }
                System.out.println("subscription complete");
                subscriber.onComplete();
            });
            t.start();
        }
    }

    private static class CaptureObserver extends AbstractSubscriber<String> {
        volatile Throwable e;

        @Override
        public void onComplete() {

        }

        @Override
        public void onError(Throwable e) {
            this.e = e;
        }

        @Override
        public void onNext(String args) {

        }

    }
    @Ignore // FIXME Reactive-Streams spec prohibits throwing from onXXX methods
    @Test
    public void testMergeSourceWhichDoesntPropagateExceptionBack() {
        Observable<Integer> source = Observable.create(new OnSubscribe<Integer>() {
            @Override
            public void accept(Subscriber<? super Integer> subscriber) {
                AbstractSubscription.setEmptyOn(subscriber);
                try {
                    subscriber.onNext(0);
                } catch (Throwable swallow) {
                    
                }
                subscriber.onNext(1);
                subscriber.onComplete();
            }
        });
        
        Observable<Integer> result = Observable.mergeDelayError(source, Observable.just(2));
        
        @SuppressWarnings("unchecked")
        final Subscriber<Integer> o = mock(Subscriber.class);
        InOrder inOrder = inOrder(o);
        
        result.unsafeSubscribe(new AbstractSubscriber<Integer>() {
            int calls;
            @Override
            public void onNext(Integer t) {
                if (calls++ == 0) {
                    throw new TestException();
                }
                o.onNext(t);
            }

            @Override
            public void onError(Throwable e) {
                o.onError(e);
            }

            @Override
            public void onComplete() {
                o.onComplete();
            }
            
        });
        
        inOrder.verify(o).onNext(2);
        inOrder.verify(o, never()).onNext(0);
        inOrder.verify(o, never()).onNext(1);
        inOrder.verify(o, never()).onNext(anyInt());
        inOrder.verify(o).onError(any(TestException.class));
        verify(o, never()).onComplete();
    }

    @Test
    public void testErrorInParentObservable() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Observable.mergeDelayError(
                Observable.just(Observable.just(1), Observable.just(2))
                        .startWith(Observable.<Integer> error(new RuntimeException()))
                ).subscribe(ts);
        ts.awaitTerminalEvent();
        ts.assertTerminalEvent();
        ts.assertValues(Arrays.asList(1, 2));
        assertEquals(1, ts.getErrors().size());

    }

    @Test
    public void testErrorInParentObservableDelayed() throws Exception {
        for (int i = 0; i < 50; i++) {
            final TestASynchronous1sDelayedObservable o1 = new TestASynchronous1sDelayedObservable();
            final TestASynchronous1sDelayedObservable o2 = new TestASynchronous1sDelayedObservable();
            Observable<Observable<String>> parentObservable = Observable.create(new Observable.OnSubscribe<Observable<String>>() {
                @Override
                public void accept(Subscriber<? super Observable<String>> op) {
                    AbstractSubscription.setEmptyOn(op);
                    op.onNext(Observable.create(o1));
                    op.onNext(Observable.create(o2));
                    op.onError(new NullPointerException("throwing exception in parent"));
                }
            });
    
            @SuppressWarnings("unchecked")
            Subscriber<String> stringObserver = mock(Subscriber.class);
            
            TestSubscriber<String> ts = new TestSubscriber<>(stringObserver);
            Observable<String> m = Observable.mergeDelayError(parentObservable);
            m.subscribe(ts);
            System.out.println("testErrorInParentObservableDelayed | " + i);
            ts.awaitTerminalEvent(2000, TimeUnit.MILLISECONDS);
            ts.assertTerminalEvent();
    
            verify(stringObserver, times(2)).onNext("hello");
            verify(stringObserver, times(1)).onError(any(NullPointerException.class));
            verify(stringObserver, never()).onComplete();
        }
    }

    private static class TestASynchronous1sDelayedObservable implements Observable.OnSubscribe<String> {
        Thread t;

        @Override
        public void accept(final Subscriber<? super String> subscriber) {
            t = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        subscriber.onError(e);
                    }
                    AbstractSubscription.setEmptyOn(subscriber);
                    subscriber.onNext("hello");
                    subscriber.onComplete();
                }

            });
            t.start();
        }
    }
}