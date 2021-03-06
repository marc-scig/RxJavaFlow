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
package rx.subjects;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.Test;
import org.mockito.*;

import rx.Flow.Subscriber;
import rx.*;
import rx.disposables.Disposable;
import rx.exceptions.*;
import rx.subscribers.*;

public class PublishSubjectTest {

    @Test
    public void testCompleted() {
        PublishSubject<String> subject = PublishSubject.create();

        @SuppressWarnings("unchecked")
        Subscriber<String> observer = mock(Subscriber.class);
        subject.subscribe(observer);

        subject.onNext("one");
        subject.onNext("two");
        subject.onNext("three");
        subject.onComplete();

        @SuppressWarnings("unchecked")
        Subscriber<String> anotherSubscriber = mock(Subscriber.class);
        subject.subscribe(anotherSubscriber);

        subject.onNext("four");
        subject.onComplete();
        subject.onError(new Throwable());

        assertCompletedSubscriber(observer);
        // todo bug?            assertNeverSubscriber(anotherSubscriber);
    }

    @Test
    public void testCompletedStopsEmittingData() {
        PublishSubject<Object> channel = PublishSubject.create();
        @SuppressWarnings("unchecked")
        Subscriber<Object> observerA = mock(Subscriber.class);
        @SuppressWarnings("unchecked")
        Subscriber<Object> observerB = mock(Subscriber.class);
        @SuppressWarnings("unchecked")
        Subscriber<Object> observerC = mock(Subscriber.class);

        Disposable a = channel.subscribeDisposable(observerA);
        channel.subscribe(observerB);

        InOrder inOrderA = inOrder(observerA);
        InOrder inOrderB = inOrder(observerB);
        InOrder inOrderC = inOrder(observerC);

        channel.onNext(42);

        inOrderA.verify(observerA).onNext(42);
        inOrderB.verify(observerB).onNext(42);

        a.dispose();
        inOrderA.verifyNoMoreInteractions();

        channel.onNext(4711);

        inOrderB.verify(observerB).onNext(4711);

        channel.onComplete();

        inOrderB.verify(observerB).onComplete();

        channel.subscribe(observerC);

        inOrderC.verify(observerC).onComplete();

        channel.onNext(13);

        inOrderB.verifyNoMoreInteractions();
        inOrderC.verifyNoMoreInteractions();
    }

    private void assertCompletedSubscriber(Subscriber<String> observer) {
        verify(observer, times(1)).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, times(1)).onNext("three");
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void testError() {
        PublishSubject<String> subject = PublishSubject.create();

        @SuppressWarnings("unchecked")
        Subscriber<String> observer = mock(Subscriber.class);
        subject.subscribe(observer);

        subject.onNext("one");
        subject.onNext("two");
        subject.onNext("three");
        subject.onError(testException);

        @SuppressWarnings("unchecked")
        Subscriber<String> anotherSubscriber = mock(Subscriber.class);
        subject.subscribe(anotherSubscriber);

        subject.onNext("four");
        subject.onError(new Throwable());
        subject.onComplete();

        assertErrorSubscriber(observer);
        // todo bug?            assertNeverSubscriber(anotherSubscriber);
    }

    private void assertErrorSubscriber(Subscriber<String> observer) {
        verify(observer, times(1)).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, times(1)).onNext("three");
        verify(observer, times(1)).onError(testException);
        verify(observer, Mockito.never()).onComplete();
    }

    @Test
    public void testSubscribeMidSequence() {
        PublishSubject<String> subject = PublishSubject.create();

        @SuppressWarnings("unchecked")
        Subscriber<String> observer = mock(Subscriber.class);
        subject.subscribe(observer);

        subject.onNext("one");
        subject.onNext("two");

        assertObservedUntilTwo(observer);

        @SuppressWarnings("unchecked")
        Subscriber<String> anotherSubscriber = mock(Subscriber.class);
        subject.subscribe(anotherSubscriber);

        subject.onNext("three");
        subject.onComplete();

        assertCompletedSubscriber(observer);
        assertCompletedStartingWithThreeSubscriber(anotherSubscriber);
    }

    private void assertCompletedStartingWithThreeSubscriber(Subscriber<String> observer) {
        verify(observer, Mockito.never()).onNext("one");
        verify(observer, Mockito.never()).onNext("two");
        verify(observer, times(1)).onNext("three");
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void testUnsubscribeFirstSubscriber() {
        PublishSubject<String> subject = PublishSubject.create();

        @SuppressWarnings("unchecked")
        Subscriber<String> observer = mock(Subscriber.class);
        Disposable subscription = subject.subscribeDisposable(observer);

        subject.onNext("one");
        subject.onNext("two");

        subscription.dispose();
        assertObservedUntilTwo(observer);

        @SuppressWarnings("unchecked")
        Subscriber<String> anotherSubscriber = mock(Subscriber.class);
        subject.subscribe(anotherSubscriber);

        subject.onNext("three");
        subject.onComplete();

        assertObservedUntilTwo(observer);
        assertCompletedStartingWithThreeSubscriber(anotherSubscriber);
    }

    private void assertObservedUntilTwo(Subscriber<String> observer) {
        verify(observer, times(1)).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, Mockito.never()).onNext("three");
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, Mockito.never()).onComplete();
    }

    @Test
    public void testNestedSubscribe() {
        final PublishSubject<Integer> s = PublishSubject.create();

        final AtomicInteger countParent = new AtomicInteger();
        final AtomicInteger countChildren = new AtomicInteger();
        final AtomicInteger countTotal = new AtomicInteger();

        final ArrayList<String> list = new ArrayList<>();

        s.flatMap(new Function<Integer, Observable<String>>() {
            // Note: turing this into a lambda causes a type-inference bug and String is lost.
            @Override
            public Observable<String> apply(Integer v) {
                    countParent.incrementAndGet();

                    // then subscribe to subject again (it will not receive the previous value)
                    return s.map(v2 -> {
                            countChildren.incrementAndGet();
                            return "Parent: " + v + " Child: " + v2;
                    });
            }
        }).subscribe(v -> {
            countTotal.incrementAndGet();
            list.add(v);
        });

        for (int i = 0; i < 10; i++) {
            s.onNext(i);
        }
        s.onComplete();

        //            System.out.println("countParent: " + countParent.get());
        //            System.out.println("countChildren: " + countChildren.get());
        //            System.out.println("countTotal: " + countTotal.get());

        // 9+8+7+6+5+4+3+2+1+0 == 45
        assertEquals(45, list.size());
    }

    /**
     * Should be able to unsubscribe all Subscribers, have it stop emitting, then subscribe new ones and it start emitting again.
     */
    @Test
    public void testReSubscribe() {
        final PublishSubject<Integer> ps = PublishSubject.create();

        @SuppressWarnings("unchecked")
        Subscriber<Integer> o1 = mock(Subscriber.class);
        Disposable s1 = ps.subscribeDisposable(o1);

        // emit
        ps.onNext(1);

        // validate we got it
        InOrder inOrder1 = inOrder(o1);
        inOrder1.verify(o1, times(1)).onNext(1);
        inOrder1.verifyNoMoreInteractions();

        // unsubscribe
        s1.dispose();

        // emit again but nothing will be there to receive it
        ps.onNext(2);

        @SuppressWarnings("unchecked")
        Subscriber<Integer> o2 = mock(Subscriber.class);
        Disposable s2 = ps.subscribeDisposable(o2);

        // emit
        ps.onNext(3);

        // validate we got it
        InOrder inOrder2 = inOrder(o2);
        inOrder2.verify(o2, times(1)).onNext(3);
        inOrder2.verifyNoMoreInteractions();

        s2.dispose();
    }

    private final Throwable testException = new Throwable();

    @Test(timeout = 1000)
    public void testUnsubscriptionCase() {
        PublishSubject<String> src = PublishSubject.create();

        for (int i = 0; i < 10; i++) {
            @SuppressWarnings("unchecked")
            final Subscriber<Object> o = mock(Subscriber.class);
            InOrder inOrder = inOrder(o);
            String v = "" + i;
            System.out.printf("Turn: %d%n", i);
            src.first()
                .flatMap(t1 -> Observable.just(t1 + ", " + t1))
                .subscribe(new AbstractSubscriber<String>() {
                    @Override
                    public void onNext(String t) {
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
            src.onNext(v);
            
            inOrder.verify(o).onNext(v + ", " + v);
            inOrder.verify(o).onComplete();
            verify(o, never()).onError(any(Throwable.class));
        }
    }
    
    
    @Test
    public void testOnErrorThrowsDoesntPreventDelivery() {
        PublishSubject<String> ps = PublishSubject.create();

        ps.subscribe();
        TestSubscriber<String> ts = new TestSubscriber<>();
        ps.subscribe(ts);

        try {
            ps.onError(new RuntimeException("an exception"));
            fail("expect OnErrorNotImplementedException");
        } catch (OnErrorNotImplementedException e) {
            // ignore
        }
        // even though the onError above throws we should still receive it on the other subscriber 
        ts.assertError();
    }
    
    /**
     * This one has multiple failures so should get a CompositeException
     */
    @Test
    public void testOnErrorThrowsDoesntPreventDelivery2() {
        PublishSubject<String> ps = PublishSubject.create();

        ps.subscribe();
        ps.subscribe();
        TestSubscriber<String> ts = new TestSubscriber<>();
        ps.subscribe(ts);
        ps.subscribe();
        ps.subscribe();
        ps.subscribe();

        try {
            ps.onError(new RuntimeException("an exception"));
            fail("expect OnErrorNotImplementedException");
        } catch (CompositeException e) {
            // we should have 5 of them
            assertEquals(5, e.getExceptions().size());
        }
        // even though the onError above throws we should still receive it on the other subscriber
        ts.assertError();
    }
    @Test
    public void testCurrentStateMethodsNormal() {
        PublishSubject<Object> as = PublishSubject.create();
        
        assertFalse(as.hasThrowable());
        assertFalse(as.hasComplete());
        assertNull(as.getThrowable());
        
        as.onNext(1);
        
        assertFalse(as.hasThrowable());
        assertFalse(as.hasComplete());
        assertNull(as.getThrowable());
        
        as.onComplete();
        
        assertFalse(as.hasThrowable());
        assertTrue(as.hasComplete());
        assertNull(as.getThrowable());
    }
    
    @Test
    public void testCurrentStateMethodsEmpty() {
        PublishSubject<Object> as = PublishSubject.create();
        
        assertFalse(as.hasThrowable());
        assertFalse(as.hasComplete());
        assertNull(as.getThrowable());
        
        as.onComplete();
        
        assertFalse(as.hasThrowable());
        assertTrue(as.hasComplete());
        assertNull(as.getThrowable());
    }
    @Test
    public void testCurrentStateMethodsError() {
        PublishSubject<Object> as = PublishSubject.create();
        
        assertFalse(as.hasThrowable());
        assertFalse(as.hasComplete());
        assertNull(as.getThrowable());
        
        as.onError(new TestException());
        
        assertTrue(as.hasThrowable());
        assertFalse(as.hasComplete());
        assertTrue(as.getThrowable() instanceof TestException);
    }
    
    @Test
    public void testOnSubscribeCalled() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        
        PublishSubject<Object> as = PublishSubject.create();
        
        as.subscribe(ts);
        
        ts.assertSubscription();
    }
    
    // FIXME: RS is adamant about on onNext unless requested
    @Test
    public void testIgnoresBackpressure() {
        TestSubscriber<Object> ts = new TestSubscriber<>(0);
        
        PublishSubject<Object> as = PublishSubject.create();
        
        as.subscribe(ts);
        
        as.onNext(1);
        
        ts.assertValues(1);
    }
}
