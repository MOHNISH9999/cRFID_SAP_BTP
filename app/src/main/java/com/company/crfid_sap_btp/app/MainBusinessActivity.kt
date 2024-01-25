package com.company.crfid_sap_btp.app

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import com.company.crfid_sap_btp.databinding.ActivityMainBusinessBinding
import com.company.crfid_sap_btp.R


import org.slf4j.LoggerFactory

import com.company.crfid_sap_btp.mdui.EntitySetListActivity

class MainBusinessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBusinessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBusinessBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    private fun startEntitySetListActivity() {
        val sapServiceManager = (application as SAPWizardApplication).sapServiceManager
        sapServiceManager?.openODataStore {
            val intent = Intent(this, EntitySetListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        startEntitySetListActivity()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MainBusinessActivity::class.java)
    }
}
