package com.company.crfid_sap_btp.mdui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.company.crfid_sap_btp.databinding.ActivitySettingsBinding

class SettingsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val settingsFragment = SettingsFragment()
        settingsFragment.retainInstance = true
        supportFragmentManager.beginTransaction().replace(binding.settingsContainer.id, settingsFragment).commit()
    }
}