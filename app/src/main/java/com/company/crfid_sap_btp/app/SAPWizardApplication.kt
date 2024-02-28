package com.company.crfid_sap_btp.app

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.sap.cloud.mobile.foundation.model.AppConfig
import com.company.crfid_sap_btp.service.SAPServiceManager
import com.company.crfid_sap_btp.repository.RepositoryFactory
import com.company.crfid_sap_btp.rfid.Barcode
import com.company.crfid_sap_btp.rfid.BarcodeEvent
import com.company.crfid_sap_btp.rfid.RFIDManager
import com.company.crfid_sap_btp.rfid.RFIDTagReadEvent
import com.company.crfid_sap_btp.rfid.RFIDTriggerEvent
import com.sap.cloud.mobile.foundation.mobileservices.MobileService
import com.sap.cloud.mobile.foundation.mobileservices.SDKInitializer
import com.sap.cloud.mobile.foundation.logging.LoggingService
import com.sap.cloud.mobile.foundation.settings.policies.LogPolicy
import com.sap.cloud.mobile.foundation.theme.ThemeDownloadService
import com.zebra.rfid.api3.TagData
import com.zebra.scannercontrol.DCSSDKDefs
import com.zebra.scannercontrol.DCSScannerInfo
import com.zebra.scannercontrol.FirmwareUpdateEvent
import com.zebra.scannercontrol.IDcsSdkApiDelegate
import com.zebra.scannercontrol.SDKHandler
import org.greenrobot.eventbus.EventBus


class SAPWizardApplication: Application() , RFIDManager.ResponseHandlerInterface,
    IDcsSdkApiDelegate {


    var rFIDManager: RFIDManager? = null








    internal var isApplicationUnlocked = false
    lateinit var preferenceManager: SharedPreferences

    /**
     * Manages and provides access to OData stores providing data for the app.
     */
    internal var sapServiceManager: SAPServiceManager? = null
    /**
     * Application-wide RepositoryFactory
     */
    lateinit var repositoryFactory: RepositoryFactory
        private set

    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        initServices()

        initRFIDMode()
    }


    //////////////////////////////////////////////////////////////////////////////////////
    //    public  RFIDController getRfidController(){
    //        return  rfidController;
    //    }
    override fun handleTagdata(tagData: Array<TagData>) {
        EventBus.getDefault().post(RFIDTagReadEvent(tagData))
    }

    override fun handleTriggerPress(pressed: Boolean) {
        EventBus.getDefault().post(RFIDTriggerEvent(pressed))
    }

    fun initRFIDMode() {
//        rfidController = new RFIDController();
        rFIDManager = RFIDManager()
        rFIDManager!!.onCreate(this, this)
    }

    fun swichToRFIDMode() {
        if (rFIDManager != null && rFIDManager!!.isReaderConnected) rFIDManager!!.changeToRFIDMode()
    }

    fun switchToSingleScanMode() {
        if (rFIDManager!!.isReaderConnected) rFIDManager!!.changeToSingleScanMode()
    }

    fun setReaderPower(power: Int) {
//        if (rFIDManager!!.isReaderConnected) rFIDManager!!.setReaderPower(power)

        if (rFIDManager != null && rFIDManager!!.isReaderConnected) {
            rFIDManager!!.setReaderPower(power)
        }
    }

    fun switchToBulkScanMode() {
        if (rFIDManager != null && rFIDManager!!.isReaderConnected) {
            rFIDManager!!.changeToBulkScanMode()
        }
    }

    fun connectToHHReader() {
        rFIDManager!!.connectToHHReader()
    }

    fun disconnectHHReader() {
        rFIDManager!!.disconnectHHReader()
    }

    //    public void connectToFixedReader(){
    //        rfidController.connect();
    //    }
    //
    //    public  void startFixedReaderScan(){
    //        rfidController.startScan();
    //    }
    //    public  void stopFixedReaderScan(){
    //        rfidController.stopScan();
    //    }
    //
    //    public void disconnectFixedReader(){
    //        rfidController.disconnect();
    //    }
    var sdkHandler: SDKHandler? = null
    private var availableScanner: DCSScannerInfo? = null
    private val mScannerInfoList = ArrayList<DCSScannerInfo?>()
    fun switchToBarcodeMode() {
        if (rFIDManager!!.isReaderConnected) {
            rFIDManager!!.changeToBarcodeMode()
            sdkHandler = SDKHandler(this@SAPWizardApplication)
            sdkHandler!!.dcssdkSetDelegate(this)
            initializeDcsSdkWithAppSettings()
            sdkHandler!!.dcssdkEnableAvailableScannersDetection(true)
            sdkHandler!!.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL)
            sdkHandler!!.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_SNAPI)
            sdkHandler!!.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE)
            sdkHandler!!.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC)
            if (sdkHandler != null) {
                mScannerInfoList.clear()
                val scannerTreeList: ArrayList<DCSScannerInfo?> = ArrayList<DCSScannerInfo?>()
                sdkHandler!!.dcssdkGetAvailableScannersList(scannerTreeList)
                sdkHandler!!.dcssdkGetActiveScannersList(scannerTreeList)
                for (s in scannerTreeList) {
                    addToScannerList(s)
                }
            }
            if (availableScanner != null) sdkHandler!!.dcssdkEstablishCommunicationSession(
                availableScanner!!.scannerID
            )
        }
    }

    private fun addToScannerList(s: DCSScannerInfo?) {
        mScannerInfoList.add(s)
        if (s!!.auxiliaryScanners != null) {
            for (aux in s.auxiliaryScanners.values) {
                addToScannerList(aux)
            }
        }
    }

    fun initializeDcsSdkWithAppSettings() {
        // Restore preferences
        MOT_SETTING_OPMODE = DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_BT_NORMAL.value
        MOT_SETTING_SCANNER_DETECTION = true
        MOT_SETTING_EVENT_ACTIVE = true
        MOT_SETTING_EVENT_AVAILABLE = true
        MOT_SETTING_EVENT_BARCODE = true
        var notifications_mask = 0
        if (MOT_SETTING_EVENT_AVAILABLE) {
            notifications_mask =
                notifications_mask or (DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value)
        }
        if (MOT_SETTING_EVENT_ACTIVE) {
            notifications_mask =
                notifications_mask or (DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value)
        }
        if (MOT_SETTING_EVENT_BARCODE) {
            notifications_mask =
                notifications_mask or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value
        }
        sdkHandler!!.dcssdkSubsribeForEvents(notifications_mask)
    }

    override fun dcssdkEventScannerAppeared(dcsScannerInfo: DCSScannerInfo) {
        if (dcsScannerInfo.scannerName.contains("RFD8500") || dcsScannerInfo.scannerName.contains("MC3300") || dcsScannerInfo.scannerName.contains(
                "RFD40"
            )
        ) {
            availableScanner = dcsScannerInfo
        }
    }

    override fun dcssdkEventScannerDisappeared(i: Int) {}
    override fun dcssdkEventCommunicationSessionEstablished(dcsScannerInfo: DCSScannerInfo) {}
    override fun dcssdkEventCommunicationSessionTerminated(i: Int) {}
    override fun dcssdkEventBarcode(barcodeData: ByteArray, barcodeType: Int, fromScannerID: Int) {
        val barcode = Barcode(barcodeData, barcodeType, fromScannerID)
        EventBus.getDefault().post(BarcodeEvent(String(barcode.getBarcodeData())))
    }

    override fun dcssdkEventImage(bytes: ByteArray, i: Int) {}
    override fun dcssdkEventVideo(bytes: ByteArray, i: Int) {}
    override fun dcssdkEventBinaryData(bytes: ByteArray, i: Int) {}
    override fun dcssdkEventFirmwareUpdate(firmwareUpdateEvent: FirmwareUpdateEvent) {}
    override fun dcssdkEventAuxScannerAppeared(
        dcsScannerInfo: DCSScannerInfo,
        dcsScannerInfo1: DCSScannerInfo
    ) {
    }

    companion object {
        //    public DaoSession getDaoSession() {
        //        return daoSession;
        //    }
        //    RFIDController rfidController;
        var appContext: SAPWizardApplication? = null
            private set
        const val ENCRYPTED = false
        var MOT_SETTING_OPMODE = 0
        var MOT_SETTING_SCANNER_DETECTION = false
        var MOT_SETTING_EVENT_ACTIVE = false
        var MOT_SETTING_EVENT_AVAILABLE = false
        var MOT_SETTING_EVENT_BARCODE = false

        const val KEY_LOG_SETTING_PREFERENCE = "key.log.settings.preference"
    }
    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize service manager with application configuration
     *
     * @param appConfig the application configuration
     */
    fun initializeServiceManager(appConfig: AppConfig) {
        sapServiceManager = SAPServiceManager(appConfig)
        repositoryFactory =
            RepositoryFactory(sapServiceManager)
    }

    /**
     * Clears all user-specific data and configuration from the application, essentially resetting it to its initial
     * state.
     *
     * If client code wants to handle the reset logic of a service, here is an example:
     *
     *   SDKInitializer.resetServices { service ->
     *       return@resetServices if( service is PushService ) {
     *           PushService.unregisterPushSync(object: CallbackListener {
     *               override fun onSuccess() {
     *               }
     *
     *               override fun onError(p0: Throwable) {
     *               }
     *           })
     *           true
     *       } else {
     *           false
     *       }
     *   }
     */
    fun resetApplication() {
        preferenceManager.also {
            it.edit().clear().apply()
        }
        isApplicationUnlocked = false
        repositoryFactory.reset()
        SDKInitializer.resetServices()

    }

    private fun initServices() {
        val services = mutableListOf<MobileService>()
        services.add(LoggingService(autoUpload = false).apply {
            policy = LogPolicy(logLevel = "WARN", entryExpiry = 0, maxFileNumber = 4)
            logToConsole = true
        })
        services.add(ThemeDownloadService(this))

        SDKInitializer.start(this, * services.toTypedArray())
    }


//    companion object {
//        const val KEY_LOG_SETTING_PREFERENCE = "key.log.settings.preference"
//    }
}