package io.boscoin.toknenet.wallet.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.boscoin.toknenet.wallet.R;
import io.boscoin.toknenet.wallet.ReceiveHistoryFragment.OnListReceiveFragInteractionListener;
import io.boscoin.toknenet.wallet.model.Payments;
import io.boscoin.toknenet.wallet.utils.Utils;

import java.util.ArrayList;


public class ReceiveHisViewAdapter extends RecyclerView.Adapter<ReceiveHisViewAdapter.ViewHolder> {

    private static final String TAG = "SendHisViewAdapter";
    private ArrayList<Payments.PayRecords> mValues;
    private final OnListReceiveFragInteractionListener mListener;
    private String mPubKey;
    private static final String CREATE_ACCOUNT = "create_account";

    public ReceiveHisViewAdapter(ArrayList<Payments.PayRecords> items, OnListReceiveFragInteractionListener listener, String pubkey) {
        this.mValues = items;
        this.mListener = listener;
        this.mPubKey = pubkey;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_payment_receive_item, parent, false);


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);



        if(CREATE_ACCOUNT.equals(mValues.get(position).getType())){
            if(!mValues.get(position).getFunder().equals(mPubKey)) {

                createDisplay(holder,position);
            }
        }else if(!mValues.get(position).getFrom().equals(mPubKey)){

            receiveDisplay(holder,position);

        }else if(mValues.get(position).getFrom().equals(mPubKey) && mValues.get(position).getTo().equals(mPubKey)){
            receiveDisplay(holder,position);
        }
        

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.ListReceiveFragInteraction(holder.mItem);
                }
            }
        });
    }

    private void receiveDisplay(ViewHolder holder, int pos) {
        holder.mTvType.setText(R.string.received);
        holder.mTvAddress.setText(Utils.contractionAddress(mValues.get(pos).getFrom()));

        String tmp = Utils.fitDigit(mValues.get(pos).getAmount());
        String amount = tmp+" BOS";

        holder.mTvAmount.setText(Utils.changeColorBlue(amount));
        holder.mTvTime.setText(Utils.convertUtcToLocal(mValues.get(pos).getCreated_at()));
    }

    private void createDisplay(ViewHolder holder, int pos) {
        holder.mTvType.setText(R.string.create_ac);
        holder.mTvAddress.setText(Utils.contractionAddress(mValues.get(pos).getFunder()));

        String tmp = Utils.fitDigit(mValues.get(pos).getStarting_balance());
        String amount = tmp+" BOS";

        holder.mTvAmount.setText(Utils.changeColorBlue( amount));
        holder.mTvTime.setText(Utils.convertUtcToLocal(mValues.get(pos).getCreated_at()));
    }

    @Override
    public int getItemCount() {

        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTvType, mTvAddress, mTvAmount, mTvTime;
        public final View mView;
        public Payments.PayRecords mItem;

        public ViewHolder(View view) {
            super(view);

            mView = itemView;
            mTvType = itemView.findViewById(R.id.txt_type);
            mTvAddress = itemView.findViewById(R.id.txt_address);
            mTvAmount = itemView.findViewById(R.id.txt_amount);
            mTvTime = itemView.findViewById(R.id.txt_time);
        }

    }
}
