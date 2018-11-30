package org.bcss.collect.naxa.sync;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.bcss.collect.naxa.common.database.BaseDaoFieldSight;
import org.bcss.collect.naxa.common.database.ProjectFilter;

import java.util.List;

import io.reactivex.Single;

@Dao
public abstract class SyncDAO implements BaseDaoFieldSight<Sync> {
    @Query("SELECT * from sync ORDER BY title DESC")
    public abstract LiveData<List<Sync>> getAll();

    @Query("UPDATE sync SET checked='1' ")
    abstract void markAllAsChecked();

    @Query("UPDATE sync SET checked='0'")
    abstract void markAllAsUnChecked();

    @Query("SELECT COUNT(checked) from sync where checked = '1'")
    abstract Single<Integer> selectedItemsCount();

    @Query("SELECT COUNT(checked) from sync where checked = '1'")
    abstract LiveData<Integer> selectedItemsCountLive();

    @Query("UPDATE sync SET checked = '0' WHERE uid=:uid")
    public abstract void markAsUnchecked(int uid);

    @Query("UPDATE sync SET checked = '1' WHERE uid=:uid")
    public abstract void markAsChecked(int uid);

    @Query("SELECT * from sync where checked = '1'")
    public abstract Single<List<Sync>> getAllChecked();

    @Query("UPDATE sync set downloadingStatus=:failed WHERE uid=:uid")
    public abstract void markSelectedAsFailed(int uid, int failed);

    @Query("UPDATE sync set downloadingStatus=:completed WHERE uid=:uid")
    public abstract void markSelectedAsCompleted(int uid, int completed);


    @Query("UPDATE sync set downloadingStatus=:running WHERE uid=:uid")
    public abstract void markSelectedAsRunning(int uid, int running);

    @Query("UPDATE sync set syncTotal=:total,syncProgress=:progress WHERE uid=:uid  ")
    public abstract void updateProgress(int uid, int total, int progress);


    @Query("SELECT COUNT(checked) from sync where downloadingStatus =:running")
    public abstract LiveData<Integer> runningItemCountLive(int running);
}