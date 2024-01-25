package com.company.crfid_sap_btp.mdui.loginsrvset

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.company.crfid_sap_btp.databinding.FragmentLoginsrvsetDetailBinding
import com.company.crfid_sap_btp.mdui.EntityKeyUtil
import com.company.crfid_sap_btp.mdui.InterfacedFragment
import com.company.crfid_sap_btp.mdui.UIConstants
import com.company.crfid_sap_btp.repository.OperationResult
import com.company.crfid_sap_btp.R
import com.company.crfid_sap_btp.viewmodel.loginsrv.LoginSrvViewModel
import com.sap.cloud.android.odata.zcims_int_srv_entities.ZCIMS_INT_SRV_EntitiesMetadata.EntitySets
import com.sap.cloud.android.odata.zcims_int_srv_entities.LoginSrv
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader


/**
 * A fragment representing a single LoginSrv detail screen.
 * This fragment is contained in an LoginSrvSetActivity.
 */
class LoginSrvSetDetailFragment : InterfacedFragment<LoginSrv, FragmentLoginsrvsetDetailBinding>() {

    /** LoginSrv entity to be displayed */
    private lateinit var loginSrvEntity: LoginSrv

    /** Fiori ObjectHeader component used when entity is to be displayed on phone */
    private var objectHeader: ObjectHeader? = null

    /** View model of the entity type that the displayed entity belongs to */
    private lateinit var viewModel: LoginSrvViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menu = R.menu.itemlist_view_options
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        fragmentBinding.handler = this
        return fragmentBinding.root
    }

    override  fun  initBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentLoginsrvsetDetailBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            currentActivity = it
            viewModel = ViewModelProvider(it)[LoginSrvViewModel::class.java]
            viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
                onDeleteComplete(result)
            }

            viewModel.selectedEntity.observe(viewLifecycleOwner) { entity ->
                loginSrvEntity = entity
                fragmentBinding.loginSrv = entity
                setupObjectHeader()
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.update_item -> {
                listener?.onFragmentStateChange(UIConstants.EVENT_EDIT_ITEM, loginSrvEntity)
                true
            }
            R.id.delete_item -> {
                listener?.onFragmentStateChange(UIConstants.EVENT_ASK_DELETE_CONFIRMATION,null)
                true
            }
            else -> super.onMenuItemSelected(menuItem)
        }
    }

    /**
     * Completion callback for delete operation
     *
     * @param [result] of the operation
     */
    private fun onDeleteComplete(result: OperationResult<LoginSrv>) {
        progressBar?.let {
            it.visibility = View.INVISIBLE
        }
        viewModel.removeAllSelected()
        result.error?.let {
            showError(getString(R.string.delete_failed_detail))
            return
        }
        listener?.onFragmentStateChange(UIConstants.EVENT_DELETION_COMPLETED, loginSrvEntity)
    }


    /**
     * Set detail image of ObjectHeader.
     * When the entity does not provides picture, set the first character of the masterProperty.
     */
    private fun setDetailImage(objectHeader: ObjectHeader, loginSrvEntity: LoginSrv) {
        if (loginSrvEntity.getOptionalValue(LoginSrv.compCode) != null && !loginSrvEntity.getOptionalValue(LoginSrv.compCode).toString().isEmpty()) {
            objectHeader.detailImageCharacter = loginSrvEntity.getOptionalValue(LoginSrv.compCode).toString().substring(0, 1)
        } else {
            objectHeader.detailImageCharacter = "?"
        }
    }

    /**
     * Setup ObjectHeader with an instance of loginSrvEntity
     */
    private fun setupObjectHeader() {
        val secondToolbar = currentActivity.findViewById<Toolbar>(R.id.secondaryToolbar)
        if (secondToolbar != null) {
            secondToolbar.title = loginSrvEntity.entityType.localName
        } else {
            currentActivity.title = loginSrvEntity.entityType.localName
        }

        // Object Header is not available in tablet mode
        objectHeader = currentActivity.findViewById(R.id.objectHeader)
        val dataValue = loginSrvEntity.getOptionalValue(LoginSrv.compCode)

        objectHeader?.let {
            it.apply {
                headline = dataValue?.toString()
                subheadline = EntityKeyUtil.getOptionalEntityKey(loginSrvEntity)
                body = "You can set the header body text here."
                footnote = "You can set the header footnote here."
                description = "You can add a detailed item description here."
            }
            it.setTag("#tag1", 0)
            it.setTag("#tag3", 2)
            it.setTag("#tag2", 1)

            setDetailImage(it, loginSrvEntity)
            it.visibility = View.VISIBLE
        }
    }
}
