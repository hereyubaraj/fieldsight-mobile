package org.bcss.collect.naxa.stages;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.bcss.collect.android.R;
import org.bcss.collect.naxa.common.OnFormItemClickListener;
import org.bcss.collect.naxa.common.RecyclerViewEmptySupport;
import org.bcss.collect.naxa.common.ViewModelFactory;
import org.bcss.collect.naxa.common.event.DataSyncEvent;
import org.bcss.collect.naxa.common.utilities.FlashBarUtils;
import org.bcss.collect.naxa.login.model.Site;
import org.bcss.collect.naxa.stages.data.Stage;
import org.bcss.collect.naxa.substages.SubStageListFragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static org.bcss.collect.naxa.common.AnimationUtils.runLayoutAnimation;
import static org.bcss.collect.naxa.common.Constant.ANIM.fragmentEnterAnimation;
import static org.bcss.collect.naxa.common.Constant.ANIM.fragmentExitAnimation;
import static org.bcss.collect.naxa.common.Constant.ANIM.fragmentPopEnterAnimation;
import static org.bcss.collect.naxa.common.Constant.ANIM.fragmentPopExitAnimation;
import static org.bcss.collect.naxa.common.Constant.EXTRA_OBJECT;

public class StageListFragment extends Fragment implements OnFormItemClickListener<Stage> {

    @Inject
    ViewModelFactory viewModelFactory;

    @BindView(R.id.android_list)
    RecyclerViewEmptySupport recyclerView;

    @BindView(R.id.root_layout_general_form_frag)
    LinearLayout rootLayout;

    private StageListAdapter listAdapter;
    private Site loadedSite;
    Unbinder unbinder;

    private StageViewModel viewModel;

    @BindView(R.id.root_layout_empty_layout)
    public RelativeLayout emptyLayout;

    public static StageListFragment newInstance(@NonNull Site loadedSite) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_OBJECT, loadedSite);
        StageListFragment scheduleListFragment = new StageListFragment();
        scheduleListFragment.setArguments(bundle);
        return scheduleListFragment;

    }

    public StageListFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadedSite = getArguments().getParcelable(EXTRA_OBJECT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView =
                inflater.inflate(R.layout.general_forms_list_fragment, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        ViewModelFactory factory = ViewModelFactory.getInstance(getActivity().getApplication());

        viewModel = ViewModelProviders.of(getActivity(), factory).get(StageViewModel.class);

        setToolbarText();
        return rootView;
    }


    private void setToolbarText() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolbar_stages);
        toolbar.setSubtitle(loadedSite.getName());
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupListAdapter();

        viewModel.getStages(false, loadedSite)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<List<Stage>>() {
                    @Override
                    public void onNext(List<Stage> stages) {
                        if (listAdapter.getItemCount() == 0) {
                            runLayoutAnimation(recyclerView);
                        }
                        listAdapter.updateList(stages);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

//        viewModel
//                .getForms(false, loadedSite)
//                .observe(this, new Observer<List<Stage>>() {
//                    @Override
//                    public void onChanged(@Nullable List<Stage> stages) {
//                        listAdapter.updateList(stages);
//                    }
//                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void setupListAdapter() {

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setEmptyView(emptyLayout, getString(R.string.empty_message, "staged forms")
                , new RecyclerViewEmptySupport.OnEmptyLayoutClickListener() {
                    @Override
                    public void onRetryButtonClick() {

                    }
                });
        listAdapter = new StageListAdapter(new ArrayList<>(0), this);
        recyclerView.setAdapter(listAdapter);
    }


    @Override
    public void onGuideBookButtonClicked(Stage stage, int position) {

    }

    @Override
    public void onFormItemClicked(Stage stage, int position) {
        Fragment fragment = SubStageListFragment.newInstance(loadedSite, stage.getId(), String.valueOf(position), stage.getFormDeployedFrom());
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(fragmentEnterAnimation, fragmentExitAnimation, fragmentPopEnterAnimation, fragmentPopExitAnimation);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack("myfrag3");
        fragmentTransaction.commit();
    }

    @Override
    public void onFormItemLongClicked(Stage stage) {

    }

    @Override
    public void onFormHistoryButtonClicked(Stage stage) {


    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DataSyncEvent event) {

        if (!isAdded() || getActivity() == null) {
            //Fragment is not added
            return;
        }


        Timber.i(event.toString());
        switch (event.getEvent()) {
            case DataSyncEvent.EventStatus.EVENT_START:
                FlashBarUtils.showFlashbar(getActivity(), getString(R.string.forms_update_start_message), true);
                break;
            case DataSyncEvent.EventStatus.EVENT_END:
                FlashBarUtils.showFlashbar(getActivity(), getString(R.string.forms_update_end_message), false);
                break;
            case DataSyncEvent.EventStatus.EVENT_ERROR:
                FlashBarUtils.showFlashbar(getActivity(), getString(R.string.forms_update_error_message), false);
                break;
        }
    }

}
