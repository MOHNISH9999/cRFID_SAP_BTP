package com.company.crfid_sap_btp.rfid;

import android.os.Handler;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class SnackBarMaker {

    public static void snack(View content, String message, String actionText, int actionTextColor, View.OnClickListener onClick){
        Snackbar.make(content, message, Snackbar.LENGTH_LONG)
                .setAction(actionText, onClick)
                .setActionTextColor(actionTextColor)
                .show();
    }

    public static void snackWithCustomTiming(View content, String message, int duration){
        final Snackbar snackbar = Snackbar.make(content, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                snackbar.dismiss();
            }
        },duration);
    }
}