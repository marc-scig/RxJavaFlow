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

package rx.subjects;

import rx.Flow.Processor;
import rx.Flow.Subscriber;
import rx.*;

/**
 * Extends upon the Processor interface by adding some common methods.
 */
public abstract class Subject<T, R> extends Observable<R> implements Processor<T, R> {
    /**
     * Constructs a subject with the given onSubscribe function.
     * @param onSubscribe
     */
    protected Subject(OnSubscribe<R> onSubscribe) {
        super(onSubscribe);
    }
    /**
     * Indicates whether the {@link Processor} has {@link Subscriber Subscribers} subscribed to it.
     * @return true if there is at least one Subscriber subscribed to this Processor, false otherwise
     */
    public abstract boolean hasSubscribers();
    /**
     * Check if the Subject has terminated with an exception.
     * @return true if the subject has received a throwable through {@code onError}.
     */
    public abstract boolean hasThrowable();
    /**
     * Check if the Subject has terminated normally.
     * @return true if the subject completed normally via {@code onComplete()}
     */
    public abstract boolean hasComplete();
    /**
     * Returns the Throwable that terminated the Subject.
     * @return the Throwable that terminated the Subject or {@code null} if the
     * subject hasn't terminated yet or it terminated normally.
     */
    public abstract Throwable getThrowable();
    /**
     * Wraps a {@link Subject} so that it is safe to call its various {@code on} methods from different threads.
     * <p>
     * When you use an ordinary {@link Subject} as a {@link Subscriber}, you must take care not to call its 
     * {@link Subscriber#onNext} method (or its other {@code on} methods) from multiple threads, as this could 
     * lead to non-serialized calls, which violates the Observable contract and creates an ambiguity in the resulting Processor.
     * <p>
     * To protect a {@code Processor} from this danger, you can convert it into a {@code SerializedProcessor} with code
     * like the following:
     * <p><pre>{@code
     * mySafeProcessor = myUnsafeProcessor.toSerialized();
     * }</pre>
     * 
     * @return SerializedProcessor wrapping the current Processor
     */
    public final Subject<T, R> toSerialized() {
        if (this instanceof SerializedSubject) {
            return this;
        }
        return new SerializedSubject<>(this);
    }
    /**
     * Check if the Subject has a value.
     * <p>Use the {@link #getValue()} method to retrieve such a value.
     * <p>Note that unless {@link #hasCompleted()} or {@link #hasThrowable()} returns true, the value
     * retrieved by {@code getValue()} may get outdated.
     * @return true if and only if the subject has some value but not an error
     */
    public boolean hasValue() {
        throw new UnsupportedOperationException();
    }
    /**
     * Returns the current value of the Subject if there is such a value and
     * the subject hasn't terminated with an exception.
     * <p>The can return {@code null} for various reasons. Use {@link #hasValue()}, {@link #hasThrowable()}
     * and {@link #hasCompleted()} to determine if such {@code null} is a valid value, there was an
     * exception or the Subject terminated without receiving any value. 
     * @return the current value or {@code null} if the Subject doesn't have a value,
     * has terminated with an exception or has an actual {@code null} as a value.
     */
    public R getValue() {
        throw new UnsupportedOperationException();
    }
    /**
     * Returns the current number of items (non-terminal events) available for replay.
     * @return the number of items available
     */
    public int size() {
        throw new UnsupportedOperationException();
    }
    /**
     * @return true if the Processor holds at least one non-terminal event available for replay
     */
    public boolean hasAnyValue() {
        throw new UnsupportedOperationException();
    }
    /**
     * @return returns a snapshot of the currently buffered non-terminal events.
     */
    @SuppressWarnings("unchecked")
    public Object[] getValues() {
        return getValues((R[])new Object[size()]);
    }
    /**
     * Returns a snapshot of the currently buffered non-terminal events into 
     * the provided {@code a} array or creates a new array if it has not enough capacity.
     * @param a the array to fill in
     * @return the array {@code a} if it had enough capacity or a new array containing the available values 
     */
    public R[] getValues(R[] a) {
        throw new UnsupportedOperationException();
    }

}
