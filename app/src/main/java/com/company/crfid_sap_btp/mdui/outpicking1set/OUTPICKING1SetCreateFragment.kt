package com.company.crfid_sap_btp.mdui.outpicking1set

import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import com.company.crfid_sap_btp.R
import com.company.crfid_sap_btp.databinding.FragmentOutpicking1setCreateBinding
import com.company.crfid_sap_btp.mdui.BundleKeys
import com.company.crfid_sap_btp.mdui.InterfacedFragment
import com.company.crfid_sap_btp.mdui.UIConstants
import com.company.crfid_sap_btp.repository.OperationResult
import com.company.crfid_sap_btp.viewmodel.outpicking1.OutPicking1ViewModel
import com.sap.cloud.android.odata.zcims_int_srv_entities.OutPicking1
import com.sap.cloud.android.odata.zcims_int_srv_entities.ZCIMS_INT_SRV_EntitiesMetadata.EntityTypes
import com.sap.cloud.mobile.fiori.formcell.SimplePropertyFormCell
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader
import com.sap.cloud.mobile.odata.Property
import org.slf4j.LoggerFactory

/**
 * A fragment that is used for both update and create for users to enter values for the properties. When used for
 * update, an instance of the entity is required. In the case of create, a new instance of the entity with defaults will
 * be created. The default values may not be acceptable for the OData service.
 * This fragment is either contained in a [OUTPICKING1SetListActivity] in two-pane mode (on tablets) or a
 * [OUTPICKING1SetDetailActivity] on handsets.
 *
 * Arguments: Operation: [OP_CREATE | OP_UPDATE]
 *            OutPicking1 if Operation is update
 */
class OUTPICKING1SetCreateFragment : InterfacedFragment<OutPicking1, FragmentOutpicking1setCreateBinding>() {

    /** OutPicking1 object and it's copy: the modifications are done on the copied object. */
    private lateinit var outPicking1Entity: OutPicking1
    private lateinit var outPicking1EntityCopy: OutPicking1

    /** Indicate what operation to be performed */
    private lateinit var operation: String

    /** outPicking1Entity ViewModel */
    private lateinit var viewModel: OutPicking1ViewModel

    /** The update menu item */
    private lateinit var updateMenuItem: MenuItem

    private val isOutPicking1Valid: Boolean
        get() {
            var isValid = true
            fragmentBinding.createUpdateOutpicking1.let { linearLayout ->
                for (i in 0 until linearLayout.childCount) {
                    val simplePropertyFormCell = linearLayout.getChildAt(i) as SimplePropertyFormCell
                    val propertyName = simplePropertyFormCell.tag as String
                    val property = EntityTypes.outPicking1.getProperty(propertyName)
                    val value = simplePropertyFormCell.value.toString()
                    if (!isValidProperty(property, value)) {
                        simplePropertyFormCell.setTag(R.id.TAG_HAS_MANDATORY_ERROR, true)
                        val errorMessage = resources.getString(R.string.mandatory_warning)
                        simplePropertyFormCell.isErrorEnabled = true
                        simplePropertyFormCell.error = errorMessage
                        isValid = false
                    } else {
                        if (simplePropertyFormCell.isErrorEnabled) {
                            val hasMandatoryError = simplePropertyFormCell.getTag(R.id.TAG_HAS_MANDATORY_ERROR) as Boolean
                            if (!hasMandatoryError) {
                                isValid = false
                            } else {
                                simplePropertyFormCell.isErrorEnabled = false
                            }
                        }
                        simplePropertyFormCell.setTag(R.id.TAG_HAS_MANDATORY_ERROR, false)
                    }
                }
            }
            return isValid
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menu = R.menu.itemlist_edit_options

        arguments?.let {
            (it.getString(BundleKeys.OPERATION))?.let { operationType ->
                operation = operationType
                activityTitle = when (operationType) {
                    UIConstants.OP_CREATE -> resources.getString(R.string.title_create_fragment, EntityTypes.outPicking1.localName)
                    else -> resources.getString(R.string.title_update_fragment) + " " + EntityTypes.outPicking1.localName

                }
            }
        }

        activity?.let {
            (it as OUTPICKING1SetActivity).isNavigationDisabled = true
            viewModel = ViewModelProvider(it)[OutPicking1ViewModel::class.java]
            viewModel.createResult.observe(this) { result -> onComplete(result) }
            viewModel.updateResult.observe(this) { result -> onComplete(result) }

            outPicking1Entity = if (operation == UIConstants.OP_CREATE) {
                createOutPicking1()
            } else {
                viewModel.selectedEntity.value!!
            }

            val workingCopy = when{ (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) -> {
                    savedInstanceState?.getParcelable<OutPicking1>(KEY_WORKING_COPY, OutPicking1::class.java)
                } else -> @Suppress("DEPRECATION") savedInstanceState?.getParcelable<OutPicking1>(KEY_WORKING_COPY)
            }

            if (workingCopy == null) {
                outPicking1EntityCopy = outPicking1Entity.copy()
                outPicking1EntityCopy.entityTag = outPicking1Entity.entityTag
                outPicking1EntityCopy.oldEntity = outPicking1Entity
                outPicking1EntityCopy.editLink = outPicking1Entity.editLink
            } else {
                outPicking1EntityCopy = workingCopy
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        currentActivity.findViewById<ObjectHeader>(R.id.objectHeader)?.let {
            it.visibility = View.GONE
        }
        fragmentBinding.outPicking1 = outPicking1EntityCopy
        return fragmentBinding.root
    }

    override  fun  initBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentOutpicking1setCreateBinding.inflate(inflater, container, false)

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.save_item -> {
                updateMenuItem = menuItem
                enableUpdateMenuItem(false)
                onSaveItem()
            }
            else -> super.onMenuItemSelected(menuItem)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(secondaryToolbar != null) secondaryToolbar!!.title = activityTitle else activity?.title = activityTitle
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_WORKING_COPY, outPicking1EntityCopy)
        super.onSaveInstanceState(outState)
    }

    /** Enables the update menu item based on [enable] */
    private fun enableUpdateMenuItem(enable : Boolean = true) {
        updateMenuItem.also {
            it.isEnabled = enable
            it.icon?.alpha = if(enable) 255 else 130
        }
    }

    /** Saves the entity */
    private fun onSaveItem(): Boolean {
        if (!isOutPicking1Valid) {
            return false
        }
        (currentActivity as OUTPICKING1SetActivity).isNavigationDisabled = false
        progressBar?.visibility = View.VISIBLE
        when (operation) {
            UIConstants.OP_CREATE -> {
                viewModel.create(outPicking1EntityCopy)
            }
            UIConstants.OP_UPDATE -> viewModel.update(outPicking1EntityCopy)
        }
        return true
    }

    /**
     * Create a new OutPicking1 instance and initialize properties to its default values
     * Nullable property will remain null
     * @return new OutPicking1 instance
     */
    private fun createOutPicking1(): OutPicking1 {
        val entity = OutPicking1(true)
        return entity
    }

    /** Callback function to complete processing when updateResult or createResult events fired */
    private fun onComplete(result: OperationResult<OutPicking1>) {
        progressBar?.visibility = View.INVISIBLE
        enableUpdateMenuItem(true)
        if (result.error != null) {
            (currentActivity as OUTPICKING1SetActivity).isNavigationDisabled = true
            handleError(result)
        } else {
            if (operation == UIConstants.OP_UPDATE && !currentActivity.resources.getBoolean(R.bool.two_pane)) {
                viewModel.selectedEntity.value = outPicking1EntityCopy
            }
            (currentActivity as OUTPICKING1SetActivity).onBackPressedDispatcher.onBackPressed()
        }
    }

    /** Simple validation: checks the presence of mandatory fields. */
    private fun isValidProperty(property: Property, value: String): Boolean {
        return !(!property.isNullable && value.isEmpty())
    }

    /**
     * Notify user of error encountered while execution the operation
     *
     * @param [result] operation result with error
     */
    private fun handleError(result: OperationResult<OutPicking1>) {
        val errorMessage = when (result.operation) {
            OperationResult.Operation.UPDATE -> getString(R.string.update_failed_detail)
            OperationResult.Operation.CREATE -> getString(R.string.create_failed_detail)
            else -> throw AssertionError()
        }
        showError(errorMessage)
    }


    companion object {
        private val KEY_WORKING_COPY = "WORKING_COPY"
        private val LOGGER = LoggerFactory.getLogger(OUTPICKING1SetActivity::class.java)
    }
}
