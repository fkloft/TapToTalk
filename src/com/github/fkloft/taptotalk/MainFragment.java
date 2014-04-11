package com.github.fkloft.taptotalk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.github.fkloft.taptotalk.OverlayService.Listener;

public class MainFragment extends PreferenceFragment implements Listener, OnSharedPreferenceChangeListener
{
	private ListPreference mPrefKeycode;
	private CheckBoxPreference mPrefMove;
	private SharedPreferences mPrefs;
	
	public void onCreate(android.os.Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		OverlayService.setListener(this);
		
		addPreferencesFromResource(R.xml.prefs);
		
		mPrefs = getPreferenceManager().getSharedPreferences();
		mPrefs.registerOnSharedPreferenceChangeListener(this);
		
		mPrefMove = (CheckBoxPreference) findPreference("pref_move");
		
		mPrefKeycode = (ListPreference) findPreference("pref_keycode");
		mPrefKeycode.setDefaultValue(Integer.toString(Utils.KEYCODE_DEFAULT));
		mPrefKeycode.setEntries(Utils.getKeycodeLabels(getActivity()));
		mPrefKeycode.setEntryValues(Utils.getKeycodeValues());
		
		for(String key : new String[] {
			"pref_start_service",
			"pref_move",
			"pref_keycode",
			"pref_padding"
		})
			onSharedPreferenceChanged(mPrefs, key);
	};
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		OverlayService.setListener(null);
		mPrefs.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		mPrefs
			.edit()
			.putBoolean("pref_move", false)
			.apply();
	}
	
	@Override
	public void onServiceStateChanged(boolean running)
	{
		onSharedPreferenceChanged(mPrefs, "pref_start_service");
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		Preference preference = findPreference(key);
		
		if("pref_start_service".equals(key))
		{
			Activity activity = getActivity();
			
			boolean shouldRun = sharedPreferences.getBoolean(key, false);
			boolean isRunning = OverlayService.isRunning();
			if(isRunning && !shouldRun)
			{
				activity.stopService(new Intent(activity, OverlayService.class));
				preference.setSummary(R.string.pref_start_service_summary_stopping);
			}
			else if(shouldRun && !isRunning)
			{
				activity.startService(new Intent(activity, OverlayService.class));
				preference.setSummary(R.string.pref_start_service_summary_starting);
			}
			else if(shouldRun && isRunning)
				preference.setSummary(R.string.pref_start_service_summary_started);
			else
				preference.setSummary(R.string.pref_start_service_summary_stopped);
			
			((CheckBoxPreference) preference).setChecked(shouldRun);
			preference.setEnabled(isRunning == shouldRun);
			mPrefMove.setEnabled(isRunning && shouldRun);
		}
		if("pref_keycode".equals(key))
		{
			try
			{
				int value = Integer.parseInt(mPrefs.getString(key, Integer.toString(Utils.KEYCODE_DEFAULT)));
				preference.setSummary(Utils.KEYCODE_LABELS.get(value));
				preference.setIcon(Utils.KEYCODE_ICONS.get(value));
			}
			catch(NumberFormatException e)
			{
				preference.setSummary("");
			}
		}
		
		mPrefMove.setSummary(
			OverlayService.isRunning()
					? mPrefMove.isChecked()
							? R.string.pref_move_summary_checked
							: R.string.pref_move_summary_unchecked
					: R.string.pref_move_summary_disabled);
	}
}
