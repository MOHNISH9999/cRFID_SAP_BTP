package com.company.crfid_sap_btp.mdui

import com.company.crfid_sap_btp.app.SAPWizardApplication

import com.sap.cloud.mobile.flowv2.core.DialogHelper
import com.sap.cloud.mobile.flowv2.core.Flow
import com.sap.cloud.mobile.flowv2.core.FlowContextRegistry
import com.sap.cloud.mobile.flowv2.model.FlowType
import com.sap.cloud.mobile.flowv2.securestore.UserSecureStoreDelegate
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.*
import android.widget.ArrayAdapter
import android.content.Context
import android.content.Intent
import java.util.ArrayList
import java.util.HashMap
import com.company.crfid_sap_btp.app.WelcomeActivity
import com.company.crfid_sap_btp.databinding.ActivityEntitySetListBinding
import com.company.crfid_sap_btp.databinding.ElementEntitySetListBinding
import com.company.crfid_sap_btp.mdui.inpoheaderset.INPOHEADERSetActivity
import com.company.crfid_sap_btp.mdui.inpoitemset.INPOITEMSetActivity
import com.company.crfid_sap_btp.mdui.inposet.INPOSetActivity
import com.company.crfid_sap_btp.mdui.inpostlocset.INPOStLocSetActivity
import com.company.crfid_sap_btp.mdui.inprintmediaset.INPRINTMEDIASetActivity
import com.company.crfid_sap_btp.mdui.inprintpoitemset.INPRINTPOITEMSetActivity
import com.company.crfid_sap_btp.mdui.inprintposet.INPRINTPOSetActivity
import com.company.crfid_sap_btp.mdui.inreprintposet.INREPRINTPOSetActivity
import com.company.crfid_sap_btp.mdui.inreprintrsnset.INREPRINTRSNSetActivity
import com.company.crfid_sap_btp.mdui.instockovwset.INSTOCKOVWSetActivity
import com.company.crfid_sap_btp.mdui.intmattomatseset.INTMattomatSeSetActivity
import com.company.crfid_sap_btp.mdui.intmattomatset.INTMattomatSetActivity
import com.company.crfid_sap_btp.mdui.intmtomchargvalidset.INTMtomChargvalidSetActivity
import com.company.crfid_sap_btp.mdui.intphyinvpostset.IntPhyInvPostSetActivity
import com.company.crfid_sap_btp.mdui.intphyinvset.IntPhyInvSetActivity
import com.company.crfid_sap_btp.mdui.intphyvalidset.IntPhyValidSetActivity
import com.company.crfid_sap_btp.mdui.intsloctoslocset.IntSlocToSlocSetActivity
import com.company.crfid_sap_btp.mdui.intslocvalidset.IntSlocValidSetActivity
import com.company.crfid_sap_btp.mdui.loginsrvset.LoginSrvSetActivity
import com.company.crfid_sap_btp.mdui.matdetailsset.MatDetailsSetActivity
import com.company.crfid_sap_btp.mdui.outgiscrapset.OUTGISCRAPSetActivity
import com.company.crfid_sap_btp.mdui.outgisoset.OUTGISOSetActivity
import com.company.crfid_sap_btp.mdui.outpicking1set.OUTPICKING1SetActivity
import com.company.crfid_sap_btp.mdui.outpickingset.OUTPICKINGSetActivity
import com.company.crfid_sap_btp.mdui.outgireservationset.OutGIReservationSetActivity
import com.company.crfid_sap_btp.mdui.rfmattagset.RFMATTAGSetActivity
import org.slf4j.LoggerFactory
import com.company.crfid_sap_btp.R

/*
 * An activity to display the list of all entity types from the OData service
 */
class EntitySetListActivity : AppCompatActivity() {
    private val entitySetNames = ArrayList<String>()
    private val entitySetNameMap = HashMap<String, EntitySetName>()
    private lateinit var binding: ActivityEntitySetListBinding


    enum class EntitySetName constructor(val entitySetName: String, val titleId: Int, val iconId: Int) {
//        INPOHEADERSet(
//            "INPOHEADERSet", R.string.eset_inpoheaderset,
//            BLUE_ANDROID_ICON
//        ),
//        INPOITEMSet("INPOITEMSet", R.string.eset_inpoitemset,
//            WHITE_ANDROID_ICON),
//        INPOSet("INPOSet", R.string.eset_inposet,
//            BLUE_ANDROID_ICON),
//        INPOStLocSet("INPOStLocSet", R.string.eset_inpostlocset,
//            WHITE_ANDROID_ICON),
//        INPRINTMEDIASet("INPRINTMEDIASet", R.string.eset_inprintmediaset,
//            BLUE_ANDROID_ICON),
//        INPRINTPOITEMSet("INPRINTPOITEMSet", R.string.eset_inprintpoitemset,
//            WHITE_ANDROID_ICON),
//        INPRINTPOSet("INPRINTPOSet", R.string.eset_inprintposet,
//            BLUE_ANDROID_ICON),
//        INREPRINTPOSet("INREPRINTPOSet", R.string.eset_inreprintposet,
//            WHITE_ANDROID_ICON),
//        INREPRINTRSNSet("INREPRINTRSNSet", R.string.eset_inreprintrsnset,
//            BLUE_ANDROID_ICON),
//        INSTOCKOVWSet("INSTOCKOVWSet", R.string.eset_instockovwset,
//            WHITE_ANDROID_ICON),
//        INTMattomatSeSet("INTMattomatSeSet", R.string.eset_intmattomatseset,
//            BLUE_ANDROID_ICON),
//        INTMattomatSet("INTMattomatSet", R.string.eset_intmattomatset,
//            WHITE_ANDROID_ICON),
//        INTMtomChargvalidSet("INTMtomChargvalidSet", R.string.eset_intmtomchargvalidset,
//            BLUE_ANDROID_ICON),
//        IntPhyInvPostSet("IntPhyInvPostSet", R.string.eset_intphyinvpostset,
//            WHITE_ANDROID_ICON),
//        IntPhyInvSet("IntPhyInvSet", R.string.eset_intphyinvset,
//            BLUE_ANDROID_ICON),
//        IntPhyValidSet("IntPhyValidSet", R.string.eset_intphyvalidset,
//            WHITE_ANDROID_ICON),
//        IntSlocToSlocSet("IntSlocToSlocSet", R.string.eset_intsloctoslocset,
//            BLUE_ANDROID_ICON),
//        IntSlocValidSet("IntSlocValidSet", R.string.eset_intslocvalidset,
//            WHITE_ANDROID_ICON),
//        LoginSrvSet("LoginSrvSet", R.string.eset_loginsrvset,
//            BLUE_ANDROID_ICON),
        MatDetailsSet("MatDetailsSet", R.string.eset_matdetailsset,
            WHITE_ANDROID_ICON),
        CycleCounting("Cycle Counting",R.string.eset_cyclecounting, WHITE_ANDROID_ICON),
//        OUTGISCRAPSet("OUTGISCRAPSet", R.string.eset_outgiscrapset,
//            BLUE_ANDROID_ICON),
//        OUTGISOSet("OUTGISOSet", R.string.eset_outgisoset,
//            WHITE_ANDROID_ICON),
//        OUTPICKING1Set("OUTPICKING1Set", R.string.eset_outpicking1set,
//            BLUE_ANDROID_ICON),
//        OUTPICKINGSet("OUTPICKINGSet", R.string.eset_outpickingset,
//            WHITE_ANDROID_ICON),
//        OutGIReservationSet("OutGIReservationSet", R.string.eset_outgireservationset,
//            BLUE_ANDROID_ICON),
//        RFMATTAGSet("RFMATTAGSet", R.string.eset_rfmattagset,
//            WHITE_ANDROID_ICON)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //navigate to launch screen if SAPServiceManager or OfflineOdataProvider is not initialized
        navForInitialize()
        binding = ActivityEntitySetListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = findViewById<Toolbar>(R.id.toolbar) // to avoid ambiguity
        setSupportActionBar(toolbar)

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
//                EntitySetName.INPOHEADERSet -> Intent(context, INPOHEADERSetActivity::class.java)
//                EntitySetName.INPOITEMSet -> Intent(context, INPOITEMSetActivity::class.java)
//                EntitySetName.INPOSet -> Intent(context, INPOSetActivity::class.java)
//                EntitySetName.INPOStLocSet -> Intent(context, INPOStLocSetActivity::class.java)
//                EntitySetName.INPRINTMEDIASet -> Intent(context, INPRINTMEDIASetActivity::class.java)
//                EntitySetName.INPRINTPOITEMSet -> Intent(context, INPRINTPOITEMSetActivity::class.java)
//                EntitySetName.INPRINTPOSet -> Intent(context, INPRINTPOSetActivity::class.java)
//                EntitySetName.INREPRINTPOSet -> Intent(context, INREPRINTPOSetActivity::class.java)
//                EntitySetName.INREPRINTRSNSet -> Intent(context, INREPRINTRSNSetActivity::class.java)
//                EntitySetName.INSTOCKOVWSet -> Intent(context, INSTOCKOVWSetActivity::class.java)
//                EntitySetName.INTMattomatSeSet -> Intent(context, INTMattomatSeSetActivity::class.java)
//                EntitySetName.INTMattomatSet -> Intent(context, INTMattomatSetActivity::class.java)
//                EntitySetName.INTMtomChargvalidSet -> Intent(context, INTMtomChargvalidSetActivity::class.java)
//                EntitySetName.IntPhyInvPostSet -> Intent(context, IntPhyInvPostSetActivity::class.java)
//                EntitySetName.IntPhyInvSet -> Intent(context, IntPhyInvSetActivity::class.java)
//                EntitySetName.IntPhyValidSet -> Intent(context, IntPhyValidSetActivity::class.java)
//                EntitySetName.IntSlocToSlocSet -> Intent(context, IntSlocToSlocSetActivity::class.java)
//                EntitySetName.IntSlocValidSet -> Intent(context, IntSlocValidSetActivity::class.java)
//                EntitySetName.LoginSrvSet -> Intent(context, LoginSrvSetActivity::class.java)
                EntitySetName.MatDetailsSet -> Intent(context, MatDetailsSetActivity::class.java)
//                EntitySetName.OUTGISCRAPSet -> Intent(context, OUTGISCRAPSetActivity::class.java)
//                EntitySetName.OUTGISOSet -> Intent(context, OUTGISOSetActivity::class.java)
//                EntitySetName.OUTPICKING1Set -> Intent(context, OUTPICKING1SetActivity::class.java)
//                EntitySetName.OUTPICKINGSet -> Intent(context, OUTPICKINGSetActivity::class.java)
//                EntitySetName.OutGIReservationSet -> Intent(context, OutGIReservationSetActivity::class.java)
//                EntitySetName.RFMATTAGSet -> Intent(context, RFMATTAGSetActivity::class.java)
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
}
