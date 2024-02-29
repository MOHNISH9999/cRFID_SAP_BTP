package com.company.crfid_sap_btp.mdui

import android.R.attr.duration
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.company.crfid_sap_btp.R
import com.company.crfid_sap_btp.app.SAPWizardApplication
import com.company.crfid_sap_btp.app.WelcomeActivity
import com.company.crfid_sap_btp.databinding.ActivityEntitySetListBinding
import com.company.crfid_sap_btp.databinding.ElementEntitySetListBinding
import com.company.crfid_sap_btp.mdui.matdetailsset.MatDetailsSetActivity
import com.company.crfid_sap_btp.rfid.BaseActivity
import com.company.crfid_sap_btp.rfid.RFIDManager
import com.company.crfid_sap_btp.rfid.SettingsActivity2
import com.google.android.material.color.utilities.Score.score
import com.sap.cloud.mobile.flowv2.core.DialogHelper
import com.sap.cloud.mobile.flowv2.core.Flow
import com.sap.cloud.mobile.flowv2.core.FlowContextRegistry
import com.sap.cloud.mobile.flowv2.model.FlowType
import com.sap.cloud.mobile.flowv2.securestore.UserSecureStoreDelegate
import com.zebra.rfid.api3.TagData
import org.slf4j.LoggerFactory


/*
 * An activity to display the list of all entity types from the OData service
 */
class EntitySetListActivity : BaseActivity() {
    private val entitySetNames = ArrayList<String>()
    private val entitySetNameMap = HashMap<String, EntitySetName>()
    private lateinit var binding: ActivityEntitySetListBinding

    var rFIDManager: RFIDManager? = null

    enum class EntitySetName constructor(val entitySetName: String, val titleId: Int, val iconId: Int) {

        MatDetailsSet("MatDetailsSet", R.string.eset_matdetailsset,
            WHITE_ANDROID_ICON),
        CycleCounting("Cycle Counting",R.string.eset_cyclecounting, WHITE_ANDROID_ICON),

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //navigate to launch screen if SAPServiceManager or OfflineOdataProvider is not initialized
        navForInitialize()
        binding = ActivityEntitySetListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = findViewById<Toolbar>(R.id.toolbar) // to avoid ambiguity
        setSupportActionBar(toolbar)


//        rFIDManager= RFIDManager()
//        rFIDManager!!.onCreate(this,this)


        entitySetNames.clear()
        entitySetNameMap.clear()
        for (entitySet in EntitySetName.values()) {
            val entitySetTitle = resources.getString(entitySet.titleId)
            entitySetNames.add(entitySetTitle)
            entitySetNameMap[entitySetTitle] = entitySet
        }
        val listView = binding.entityList
        val adapter = EntitySetListAdapter(this, R.layout.element_entity_set_list, entitySetNames)

        listView.adapter = adapter

        listView.setOnItemClickListener listView@{ _, _, position, _ ->
            val entitySetName = entitySetNameMap[adapter.getItem(position)!!]
            val context = this@EntitySetListActivity
            val intent: Intent = when (entitySetName) {
                EntitySetName.MatDetailsSet -> Intent(context, MatDetailsSetActivity::class.java)
                EntitySetName.CycleCounting -> Intent(context, MatDetailsSetActivity::class.java)
                else -> return@listView
            }
            context.startActivity(intent)
        }
    }
    inner class EntitySetListAdapter internal constructor(context: Context, resource: Int, entitySetNames: List<String>)
                    : ArrayAdapter<String>(context, resource, entitySetNames) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            var viewBind :ElementEntitySetListBinding
            val entitySetName = entitySetNameMap[getItem(position)!!]
            if (view == null) {
                viewBind = ElementEntitySetListBinding.inflate(LayoutInflater.from(context), parent, false)
                view = viewBind.root
            } else {
                viewBind = ElementEntitySetListBinding.bind(view)
            }
            val entitySetCell = viewBind.entitySetName
            entitySetCell.headline = entitySetName?.titleId?.let {
                context.resources.getString(it)
            }
            entitySetName?.iconId?.let { entitySetCell.setDetailImage(it) }
            return view
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.entity_set_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menu_delete_registration)?.isEnabled =
            UserSecureStoreDelegate.getInstance().getRuntimeMultipleUserModeAsync() == true
        menu?.findItem(R.id.menu_delete_registration)?.isVisible =
            UserSecureStoreDelegate.getInstance().getRuntimeMultipleUserModeAsync() == true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        LOGGER.debug("onOptionsItemSelected: " + item.title)
        return when (item.itemId) {
            R.id.menu_settings -> {
                LOGGER.debug("settings screen menu item selected.")
                Intent(this, SettingsActivity::class.java).also {
                    this.startActivity(it)
                }
                true
            }R.id.menu_rfid_settings -> {
                LOGGER.debug("rfid settings screen menu item selected.")
                Intent(this, SettingsActivity2::class.java).also {
                    this.startActivity(it)
                }
                true
            }
            R.id.menu_logout -> {
                Flow.start(this, FlowContextRegistry.flowContext.copy(
                    flowType = FlowType.LOGOUT,
                )) { _, resultCode, _ ->
                    if (resultCode == RESULT_OK) {
                        Intent(this, WelcomeActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(this)
                        }
                    }
                }
                true
            }
            R.id.menu_delete_registration -> {
                DialogHelper.ErrorDialogFragment(
                    message = getString(R.string.delete_registration_warning),
                    title = getString(R.string.dialog_warn_title),
                    positiveButtonCaption = getString(R.string.confirm_yes),
                    negativeButtonCaption = getString(R.string.cancel),
                    positiveAction = {
                        Flow.start(this, FlowContextRegistry.flowContext.copy(
                            flowType = FlowType.DEL_REGISTRATION
                        )) { _, resultCode, _ ->
                            if (resultCode == RESULT_OK) {
                                Intent(this, WelcomeActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(this)
                                }
                            }
                        }
                    }
                ).apply {
                    isCancelable = false
                    show(supportFragmentManager, this@EntitySetListActivity.getString(R.string.delete_registration))
                }
                true
            }
            else -> false
        }
    }

    override fun onBarcodeResult(barcode: String?) {
        TODO("Not yet implemented")
    }

    override fun onRFIDTagIDResult(tagID: String?) {
        Toast.makeText(this,tagID,Toast.LENGTH_SHORT).show()

    }

    override fun onFixedReaderTagIDResult(tagID: String?) {
        TODO("Not yet implemented")
    }

    override fun onRFIDTagSearched(rssi: Short) {
        TODO("Not yet implemented")
    }

    private fun navForInitialize() {
        if ((application as SAPWizardApplication).sapServiceManager == null) {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }


    companion object {
        private val LOGGER = LoggerFactory.getLogger(EntitySetListActivity::class.java)
        private const val BLUE_ANDROID_ICON = R.drawable.ic_android_blue
        private const val WHITE_ANDROID_ICON = R.drawable.ic_android_white
    }

//    override fun handleTagdata(tagData: Array<out TagData>?) {
//    }
//
//    override fun handleTriggerPress(pressed: Boolean) {
//
//        runOnUiThread {
//            Toast.makeText(this,"Trigger Pressed",Toast.LENGTH_SHORT).show()
//        }
//    }
}
