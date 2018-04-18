package io.boscoin.toknenet.wallet.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.boscoin.toknenet.wallet.AllHistoryFragment.OnListAllFragInteractionListener;
import io.boscoin.toknenet.wallet.R;
import io.boscoin.toknenet.wallet.Utils;
import io.boscoin.toknenet.wallet.dummy.DummyContent.DummyItem;
import io.boscoin.toknenet.wallet.model.Payments;

import java.util.ArrayList;


/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListAllFragInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class AllHisViewAdapter extends RecyclerView.Adapter<AllHisViewAdapter.ViewHolder> {

    private ArrayList<Payments.PayRecords> mValues;
    private final OnListAllFragInteractionListener mListener;

    public AllHisViewAdapter(ArrayList<Payments.PayRecords> items, OnListAllFragInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_all_his_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mDateView.setText(Utils.convertUtcToLocal(mValues.get(position).getCreated_at()));
        holder.mAddressView.setText(Utils.contractionAddress(mValues.get(position).getFrom()));
        holder.mAmmountView.setText(mValues.get(position).getAmount());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.ListAllFragInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mDateView;
        public final TextView mAddressView;
        public final TextView mTypeView;
        public final TextView mAmmountView;
        public Payments.PayRecords mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDateView = (TextView) view.findViewById(R.id.tv_date);
            mAddressView = (TextView) view.findViewById(R.id.tv_address);
            mTypeView = (TextView)view.findViewById(R.id.tv_type);
            mAmmountView = (TextView)view.findViewById(R.id.tv_ammounts);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mAddressView.getText() + "'";
        }
    }
}
