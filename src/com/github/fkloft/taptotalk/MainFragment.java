package com.github.fkloft.taptotalk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.KeyEvent;

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
		mPrefKeycode.setDefaultValue(Integer.toString(KeyEvent.KEYCODE_MEDIA_RECORD));
		mPrefKeycode.setEntries(Utils.getKeycodeLabels(getActivity()));
		mPrefKeycode.setEntryValues(Utils.getKeycodeValues());
		
		for(String key : new String[] {
			"pref_start_service",
			"pref_move",
			"pref_keycode"
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
				preference.setSummary("Stopping...");// TODO l10n
			}
			else if(shouldRun && !isRunning)
			{
				activity.startService(new Intent(activity, OverlayService.class));
				preference.setSummary("Starting...");// TODO l10n
			}
			else if(shouldRun && isRunning)
				preference.setSummary("Running.");// TODO l10n
			else
				preference.setSummary("Select to start service");// TODO l10n
				
			preference.setEnabled(isRunning == shouldRun);
			mPrefMove.setEnabled(isRunning && shouldRun);
		}
		if("pref_keycode".equals(key))
		{
			try
			{
				int value = Integer.parseInt(mPrefs.getString(key, Integer.toString(KeyEvent.KEYCODE_MEDIA_RECORD)));
				preference.setSummary(Utils.KEYCODE_LABELS.get(value));
			}
			catch(NumberFormatException e)
			{
				preference.setSummary("");
			}
		}
	}
}
