package org.bcss.collect.naxa.site;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.bcss.collect.android.R;
import org.bcss.collect.android.activities.FormEntryActivity;
import org.bcss.collect.android.activities.InstanceUploaderActivity;
import org.bcss.collect.android.application.Collect;
import org.bcss.collect.android.provider.FormsProviderAPI;
import org.bcss.collect.android.provider.InstanceProviderAPI;
import org.bcss.collect.naxa.common.DialogFactory;
import org.bcss.collect.naxa.common.FieldSightUserSession;
import org.bcss.collect.naxa.common.PaginationScrollListener;
import org.bcss.collect.naxa.login.model.Project;
import org.bcss.collect.naxa.login.model.Site;
import org.bcss.collect.naxa.login.model.SiteBuilder;
import org.bcss.collect.naxa.site.db.SiteRemoteSource;
import org.bcss.collect.naxa.site.db.SiteViewModel;
import org.bcss.collect.naxa.survey.SurveyFormsActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static org.bcss.collect.android.activities.InstanceUploaderList.INSTANCE_UPLOADER;
import static org.bcss.collect.naxa.common.Constant.EXTRA_OBJECT;

public class SiteListFragment extends Fragment implements SiteListAdapter.SiteListAdapterListener {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    Unbinder unbinder;

    private List<Site> sitelist = new ArrayList<>();
    private SiteListAdapter siteListAdapter;

    private SiteUploadActionModeCallback siteUploadActionModeCallback;
    private android.support.v7.view.ActionMode actionMode;
    private String projectId;

    private ProgressDialog DownloadProgressDialog;
    private ProgressDialog uploadProgressDialog;

    private LinearLayoutManager mLayoutManager;
    private boolean isLoading = false;

    private boolean isLastPage = false;
    private String paginationUniqueLastKey = "";
    private Project loadedProject;

    public static SiteListFragment getInstance(String projectId, Project project) {

        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_OBJECT, project);
        SiteListFragment siteListFragment = new SiteListFragment();
        siteListFragment.setArguments(bundle);
        return siteListFragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_site_list, container, false);
        unbinder = ButterKnife.bind(this, view);

        loadedProject = getArguments().getParcelable(EXTRA_OBJECT);
        setHasOptionsMenu(true);
        paginateSitesList();

        siteUploadActionModeCallback = new SiteUploadActionModeCallback();

        return view;
    }

    private void setupRecycleView() {

        siteListAdapter = new SiteListAdapter(getActivity(), sitelist, this);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(siteListAdapter);
    }

    private void setupPagination() {
        recyclerView.addOnScrollListener(new PaginationScrollListener(mLayoutManager) {
            @Override
            protected void loadMoreItems() {
                paginateSitesList();
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
    }

    private void paginateSitesList() {
        new SiteViewModel(Collect.getInstance())
                .getSiteByProject(loadedProject)
                .observe(this, sites -> {
                    Site site = new SiteBuilder().createSite();
                    site.setName("survey");
                    sitelist.add(site);
                    if (sites != null) {
                        sitelist.addAll(sites);
                    }
                    setupRecycleView();
                });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onIconClicked(int position) {
        enableActionMode(position);
    }

    private void enableActionMode(int position) {
        if (actionMode == null) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            actionMode = activity.startSupportActionMode(siteUploadActionModeCallback);
        }

        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        siteListAdapter.toggleSelection(position);
        int count = siteListAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }


    public void createFormsUploadDialog(String siteId) {

////        String dialogTitle = context.getApplicationContext().getString(R.string.dialog_title_warning_logout);
////        String dialogMsg;
////        String posMsg = context.getApplicationContext().getString(R.string.dialog_warning_logout_pos);
////        String negMsg = context.getApplicationContext().getString(R.string.dialog_warning_logout_neg);
//        @ColorInt int color =getResources().getColor(R.color.primaryColor);
//        Drawable drawable = getResources().getDrawable(android.R.drawable.ic_dialog_alert);
//        Drawable wrapped = DrawableCompat.wrap(drawable);
//        DrawableCompat.setTint(wrapped, color);
//
//
//        DialogFactory.createActionDialog(getActivity(), dialogTitle, dialogMsg).setPositiveButton(posMsg, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//
//            }
//        }).setNegativeButton(negMsg, null).setIcon(wrapped).show();

    }


    public void prepareToSendUnSentForms(String siteIdForFilter) {

        String selection = null;
        String[] selectionArgs = null;


        selection = InstanceProviderAPI.InstanceColumns.FS_SITE_ID + "=? and (" +
                InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                + InstanceProviderAPI.InstanceColumns.STATUS + "=? )";

        selectionArgs = new String[]{
                siteIdForFilter,
                InstanceProviderAPI.STATUS_COMPLETE,
                InstanceProviderAPI.STATUS_SUBMISSION_FAILED
        };


        String sortOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC";

        Cursor c = getContext().getContentResolver().query(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, selection,
                selectionArgs, sortOrder);


        try {

            long[] instanceIDs = new long[c.getCount()];
            int i = 0;

            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                instanceIDs[i] = c.getInt(c.getColumnIndex(FormsProviderAPI.FormsColumns._ID));
                i = i + 1;
            }

            uploadSelectedFiles(instanceIDs);

        } catch (Exception e) {

            e.printStackTrace();


        } finally {
            if (c != null) {
                c.close();
            }
        }

    }

    private void uploadSelectedFiles(long[] instanceIDs) {


        Intent i = new Intent(getActivity(), InstanceUploaderActivity.class);
        i.putExtra(FormEntryActivity.KEY_INSTANCES, instanceIDs);


        startActivityForResult(i, INSTANCE_UPLOADER);
    }


    @Override
    public void onRowLongClicked(int position) {
        enableActionMode(position);
    }

    @Override
    public void onUselessLayoutClicked(Site site) {
        FragmentHostActivity.start(getActivity(), site);
    }

    @Override
    public void onSurveyFormClicked() {
        SurveyFormsActivity.start(getActivity(), loadedProject);
    }

    public class SiteUploadActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_upload_items, menu);
            return true;
        }


        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete_sites:
                    return true;

                case R.id.action_upload_sites:
                    SiteRemoteSource.getInstance().create(siteListAdapter.getSelected());
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            disableActionMode();
        }
    }

    public void disableActionMode() {
        siteListAdapter.clearSelections();
        actionMode.finish();

        actionMode = null;
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                siteListAdapter.resetAnimationIndex();

                //siteListAdapter.notifyDataSetChanged();
            }
        });
    }

}
