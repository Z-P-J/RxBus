package com.zpj.rxbus;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.view.View;

import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public interface IObserver<T> {

    IObserver<T> subscribeOn(Scheduler scheduler);

    IObserver<T> observeOn(Scheduler scheduler);

    IObserver<T> bindTag(Object tag);

    IObserver<T> bindTag(Object tag, boolean disposeBefore);

    IObserver<T> bindView(View view);

    IObserver<T> bindToLife(LifecycleOwner lifecycleOwner);

    IObserver<T> bindToLife(LifecycleOwner lifecycleOwner, Lifecycle.Event event);

    IObserver<T> doOnNext(final T onNext);

    IObserver<T> doOnError(final Consumer<? super Throwable> onError);

    IObserver<T> doOnComplete(final Action onComplete);

    IObserver<T> doOnSubscribe(final Consumer<? super Disposable> onSubscribe);

    @Deprecated
    void subscribe(final T onNext);

    @Deprecated
    void subscribe(final T onNext, final Consumer<? super Throwable> onError);

    @Deprecated
    void subscribe(T onNext, Consumer<? super Throwable> onError,
                   Action onComplete);

    @Deprecated
    void subscribe(T onNext, Consumer<? super Throwable> onError,
                   Action onComplete, Consumer<? super Disposable> onSubscribe);

    void subscribe();

}
