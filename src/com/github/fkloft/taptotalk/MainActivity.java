package com.github.fkloft.taptotalk;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MainActivity extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		MainFragment fragment = new MainFragment();
		fragment.setArguments(getIntent().getExtras());
		
		getFragmentManager()
			.beginTransaction()
			.replace(android.R.id.content, fragment)
			.commit();
	}
}
