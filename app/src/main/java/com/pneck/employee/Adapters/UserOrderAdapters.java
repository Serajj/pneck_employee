package com.pneck.employee.Adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pneck.employee.R;
import com.pneck.employee.ViewHolders.UserOrderViewHolder;
import com.pneck.employee.models.UserOrderModel;
import java.util.ArrayList;

public class UserOrderAdapters extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int ITEM_VIEW_BASIC_TYPE = 1;

    private ArrayList<UserOrderModel> itemsList;
    private Activity mContext;
    private boolean value;


    public UserOrderAdapters(Activity context, ArrayList<UserOrderModel> itemsList) {
        this.itemsList = itemsList;
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        RecyclerView.ViewHolder vh;
        if (i == ITEM_VIEW_BASIC_TYPE) {

            View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.individual_user_order_layout, viewGroup, false);
            vh = new UserOrderViewHolder(v);

        }
        else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.progress_item, viewGroup, false);
            vh = new ProgressViewHolder(v);
        }

        return vh;

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder rholder, final int i) {

        if (rholder instanceof UserOrderViewHolder){
            final UserOrderViewHolder holder;
            holder = (UserOrderViewHolder) rholder;
            holder.setUpCardView(holder, mContext, itemsList.get(i),i);
        }
        else if (rholder instanceof ProgressViewHolder){
            /*if (!value) {
                ((ProgressViewHolder) rholder).progressBar.setVisibility(View.VISIBLE);
                CustomProgressLoading.StartOnlyProgress(((ProgressViewHolder) rholder).progressBar);
            } else ((ProgressViewHolder) rholder).progressBar.setVisibility(View.GONE);*/
        }


    }


    @Override
    public int getItemViewType(int position) {
       return ITEM_VIEW_BASIC_TYPE;
    }


    @Override
    public int getItemCount() {
        return (null != itemsList ? itemsList.size() : 0);
    }

    public interface ClickAdapterListener {


        void onRowLongClicked(int position);
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ImageView progressBar;

        public ProgressViewHolder(View v) {
            super(v);
           /* progressBar = (ImageView) v.findViewById(R.id.loading_progress_bar);
            CustomProgressLoading.StartOnlyProgress(progressBar);*/
        }
    }

}