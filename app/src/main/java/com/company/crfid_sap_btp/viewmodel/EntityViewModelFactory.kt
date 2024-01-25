package com.company.crfid_sap_btp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.os.Parcelable

import com.company.crfid_sap_btp.viewmodel.inpoheader.InPoHeaderViewModel
import com.company.crfid_sap_btp.viewmodel.inpoitem.InPoItemViewModel
import com.company.crfid_sap_btp.viewmodel.inpo.InPoViewModel
import com.company.crfid_sap_btp.viewmodel.inpostloc.INPOStLocViewModel
import com.company.crfid_sap_btp.viewmodel.inprintmedia.InPrintMediaViewModel
import com.company.crfid_sap_btp.viewmodel.inprintpoitem.InPrintPoItemViewModel
import com.company.crfid_sap_btp.viewmodel.inprintpo.InPrintPoViewModel
import com.company.crfid_sap_btp.viewmodel.inreprintpo.InReprintPoViewModel
import com.company.crfid_sap_btp.viewmodel.inreprintrsn.InReprintRsnViewModel
import com.company.crfid_sap_btp.viewmodel.instockovw.InStockOvwViewModel
import com.company.crfid_sap_btp.viewmodel.intmattomatse.INTMattomatSeViewModel
import com.company.crfid_sap_btp.viewmodel.intmattomat.INTMattomatViewModel
import com.company.crfid_sap_btp.viewmodel.intmtomchargvalid.INTMtomChargvalidViewModel
import com.company.crfid_sap_btp.viewmodel.intphyinvpost.IntPhyInvPostViewModel
import com.company.crfid_sap_btp.viewmodel.intphyinv.IntPhyInvViewModel
import com.company.crfid_sap_btp.viewmodel.intphyvalid.IntPhyValidViewModel
import com.company.crfid_sap_btp.viewmodel.intsloctosloc.IntSlocToSlocViewModel
import com.company.crfid_sap_btp.viewmodel.intslocvalid.IntSlocValidViewModel
import com.company.crfid_sap_btp.viewmodel.loginsrv.LoginSrvViewModel
import com.company.crfid_sap_btp.viewmodel.matdetails.MatDetailsViewModel
import com.company.crfid_sap_btp.viewmodel.outgiscrap.OutGiScrapViewModel
import com.company.crfid_sap_btp.viewmodel.outgiso.OutGiSoViewModel
import com.company.crfid_sap_btp.viewmodel.outpicking1.OutPicking1ViewModel
import com.company.crfid_sap_btp.viewmodel.outpicking.OutPickingViewModel
import com.company.crfid_sap_btp.viewmodel.outgireservation.OutGIReservationViewModel
import com.company.crfid_sap_btp.viewmodel.rfmattag.RfMattagViewModel

/**
 * Custom factory class, which can create view models for entity subsets, which are
 * reached from a parent entity through a navigation property.
 *
 * @param application parent application
 * @param navigationPropertyName name of the navigation link
 * @param entityData parent entity
 */
class EntityViewModelFactory (
        val application: Application, // name of the navigation property
        val navigationPropertyName: String, // parent entity
        val entityData: Parcelable) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass.simpleName) {
			"InPoHeaderViewModel" -> InPoHeaderViewModel(application, navigationPropertyName, entityData) as T
            			"InPoItemViewModel" -> InPoItemViewModel(application, navigationPropertyName, entityData) as T
            			"InPoViewModel" -> InPoViewModel(application, navigationPropertyName, entityData) as T
            			"INPOStLocViewModel" -> INPOStLocViewModel(application, navigationPropertyName, entityData) as T
            			"InPrintMediaViewModel" -> InPrintMediaViewModel(application, navigationPropertyName, entityData) as T
            			"InPrintPoItemViewModel" -> InPrintPoItemViewModel(application, navigationPropertyName, entityData) as T
            			"InPrintPoViewModel" -> InPrintPoViewModel(application, navigationPropertyName, entityData) as T
            			"InReprintPoViewModel" -> InReprintPoViewModel(application, navigationPropertyName, entityData) as T
            			"InReprintRsnViewModel" -> InReprintRsnViewModel(application, navigationPropertyName, entityData) as T
            			"InStockOvwViewModel" -> InStockOvwViewModel(application, navigationPropertyName, entityData) as T
            			"INTMattomatSeViewModel" -> INTMattomatSeViewModel(application, navigationPropertyName, entityData) as T
            			"INTMattomatViewModel" -> INTMattomatViewModel(application, navigationPropertyName, entityData) as T
            			"INTMtomChargvalidViewModel" -> INTMtomChargvalidViewModel(application, navigationPropertyName, entityData) as T
            			"IntPhyInvPostViewModel" -> IntPhyInvPostViewModel(application, navigationPropertyName, entityData) as T
            			"IntPhyInvViewModel" -> IntPhyInvViewModel(application, navigationPropertyName, entityData) as T
            			"IntPhyValidViewModel" -> IntPhyValidViewModel(application, navigationPropertyName, entityData) as T
            			"IntSlocToSlocViewModel" -> IntSlocToSlocViewModel(application, navigationPropertyName, entityData) as T
            			"IntSlocValidViewModel" -> IntSlocValidViewModel(application, navigationPropertyName, entityData) as T
            			"LoginSrvViewModel" -> LoginSrvViewModel(application, navigationPropertyName, entityData) as T
            			"MatDetailsViewModel" -> MatDetailsViewModel(application, navigationPropertyName, entityData) as T
            			"OutGiScrapViewModel" -> OutGiScrapViewModel(application, navigationPropertyName, entityData) as T
            			"OutGiSoViewModel" -> OutGiSoViewModel(application, navigationPropertyName, entityData) as T
            			"OutPicking1ViewModel" -> OutPicking1ViewModel(application, navigationPropertyName, entityData) as T
            			"OutPickingViewModel" -> OutPickingViewModel(application, navigationPropertyName, entityData) as T
            			"OutGIReservationViewModel" -> OutGIReservationViewModel(application, navigationPropertyName, entityData) as T
             else -> RfMattagViewModel(application, navigationPropertyName, entityData) as T
        }
    }
}
