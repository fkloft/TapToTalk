package com.github.fkloft.taptotalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class TaskerReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Bundle bundle = intent.getExtras().getBundle(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
		
		setState(context, bundle.getBoolean("enable"));
	}
	
	private void setState(Context context, boolean shouldRun)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		sharedPreferences
			.edit()
			.putBoolean("pref_start_service", shouldRun)
			.apply();
		
		boolean isRunning = OverlayService.isRunning();
		if(isRunning && !shouldRun)
		{
			context.stopService(new Intent(context, OverlayService.class));
		}
		else if(shouldRun && !isRunning)
		{
			context.startService(new Intent(context, OverlayService.class));
		}
	}
}
