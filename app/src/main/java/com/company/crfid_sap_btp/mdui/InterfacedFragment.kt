package com.company.crfid_sap_btp.mdui

import android.os.AsyncTask
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.company.crfid_sap_btp.R
import com.company.crfid_sap_btp.app.SAPWizardApplication
import com.company.crfid_sap_btp.rfid.BarcodeEvent
import com.company.crfid_sap_btp.rfid.RFIDTagReadEvent
import com.company.crfid_sap_btp.rfid.RFIDTriggerEvent
import com.company.crfid_sap_btp.rfid.SnackBarMaker
import com.sap.cloud.mobile.flowv2.core.DialogHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class InterfacedFragment<TE, TVB: ViewBinding>: Fragment(), MenuProvider {


    //////////////////////////////////////////////////////////////////////////
    var isFixedReaderScanning: Boolean = false
    var craveApplication: SAPWizardApplication? = null
    //////////////////////////////////////////////////////////////////////////
    /** Hold the current context */
    internal lateinit var currentActivity: FragmentActivity

    /** Store the toolbar title of the actual fragment */
    internal var activityTitle: String = ""

    /** Store the toolbar menu resource of the actual fragment */
    internal var menu: Int = 0

    /** Navigation parameter: name of the link */
    internal var parentEntityData: Parcelable? = null

    /** Navigation parameter: starting entity */
    internal var navigationPropertyName: String? = null

    private var _binding: TVB? = null
    val fragmentBinding get() = _binding!!

    /** The progress bar */
    internal val secondaryToolbar: Toolbar?
        get() = currentActivity.findViewById<Toolbar>(R.id.secondaryToolbar)

    /** The progress bar */
    internal val progressBar : ProgressBar?
        get() = currentActivity.findViewById<ProgressBar>(R.id.indeterminateBar)

    /** The listener **/
    internal var listener: InterfacedFragmentListener<TE>? = null

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EventBus.getDefault().register(this)
        craveApplication = SAPWizardApplication()

        activity?.let {
            currentActivity = it
            if (it is InterfacedFragmentListener<*>) {
                listener = it as InterfacedFragmentListener<TE>
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = initBinding(inflater, container)
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val menuHost : MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    abstract fun initBinding(inflater: LayoutInflater, container: ViewGroup?): TVB

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        secondaryToolbar?.let {
            it.menu.clear()
            it.inflateMenu(this.menu)
            it.setOnMenuItemClickListener(this::onMenuItemSelected)
            return@onCreateMenu
        }
        menuInflater.inflate(this.menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    protected fun showError(message: String) {
        DialogHelper(requireContext()).showOKOnlyDialog(
            fragmentManager = requireActivity().supportFragmentManager,
            message = message
        )
    }

    interface InterfacedFragmentListener<T> {
        fun onFragmentStateChange(evt: Int, entity: T?)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onEvent(rfidTriggerEvent: RFIDTriggerEvent) {
        if (rfidTriggerEvent.isPressed) {
            (requireActivity().application as SAPWizardApplication).rFIDManager!!.performInventory()
        } else {
            (requireActivity().application as SAPWizardApplication).rFIDManager!!.stopInventory()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onEvent(barcodeEvent: BarcodeEvent) {
        onBarcodeResult(barcodeEvent.barcode)
    }

    protected abstract fun onBarcodeResult(barcode: String?)

    protected abstract fun onRFIDTagIDResult(tagID: String?)

    protected abstract fun onFixedReaderTagIDResult(tagID: String?)

    protected abstract fun onRFIDTagSearched(rssi: Short)

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onEvent(rfidTriggerEvent: RFIDTagReadEvent) {
        val tagDatas = rfidTriggerEvent.tagData
        if (tagDatas != null && tagDatas.size > 0) {
            if (tagDatas[0].tagID != null) onRFIDTagIDResult(tagDatas[0].tagID) else onRFIDTagSearched(
                if (tagDatas[0].LocationInfo != null) tagDatas[0].LocationInfo.relativeDistance else 0
            )
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


    //    @Subscribe(threadMode = ThreadMode.MAIN)
    //    public void onEvent(FixedReaderEvent fixedReaderEvent) {
    //        onFixedReaderTagIDResult(fixedReaderEvent.getTagid());
    //    }
    open fun changeScanningMode(mode: Int) {  // 1 = RFID, 2 = Barcode
        val task: AsyncTask<*, *, *> = object : AsyncTask<Any?, Any?, Any?>() {
            override fun doInBackground(objects: Array<Any?>): Any? {
                if (mode == 2) craveApplication!!.switchToBarcodeMode() else craveApplication!!.swichToRFIDMode()
                return null
            }
        }
        task.execute()
    }

    open fun searchTag(tagID: String?) {
        (requireActivity().application as SAPWizardApplication).rFIDManager!!.searchTag(tagID)
    }

    open fun setReaderPower(power: Int) {
        craveApplication!!.setReaderPower(power)
    }


    open fun switchToSingleScanMode() {
        (requireActivity().application as SAPWizardApplication).switchToSingleScanMode()
    }

    open fun switchToBulkScanMode() {
        craveApplication!!.switchToBulkScanMode()
    }

    open fun connectToHHReader() {
        craveApplication!!.connectToHHReader()
    }

    open fun disconnectHHReader() {
        (requireActivity().application as SAPWizardApplication).disconnectHHReader()
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
    open fun showMessage(title: String?, message: String?, listener: View.OnClickListener?) {
        SnackBarMaker.snackWithCustomTiming(
            requireActivity().window.decorView.rootView,
            message,
            1000
        )
    }

}