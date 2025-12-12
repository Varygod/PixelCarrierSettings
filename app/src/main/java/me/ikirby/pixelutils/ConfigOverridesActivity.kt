package me.ikirby.pixelutils

import android.app.Activity
import android.app.ActivityManager
import android.app.IActivityManager
import android.app.UiAutomationConnection
import android.content.ComponentName
import android.os.Bundle
import android.os.PersistableBundle
import android.os.ServiceManager
import android.telephony.CarrierConfigManager
import android.telephony.TelephonyManager
import android.view.MenuItem
import android.widget.Toast
import me.ikirby.pixelutils.databinding.ActivityConfigOverridesBinding
import rikka.shizuku.ShizukuBinderWrapper

class ConfigOverridesActivity : Activity() {

    private lateinit var binding: ActivityConfigOverridesBinding

    private var subId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConfigOverridesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subId = intent.getIntExtra("subId", 0)
        title = intent.getStringExtra("displayName") ?: ""
        actionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnOverrideVoLTE.setOnClickListener { overrideVoLTE() }
        binding.btnOverrideVoNR.setOnClickListener { overrideVoNR() }
        binding.btnOverrideNRMode.setOnClickListener { overrideNRMode() }
        binding.btnOverrideWFC.setOnClickListener { overrideWFC() }
        binding.btnOverride5GSignalThreshold.setOnClickListener { override5GSignalThreshold() }
        binding.btnOverrideCarrier.setOnClickListener { overrideCarrier() }
        binding.btnVerifyCarrier.setOnClickListener { verifyCarrier() }
        binding.btnResetConfig.setOnClickListener { resetConfig() }
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        finish()
        return true
    }

    private fun showToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun overrideConfig(overrides: PersistableBundle?) {
        val args = Bundle().apply {
            putInt("subId", subId)
            putParcelable("overrides", overrides)
            putBoolean("persistent", false)
        }
        val ams = IActivityManager.Stub.asInterface(
            ShizukuBinderWrapper(ServiceManager.getService(ACTIVITY_SERVICE))
        )
        ams.startInstrumentation(
            ComponentName(this, InstrumentationHelper::class.java),
            null,
            ActivityManager.INSTR_FLAG_NO_RESTART,
            args,
            null,
            UiAutomationConnection(),
            0,
            null
        )
        if (overrides == null) {
            showToast(R.string.config_reset)
        } else {
            showToast(R.string.config_updated)
        }
    }

    private fun resetConfig() {
        overrideConfig(null)
    }

    private fun overrideVoLTE() {
        val overrides = PersistableBundle().apply {
            putBoolean(CarrierConfigManager.KEY_CARRIER_VOLTE_AVAILABLE_BOOL, true)
        }
        overrideConfig(overrides)
    }

    private fun overrideVoNR() {
        val overrides = PersistableBundle().apply {
            putBoolean(CarrierConfigManager.KEY_VONR_ENABLED_BOOL, true)
            putBoolean(CarrierConfigManager.KEY_VONR_SETTING_VISIBILITY_BOOL, true)
        }
        overrideConfig(overrides)
    }

    private fun overrideNRMode() {
        val overrides = PersistableBundle().apply {
            putIntArray(
                CarrierConfigManager.KEY_CARRIER_NR_AVAILABILITIES_INT_ARRAY,
                intArrayOf(
                    CarrierConfigManager.CARRIER_NR_AVAILABILITY_NSA,
                    CarrierConfigManager.CARRIER_NR_AVAILABILITY_SA
                )
            )
        }
        overrideConfig(overrides)
    }

    private fun overrideWFC() {
        val overrides = PersistableBundle().apply {
            putBoolean(CarrierConfigManager.KEY_CARRIER_WFC_IMS_AVAILABLE_BOOL, true)
            putBoolean(CarrierConfigManager.KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL, true)
            putBoolean(CarrierConfigManager.KEY_EDITABLE_WFC_MODE_BOOL, true)
            putBoolean(CarrierConfigManager.KEY_EDITABLE_WFC_ROAMING_MODE_BOOL, true)
        }
        overrideConfig(overrides)
    }

    private fun override5GSignalThreshold() {
        val overrides = PersistableBundle().apply {
            putIntArray(
                CarrierConfigManager.KEY_5G_NR_SSRSRP_THRESHOLDS_INT_ARRAY,
                intArrayOf(-115, -105, -95, -85)
            )
        }
        overrideConfig(overrides)
    }

    private fun overrideCarrier() {
        val overrides = PersistableBundle().apply {
            // Set country ISO to "us"
            putString(CarrierConfigManager.KEY_SIM_COUNTRY_ISO_OVERRIDE_STRING, "us")
            // Enable carrier name override
            putBoolean(CarrierConfigManager.KEY_CARRIER_NAME_OVERRIDE_BOOL, true)
            // Set carrier name to "Verizon"
            putString(CarrierConfigManager.KEY_CARRIER_NAME_STRING, "Verizon")
        }
        overrideConfig(overrides)
    }

    private fun verifyCarrier() {
        val ccm = getSystemService(CarrierConfigManager::class.java)
        val config = ccm.getConfigForSubId(subId)

        val countryIso = config?.getString(CarrierConfigManager.KEY_SIM_COUNTRY_ISO_OVERRIDE_STRING)
        val carrierNameOverride = config?.getBoolean(CarrierConfigManager.KEY_CARRIER_NAME_OVERRIDE_BOOL)
        val carrierName = config?.getString(CarrierConfigManager.KEY_CARRIER_NAME_STRING)

        val tm = getSystemService(TelephonyManager::class.java)
        val currentCountryIso = tm.getSimCountryIso()
        val currentCarrierName = tm.getSimOperatorName()

        val message = """
            Carrier Config:
            Country ISO Override: $countryIso
            Carrier Name Override Enabled: $carrierNameOverride
            Carrier Name Override: $carrierName

            Current System Values:
            Current Country ISO: $currentCountryIso
            Current Carrier Name: $currentCarrierName
        """.trimIndent()

        showToast(message)
    }
}
