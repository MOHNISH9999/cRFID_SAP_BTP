package com.company.crfid_sap_btp.rfid;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.company.crfid_sap_btp.R;

public
class SettingsActivity2 extends BaseActivity {

    AppCompatButton buttonConnect;
    AppCompatButton buttonDisConnect;
    AppCompatTextView tvInfo;

    @Override
    protected
    void onCreate ( Bundle savedInstanceState ) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_settings2 );
        switchToBulkScanMode();
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setBackgroundDrawable(new ColorDrawable ( ContextCompat.getColor(this, R.color.colorPrimary)));
//        actionBar.setTitle( Html.fromHtml("<font color='#FFFFFF'>Settings</font>"));
        buttonConnect = findViewById(R.id.buttonConnect);
        buttonDisConnect = findViewById(R.id.buttonDisConnect);
        tvInfo = findViewById(R.id.tvInfo);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHHReader();
            }
        });
        buttonDisConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnectHHReader();
            }
        });

        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        RadioGroup radioGroup2 = findViewById(R.id.radioGroup2);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                // on below line we are getting radio button from our group.
                RadioButton radioButton = findViewById(checkedId);

                switch (radioButton.getId()){
                    case R.id.rbMin:
                        CraveConstant.READER_POWER = 0;
                        setReaderPower(0);
                        break;
                    case R.id.rbAVG:
                        CraveConstant.READER_POWER = 1;
                        setReaderPower(90);
                        break;
                    case R.id.rbMAX:
                        CraveConstant.READER_POWER = 2;
                        setReaderPower(180);
                        break;
                }

            }
        });

        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                // on below line we are getting radio button from our group.
                RadioButton radioButton = findViewById(checkedId);

                switch (radioButton.getId()){
                    case R.id.rbRFID:
                        CraveConstant.SCAN_MODE = 1;
                        changeScanningMode(1);
                        break;
                    case R.id.rbBarcode:
                        CraveConstant.SCAN_MODE = 2;
                        changeScanningMode(2);
                        break;
                }

            }
        });

        RadioButton rbMin = (RadioButton) findViewById(R.id.rbMin);
        RadioButton rbAVG = (RadioButton) findViewById(R.id.rbAVG);
        RadioButton rbMAX = (RadioButton) findViewById(R.id.rbMAX);
        switch (CraveConstant.READER_POWER){
            case 0:
                rbMin.setChecked(true);
                break;
            case 1:
                rbAVG.setChecked(true);
                break;
            case 2:
                rbMAX.setChecked(true);
                break;
        }

        RadioButton rbRFID = (RadioButton) findViewById(R.id.rbRFID);
        RadioButton rbBarcode = (RadioButton) findViewById(R.id.rbBarcode);
        switch (CraveConstant.SCAN_MODE){
            case 1:
                rbRFID.setChecked(true);
                break;
            case 2:
                rbBarcode.setChecked(true);
                break;
        }


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    protected void onBarcodeResult(String barcode) {
        onRFIDTagIDResult(barcode);
    }

    @Override
    protected void onRFIDTagSearched(short rssi) {

    }

    @Override
    protected void onRFIDTagIDResult(String tagID) {
        tvInfo.setText(tagID);
        Intent intent = new Intent("com.crave.rfid.stream");
        intent.putExtra("message", tagID);
        getApplicationContext().sendBroadcast(intent);
    }

    @Override
    protected void onFixedReaderTagIDResult(String tagID) {
        onRFIDTagIDResult(tagID);
    }
}