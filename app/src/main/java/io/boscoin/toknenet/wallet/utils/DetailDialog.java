package io.boscoin.toknenet.wallet.utils;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import io.boscoin.toknenet.wallet.R;
import io.boscoin.toknenet.wallet.model.Payments;

import static android.content.Context.CLIPBOARD_SERVICE;

public class DetailDialog extends Dialog{

    private String mDate, mType, mFrom, mTo, mAmount, mBalance;
    private TextView mTvDate, mTvType, mTvFrom, mTvTo, mTvAmount, mTvBalance;
    private Context mContext;

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
        mContext = context;
        this.mDate = Utils.convertUtcToLocal(item.getCreated_at());
        if(item.getType_i().equals("0")){
            this.mType = context.getResources().getString(R.string.create_ac);
            this.mFrom = item.getSource_account();
            this.mTo = item.getAccount();

            String tmp = Utils.fitDigit(item.getStarting_balance());
            this.mAmount =  tmp+" BOS";
        }


        else{
            if(item.getFrom().equals(myPubkey)){
                this.mType = context.getResources().getString(R.string.sent);
            }else{
                this.mType = context.getResources().getString(R.string.received);
            }
            this.mFrom = item.getFrom();
            this.mTo = item.getTo();

            String tmp = Utils.fitDigit(item.getAmount());
            this.mAmount =  tmp+" BOS";
        }

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

        mTvAmount.setText(Utils.displayBalance(mAmount));

       mTvFrom.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
               ClipData clipData = ClipData.newPlainText("From", mFrom);
               clipboard.setPrimaryClip(clipData);
               Toast.makeText(mContext, mContext.getString(R.string.toast_text_clipboard_address), Toast.LENGTH_SHORT).show();
           }
       });

       mTvTo.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
               ClipData clipData = ClipData.newPlainText("To", mTo);
               clipboard.setPrimaryClip(clipData);
               Toast.makeText(mContext, mContext.getString(R.string.toast_text_clipboard_address), Toast.LENGTH_SHORT).show();
           }
       });
    }
}
