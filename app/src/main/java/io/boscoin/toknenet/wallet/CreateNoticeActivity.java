package io.boscoin.toknenet.wallet;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

public class CreateNoticeActivity extends AppCompatActivity {

    private CheckBox mCheckMin, mCheckWallet, mCheckPw, mCheckSeed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_notice);

        mCheckMin = findViewById(R.id.check_min);


        mCheckWallet = findViewById(R.id.check_name);


        mCheckPw = findViewById(R.id.check_pw);


        mCheckSeed = findViewById(R.id.check_seed);


        findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCheckMin.isChecked() && mCheckWallet.isChecked() && mCheckPw.isChecked() && mCheckSeed.isChecked()){
                    Intent it = new Intent(CreateNoticeActivity.this, CreateWalletActivity.class);

                    startActivity(it);
                }else{
                    Toast.makeText(getApplicationContext(), R.string.error_all_check, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

    }



}
