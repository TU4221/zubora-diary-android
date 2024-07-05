package com.websarva.wings.android.zuboradiary;

import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.rxjava3.RxDataStore;

public class DataStoreSingleton {
    RxDataStore<Preferences> datastore;
    private static final DataStoreSingleton ourInstance = new DataStoreSingleton();
    public static DataStoreSingleton getInstance() {
        return ourInstance;
    }
    private DataStoreSingleton() {
    }
    public void setDataStore(RxDataStore<Preferences> datastore) {
        this.datastore = datastore;
    }
    public RxDataStore<Preferences> getDataStore() {
        return datastore;
    }
}
