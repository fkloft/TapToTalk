/**
 * @author Kirk Baucom
 * @homepage http://robobunny.com/wp/2013/08/24/android-seekbar-preference-v2/
 * @license public domain
 * @contributor Felix Kloft
 */

package com.robobunny;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.github.fkloft.taptotalk.R;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener
{
	private static final String TAG = SeekBarPreference.class.getName();
	
	private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";
	private static final String APPLICATIONNS = "http://robobunny.com";
	private static final int DEFAULT_VALUE = 50;
	
	private int mMaxValue = 100;
	private int mMinValue = 0;
	private int mInterval = 1;
	private int mCurrentValue;
	private String mUnitsLeft = "";
	private String mUnitsRight = "";
	private SeekBar mSeekBar;
	
	private TextView mStatusText;
	
	public SeekBarPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initPreference(context, attrs);
	}
	
	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initPreference(context, attrs);
	}
	
	private void initPreference(Context context, AttributeSet attrs)
	{
		setValuesFromXml(attrs);
		mSeekBar = new SeekBar(context, attrs);
		mSeekBar.setMax(mMaxValue - mMinValue);
		mSeekBar.setOnSeekBarChangeListener(this);
		
		setWidgetLayoutResource(R.layout.seek_bar_preference);
	}
	
	private void setValuesFromXml(AttributeSet attrs)
	{
		mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", 100);
		mMinValue = attrs.getAttributeIntValue(APPLICATIONNS, "min", 0);
		
		mUnitsLeft = getAttributeStringValue(attrs, APPLICATIONNS, "unitsLeft", "");
		String units = getAttributeStringValue(attrs, APPLICATIONNS, "units", "");
		mUnitsRight = getAttributeStringValue(attrs, APPLICATIONNS, "unitsRight", units);
		
		try
		{
			String newInterval = attrs.getAttributeValue(APPLICATIONNS, "interval");
			if(newInterval != null)
				mInterval = Integer.parseInt(newInterval);
		}
		catch(Exception e)
		{
			Log.e(TAG, "Invalid interval value", e);
		}
		
	}
	
	private String getAttributeStringValue(AttributeSet attrs, String namespace, String name, String defaultValue)
	{
		String value = attrs.getAttributeValue(namespace, name);
		if(value == null)
			value = defaultValue;
		
		return value;
	}
	
	@Override
	protected View onCreateView(ViewGroup parent)
	{
		View view = super.onCreateView(parent);
		
		LinearLayout container = (LinearLayout) mSeekBar.getParent();
		if(container != null)
			container.removeView(mSeekBar);
		
		container = new LinearLayout(getContext());
		container.setOrientation(LinearLayout.VERTICAL);
		container.setMinimumHeight(view.getMinimumHeight());
		
		view.setMinimumHeight(0);
		
		container.addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		container.addView(mSeekBar, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		return container;
	}
	
	@Override
	public void onBindView(View view)
	{
		super.onBindView(view);
		
		updateView(view);
	}
	
	/**
	 * Update a SeekBarPreference view with our current state
	 * 
	 * @param view
	 */
	protected void updateView(View view)
	{
		
		try
		{
			mStatusText = (TextView) view.findViewById(R.id.seekBarPrefValue);
			
			mStatusText.setText(String.valueOf(mCurrentValue));
			mStatusText.setMinimumWidth(30);
			
			mSeekBar.setProgress(mCurrentValue - mMinValue);
			
			TextView unitsRight = (TextView) view.findViewById(R.id.seekBarPrefUnitsRight);
			unitsRight.setText(mUnitsRight);
			
			TextView unitsLeft = (TextView) view.findViewById(R.id.seekBarPrefUnitsLeft);
			unitsLeft.setText(mUnitsLeft);
			
		}
		catch(Exception e)
		{
			Log.e(TAG, "Error updating seek bar preference", e);
		}
		
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		int newValue = progress + mMinValue;
		
		if(newValue > mMaxValue)
			newValue = mMaxValue;
		else if(newValue < mMinValue)
			newValue = mMinValue;
		else if(mInterval != 1 && newValue % mInterval != 0)
			newValue = Math.round(((float) newValue) / mInterval) * mInterval;
		
		// change rejected, revert to the previous value
		if(!callChangeListener(newValue))
		{
			seekBar.setProgress(mCurrentValue - mMinValue);
			return;
		}
		
		// change accepted, store it
		mCurrentValue = newValue;
		mStatusText.setText(String.valueOf(newValue));
		persistInt(newValue);
		
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		notifyChanged();
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray ta, int index)
	{
		
		int defaultValue = ta.getInt(index, DEFAULT_VALUE);
		return defaultValue;
		
	}
	
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
	{
		
		if(restoreValue)
		{
			mCurrentValue = getPersistedInt(mCurrentValue);
		}
		else
		{
			int temp = 0;
			try
			{
				temp = (Integer) defaultValue;
			}
			catch(Exception ex)
			{
				Log.e(TAG, "Invalid default value: " + defaultValue.toString());
			}
			
			persistInt(temp);
			mCurrentValue = temp;
		}
		
	}
	
	/**
	 * make sure that the seekbar is disabled if the preference is disabled
	 */
	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		mSeekBar.setEnabled(enabled);
	}
	
	@Override
	public void onDependencyChanged(Preference dependency, boolean disableDependent)
	{
		super.onDependencyChanged(dependency, disableDependent);
		
		// Disable movement of seek bar when dependency is false
		if(mSeekBar != null)
		{
			mSeekBar.setEnabled(!disableDependent);
		}
	}
}
