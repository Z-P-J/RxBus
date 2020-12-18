package com.zpj.rxbus;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.view.View;

import com.zpj.rxlife.LifecycleTransformer;
import com.zpj.rxlife.RxLife;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;

class RxObserver<T> implements IObserver<Consumer<? super T>> {

    private final Observable<T> observable;
    private Scheduler subscribeScheduler = Schedulers.io();
    private Scheduler observeScheduler = AndroidSchedulers.mainThread();

    private List<LifecycleTransformer<T>> composerList;

    private Consumer<? super T> onNext = Functions.emptyConsumer();
    private Consumer<? super Throwable> onError = Functions.ON_ERROR_MISSING;
    private Action onComplete = Functions.EMPTY_ACTION;
    private Consumer<? super Disposable> onSubscribe = Functions.emptyConsumer();

    RxObserver(Object o, Observable<T> observable) {
        this.observable = observable;
        this.composerList = new ArrayList<>();
        this.composerList.add(RxLife.<T>bindTag(o, false));
//        if (o instanceof LifecycleOwner) {
//            this.composerList.add(RxLife.<T>bindLifeOwner((LifecycleOwner) o));
//        }
    }

    @Override
    public IObserver<Consumer<? super T>> subscribeOn(Scheduler scheduler) {
        this.subscribeScheduler = scheduler;
        return this;
    }

    @Override
    public IObserver<Consumer<? super T>> observeOn(Scheduler scheduler) {
        this.observeScheduler = scheduler;
        return this;
    }

    @Override
    public IObserver<Consumer<? super T>> bindTag(Object tag) {
        return bindTag(tag, false);
    }

    @Override
    public IObserver<Consumer<? super T>> bindTag(Object tag, boolean disposeBefore) {
        this.composerList.add(RxLife.<T>bindTag(tag, disposeBefore));
        return this;
    }

    @Override
    public IObserver<Consumer<? super T>> bindView(View view) {
        this.composerList.add(RxLife.<T>bindView(view));
        return this;
    }

    @Override
    public IObserver<Consumer<? super T>> bindToLife(LifecycleOwner lifecycleOwner) {
        this.composerList.add(RxLife.<T>bindLifeOwner(lifecycleOwner));
        return this;
    }

    @Override
    public IObserver<Consumer<? super T>> bindToLife(LifecycleOwner lifecycleOwner, Lifecycle.Event event) {
        this.composerList.add(RxLife.<T>bindLifeOwner(lifecycleOwner, event));
        return this;
    }

    @Override
    public IObserver<Consumer<? super T>> doOnNext(Consumer<? super T> onNext) {
        this.onNext = onNext;
        return this;
    }

    @Override
    public IObserver<Consumer<? super T>> doOnError(Consumer<? super Throwable> onError) {
        this.onError = onError;
        return this;
    }

    @Override
    public IObserver<Consumer<? super T>> doOnComplete(Action onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    @Override
    public IObserver<Consumer<? super T>> doOnSubscribe(Consumer<? super Disposable> onSubscribe) {
        this.onSubscribe = onSubscribe;
        return this;
    }

    @Override
    public void subscribe(Consumer<? super T> onNext) {
        subscribe(onNext, onError, onComplete, onSubscribe);
    }

    @Override
    public void subscribe(Consumer< ? super T> onNext, Consumer<? super Throwable> onError) {
        subscribe(onNext, onError, onComplete, onSubscribe);
    }

    @Override
    public void subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
                          Action onComplete) {
        subscribe(onNext, onError, onComplete, onSubscribe);
    }

    @Override
    public void subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
                          Action onComplete, Consumer<? super Disposable> onSubscribe) {
        if (subscribeScheduler == null) {
            subscribeScheduler = Schedulers.io();
        }
        if (observeScheduler == null) {
            observeScheduler = AndroidSchedulers.mainThread();
        }

        Observable<T> observable = this.observable
                .subscribeOn(subscribeScheduler)
                .observeOn(observeScheduler);

        for (LifecycleTransformer<T> composer : composerList) {
            observable = observable.compose(composer);
        }
        this.composerList.clear();
        this.composerList = null;

        Disposable disposable = observable.subscribe(onNext, onError, onComplete, onSubscribe);
    }

    @Override
    public void subscribe() {
        subscribe(onNext, onError, onComplete, onSubscribe);
    }

}