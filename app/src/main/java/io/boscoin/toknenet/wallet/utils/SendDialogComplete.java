package io.boscoin.toknenet.wallet.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import io.boscoin.toknenet.wallet.R;

public class SendDialogComplete extends Dialog{
    private String mTotal;
    private TextView mTvAmmount, mTvTitle;
    private View.OnClickListener mRightClickListener;


    public SendDialogComplete(@NonNull Context context, String total, View.OnClickListener rclistener) {
        super(context);

        this.mTotal = total;
        this.mRightClickListener = rclistener;

    }

    public SendDialogComplete(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected SendDialogComplete(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.layout_dialog_complete);

        mTvAmmount = findViewById(R.id.total_val);
        mTvAmmount.setText(mTotal + " BOS");



       findViewById(R.id.btn_ok).setOnClickListener(mRightClickListener);
    }
}
