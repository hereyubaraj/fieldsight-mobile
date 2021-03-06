package org.bcss.collect.naxa.project.data;

import org.bcss.collect.android.application.Collect;
import org.bcss.collect.naxa.common.BaseRemoteDataSource;
import org.bcss.collect.naxa.common.Constant;
import org.bcss.collect.naxa.common.GSONInstance;
import org.bcss.collect.naxa.common.SharedPreferenceUtils;
import org.bcss.collect.naxa.common.event.DataSyncEvent;
import org.bcss.collect.naxa.data.source.local.FieldSightNotificationLocalSource;
import org.bcss.collect.naxa.login.model.MeResponse;
import org.bcss.collect.naxa.login.model.MySites;
import org.bcss.collect.naxa.login.model.Project;
import org.bcss.collect.naxa.network.APIEndpoint;
import org.bcss.collect.naxa.network.ApiInterface;
import org.bcss.collect.naxa.network.ServiceGenerator;
import org.bcss.collect.naxa.site.data.SiteRegion;
import org.bcss.collect.naxa.site.db.SiteLocalSource;
import org.bcss.collect.naxa.site.db.SiteRemoteSource;
import org.bcss.collect.naxa.site.db.SiteRepository;
import org.bcss.collect.naxa.sync.DisposableManager;
import org.bcss.collect.naxa.sync.SyncLocalSource;
import org.bcss.collect.naxa.sync.SyncRepository;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class ProjectSitesRemoteSource implements BaseRemoteDataSource<MeResponse> {
    private static ProjectSitesRemoteSource INSTANCE;
    private SiteRepository siteRepository;
    private ProjectLocalSource projectLocalSource;
    private SyncRepository syncRepository;

    public static ProjectSitesRemoteSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProjectSitesRemoteSource();
        }
        return INSTANCE;
    }

    public ProjectSitesRemoteSource() {
        siteRepository = SiteRepository.getInstance(SiteLocalSource.getInstance(), SiteRemoteSource.getInstance());
        projectLocalSource = ProjectLocalSource.getInstance();
        syncRepository = SyncRepository.getInstance();
    }


    private Single<List<Object>> fetchProjectAndSites() {
        return ServiceGenerator.getRxClient()
                .create(ApiInterface.class)
                .getUser()
                .flatMap(new Function<MeResponse, ObservableSource<MySiteResponse>>() {
                    @Override
                    public ObservableSource<MySiteResponse> apply(MeResponse meResponse) {

                        if (!meResponse.getData().getIsSupervisor()) {
                            throw new BadUserException(meResponse.getData().getFullName() + " has not been assigned as a site supervisor.");
                        }

                        String user = GSONInstance.getInstance().toJson(meResponse.getData());
                        SharedPreferenceUtils.saveToPrefs(Collect.getInstance(), SharedPreferenceUtils.PREF_KEY.USER, user);

                        return getPageAndNext(APIEndpoint.GET_MY_SITES);

                    }
                })
                .concatMap(new Function<MySiteResponse, Observable<MySiteResponse>>() {
                    @Override
                    public Observable<MySiteResponse> apply(MySiteResponse mySiteResponse) {
                        return Observable.just(mySiteResponse);
                    }
                })
                .map(new Function<MySiteResponse, List<MySites>>() {
                    @Override
                    public List<MySites> apply(MySiteResponse mySiteResponse) {

                        return mySiteResponse.getResult();
                    }
                })

                .flatMapIterable((Function<List<MySites>, Iterable<MySites>>) mySites -> mySites)
                .map(new Function<MySites, Project>() {
                    @Override
                    public Project apply(MySites mySites) {
                        siteRepository.saveSitesAsVerified(mySites.getSite(), mySites.getProject());
                        return mySites.getProject();
                    }
                })
                .toList()
                .map(new Function<List<Project>, Set<Project>>() {
                    @Override
                    public Set<Project> apply(List<Project> projects) {
                        ArrayList<Project> uniqueList = new ArrayList<>();
                        ArrayList<String> projectIds = new ArrayList<>();
                        for (Project project : projects) {
                            if (!projectIds.contains(project.getId())) {
                                projectIds.add(project.getId());
                                uniqueList.add(project);
                            }
                        }

                        return new HashSet<Project>(uniqueList);
                    }
                })
                .toObservable()
                .flatMapIterable(new Function<Set<Project>, Iterable<Project>>() {
                    @Override
                    public Iterable<Project> apply(Set<Project> projects) {

                        return projects;
                    }
                })
                .flatMap(new Function<Project, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Project project) {
                        projectLocalSource.save(project);

                        Observable<Project> siteRegionObservable = ServiceGenerator.getRxClient().create(ApiInterface.class)
                                .getRegionsByProjectId(project.getId())
                                .flatMap(new Function<List<SiteRegion>, ObservableSource<Project>>() {
                                    @Override
                                    public ObservableSource<Project> apply(List<SiteRegion> siteRegions) {
                                        siteRegions.add(new SiteRegion("", "Unassigned ", ""));
                                        String value = GSONInstance.getInstance().toJson(siteRegions);
                                        ProjectLocalSource.getInstance().updateSiteClusters(project.getId(), value);
                                        return Observable.just(project);
                                    }
                                });

                        return Observable.concat(siteRegionObservable, Observable.just("demo"));
                    }
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }


    private Observable<MySiteResponse> getPageAndNext(String url) {
        return ServiceGenerator.getRxClient().create(ApiInterface.class)
                .getAssignedSites(url)
                .concatMap(new Function<MySiteResponse, ObservableSource<MySiteResponse>>() {
                    @Override
                    public ObservableSource<MySiteResponse> apply(MySiteResponse mySiteResponse) {
                        if (mySiteResponse.getNext() == null) {
                            return Observable.just(mySiteResponse);
                        }

                        return Observable.just(mySiteResponse)
                                .concatWith(getPageAndNext(mySiteResponse.getNext())
                                );
                    }
                });


    }


    @Override
    public void getAll() {
        int uid = Constant.DownloadUID.PROJECT_SITES;

        Single<List<Object>> observable = fetchProjectAndSites();


        DisposableObserver<List<Object>> dis = observable.toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnDispose(new Action() {
                    @Override
                    public void run() {
                        SyncLocalSource.getINSTANCE()
                                .markAsFailed(Constant.DownloadUID.PROJECT_SITES);
                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) {

                        ProjectLocalSource.getInstance().deleteAll();
                        SiteLocalSource.getInstance().deleteSyncedSitesAsync();
                        EventBus.getDefault().post(new DataSyncEvent(uid, DataSyncEvent.EventStatus.EVENT_START));
                        SyncRepository.getInstance().showProgress(Constant.DownloadUID.PROJECT_SITES);
                        SyncLocalSource.getINSTANCE()
                                .markAsRunning(Constant.DownloadUID.PROJECT_SITES);
                    }
                })
                .subscribeWith(new DisposableObserver<List<Object>>() {
                    @Override
                    public void onNext(List<Object> objects) {
                        EventBus.getDefault().post(new DataSyncEvent(uid, DataSyncEvent.EventStatus.EVENT_END));
                        FieldSightNotificationLocalSource.getInstance().markSitesAsRead();
                        syncRepository.setSuccess(Constant.DownloadUID.PROJECT_SITES);

                        SyncLocalSource.getINSTANCE()
                                .markAsCompleted(Constant.DownloadUID.PROJECT_SITES);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        EventBus.getDefault().post(new DataSyncEvent(uid, DataSyncEvent.EventStatus.EVENT_ERROR));
                        syncRepository.setError(Constant.DownloadUID.PROJECT_SITES);
                        String message = e.getMessage();
                        SyncLocalSource.getINSTANCE().markAsFailed(Constant.DownloadUID.PROJECT_SITES,message);



                    }

                    @Override
                    public void onComplete() {

                    }
                });

        DisposableManager.add(dis);

    }


}
