package org.bcss.collect.naxa.project.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.bcss.collect.android.R;
import org.bcss.collect.naxa.login.model.Project;
import org.bcss.collect.naxa.project.ProjectsDiffCallback;

import java.util.List;

import static org.bcss.collect.naxa.common.ViewUtils.loadRemoteImage;


public class MyProjectsAdapter extends RecyclerView.Adapter<MyProjectsAdapter.MyViewHolder> {

    private List<Project> myProjectList;

    private OnItemClickListener onItemClickListener;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title, organizationName;
        private ImageView ivLogo;

        private RelativeLayout rootLayout;


        private MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tv_project_list_name);
            organizationName = view.findViewById(R.id.tv_organization_name);
            ivLogo = view.findViewById(R.id.iv_org_logo);
            rootLayout = view.findViewById(R.id.project_list_item_root_layout);


            rootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(myProjectList.get(getAdapterPosition()));
                }
            });
        }
    }


    public MyProjectsAdapter(final List<Project> myProjectList, OnItemClickListener onItemClickListener) {
        this.myProjectList = myProjectList;
        this.onItemClickListener = onItemClickListener;

    }

    public void updateList(List<Project> newList) {

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ProjectsDiffCallback(newList, myProjectList));
        myProjectList.clear();
        myProjectList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.project_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Project project = myProjectList.get(holder.getAdapterPosition());

        Context context = holder.title.getContext();

        holder.title.setText(project.getName());

        loadRemoteImage(context, project.getOrganizationlogourl()).into(holder.ivLogo);


        holder.organizationName.setText(formatOrganizationName(project.getOrganizationName()));


    }

    @Override
    public int getItemCount() {
        return myProjectList.size();
    }

    private String formatOrganizationName(String name) {
        return "A Project by " + name;
    }


    public interface OnItemClickListener {
        void onItemClick(Project project);
    }

}
