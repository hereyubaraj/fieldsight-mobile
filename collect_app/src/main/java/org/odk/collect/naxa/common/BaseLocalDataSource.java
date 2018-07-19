package org.odk.collect.naxa.common;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public interface BaseLocalDataSource<T> {

    LiveData<List<T>> getById(@NonNull String id);

    LiveData<List<T>> getAll();

    void save(T... items);

    void save(ArrayList<T> items);

    void refresh();

    void deleteAll();

    void updateAll();

}