package io.boscoin.toknenet.wallet.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import io.boscoin.toknenet.wallet.R;
import io.boscoin.toknenet.wallet.model.Payments;

public class DetailDialog extends Dialog{

    private String mDate, mType, mFrom, mTo, mAmount, mBalance;
    private TextView mTvDate, mTvType, mTvFrom, mTvTo, mTvAmount, mTvBalance;

    public DetailDialog(@NonNull Context context, String date, String type, String from, String to, String amount, String bal) {
        super(context);
        this.mDate = date;
        this.mType = type;
        this.mFrom = from;
        this.mTo = to;
        this.mAmount = amount;
        this.mBalance = bal;
    }

    public DetailDialog(@NonNull Context context, Payments.PayRecords item, String myPubkey) {
        super(context);
        this.mDate = item.getCreated_at();
        if(item.getType_i().equals("0")){
            this.mType = context.getResources().getString(R.string.create_ac);
            this.mFrom = item.getFunder();
        }else{
            if(item.getFrom().equals(myPubkey)){
                this.mType = context.getResources().getString(R.string.sent);
            }else{
                this.mType = context.getResources().getString(R.string.received);
            }
            this.mFrom = item.getFrom();
        }

        this.mTo = item.getTo();
        this.mAmount = item.getAmount();

    }

    public DetailDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected DetailDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.layout_dialog_detail);

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mTvDate = findViewById(R.id.txt_daytime);
        mTvDate.setText(mDate);

        mTvType = findViewById(R.id.type_property);
        mTvType.setText(mType);

        mTvFrom = findViewById(R.id.from_key);
        mTvFrom.setText(mFrom);

        mTvTo = findViewById(R.id.to_key);
        mTvTo.setText(mTo);

        mTvAmount = findViewById(R.id.ammount);
        mTvAmount.setText(mAmount);


    }
}
