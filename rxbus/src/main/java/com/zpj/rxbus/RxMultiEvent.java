package com.zpj.rxbus;

class RxMultiEvent {

    private final String key;
    private final Object[] objects;

    RxMultiEvent(String key, Object...objects) {
        this.key = key;
        this.objects = objects;
    }

    public String getKey() {
        return key;
    }

    public Object[] getObjects() {
        return objects;
    }

    public Object getObject(int i) {
        return objects[i];
    }

}
