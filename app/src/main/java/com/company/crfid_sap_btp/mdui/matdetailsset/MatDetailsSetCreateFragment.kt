package com.company.crfid_sap_btp.mdui.matdetailsset

import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import com.company.crfid_sap_btp.R
import com.company.crfid_sap_btp.databinding.FragmentMatdetailssetCreateBinding
import com.company.crfid_sap_btp.mdui.BundleKeys
import com.company.crfid_sap_btp.mdui.InterfacedFragment
import com.company.crfid_sap_btp.mdui.UIConstants
import com.company.crfid_sap_btp.repository.OperationResult
import com.company.crfid_sap_btp.viewmodel.matdetails.MatDetailsViewModel
import com.sap.cloud.android.odata.zcims_int_srv_entities.MatDetails
import com.sap.cloud.android.odata.zcims_int_srv_entities.ZCIMS_INT_SRV_EntitiesMetadata.EntityTypes
import com.sap.cloud.mobile.fiori.formcell.SimplePropertyFormCell
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader
import com.sap.cloud.mobile.odata.Property
import org.slf4j.LoggerFactory

/**
 * A fragment that is used for both update and create for users to enter values for the properties. When used for
 * update, an instance of the entity is required. In the case of create, a new instance of the entity with defaults will
 * be created. The default values may not be acceptable for the OData service.
 * This fragment is either contained in a [MatDetailsSetListActivity] in two-pane mode (on tablets) or a
 * [MatDetailsSetDetailActivity] on handsets.
 *
 * Arguments: Operation: [OP_CREATE | OP_UPDATE]
 *            MatDetails if Operation is update
 */
class MatDetailsSetCreateFragment : InterfacedFragment<MatDetails, FragmentMatdetailssetCreateBinding>() {

    /** MatDetails object and it's copy: the modifications are done on the copied object. */
    private lateinit var matDetailsEntity: MatDetails
    private lateinit var matDetailsEntityCopy: MatDetails

    /** Indicate what operation to be performed */
    private lateinit var operation: String

    /** matDetailsEntity ViewModel */
    private lateinit var viewModel: MatDetailsViewModel

    /** The update menu item */
    private lateinit var updateMenuItem: MenuItem

    var tagIDNEW:String=""
    lateinit var tagidid:SimplePropertyFormCell

    private val isMatDetailsValid: Boolean
        get() {
            var isValid = true
            fragmentBinding.createUpdateMatdetails.let { linearLayout ->
                for (i in 0 until linearLayout.childCount) {
                    val simplePropertyFormCell = linearLayout.getChildAt(i) as SimplePropertyFormCell
                    val propertyName = simplePropertyFormCell.tag as String
                    val property = EntityTypes.matDetails.getProperty(propertyName)
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
                    UIConstants.OP_CREATE -> resources.getString(R.string.title_create_fragment, EntityTypes.matDetails.localName)
                    else -> resources.getString(R.string.title_update_fragment) + " " + EntityTypes.matDetails.localName

                }
            }
        }

        activity?.let {
            (it as MatDetailsSetActivity).isNavigationDisabled = true
            viewModel = ViewModelProvider(it)[MatDetailsViewModel::class.java]
            viewModel.createResult.observe(this) { result -> onComplete(result) }
            viewModel.updateResult.observe(this) { result -> onComplete(result) }

            matDetailsEntity = if (operation == UIConstants.OP_CREATE) {
                createMatDetails()
            } else {
                viewModel.selectedEntity.value!!
            }

            val workingCopy = when{ (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) -> {
                    savedInstanceState?.getParcelable<MatDetails>(KEY_WORKING_COPY, MatDetails::class.java)
                } else -> @Suppress("DEPRECATION") savedInstanceState?.getParcelable<MatDetails>(KEY_WORKING_COPY)
            }

            if (workingCopy == null) {
                matDetailsEntityCopy = matDetailsEntity.copy()
                matDetailsEntityCopy.entityTag = matDetailsEntity.entityTag
                matDetailsEntityCopy.oldEntity = matDetailsEntity
                matDetailsEntityCopy.editLink = matDetailsEntity.editLink
            } else {
                matDetailsEntityCopy = workingCopy
            }
        }
        switchToSingleScanMode()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        currentActivity.findViewById<ObjectHeader>(R.id.objectHeader)?.let {
            it.visibility = View.GONE
        }
        fragmentBinding.matDetails = matDetailsEntityCopy
        return fragmentBinding.root
    }

    override  fun  initBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentMatdetailssetCreateBinding.inflate(inflater, container, false)

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

    override fun onBarcodeResult(barcode: String?) {
        TODO("Not yet implemented")
    }

    override fun onRFIDTagIDResult(tagID: String?) {

        tagidid.value = tagID
    }

    override fun onFixedReaderTagIDResult(tagID: String?) {
        TODO("Not yet implemented")
    }

    override fun onRFIDTagSearched(rssi: Short) {
        TODO("Not yet implemented")
    }
     fun givTag():String{
         return tagIDNEW
     }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(secondaryToolbar != null) secondaryToolbar!!.title = activityTitle else activity?.title = activityTitle
        tagidid=view.findViewById(R.id.tagidid)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_WORKING_COPY, matDetailsEntityCopy)
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
        if (!isMatDetailsValid) {
            return false
        }
        (currentActivity as MatDetailsSetActivity).isNavigationDisabled = false
        progressBar?.visibility = View.VISIBLE
        when (operation) {
            UIConstants.OP_CREATE -> {
                viewModel.create(matDetailsEntityCopy)
            }
            UIConstants.OP_UPDATE -> viewModel.update(matDetailsEntityCopy)
        }
        return true
    }

    /**
     * Create a new MatDetails instance and initialize properties to its default values
     * Nullable property will remain null
     * @return new MatDetails instance
     */
    private fun createMatDetails(): MatDetails {
        val entity = MatDetails(true)
        return entity
    }

    /** Callback function to complete processing when updateResult or createResult events fired */
    private fun onComplete(result: OperationResult<MatDetails>) {
        progressBar?.visibility = View.INVISIBLE
        enableUpdateMenuItem(true)
        if (result.error != null) {
            (currentActivity as MatDetailsSetActivity).isNavigationDisabled = true
            handleError(result)
        } else {
            if (operation == UIConstants.OP_UPDATE && !currentActivity.resources.getBoolean(R.bool.two_pane)) {
                viewModel.selectedEntity.value = matDetailsEntityCopy
            }
            (currentActivity as MatDetailsSetActivity).onBackPressedDispatcher.onBackPressed()
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
    private fun handleError(result: OperationResult<MatDetails>) {
        val errorMessage = when (result.operation) {
            OperationResult.Operation.UPDATE -> getString(R.string.update_failed_detail)
            OperationResult.Operation.CREATE -> getString(R.string.create_failed_detail)
            else -> throw AssertionError()
        }
        showError(errorMessage)
    }


    companion object {
        private val KEY_WORKING_COPY = "WORKING_COPY"
        private val LOGGER = LoggerFactory.getLogger(MatDetailsSetActivity::class.java)
    }
}
