package com.company.crfid_sap_btp.rfid;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.company.crfid_sap_btp.app.SAPWizardApplication;
import com.zebra.rfid.api3.TagData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

abstract public
class BaseFragment_RFID extends Fragment {

    boolean isFixedReaderScanning = false;
    SAPWizardApplication craveApplication;

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//            case R.id.action_settings:
//                onRFIDTagIDResult("8745");
////                if(isFixedReaderScanning)
////                    stopFixedReaderScan();
////                else
////                    scanFixedReader();
////                isFixedReaderScanning = !isFixedReaderScanning;
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    @Override
    public
    void onCreate ( @Nullable Bundle savedInstanceState ) {
        super.onCreate ( savedInstanceState );
        EventBus.getDefault ( ).register ( this );
        craveApplication = new SAPWizardApplication ( );

    }

    @Override
    public
    void onDestroy () {
        EventBus.getDefault ( ).unregister ( this );
        super.onDestroy ( );
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public
    void onEvent ( RFIDTriggerEvent rfidTriggerEvent ) {
        if ( rfidTriggerEvent.isPressed ( ) ) {
            ((SAPWizardApplication) getActivity (). getApplication ( )).getRFIDManager ( ).performInventory ( );
        } else {
            ((SAPWizardApplication) getActivity ().getApplication ( )).getRFIDManager ( ).stopInventory ( );
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public
    void onEvent ( BarcodeEvent barcodeEvent ) {
        onBarcodeResult ( barcodeEvent.getBarcode ( ) );
    }

    abstract protected
    void onBarcodeResult ( String barcode );

    abstract protected
    void onRFIDTagIDResult ( String tagID );

    abstract protected
    void onFixedReaderTagIDResult ( String tagID );

    abstract protected
    void onRFIDTagSearched ( short rssi );

    @Subscribe(threadMode = ThreadMode.MAIN)
    public
    void onEvent ( RFIDTagReadEvent rfidTriggerEvent ) {
        TagData[] tagDatas = rfidTriggerEvent.getTagData ( );
        if ( tagDatas != null && tagDatas.length > 0 ) {
            if ( tagDatas[ 0 ].getTagID ( ) != null )
                onRFIDTagIDResult ( tagDatas[ 0 ].getTagID ( ) );
            else
                onRFIDTagSearched ( tagDatas[ 0 ].LocationInfo != null ? tagDatas[ 0 ].LocationInfo.getRelativeDistance ( ) : 0 );
        }
//        for (int index = 0; index < tagDatas.length; index++) {
//            TagData tagData = tagDatas[index];
//            Log.d("AAAAAAAAAAA",""+tagData.getTagID());
//        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(FixedReaderEvent fixedReaderEvent) {
//        onFixedReaderTagIDResult(fixedReaderEvent.getTagid());
//    }


    void changeScanningMode ( int mode ) {  // 1 = RFID, 2 = Barcode
        AsyncTask task = new AsyncTask ( ) {
            @Override
            protected
            Object doInBackground ( Object[] objects ) {
                if ( mode == 2 )
                    craveApplication.switchToBarcodeMode ( );
                else
                    craveApplication.swichToRFIDMode ( );
                return null;
            }
        };
        task.execute ( );
    }

    void searchTag ( String tagID ) {
        ((SAPWizardApplication) getActivity ().getApplication ( )).getRFIDManager ( ).searchTag ( tagID );
    }

    void setReaderPower ( int power ) {
        craveApplication.setReaderPower ( power );
    }


    void switchToSingleScanMode () {
        ((SAPWizardApplication) getActivity ().getApplication ( )).switchToSingleScanMode ( );
    }

    void switchToBulkScanMode () {
        craveApplication.switchToBulkScanMode ( );
    }

    void connectToHHReader () {
        craveApplication.connectToHHReader ( );
    }

    void disconnectHHReader () {
        ((SAPWizardApplication) getActivity ().getApplication ( )).disconnectHHReader ( );
    }

//    void connectToFixedReader(){
//        ((CraveApplication) getApplication()).connectToFixedReader();
//    }
//    void scanFixedReader(){
//        ((CraveApplication) getApplication()).startFixedReaderScan();
//    }
//    void stopFixedReaderScan(){
//        ((CraveApplication) getApplication()).stopFixedReaderScan();
//    }
//    void disconnectFixedReader(){
//        ((CraveApplication) getApplication()).disconnectFixedReader();
//    }


    void showMessage ( String title , String message , View.OnClickListener listener ) {

        SnackBarMaker.snackWithCustomTiming ( getActivity ().getWindow ( ).getDecorView ( ).getRootView ( ) ,
                message ,
                1000 );
    }
}
