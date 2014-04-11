package com.github.fkloft.taptotalk;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class OverlayButton extends ImageButton
{
	private OnPressedHandler mSetPressedHandler;
	
	public OverlayButton(Context context)
	{
		super(context);
	}
	
	public OverlayButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	
	public OverlayButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	
	public void setOnPressedHandler(OnPressedHandler onPressedHandler)
	{
		this.mSetPressedHandler = onPressedHandler;
	}
	
	@Override
	public void setPressed(boolean pressed)
	{
		super.setPressed(pressed);
		if(mSetPressedHandler != null)
			mSetPressedHandler.onPressed(pressed);
	}
	
	public static interface OnPressedHandler
	{
		public void onPressed(boolean pressed);
	}
}
