package org.odk.collect.naxa.scheduled.data;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.naxa.common.FieldSightFormListFragment;
import org.odk.collect.naxa.common.OnFormItemClickListener;
import org.odk.collect.naxa.common.RecyclerViewEmptySupport;
import org.odk.collect.naxa.common.SharedPreferenceUtils;
import org.odk.collect.naxa.common.event.DataSyncEvent;
import org.odk.collect.naxa.common.utilities.FlashBarUtils;
import org.odk.collect.naxa.common.ViewModelFactory;
import org.odk.collect.naxa.login.model.Site;
import org.odk.collect.naxa.scheduled.ScheduledFormsAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

import static org.odk.collect.naxa.common.Constant.EXTRA_OBJECT;
import static org.odk.collect.naxa.common.Constant.FormDeploymentFrom.PROJECT;

public class ScheduledFormsFragment extends FieldSightFormListFragment implements OnFormItemClickListener<ScheduleForm> {

    @Inject
    ViewModelFactory viewModelFactory;

    @BindView(R.id.recycler_view)
    RecyclerViewEmptySupport recyclerView;

    @BindView(R.id.root_layout_empty_layout)
    View emptyLayout;

    private Site loadedSite;
    private Unbinder unbinder;
    private ScheduledFormsAdapter scheduledFormsAdapter;
    private ScheduledFormViewModel viewModel;

    public static ScheduledFormsFragment newInstance(@NonNull Site loadedSite) {

        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_OBJECT, loadedSite);
        ScheduledFormsFragment scheduleFormListFragment = new ScheduledFormsFragment();
        scheduleFormListFragment.setArguments(bundle);
        return scheduleFormListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView =
                inflater.inflate(R.layout.scheduled_forms_list_fragment, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        ViewModelFactory factory = ViewModelFactory.getInstance(getActivity().getApplication());

        viewModel =
                ViewModelProviders.of(getActivity(), factory).get(ScheduledFormViewModel.class);

        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupListAdapter();
        viewModel.getBySiteId(true, loadedSite.getId(), loadedSite.getScheduleFormDeployedForm())
                .observe(this, new Observer<List<ScheduleForm>>() {
                    @Override
                    public void onChanged(@Nullable List<ScheduleForm> scheduleForms) {
                        scheduledFormsAdapter.updateList(scheduleForms);

                        if (!isAdded() || getActivity() == null) {
                            //Fragment is not added
                            return;
                        }
                        FlashBarUtils.showFlashbar(getActivity(),getString(R.string.msg_schedule_form_found, scheduleForms != null ? scheduleForms.size() : 0,loadedSite.getName()),false);

                    }
                });
    }



    private void setupListAdapter() {

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        scheduledFormsAdapter = new ScheduledFormsAdapter(new ArrayList<>(0), this);
        recyclerView.setEmptyView(emptyLayout,
                getString(R.string.empty_message, "general forms"),
                new RecyclerViewEmptySupport.OnEmptyLayoutClickListener() {
                    @Override
                    public void onRetryButtonClick() {
                        viewModel.getBySiteId(true, loadedSite.getId(), loadedSite.getGeneralFormDeployedFrom());
                    }
                });
        recyclerView.setAdapter(scheduledFormsAdapter);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        loadedSite = getArguments().getParcelable(EXTRA_OBJECT);
    }

    @Override
    public void onGuideBookButtonClicked(ScheduleForm scheduleForm, int position) {

    }

    @Override
    public void onFormItemClicked(ScheduleForm scheduleForm) {
        String submissionUrl = generateSubmissionUrl(PROJECT, loadedSite.getProject(), scheduleForm.getFsFormId());
        SharedPreferenceUtils.saveToPrefs(Collect.getInstance().getApplicationContext(), SharedPreferenceUtils.PREF_VALUE_KEY.KEY_URL, submissionUrl);

        fillODKForm(scheduleForm.getIdString());
    }

    @Override
    public void onFormItemLongClicked(ScheduleForm scheduleForm) {

    }

    @Override
    public void onFormHistoryButtonClicked(ScheduleForm scheduleForm) {

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