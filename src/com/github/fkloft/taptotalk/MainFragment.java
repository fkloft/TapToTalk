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
	private SharedPreferences mPrefs;
	private CheckBoxPreference mPrefStartStop;
	private CheckBoxPreference mPrefMove;
	private ListPreference mPrefKeycode;
	
	public void onCreate(android.os.Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		OverlayService.setListener(this);
		
		addPreferencesFromResource(R.xml.prefs);
		
		mPrefs = getPreferenceManager().getSharedPreferences();
		mPrefs.registerOnSharedPreferenceChangeListener(this);
		
		mPrefStartStop = (CheckBoxPreference) findPreference("pref_start_service");
		
		mPrefMove = (CheckBoxPreference) findPreference("pref_move");
		
		mPrefKeycode = (ListPreference) findPreference("pref_keycode");
		mPrefKeycode.setDefaultValue(KeyEvent.KEYCODE_MEDIA_RECORD);
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
		else if("pref_keycode".equals(key))
		{
			try
			{
				int value = Integer.parseInt(mPrefs.getString(key, null));
				preference.setSummary(Utils.KEYCODE_LABELS.get(value));
			}
			catch(NumberFormatException e)
			{
				preference.setSummary("");
			}
		}
	}
}
