package org.bcss.collect.naxa.project.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.bcss.collect.naxa.common.BaseRepository;
import org.bcss.collect.naxa.generalforms.data.GeneralFormRepository;
import org.bcss.collect.naxa.login.model.Project;
import org.bcss.collect.naxa.scheduled.data.ScheduleForm;

import java.util.ArrayList;
import java.util.List;

public class ProjectRepository implements BaseRepository<Project> {

    private static ProjectRepository INSTANCE = null;
    private final ProjectLocalSource localSource;
    private final ProjectSitesRemoteSource remoteSource;

    private MediatorLiveData<List<ScheduleForm>> mediatorLiveData = new MediatorLiveData<>();

    public static ProjectRepository getInstance() {
        return getInstance(ProjectLocalSource.getInstance(), ProjectSitesRemoteSource.getInstance());
    }

    public static ProjectRepository getInstance(ProjectLocalSource localSource, ProjectSitesRemoteSource remoteSource) {
        if (INSTANCE == null) {
            synchronized (GeneralFormRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ProjectRepository(localSource, remoteSource);
                }
            }
        }
        return INSTANCE;
    }


    private ProjectRepository(@NonNull ProjectLocalSource localSource, @NonNull ProjectSitesRemoteSource remoteSource) {
        this.localSource = localSource;
        this.remoteSource = remoteSource;
    }

    @Override
    public LiveData<List<Project>> getAll(boolean forceUpdate) {
        if (forceUpdate) remoteSource.getAll();
        return localSource.getAll();
    }



    @Override
    public void save(Project... items) {
        AsyncTask.execute(() -> localSource.save(items));
    }

    @Override
    public void save(ArrayList<Project> items) {
        AsyncTask.execute(() -> localSource.save(items));
    }

    @Override
    public void updateAll(ArrayList<Project> items) {
        localSource.updateAll(items);
    }


}
