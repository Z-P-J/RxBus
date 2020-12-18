package com.zpj.rxbus;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.view.View;

import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

class RxMultiObserver<S, T extends Consumer<S>>
        implements IObserver<T> {

    protected final RxObserver<S> observer;

    RxMultiObserver(RxObserver<S> observer) {
        this.observer = observer;
    }

    @Override
    public IObserver<T> subscribeOn(Scheduler scheduler) {
        observer.subscribeOn(scheduler);
        return this;
    }

    @Override
    public IObserver<T> observeOn(Scheduler scheduler) {
        observer.observeOn(scheduler);
        return this;
    }

    @Override
    public IObserver<T> bindTag(Object tag) {
        observer.bindTag(tag);
        return this;
    }

    @Override
    public IObserver<T> bindTag(Object tag, boolean disposeBefore) {
        observer.bindTag(tag, disposeBefore);
        return this;
    }

    @Override
    public IObserver<T> bindView(View view) {
        observer.bindView(view);
        return this;
    }

    @Override
    public IObserver<T> bindToLife(LifecycleOwner lifecycleOwner) {
        observer.bindToLife(lifecycleOwner);
        return this;
    }

    @Override
    public IObserver<T> bindToLife(LifecycleOwner lifecycleOwner, Lifecycle.Event event) {
        observer.bindToLife(lifecycleOwner, event);
        return this;
    }

    @Override
    public IObserver<T> doOnNext(final T onNext) {
        observer.doOnNext(onNext);
        return this;
    }

    @Override
    public IObserver<T> doOnError(Consumer<? super Throwable> onError) {
        observer.doOnError(onError);
        return this;
    }

    @Override
    public IObserver<T> doOnComplete(final Action onComplete) {
        observer.doOnComplete(onComplete);
        return this;
    }

    @Override
    public IObserver<T> doOnSubscribe(final Consumer<? super Disposable> onSubscribe) {
        observer.doOnSubscribe(onSubscribe);
        return this;
    }

    @Override
    public void subscribe(T onNext) {
        observer.subscribe(onNext);
    }

    @Override
    public void subscribe(final T onNext, Consumer<? super Throwable> onError) {
        observer.subscribe(onNext, onError);
    }

    @Override
    public void subscribe(T onNext, Consumer<? super Throwable> onError, Action onComplete) {
        observer.subscribe(onNext, onError, onComplete);
    }

    @Override
    public void subscribe(final T onNext, Consumer<? super Throwable> onError, Action onComplete, Consumer<? super Disposable> onSubscribe) {
        observer.subscribe(onNext, onError, onComplete, onSubscribe);
    }

    @Override
    public void subscribe() {
        observer.subscribe();
    }

}