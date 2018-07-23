package org.odk.collect.naxa.generalforms;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.odk.collect.naxa.generalforms.data.GeneralFormLocalSource;
import org.odk.collect.naxa.generalforms.data.GeneralFormRemoteSource;
import org.odk.collect.naxa.generalforms.data.GeneralFormRepository;
import org.odk.collect.naxa.scheduled.data.ScheduledFormRepository;
import org.odk.collect.naxa.scheduled.data.ScheduledFormViewModel;
import org.odk.collect.naxa.scheduled.data.ScheduledFormsLocalSource;
import org.odk.collect.naxa.scheduled.data.ScheduledFormsRemoteSource;

public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    @SuppressLint("StaticFieldLeak")
    private static volatile ViewModelFactory INSTANCE;

    private final GeneralFormRepository generalFormRepository;
    private final ScheduledFormRepository scheduledFormRepository;


    private final Application application;

    public ViewModelFactory(Application application, GeneralFormRepository repository, ScheduledFormRepository scheduledFormRepository) {
        this.application = application;
        this.generalFormRepository = repository;
        this.scheduledFormRepository = scheduledFormRepository;
    }

    public static ViewModelFactory getInstance(Application application) {

        if (INSTANCE == null) {
            synchronized (ViewModelFactory.class) {
                if (INSTANCE == null) {
                    GeneralFormRepository generalFormRepository = GeneralFormRepository.getInstance(
                            GeneralFormLocalSource.getInstance(), GeneralFormRemoteSource.getInstance());


                    ScheduledFormRepository scheduledFormRepository = ScheduledFormRepository.getInstance(
                            ScheduledFormsLocalSource.getInstance(), ScheduledFormsRemoteSource.getInstance());

                    INSTANCE = new ViewModelFactory(application, generalFormRepository, scheduledFormRepository);
                }
            }
        }
        return INSTANCE;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(GeneralFormViewModel.class)) {
            //noinspection unchecked
            return (T) new GeneralFormViewModel(application, generalFormRepository);
        } else if (modelClass.isAssignableFrom(ScheduledFormViewModel.class)) {
            //noinspection unchecked
            return (T) new ScheduledFormViewModel(scheduledFormRepository);
        }

        throw new IllegalArgumentException("Unknown ViewModel class" + modelClass.getName());
    }
}
