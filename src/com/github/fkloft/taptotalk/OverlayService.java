package com.github.fkloft.taptotalk;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class OverlayService extends Service implements OnSharedPreferenceChangeListener
{
	private static OverlayService instance = null;
	private static Listener listener;
	private static final int NOTIFICATION_MAIN = 1;
	private static final int REQUEST_MAIN = 1;
	
	public static boolean isRunning()
	{
		return instance != null;
	}
	
	public static void setListener(Listener listener)
	{
		OverlayService.listener = listener;
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			onConfigurationChange(intent);
		}
	};
	private OverlayButton mButton;
	private boolean mDragging = false;
	private PointF mDragOffset = new PointF(0, 0);
	private int mKeyCode = KeyEvent.KEYCODE_MEDIA_RECORD;
	private boolean mLandscape;
	private LayoutParams mLayoutParams;
	private PointF mPosition = new PointF(0, 0);
	private SharedPreferences mPrefs;
	private boolean mPressed = false;
	private WindowManager mWindowManager;
	
	public OverlayService()
	{}
	
	private void onConfigurationChange(Intent intent)
	{
		boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		if(landscape != mLandscape)
		{
			if(mDragging)
			{
				writePosition();
				mDragging = false;
			}
			
			mLandscape = landscape;
			readPosition();
			updateLayout();
		}
	}
	
	private void readPosition()
	{
		if(mLandscape)
		{
			mPosition.x = mPrefs.getFloat("pref_pos_landscape_x", 0);
			mPosition.y = mPrefs.getFloat("pref_pos_landscape_y", 0);
		}
		else
		{
			mPosition.x = mPrefs.getFloat("pref_pos_portrait_x", 0);
			mPosition.y = mPrefs.getFloat("pref_pos_portrait_y", 0);
		}
	}
	
	private void sendKeyEvent(boolean pressed)
	{
		if(pressed == mPressed)
			return;
		mPressed = pressed;
		
		long time = SystemClock.uptimeMillis();
		
		KeyEvent event = new KeyEvent(time, time, pressed ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP, mKeyCode, 0);
		Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, event);
		// Note that sendOrderedBroadcast is needed since there is only
		// one official receiver of the media button intents at a time
		// (controlled via AudioManager) so the system needs to figure
		// out who will handle it rather than just send it to everyone.
		sendOrderedBroadcast(intent, null);
	}
	
	private void updateLayout()
	{
		mLayoutParams.x = (int) mPosition.x;
		mLayoutParams.y = (int) mPosition.y;
		mWindowManager.updateViewLayout(mButton, mLayoutParams);
	}
	
	private void writePosition()
	{
		if(mLandscape)
		{
			mPrefs
				.edit()
				.putFloat("pref_pos_landscape_x", mPosition.x)
				.putFloat("pref_pos_landscape_y", mPosition.y)
				.apply();
		}
		else
		{
			mPrefs
				.edit()
				.putFloat("pref_pos_portrait_x", mPosition.x)
				.putFloat("pref_pos_portrait_y", mPosition.y)
				.apply();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	public void onButtonPressed(boolean pressed)
	{
		if(mButton.isPressed() != mPressed)
			sendKeyEvent(mButton.isPressed());
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		instance = this;
		if(listener != null)
			listener.onServiceStateChanged(true);
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mPrefs.registerOnSharedPreferenceChangeListener(this);
		
		onSharedPreferenceChanged(mPrefs, "pref_keycode");
		
		mLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		readPosition();
		
		startForeground(NOTIFICATION_MAIN, new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(getString(R.string.app_name))
			.setContentText("Overlay enabled. Tap to configure") // TODO l10n
			.setPriority(NotificationCompat.PRIORITY_MIN)
			.setContentIntent(PendingIntent.getActivity(this, REQUEST_MAIN, new Intent(this, MainActivity.class), 0))
			.build());
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
		registerReceiver(mBroadcastReceiver, filter);
		
		mLayoutParams = new WindowManager.LayoutParams(
			WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
			WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
			PixelFormat.TRANSLUCENT);
		
		mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		mLayoutParams.x = (int) mPosition.x;
		mLayoutParams.y = (int) mPosition.y;
		
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		
		mButton = (OverlayButton) inflater.inflate(R.layout.overlay_button, null);
		mButton.setService(this);
		
		// Add layout to window manager
		mWindowManager.addView(mButton, mLayoutParams);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		instance = null;
		if(listener != null)
			listener.onServiceStateChanged(false);
		
		stopForeground(true);
		
		mPrefs.unregisterOnSharedPreferenceChangeListener(this);
		unregisterReceiver(mBroadcastReceiver);
		
		if(mPressed)
			sendKeyEvent(false);
		
		mWindowManager.removeView(mButton);
	}
	
	public void onDragEnd(PointF point)
	{
		if(!mDragging)
			return;
		mDragging = false;
		mPosition.x = point.x - mDragOffset.x;
		mPosition.y = point.y - mDragOffset.y;
		
		writePosition();
		updateLayout();
	}
	
	public void onDragMove(PointF point)
	{
		if(!mDragging)
			return;
		mPosition.x = point.x - mDragOffset.x;
		mPosition.y = point.y - mDragOffset.y;
		
		updateLayout();
	}
	
	public void onDragStart(PointF point)
	{
		mDragging = true;
		mDragOffset.x = point.x - mPosition.x;
		mDragOffset.y = point.y - mPosition.y;
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if("pref_move".equals(key))
		{
			boolean move = mPrefs.getBoolean(key, false);
			if(move && mPressed)
				sendKeyEvent(false);
			if(!move)
			{
				if(mDragging)
					writePosition();
				mDragging = false;
			}
			mButton.setDragable(move);
		}
		if("pref_keycode".equals(key))
		{
			try
			{
				if(mPressed)
					sendKeyEvent(false);
				
				mKeyCode = Integer.parseInt(mPrefs.getString(key, Integer.toString(KeyEvent.KEYCODE_MEDIA_RECORD)));
				mButton.setImageResource(Utils.KEYCODE_ICONS.get(mKeyCode));
				mButton.setContentDescription(getString(Utils.KEYCODE_LABELS.get(mKeyCode)));
			}
			catch(NumberFormatException e)
			{}
		}
	}
	
	public static interface Listener
	{
		public void onServiceStateChanged(boolean running);
	}
}
