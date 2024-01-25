package com.company.crfid_sap_btp.service

import com.sap.cloud.mobile.foundation.model.AppConfig
import com.sap.cloud.android.odata.espmcontainer.ESPMContainer
import com.sap.cloud.android.odata.zcims_int_srv_entities.ZCIMS_INT_SRV_Entities
import com.sap.cloud.mobile.foundation.common.ClientProvider
import com.sap.cloud.mobile.odata.OnlineODataProvider
import com.sap.cloud.mobile.odata.http.OKHttpHandler

class SAPServiceManager(private val appConfig: AppConfig) {

    var serviceRoot: String = ""
        private set
        get() {
            return (zCIMS_INT_SRV_Entities?.provider as OnlineODataProvider).serviceRoot
        }

    var eSPMContainer: ESPMContainer? = null
        private set
        get() {
            return field ?: throw IllegalStateException("SAPServiceManager was not initialized")
        }
    var zCIMS_INT_SRV_Entities: ZCIMS_INT_SRV_Entities? = null
        private set
        get() {
            return field ?: throw IllegalStateException("SAPServiceManager was not initialized")
        }

    fun openODataStore(callback: () -> Unit) {
        if( appConfig != null ) {
            appConfig.serviceUrl?.let { _serviceURL ->
                eSPMContainer = ESPMContainer (
                    OnlineODataProvider("SAPService", _serviceURL + CONNECTION_ID_ESPMCONTAINER).apply {
                        networkOptions.httpHandler = OKHttpHandler(ClientProvider.get())
                        serviceOptions.checkVersion = false
                        serviceOptions.requiresType = true
                        serviceOptions.cacheMetadata = false
                    }
                )
                zCIMS_INT_SRV_Entities = ZCIMS_INT_SRV_Entities (
                    OnlineODataProvider("SAPService", _serviceURL + CONNECTION_ID_ZCIMS_INT_SRV_ENTITIES).apply {
                        networkOptions.httpHandler = OKHttpHandler(ClientProvider.get())
                        serviceOptions.checkVersion = false
                        serviceOptions.requiresType = true
                        serviceOptions.cacheMetadata = false
                    }
                )
            } ?: run {
                throw IllegalStateException("ServiceURL of Configuration Data is not initialized")
            }
        }
        callback.invoke()
    }

    companion object {
        const val CONNECTION_ID_ESPMCONTAINER: String = "com.sap.edm.sampleservice.v2"
        const val CONNECTION_ID_ZCIMS_INT_SRV_ENTITIES: String = "com.sap.crfid.cRFID_Demo"
    }
}
