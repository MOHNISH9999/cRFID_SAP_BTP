package com.company.crfid_sap_btp.rfid;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.SL_FLAG;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.TriggerInfo;

import java.util.ArrayList;

public
class RFIDManager implements Readers.RFIDReaderEventHandler {

    final static String TAG = "RFID_SAMPLE";
    // RFID Reader
    private static Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    // In case of RFD8500 change reader name with intended device below from list of paired RFD8500
    String readername1 = "RFD8500";
    String readername2 = "MC3300";
    String readername3 = "RFD40";
    ResponseHandlerInterface responseHandlerInterface;
    private EventHandler eventHandler;
    private Context context;
    // general
    private int MAX_POWER = 270;

    public
    void onCreate ( Context activity , ResponseHandlerInterface responseHandlerInterface ) {
        // application context
        context = activity;
        this.responseHandlerInterface = responseHandlerInterface;
        // SDK
        InitSDK ( );
    }


    public
    boolean isReaderConnected () {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter ( );
        if ( mBluetoothAdapter == null ) {
            // Device does not support Bluetooth
            return false;
        } else if ( !mBluetoothAdapter.isEnabled ( ) ) {
            // Bluetooth is not enabled :)
            return false;
        } else {
            if ( reader != null && reader.isConnected ( ) )
                return true;
            else {
                Log.d ( TAG ,
                        "reader is not connected" );
                return false;
            }
        }
    }

    //
    //  Activity life cycle behavior
    //

    public
    String onResume () {
        return connect ( );
    }

    public
    void onPause () {
        disconnect ( );
    }

    public
    void onDestroy () {
        dispose ( );
    }

    //
    // RFID SDK
    //

    protected
    void InitSDK () {
        Log.d ( TAG ,
                "InitSDK" );
        if ( readers == null ) {
            new CreateInstanceTask ( ).execute ( );
        } else
            new ConnectionTask ( ).execute ( );
    }

    private synchronized
    void GetAvailableReader () throws Exception {
        Log.d ( TAG ,
                "GetAvailableReader" );
        if ( readers != null ) {
            readers.attach ( this );
            if ( readers.GetAvailableRFIDReaderList ( ) != null ) {
                availableRFIDReaderList = readers.GetAvailableRFIDReaderList ( );
                if ( availableRFIDReaderList.size ( ) != 0 ) {
                    // if single reader is available then connect it
                    if ( availableRFIDReaderList.size ( ) == 1 ) {
                        readerDevice = availableRFIDReaderList.get ( 0 );
                        reader = readerDevice.getRFIDReader ( );
                    } else {
                        // search reader specified by name
                        for (ReaderDevice device : availableRFIDReaderList) {
                            if ( device.getName ( ).contains ( readername1 ) || device.getName ( ).contains ( readername2 ) || device.getName ( ).contains ( readername3 ) ) {
                                readerDevice = device;
                                reader = readerDevice.getRFIDReader ( );
                            }
                        }
                    }
                }
            }
        }
    }

    // handler for receiving reader appearance events
    @Override
    public
    void RFIDReaderAppeared ( ReaderDevice readerDevice ) {
        Log.d ( TAG ,
                "RFIDReaderAppeared " + readerDevice.getName ( ) );
        new ConnectionTask ( ).execute ( );
    }

    @Override
    public
    void RFIDReaderDisappeared ( ReaderDevice readerDevice ) {
        Log.d ( TAG ,
                "RFIDReaderDisappeared " + readerDevice.getName ( ) );
        if ( readerDevice.getName ( ).equals ( reader.getHostName ( ) ) )
            disconnect ( );
    }

    private synchronized
    String connect () {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter ( );
        if ( mBluetoothAdapter == null ) {
            // Device does not support Bluetooth
        } else if ( !mBluetoothAdapter.isEnabled ( ) ) {
            // Bluetooth is not enabled :)
        } else {
            // Bluetooth is enabled
            if ( reader != null ) {
                Log.d ( TAG ,
                        "connect " + reader.getHostName ( ) );
                try {
                    if ( !reader.isConnected ( ) ) {
                        // Establish connection to the RFID Reader
                        reader.connect ( );
                        ConfigureReader ( );
                        return "Connected";
                    }
                }
                catch ( InvalidUsageException e ) {
                    e.printStackTrace ( );
                }
                catch ( OperationFailureException e ) {
                    e.printStackTrace ( );
                    Log.d ( TAG ,
                            "OperationFailureException " + e.getVendorMessage ( ) );
                    String des = e.getResults ( ).toString ( );
                    return "Connection failed" + e.getVendorMessage ( ) + " " + des;
                }
            }
        }
        return "";
    }

    private
    void ConfigureReader () {
        Log.d ( TAG ,
                "ConfigureReader " + reader.getHostName ( ) );
        if ( reader.isConnected ( ) ) {
            TriggerInfo triggerInfo = new TriggerInfo ( );
            triggerInfo.StartTrigger.setTriggerType ( START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE );
            triggerInfo.StopTrigger.setTriggerType ( STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE );
//            triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_TAG_OBSERVATION_WITH_TIMEOUT);
//            triggerInfo.StopTrigger.TagObservation.setN((short) 1);
            try {
                // receive events from reader
                if ( eventHandler == null )
                    eventHandler = new EventHandler ( );
                reader.Events.addEventsListener ( eventHandler );
                // HH event
                reader.Events.setHandheldEvent ( true );
                // tag event with tag data
                reader.Events.setTagReadEvent ( true );
                reader.Events.setAttachTagDataWithReadEvent ( false );
                // set trigger mode as rfid so scanner beam will not come
                reader.Config.setTriggerMode ( ENUM_TRIGGER_MODE.RFID_MODE ,
                        true );
                // set start and stop triggers
                reader.Config.setStartTrigger ( triggerInfo.StartTrigger );
                reader.Config.setStopTrigger ( triggerInfo.StopTrigger );
                // power levels are index based so maximum power supported get the last one
                MAX_POWER = reader.ReaderCapabilities.getTransmitPowerLevelValues ( ).length - 1;
                // set antenna configurations
                Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig ( 1 );
                config.setTransmitPowerIndex ( 180 );
                config.setrfModeTableIndex ( 0 );
                config.setTari ( 0 );
                reader.Config.Antennas.setAntennaRfConfig ( 1 ,
                        config );
                // Set the singulation control
                Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl ( 1 );
                s1_singulationControl.setSession ( SESSION.SESSION_S0 );// Set the singulation control to S0 which will read each tag multiple times
//                s1_singulationControl.setSession(SESSION.SESSION_S2);// Set the singulation control to S2 which will read each tag once only
                s1_singulationControl.Action.setInventoryState ( INVENTORY_STATE.INVENTORY_STATE_A );
                s1_singulationControl.Action.setSLFlag ( SL_FLAG.SL_ALL );
                reader.Config.Antennas.setSingulationControl ( 1 ,
                        s1_singulationControl );
                // delete any prefilters
                reader.Actions.PreFilters.deleteAll ( );
                //
            }
            catch ( InvalidUsageException | OperationFailureException e ) {
                e.printStackTrace ( );
            }
        }
    }

    private synchronized
    void disconnect () {
        Log.d ( TAG ,
                "disconnect " + reader );
        try {
            if ( reader != null ) {
                reader.Events.removeEventsListener ( eventHandler );
                reader.disconnect ( );
            }
        }
        catch ( InvalidUsageException e ) {
            e.printStackTrace ( );
        }
        catch ( OperationFailureException e ) {
            e.printStackTrace ( );
        }
        catch ( Exception e ) {
            e.printStackTrace ( );
        }
    }

    private synchronized
    void dispose () {
        try {
            if ( readers != null ) {
                reader = null;
                readers.Dispose ( );
                readers = null;
            }
        }
        catch ( Exception e ) {
            e.printStackTrace ( );
        }
    }

    public
    void changeMode () {
        try {
            reader.Config.setTriggerMode ( ENUM_TRIGGER_MODE.BARCODE_MODE ,
                    true );
        }
        catch ( InvalidUsageException e ) {
            e.printStackTrace ( );
        }
        catch ( OperationFailureException e ) {
            e.printStackTrace ( );
        }

    }

    public synchronized
    void performInventory () {
        // check reader connection
        if ( !isReaderConnected ( ) )
            return;
        try {
            reader.Actions.Inventory.perform ( );
        }
        catch ( InvalidUsageException e ) {
            e.printStackTrace ( );
        }
        catch ( OperationFailureException e ) {
            e.printStackTrace ( );
        }
    }

    public synchronized
    void searchTag ( String tagID ) {
        if ( isReaderConnected ( ) ) {
            try {
                reader.Actions.TagLocationing.Perform ( tagID ,
                        null ,
                        null );
            }
            catch ( InvalidUsageException e ) {
                e.printStackTrace ( );
            }
            catch ( OperationFailureException e ) {
                e.printStackTrace ( );
            }
        }
    }

    public synchronized
    void stopInventory () {
        // check reader connection
        if ( !isReaderConnected ( ) )
            return;
        try {
            reader.Actions.Inventory.stop ( );
        }
        catch ( InvalidUsageException e ) {
            e.printStackTrace ( );
        }
        catch ( OperationFailureException e ) {
            e.printStackTrace ( );
        }
    }

    public
    void changeToRFIDMode () {
        ConfigureReader ( );
    }

    public
    void connectToHHReader () {
        InitSDK ( );
    }

    public
    void disconnectHHReader () {
        try {
            reader.disconnect ( );
        }
        catch ( InvalidUsageException e ) {
            e.printStackTrace ( );
        }
        catch ( OperationFailureException e ) {
            e.printStackTrace ( );
        }
    }

    public
    void changeToBarcodeMode () {
        try {
            reader.Config.setTriggerMode ( ENUM_TRIGGER_MODE.BARCODE_MODE ,
                    true );
            TriggerInfo triggerInfo = new TriggerInfo ( );
            triggerInfo.StopTrigger.setTriggerType ( STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_TAG_OBSERVATION_WITH_TIMEOUT );
            Long tagObservation = Long.parseLong ( "1" );
            triggerInfo.StopTrigger.TagObservation.setN ( tagObservation.shortValue ( ) );
            triggerInfo.StopTrigger.TagObservation.setTimeout ( 3000 );
            reader.Config.setStartTrigger ( triggerInfo.StartTrigger );
            reader.Config.setStopTrigger ( triggerInfo.StopTrigger );
        }
        catch ( InvalidUsageException e ) {
            e.printStackTrace ( );
        }
        catch ( OperationFailureException e ) {
            e.printStackTrace ( );
        }

    }

    public
    void setReaderPower ( int power ) {
        AsyncTask task = new AsyncTask ( ) {
            @Override
            protected
            Object doInBackground ( Object[] objects ) {
                try {
                    Antennas.AntennaRfConfig antennaRfConfig = reader.Config.Antennas.getAntennaRfConfig ( 1 );
                    antennaRfConfig.setTransmitPowerIndex ( power );
                    reader.Config.Antennas.setAntennaRfConfig ( 1 ,
                            antennaRfConfig );
//                    reader.Config.Antennas.getAntennaRfConfig(1).setTransmitPowerIndex(power);
                }
                catch ( InvalidUsageException e ) {
                    e.printStackTrace ( );
                }
                catch ( OperationFailureException e ) {
                    e.printStackTrace ( );
                }
                return null;
            }
        };
        task.execute ( );
    }

    public
    void changeToSingleScanMode () {
        try {
            TriggerInfo triggerInfo = new TriggerInfo ( );
            triggerInfo.StopTrigger.setTriggerType ( STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_TAG_OBSERVATION_WITH_TIMEOUT );
            Long tagObservation = Long.parseLong ( "1" );
            triggerInfo.StopTrigger.TagObservation.setN ( tagObservation.shortValue ( ) );
            reader.Config.setStartTrigger ( triggerInfo.StartTrigger );
            reader.Config.setStopTrigger ( triggerInfo.StopTrigger );
        }
        catch ( InvalidUsageException e ) {
            e.printStackTrace ( );
        }
        catch ( OperationFailureException e ) {
            e.printStackTrace ( );
        }

    }

    public
    void changeToBulkScanMode () {
        try {
            TriggerInfo triggerInfo = new TriggerInfo ( );
            triggerInfo.StartTrigger.setTriggerType ( START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE );
            triggerInfo.StopTrigger.setTriggerType ( STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE );
            reader.Config.setStartTrigger ( triggerInfo.StartTrigger );
            reader.Config.setStopTrigger ( triggerInfo.StopTrigger );
        }
        catch ( InvalidUsageException e ) {
            e.printStackTrace ( );
        }
        catch ( OperationFailureException e ) {
            e.printStackTrace ( );
        }

    }


    public
    interface ResponseHandlerInterface {
        void handleTagdata ( TagData[] tagData );

        void handleTriggerPress ( boolean pressed );
        //void handleStatusEvents(Events.StatusEventData eventData);
    }

    // Enumerates SDK based on host device
    private
    class CreateInstanceTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected
        Void doInBackground ( Void... voids ) {
            Log.d ( TAG ,
                    "CreateInstanceTask" );
            // Based on support available on host device choose the reader type
            InvalidUsageException invalidUsageException = null;
            readers = new Readers ( context ,
                    ENUM_TRANSPORT.ALL );
            try {
                availableRFIDReaderList = readers.GetAvailableRFIDReaderList ( );
            }
            catch ( Exception e ) {
                throw new RuntimeException ( e );
            }
            if ( invalidUsageException != null ) {
                readers.Dispose ( );
                readers = null;
                if ( readers == null ) {
                    readers = new Readers ( context ,
                            ENUM_TRANSPORT.BLUETOOTH );
                }
            }
            return null;
        }

        @Override
        protected
        void onPostExecute ( Void aVoid ) {
            super.onPostExecute ( aVoid );
            new ConnectionTask ( ).execute ( );
        }
    }

    private
    class ConnectionTask extends AsyncTask<Void, Void, String> {
        @Override
        protected
        String doInBackground ( Void... voids ) {
            Log.d ( TAG ,
                    "ConnectionTask" );
            try {
                GetAvailableReader ( );
            }
            catch ( Exception e ) {
                throw new RuntimeException ( e );
            }
            if ( reader != null )
                return connect ( );
            return "Failed to find or connect reader";
        }

        @Override
        protected
        void onPostExecute ( String result ) {
            super.onPostExecute ( result );
//            Toast.makeText(context,result,Toast.LENGTH_SHORT).show();
        }
    }

    // Read/Status Notify handler
    // Implement the RfidEventsLister class to receive event notifications
    public
    class EventHandler implements RfidEventsListener {
        // Read Event Notification
        public
        void eventReadNotify ( RfidReadEvents e ) {
            // Recommended to use new method getReadTagsEx for better performance in case of large tag population
            TagData[] myTags = reader.Actions.getReadTags ( 100 );
            if ( myTags != null ) {
                responseHandlerInterface.handleTagdata ( myTags );

            }
        }

        // Status Event Notification
        public
        void eventStatusNotify ( RfidStatusEvents rfidStatusEvents ) {
            Log.d ( TAG ,
                    "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType ( ) );
            if ( rfidStatusEvents.StatusEventData.getStatusEventType ( ) == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT ) {
                if ( rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent ( ) == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED ) {
                    new AsyncTask<Void, Void, Void> ( ) {
                        @Override
                        protected
                        Void doInBackground ( Void... voids ) {
                            responseHandlerInterface.handleTriggerPress ( true );
                            return null;
                        }
                    }.execute ( );
                }
                if ( rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent ( ) == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED ) {
                    new AsyncTask<Void, Void, Void> ( ) {
                        @Override
                        protected
                        Void doInBackground ( Void... voids ) {
                            responseHandlerInterface.handleTriggerPress ( false );
                            return null;
                        }
                    }.execute ( );
                }
            }
        }
    }

}
