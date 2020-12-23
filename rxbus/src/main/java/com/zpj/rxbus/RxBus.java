package com.zpj.rxbus;

import android.text.TextUtils;

import com.zpj.rxlife.RxLife;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public final class RxBus {

    private static volatile RxBus INSTANCE;

    private final Subject<Object> mSubject;

    private final Map<Object, Object> mStickyEventMap;

    static RxBus get() {
        if (INSTANCE == null) {
            synchronized (RxBus.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RxBus();
                }
            }
        }
        return INSTANCE;
    }

    private RxBus() {
        mSubject = PublishSubject.create().toSerialized();
        mStickyEventMap = new ConcurrentHashMap<>();
    }

    //---------------------------------------------------------------post Event-----------------------------------------------------------

    public static void post(Object o) {
        get().mSubject.onNext(o);
    }

    public static void post(String key, Object o) {
        post(new RxMultiEvent(key, o));
    }

    public static void post(String key, Object s, Object t) {
        post(new RxMultiEvent(key, s, t));
    }

    public static void post(String key, Object r, Object s, Object t) {
        post(new RxMultiEvent(key, r, s, t));
    }

    public static void postDelayed(final Object o, long delay) {
        postDelayed(new Action() {
            @Override
            public void run() throws Exception {
                post(o);
            }
        }, delay);
    }

    public static void postDelayed(final String key, final Object o, long delay) {
        postDelayed(new Action() {
            @Override
            public void run() throws Exception {
                post(key, o);
            }
        }, delay);
    }

    public static void postDelayed(final String key, final Object s, final Object t, long delay) {
        postDelayed(new Action() {
            @Override
            public void run() throws Exception {
                post(key, s, t);
            }
        }, delay);
    }

    public static void postDelayed(final String key, final Object r, final Object s, final Object t, long delay) {
        postDelayed(new Action() {
            @Override
            public void run() throws Exception {
                post(new RxMultiEvent(key, r, s, t));
            }
        }, delay);
    }


    public static void postSticky(Object o) {
        synchronized (get().mStickyEventMap) {
            get().mStickyEventMap.put(o.getClass(), o);
        }
        post(o);
    }

    public static void postSticky(String key, Object o) {
        synchronized (get().mStickyEventMap) {
            get().mStickyEventMap.put(key, new RxMultiEvent(key, o));
        }
        post(o);
    }

    public static void postSticky(String key, Object s, Object t) {
        postSticky(key, new RxMultiEvent(key, s, t));
    }

    public static void postSticky(String key, Object r, Object s, Object t) {
        postSticky(new RxMultiEvent(key, r, s, t));
    }

    public static void postStickyDelayed(final Object o, long delay) {
        postDelayed(new Action() {
            @Override
            public void run() throws Exception {
                postSticky(o);
            }
        }, delay);
    }

    public static void postStickyDelayed(final String key, final Object o, long delay) {
        postDelayed(new Action() {
            @Override
            public void run() throws Exception {
                postSticky(key, o);
            }
        }, delay);
    }

    public static void postStickyDelayed(final String key, final Object s, final Object t, long delay) {
        postDelayed(new Action() {
            @Override
            public void run() throws Exception {
                postSticky(key, s, t);
            }
        }, delay);
    }

    public static void postStickyDelayed(final String key, final Object r, final Object s, final Object t, long delay) {
        postDelayed(new Action() {
            @Override
            public void run() throws Exception {
                postSticky(key, r, s, t);
            }
        }, delay);
    }


    public static void postDelayed(Action action, long delay) {
        Observable.timer(delay, TimeUnit.MILLISECONDS)
                .doOnComplete(action)
                .subscribe();
    }


    //--------------------------------------------------------------Observer-------------------------------------------------------------

    public static <T> IObserver<Consumer<? super T>> observe(@NonNull Object o, @NonNull Class<T> type) {
        return new RxObserver<>(o, toObservable(type));
    }

    public static IObserver<Consumer<? super String>> observe(@NonNull Object o, @NonNull String key) {
        return new RxObserver<>(o, toObservable(key));
    }

    public static <T> IObserver<SingleConsumer<? super T>> observe(@NonNull Object o, @NonNull String key, @NonNull Class<T> type) {
        return new RxMultiObserver<>(new RxObserver<>(o, get().toObservable(key, type)));
    }

    public static <S, T> IObserver<PairConsumer<? super S, ? super T>> observe(@NonNull Object o,
                                                               @NonNull String key,
                                                               @NonNull Class<S> type1,
                                                               @NonNull Class<T> type2) {
        return new RxMultiObserver<>(new RxObserver<>(o, get().toObservable(key, type1, type2)));
    }

    public static <R, S, T> IObserver<TripleConsumer<? super R, ? super S, ? super T>> observe(@NonNull Object o,
                                                                       @NonNull String key,
                                                                       @NonNull Class<R> type1,
                                                                       @NonNull Class<S> type2,
                                                                       @NonNull Class<T> type3) {
        return new RxMultiObserver<>(new RxObserver<>(o, get().toObservable(key, type1, type2, type3)));
    }

    private static <T> Observable<T> toObservable(final Class<T> type) {
        return get().mSubject.ofType(type);
    }

    private static Observable<String> toObservable(final String key) {
        return get().mSubject.ofType(String.class)
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(@NonNull String s) throws Exception {
                        return TextUtils.equals(s, key);
                    }
                })
                .map(new Function<String, Object>() {
                    @Override
                    public Object apply(@NonNull String text) throws Exception {
                        return text;
                    }
                })
                .cast(String.class)
                .compose(RxLife.<String>bindTag(key, false));
    }

    private Observable<RxMultiEvent> toObservable(final String key, final Class<?>...types) {
        return mSubject.ofType(RxMultiEvent.class)
                .filter(new Predicate<RxMultiEvent>() {
                    @Override
                    public boolean test(@NonNull RxMultiEvent msg) throws Exception {
                        boolean isSameKey = TextUtils.equals(msg.getKey(), key) && msg.getObjects().length == types.length;
                        for (int i = 0; i < types.length; i++) {
                            isSameKey = isSameKey && types[i].isInstance(msg.getObject(i));
                        }
                        return isSameKey;
                    }
                })
                .compose(RxLife.<RxMultiEvent>bindTag(key, false));
    }


    //------------------------------------------------------------------Sticky Event相关-----------------------------------------------------

    public static <T> IObserver<Consumer<? super T>> observeSticky(@NonNull Object o,
                                                                   @NonNull Class<T> type) {
        return new RxObserver<>(o, get().toObservableSticky(type));
    }

    public static IObserver<Consumer<? super String>> observeSticky(@NonNull Object o,
                                                                    @NonNull String key) {
        return new RxObserver<>(o, get().toObservableSticky(key));
    }

    public static <T> IObserver<SingleConsumer<? super T>> observeSticky(@NonNull Object o,
                                                                   @NonNull String key,
                                                                   @NonNull Class<T> type) {
        return new RxMultiObserver<>(new RxObserver<>(o, get().toObservableSticky(key, type)));
    }

    public static <S, T> IObserver<PairConsumer<S, T>> observeSticky(@NonNull Object o,
                                                                     @NonNull String key,
                                                                     @NonNull Class<S> type1,
                                                                     @NonNull Class<T> type2) {
        return new RxMultiObserver<>(new RxObserver<>(o, get().toObservableSticky(key, type1, type2)));
    }

    public static <R, S, T> IObserver<TripleConsumer<R, S, T>> observeSticky(@NonNull Object o,
                                                                             @NonNull String key,
                                                                             @NonNull Class<R> type1,
                                                                             @NonNull Class<S> type2,
                                                                             @NonNull Class<T> type3) {
        return new RxMultiObserver<>(new RxObserver<>(o, get().toObservableSticky(key, type1, type2, type3)));
    }

    private <T> Observable<T> toObservableSticky(final Class<T> eventType) {
        synchronized (mStickyEventMap) {
            Observable<T> observable = toObservable(eventType);
            final Object event = mStickyEventMap.get(eventType);

            if (event != null) {
                return observable.mergeWith(Observable.just(event).cast(eventType));
            } else {
                return observable;
            }
        }
    }

    private Observable<String> toObservableSticky(final String tag) {
        synchronized (mStickyEventMap) {
            Observable<String> observable = toObservable(tag);
            final Object event = mStickyEventMap.get(tag);

            if (event != null) {
                return observable.mergeWith(Observable.just(tag));
            } else {
                return observable;
            }
        }
    }

    private <T> Observable<RxMultiEvent> toObservableSticky(final String tag, final Class<?>...types) {
        synchronized (mStickyEventMap) {
            Observable<RxMultiEvent> observable = toObservable(tag, types);
            final Object event = mStickyEventMap.get(tag);

            if (event != null) {
                return observable.mergeWith(Observable.just(event).cast(RxMultiEvent.class));
            } else {
                return observable;
            }
        }
    }

    public static <T> T removeStickyEvent(Class<T> eventType) {
        synchronized (get().mStickyEventMap) {
            return eventType.cast(get().mStickyEventMap.remove(eventType));
        }
    }

    public static Object removeStickyEvent(String key) {
        synchronized (get().mStickyEventMap) {
            return get().mStickyEventMap.remove(key);
        }
    }

    public static <T> T removeStickyEvent(String key, Class<T> type) {
        synchronized (get().mStickyEventMap) {
            return type.cast(get().mStickyEventMap.remove(key));
        }
    }

    public static void removeAllStickyEvents() {
        synchronized (get().mStickyEventMap) {
            get().mStickyEventMap.clear();
        }
    }

    public static <T> T getStickyEvent(Class<T> event) {
        synchronized (get().mStickyEventMap) {
            return event.cast(get().mStickyEventMap.get(event));
        }
    }

    public static Object getStickyEvent(String key) {
        synchronized (get().mStickyEventMap) {
            return get().mStickyEventMap.get(key);
        }
    }

    public static <T> T getStickyEvent(String key, Class<T> event) {
        synchronized (get().mStickyEventMap) {
            return event.cast(get().mStickyEventMap.get(key));
        }
    }


    //---------------------------------------------------------------其他-----------------------------------------------------------------

    public static void removeObservers(Object o) {
        if (o == null) {
            return;
        }
        RxLife.removeByTag(o);
    }

    private static boolean hasObservers() {
        return get().mSubject.hasObservers();
    }


    //---------------------------------------------------------------Consumer-------------------------------------------------------------

    public abstract static class SingleConsumer<T> implements Consumer<RxMultiEvent> {

        @Override
        public final void accept(RxMultiEvent event) throws Exception {
            onAccept((T) event.getObjects()[0]);
        }

        public abstract void onAccept(T t) throws Exception;
    }

    public abstract static class PairConsumer<S, T> implements Consumer<RxMultiEvent> {

        @Override
        public final void accept(RxMultiEvent event) throws Exception {
            onAccept((S) event.getObjects()[0], (T) event.getObjects()[1]);
        }

        public abstract void onAccept(S s, T t) throws Exception;
    }

    public abstract static class TripleConsumer<R, S, T> implements Consumer<RxMultiEvent> {

        @Override
        public final void accept(RxMultiEvent event) throws Exception {
            R r = (R) event.getObjects()[0];
            S s = (S) event.getObjects()[1];
            T t = (T) event.getObjects()[2];
            onAccept(r, s, t);
        }

        public abstract void onAccept(R r, S s, T t) throws Exception;
    }


}
