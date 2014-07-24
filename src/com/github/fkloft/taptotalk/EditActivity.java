package com.github.fkloft.taptotalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class EditActivity extends Activity implements OnCheckedChangeListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		
		RadioGroup rg = (RadioGroup) findViewById(R.id.rg_main);
		rg.setOnCheckedChangeListener(this);
		
		Bundle extras = getIntent().getExtras();
		if(extras == null)
		{
			finish();
			return;
		}
		Bundle bundle = extras.getBundle(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
		
		boolean enable = bundle == null ? true : bundle.getBoolean("enable");
		
		int checkedId = enable ? R.id.rb_enable : R.id.rb_disable;
		
		((RadioButton) findViewById(checkedId)).setChecked(true);
		onCheckedChanged(rg, checkedId);
	}
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId)
	{
		boolean enable = ((RadioButton) findViewById(R.id.rb_enable)).isChecked();
		String label = getString(enable ? R.string.title_enable : R.string.title_disable);
		Bundle bundle = new Bundle();
		bundle.putBoolean("enable", enable);
		
		setResult(
			RESULT_OK,
			new Intent()
				.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, label)
				.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, bundle));
	}
}
