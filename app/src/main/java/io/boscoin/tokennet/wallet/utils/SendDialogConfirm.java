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


public class SendDialogConfirm extends Dialog {

    private TextView mBtnSendOk;
    private String mPubKey, mAmount, mTotal;
    private TextView mTvPubKey, mTvAmount, mTvtotal;
    private View.OnClickListener mRightClickListener;

    public SendDialogConfirm(@NonNull Context context , String pubKey, String amount, String total, View.OnClickListener rclistener) {
        super(context);
        this.mPubKey = pubKey;
        this.mAmount = amount;
        this.mTotal = total;
        this.mRightClickListener = rclistener;
    }

    public SendDialogConfirm(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected SendDialogConfirm(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.layout_dialog_confirm);


        findViewById(R.id.btn_send_ok).setOnClickListener(mRightClickListener);

        mTvPubKey = findViewById(R.id.pubkey);
        mTvPubKey.setText(mPubKey);

        mTvAmount = findViewById(R.id.ammount);
        mTvAmount.setText(mAmount);

        mTvtotal = findViewById(R.id.total);

        mTvtotal.setText(mTotal + " BOS");
    }
}
