package io.boscoin.toknenet.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.utils.Utils;


public class RecoverActivity extends AppCompatActivity {

    private final int SEED_REQUEST_CODE = 0x0000ffff;
    private final int BOSKEY_REQUEST_CODE = 0x0000fff0;
    private EditText eSeedKey, eBosKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover);

        eSeedKey = findViewById(R.id.input_seedkey);
        eBosKey = findViewById(R.id.input_boskey);

    }



    public void readQRSeed(View view) {
        new IntentIntegrator(this).setCaptureActivity(SmallCaptureActivity.class)
                .setRequestCode(SEED_REQUEST_CODE).initiateScan();
    }

    public void readQRBosKey(View view) {
        new IntentIntegrator(this).setOrientationLocked(true).setCaptureActivity(SmallCaptureActivity.class)
                .setRequestCode(BOSKEY_REQUEST_CODE).initiateScan();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult( resultCode, data);

        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                // TODO: 2018. 4. 5. string -> byte
                switch (requestCode){
                    case SEED_REQUEST_CODE:
                        eSeedKey.setText(result.getContents());
                        break;

                    case BOSKEY_REQUEST_CODE:
                        eBosKey.setText(result.getContents());
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void recoverWallet(View view) {
        String seedKey = eSeedKey.getText().toString();
        String boskey = eBosKey.getText().toString();
        if(!seedKey.equals("") && boskey.equals("")){
            //we have seed key and check valid key
            try{
                Utils.decodeCheck(Utils.VersionByte.SEED, eSeedKey.getText().toString().toCharArray());
                Intent it = new Intent(RecoverActivity.this, CreateWalletActivity.class);
                it.putExtra(Constants.Invoke.RECOVER_WALLET, "seedkey-recover");
                it.putExtra(Constants.Invoke.KEY, seedKey);
                startActivity(it);
            } catch (Exception e){
                Toast.makeText(this, R.string.error_decode_seed_key, Toast.LENGTH_LONG).show();
                return;
            }

        } else if(seedKey.equals("") && !boskey.equals("")){
            Intent it = new Intent(RecoverActivity.this, CreateWalletActivity.class);
            it.putExtra(Constants.Invoke.RECOVER_WALLET, "boskey-recover");
            it.putExtra(Constants.Invoke.KEY, boskey);
            startActivity(it);
        } else if(!seedKey.equals("") && !boskey.equals("")){
            Toast.makeText(this, R.string.error_two_key, Toast.LENGTH_LONG).show();
            return;
        } else{
            Toast.makeText(this, R.string.error_no_key, Toast.LENGTH_LONG).show();
            return;
        }


    }
}
