# RxBus

[![](https://img.shields.io/badge/platform-android-brightgreen.svg)](https://developer.android.com/index.html) [![API](https://img.shields.io/badge/API-19+-blue.svg?style=flat-square)](https://developer.android.com/about/versions/android-4.0.html)[![License](http://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0)

#### Event Bus By RxJava. 使用RxJava实现的事件发布/订阅框架，替代EventBus，支持Sticky Event,支持生命周期管理。

## 一、安装

#### Latest Version：[![Download](https://api.bintray.com/packages/z-p-j/maven/RxBus/images/download.svg?version-1.0.0)](https://bintray.com/z-p-j/maven/RxBus/1.0.0/link)
```groovy
    // RxJava2
    implementation 'io.reactivex.rxjava2:rxjava:2.2.17'
	// RxAndroid
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
    // RxLife
    implementation 'com.zpj.rxbus:RxBus:latest_version'

```
## 二、使用（[查看 demo](https://github.com/Z-P-J/RxLife/tree/master/app)）

### 1. 订阅和发送自定义Event类型事件

```java
    // 订阅自定义的Event类型事件
    RxBus.observe(object, Event.class)
        .bindTag("TAG") // 绑定TAG，可根据该TAG移除订阅
        .bindView(tvText) // 绑定View，当View销毁时自动取消订阅
        .bindToLife(this, Lifecycle.Event.ON_PAUSE) // 绑定Activity/Fragment生命周期
        .doOnNext(new Consumer<Event>() {
            @Override
            public void accept(Event event) throws Exception {
                // 接收Event类型的信息
            }
        })
        .doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                // 订阅出错了
            }
        })
        .doOnComplete(new Action() {
            @Override
            public void run() throws Exception {
                // 取消订阅
            }
        })
        .doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                // 开始订阅
            }
        })
        .subscribe();

    // 发送Event类型事件
    RxBus.post(new Event());
```

### 2. 订阅和发送Key事件

```java
    // 订阅Key事件
    RxBus.observe(object, "Key")
        .doOnNext(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                // 收到信息
            }
        })
        .subscribe();
    // 发送Key事件
    RxBus.post("Key");
```

### 3. 订阅和发送Key and Event类型事件

```java
    // 订阅者1：订阅Key1和Event1类型事件
    RxBus.observe(object, "Key1", Event1.class)
        .doOnNext(new Consumer<Event1>() {
            @Override
            public void accept(Event1 event) throws Exception {
                // 收到event1
            }
        })
        .subscribe();

    // 订阅者2：订阅Key2和Event1类型事件
    RxBus.observe(object, "Key2", Event1.class)
        .doOnNext(new Consumer<Event1>() {
            @Override
            public void accept(Event1 event) throws Exception {
                // 收到event1
            }
        })
        .subscribe();

    // 订阅者3：订阅Key1和Event2类型事件
    RxBus.observe(object, "Key1", Event2.class)
        .doOnNext(new Consumer<Event2>() {
            @Override
            public void accept(Event2 event) throws Exception {
                // 收到event2
            }
        })
        .subscribe();

    // 发送Key and Event事件
    RxBus.post("Key1", new Event1()); // 订阅者1将收到事件
    RxBus.post("Key2", new Event1()); // 订阅者2将收到事件
    RxBus.post("Key1", new Event2()); // 订阅者3将收到事件
```

### 4. 传送两或三个参数，减少自定义Event类

```java
    // 当我们要同时传送两个参数时我们无需自定义新的Event类，可直接使用以下方法
    RxBus.observe(object, "Key", Boolean.class, Integer.class)
        .doOnNext(new RxBus.PairConsumer<Boolean, Integer>() {
            @Override
            public void accept(Boolean b, Integer i) {
                // 收到信息: b=true and i=100
            }
        })
        .subscribe();
    // 发送TAG事件
    RxBus.post("Key", true, 100);

	// 当我们要同时传送三个参数时我们无需自定义新的Event类，可直接使用以下方法
    RxBus.observe(object, "Key", String.class, Boolean.class, Double.class)
        .doOnNext(new RxBus.TripleConsumer<String, Boolean, Integer>() {
            @Override
            public void accept(String s, Boolean b, Double d) {
                // 收到信息: s="msg" and b=false and d=100.0
            }
        })
        .subscribe();
    // 发送数据
    RxBus.post("Key", "msg", false, 100.0);
	// 注意：若发送以下数据上面的订阅者将接收不到，第三个参数必须是Double类型才能接收到
	RxBus.post("Key", "msg", false, 100);
```

### 5. Sticky Event

```java
    /*
    支持Sticky事件，只需调用RxBus.withSticky和RxBus.postSticky，其他用法同上
    */

    // 订阅Sticky事件
    RxBus.observeSticky(...)

    // 发送Sticky事件
    RxBus.postSticky(...);

	// 获取Sticky事件
	RxBus.getStickyEvent("key");
	RxBus.getStickyEvent(clazz);
	RxBus.getStickyEvent("key", clazz);

	// 移除Sticky事件
	RxBus.removeStickyEvent("key");
	RxBus.removeStickyEvent(clazz);
	RxBus.removeStickyEvent("key", clazz);
	RxBus.removeAllStickyEvents();
```

### 6. 生命周期管理

```java
    /*
    订阅者1、2和3都依赖于object1，订阅者4依赖于object2
    */

    // 订阅者1
    RxBus.observe(object1, "Key1", Event1.class)
        .bindTag("TAG1") // 绑定TAG，可根据该TAG移除订阅
        .bindView(bindView) // 绑定View，当View销毁时自动取消订阅
        .bindToLife(this, Lifecycle.Event.ON_PAUSE) // 绑定Activity/Fragment生命周期
        .bindToLife(this) // 绑定Activity/Fragment生命周期，默认为onDestroy时移除取消订阅
        .subscribe();

    // 订阅者2
    RxBus.observe(object1, "Key2", Event1.class)
        .bindTag("TAG2")
        .subscribe();

    // 订阅者3
    RxBus.observe(object1, "Key1", Event2.class)
        .bindTag("TAG3")
        .subscribe();

    // 订阅者4
    RxBus.observe(object2, "Key1", Event2.class)
        .bindTag("TAG3")
        .subscribe();

    // 移除bindView，当bindView调用onViewDetachedFromWindow时将自动取消订阅者1
    view.removeView(bindView); 

    // 当Activity/Fragment onPause时将自动取消订阅者1
    onPause();
	
	// 根据依赖的Object取消订阅
    RxBus.removeObservers(object1); // 订阅者1、订阅者2和订阅者3都会被取消
    RxBus.removeObservers(object2); // 只取消订阅者4
	// 根据TAG取消订阅
    RxBus.removeObservers("TAG1"); // 只取消订阅者1
    RxBus.removeObservers("TAG2"); // 只取消订阅者2
    RxBus.removeObservers("TAG3"); // 将取消订阅者3和订阅者4
	// 根据Key取消订阅
	RxBus.removeObservers("Key1"); // 将取消订阅者1、订阅者3和订阅者4
    RxBus.removeObservers("Key2"); // 只取消订阅者2
```

## 三、License

```
   Copyright 2019 Z-P-J

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
