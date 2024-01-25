package com.company.crfid_sap_btp.app

import android.content.Intent
import com.sap.cloud.mobile.flowv2.ext.FlowStateListener
import com.sap.cloud.mobile.foundation.model.AppConfig
import android.widget.Toast
import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler
import com.sap.cloud.mobile.foundation.settings.policies.ClientPolicies
import com.sap.cloud.mobile.foundation.settings.policies.LogPolicy
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Level
import com.company.crfid_sap_btp.R

class WizardFlowStateListener(private val application: SAPWizardApplication) :
    FlowStateListener() {

    private var userSwitchFlag = false

    override fun onAppConfigRetrieved(appConfig: AppConfig) {
        logger.debug("onAppConfigRetrieved: $appConfig")
        application.initializeServiceManager(appConfig)
    }

    override fun onApplicationReset() {
        this.application.resetApplication()
    }

    override fun onApplicationLocked() {
        super.onApplicationLocked()
        application.isApplicationUnlocked = false
    }

    override fun onFlowFinished(flowName: String?) {
        flowName?.let{
            application.isApplicationUnlocked = true
        }

        if (userSwitchFlag) {
            Intent(application, MainBusinessActivity::class.java).also {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                application.startActivity(it)
            }
        }
    }

    override fun onClientPolicyRetrieved(policies: ClientPolicies) {
        policies.logPolicy?.also { logSettings ->
            val sharedPreferences = application.preferenceManager
            val existing =
                    sharedPreferences.getString(SAPWizardApplication.KEY_LOG_SETTING_PREFERENCE, "")
            val currentSettings = if (existing.isNullOrEmpty()) {
                LogPolicy()
            } else {
                LogPolicy.createFromJsonString(existing)
            }
            if (currentSettings.logLevel != logSettings.logLevel || existing.isNullOrEmpty()) {
                val editor = sharedPreferences.edit()
                editor.putString(
                    SAPWizardApplication.KEY_LOG_SETTING_PREFERENCE,
                    logSettings.toString()
                )
                editor.apply()
                AppLifecycleCallbackHandler.getInstance().activity?.let {
                    it.runOnUiThread {
                        val logString = when (LogPolicy.getLogLevel(logSettings)) {
                            Level.ALL -> application.getString(R.string.log_level_path)
                            Level.INFO -> application.getString(R.string.log_level_info)
                            Level.WARN -> application.getString(R.string.log_level_warning)
                            Level.ERROR -> application.getString(R.string.log_level_error)
                            Level.OFF -> application.getString(R.string.log_level_none)
                            else -> application.getString(R.string.log_level_debug)
                        }
                        Toast.makeText(
                                application,
                                String.format(
                                        application.getString(R.string.log_level_changed),
                                        logString
                                ),
                                Toast.LENGTH_SHORT
                        ).show()
                        logger.info(String.format(
                                application.getString(R.string.log_level_changed),
                                logString
                        ))
                    }
                }
            }
        }
    }


    companion object {
        private val logger = LoggerFactory.getLogger(WizardFlowStateListener::class.java)
    }
}
