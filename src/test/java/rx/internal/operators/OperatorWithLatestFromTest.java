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
package rx.internal.operators;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.Test;
import org.mockito.InOrder;

import rx.Observable;
import rx.Observer;
import rx.exceptions.TestException;
import rx.functions.BiFunction;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

public class OperatorWithLatestFromTest {
    static final BiFunction<Integer, Integer, Integer> COMBINER = new BiFunction<Integer, Integer, Integer>() {
        @Override
        public Integer call(Integer t1, Integer t2) {
            return (t1 << 8) + t2;
        }
    };
    static final BiFunction<Integer, Integer, Integer> COMBINER_ERROR = new BiFunction<Integer, Integer, Integer>() {
        @Override
        public Integer call(Integer t1, Integer t2) {
            throw new TestException("Forced failure");
        }
    };
    @Test
    public void testSimple() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        
        @SuppressWarnings("unchecked")
        Observer<Integer> o = mock(Observer.class);
        InOrder inOrder = inOrder(o);
        
        Observable<Integer> result = source.withLatestFrom(other, COMBINER);
        
        result.subscribe(o);
        
        source.onNext(1);
        inOrder.verify(o, never()).onNext(anyInt());
        
        other.onNext(1);
        inOrder.verify(o, never()).onNext(anyInt());
        
        source.onNext(2);
        inOrder.verify(o).onNext((2 << 8) + 1);
        
        other.onNext(2);
        inOrder.verify(o, never()).onNext(anyInt());
        
        other.onComplete();
        inOrder.verify(o, never()).onComplete();
        
        source.onNext(3);
        inOrder.verify(o).onNext((3 << 8) + 2);
        
        source.onComplete();
        inOrder.verify(o).onComplete();
        
        verify(o, never()).onError(any(Throwable.class));
    }
    
    @Test
    public void testEmptySource() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        
        Observable<Integer> result = source.withLatestFrom(other, COMBINER);
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        
        result.subscribe(ts);

        assertTrue(source.hasObservers());
        assertTrue(other.hasObservers());

        other.onNext(1);
        
        source.onComplete();
        
        ts.assertNoErrors();
        ts.assertTerminalEvent();
        assertEquals(0, ts.getOnNextEvents().size());
        
        assertFalse(source.hasObservers());
        assertFalse(other.hasObservers());
    }
    
    @Test
    public void testEmptyOther() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        
        Observable<Integer> result = source.withLatestFrom(other, COMBINER);
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        
        result.subscribe(ts);

        assertTrue(source.hasObservers());
        assertTrue(other.hasObservers());

        source.onNext(1);
        
        source.onComplete();
        
        ts.assertNoErrors();
        ts.assertTerminalEvent();
        assertEquals(0, ts.getOnNextEvents().size());
        
        assertFalse(source.hasObservers());
        assertFalse(other.hasObservers());
    }
    
    
    @Test
    public void testUnsubscription() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        
        Observable<Integer> result = source.withLatestFrom(other, COMBINER);
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        
        result.subscribe(ts);

        assertTrue(source.hasObservers());
        assertTrue(other.hasObservers());

        other.onNext(1);
        source.onNext(1);
        
        ts.unsubscribe();
        
        ts.assertValues(((1 << 8) + 1));
        ts.assertNoErrors();
        assertEquals(0, ts.getonComplete()Events().size());
        
        assertFalse(source.hasObservers());
        assertFalse(other.hasObservers());
    }

    @Test
    public void testSourceThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        
        Observable<Integer> result = source.withLatestFrom(other, COMBINER);
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        
        result.subscribe(ts);

        assertTrue(source.hasObservers());
        assertTrue(other.hasObservers());

        other.onNext(1);
        source.onNext(1);
        
        source.onError(new TestException());
        
        ts.assertTerminalEvent();
        ts.assertValues(((1 << 8) + 1));
        assertEquals(1, ts.getOnErrorEvents().size());
        assertTrue(ts.getOnErrorEvents().get(0) instanceof TestException);
        
        assertFalse(source.hasObservers());
        assertFalse(other.hasObservers());
    }
    @Test
    public void testOtherThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        
        Observable<Integer> result = source.withLatestFrom(other, COMBINER);
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        
        result.subscribe(ts);

        assertTrue(source.hasObservers());
        assertTrue(other.hasObservers());

        other.onNext(1);
        source.onNext(1);
        
        other.onError(new TestException());
        
        ts.assertTerminalEvent();
        ts.assertValues(((1 << 8) + 1));
        assertEquals(1, ts.getOnErrorEvents().size());
        assertTrue(ts.getOnErrorEvents().get(0) instanceof TestException);
        
        assertFalse(source.hasObservers());
        assertFalse(other.hasObservers());
    }
    
    @Test
    public void testFunctionThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        
        Observable<Integer> result = source.withLatestFrom(other, COMBINER_ERROR);
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        
        result.subscribe(ts);

        assertTrue(source.hasObservers());
        assertTrue(other.hasObservers());

        other.onNext(1);
        source.onNext(1);
        
        ts.assertTerminalEvent();
        assertEquals(0, ts.getOnNextEvents().size());
        assertEquals(1, ts.getOnErrorEvents().size());
        assertTrue(ts.getOnErrorEvents().get(0) instanceof TestException);
        
        assertFalse(source.hasObservers());
        assertFalse(other.hasObservers());
    }
    
    @Test
    public void testNoDownstreamUnsubscribe() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        
        Observable<Integer> result = source.withLatestFrom(other, COMBINER);
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        
        result.unsafeSubscribe(ts);
        
        source.onComplete();
        
        assertFalse(ts.isUnsubscribed());
    }
    @Test
    public void testBackpressure() {
        Observable<Integer> source = Observable.range(1, 10);
        PublishSubject<Integer> other = PublishSubject.create();
        
        Observable<Integer> result = source.withLatestFrom(other, COMBINER);
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {
            @Override
            public void onStart() {
                request(0);
            }
        };
        
        result.subscribe(ts);
        
        ts.requestMore(1);
        
        ts.assertReceivedOnNext(Collections.<Integer>emptyList());
        
        other.onNext(1);
        
        ts.requestMore(1);
        
        ts.assertValues(((2 << 8) + 1));
        
        ts.requestMore(5);
        ts.assertValues((
                (2 << 8) + 1, (3 << 8) + 1, (4 << 8) + 1, (5 << 8) + 1, 
                (6 << 8) + 1, (7 << 8) + 1 
        ));
        
        ts.unsubscribe();
        
        assertFalse("Other has observers!", other.hasObservers());

        ts.assertNoErrors();
    }
}
