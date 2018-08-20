package io.boscoin.tokennet.wallet.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;

import io.boscoin.tokennet.wallet.R;

public class SendDialogFail extends Dialog {
    private View.OnClickListener mRightClickListener;

    public SendDialogFail(@NonNull Context context, View.OnClickListener rclistener) {
        super(context);
        this.mRightClickListener = rclistener;
    }

    public SendDialogFail(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected SendDialogFail(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.layout_dialog_fail);




        findViewById(R.id.btn_ok).setOnClickListener(mRightClickListener);
    }
}
