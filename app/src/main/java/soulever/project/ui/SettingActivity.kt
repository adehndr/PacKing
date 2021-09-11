package soulever.project.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import soulever.project.R
import soulever.project.databinding.ActivitySettingBinding
import soulever.project.ui.fragment.PreferencesFragment

class SettingActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar()?.setCustomView(R.layout.action_bar);
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        showPreferenceFragment()

    }

    private fun showPreferenceFragment() {
        supportFragmentManager.beginTransaction().add(R.id.activity_setting, PreferencesFragment())
            .commit()
        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreference.registerOnSharedPreferenceChangeListener(this)

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        TODO("Not yet implemented")
    }


}