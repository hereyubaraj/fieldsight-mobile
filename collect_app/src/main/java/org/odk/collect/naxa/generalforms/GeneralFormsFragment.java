package org.odk.collect.naxa.generalforms;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.rockerhieu.rvadapter.states.StatesRecyclerViewAdapter;

import org.odk.collect.android.R;
import org.odk.collect.naxa.site.FragmentHostActivity;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

public class GeneralFormsFragment extends Fragment {

    @Inject
    ViewModelFactory viewModelFactory;

    @Inject
    SyncCommentLifecycleObserver syncCommentLifecycleObserver;

    private GeneralFormViewModel viewModel;

    @BindView(R.id.android_list)
    RecyclerView recyclerView;

    @BindView(R.id.root_layout_general_form_frag)
    LinearLayout rootLayout;

    Unbinder unbinder;
    private DisplayGeneralFormsAdapter generalFormsAdapter;
    private StatesRecyclerViewAdapter statesRecyclerViewAdapter;

    public static GeneralFormsFragment newInstance() {
        return new GeneralFormsFragment();
    }

    public GeneralFormsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView =
                inflater.inflate(R.layout.general_forms_list_fragment, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        viewModel = FragmentHostActivity.obtainViewModel(getActivity());

        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupListAdapter();
        viewModel.loadGeneralForms(true)
                .observe(this, generalForms -> {
                    Timber.i("General forms data has been changed");
                    generalFormsAdapter.updateList(generalForms);

                });
    }

    private void setupListAdapter() {

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        generalFormsAdapter = new DisplayGeneralFormsAdapter(new ArrayList<>(0));

//        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_layout,rootLayout);
//        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        rootLayout.setLayoutParams(lp);


        //statesRecyclerViewAdapter = new StatesRecyclerViewAdapter(generalFormsAdapter, null, emptyView, null);
        recyclerView.setAdapter(generalFormsAdapter);
    }
}
