package com.github.fkloft.taptotalk;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

public class OverlayButton extends ImageButton
{
	private boolean mMove = false;
	private OverlayService mOverlayService;
	
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
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if(mOverlayService != null && mMove)
		{
			PointF point = new PointF(event.getRawX(), event.getRawY());
			if(event.getActionMasked() == MotionEvent.ACTION_DOWN)
				mOverlayService.onDragStart(point);
			if(event.getActionMasked() == MotionEvent.ACTION_MOVE)
				mOverlayService.onDragMove(point);
			if(event.getActionMasked() == MotionEvent.ACTION_UP)
				mOverlayService.onDragEnd(point);
		}
		return super.onTouchEvent(event);
	}
	
	public void setDragable(boolean move)
	{
		mMove = move;
	}
	
	@Override
	public void setPressed(boolean pressed)
	{
		super.setPressed(pressed);
		if(mOverlayService != null && !mMove)
			mOverlayService.onButtonPressed(pressed);
	}
	
	public void setService(OverlayService overlayService)
	{
		mOverlayService = overlayService;
	}
}
