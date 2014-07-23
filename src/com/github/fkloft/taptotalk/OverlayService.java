package com.github.fkloft.taptotalk;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
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
	private static final String ACTION_HIDE = "com.github.kloft.taptotalk.ACTION_HIDE";
	private static final int NOTIFICATION_MAIN = 1;
	private static final int REQUEST_MAIN = 1;
	private static final int REQUEST_CLOSE = 2;
	
	private static OverlayService instance = null;
	private static Listener listener;
	
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
			if(Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction()))
			{
				onConfigurationChange(intent);
			}
			else if(ACTION_HIDE.equals(intent.getAction()))
			{
				mPrefs
					.edit()
					.putBoolean("pref_start_service", false)
					.apply();
				stopSelf();
			}
		}
	};
	private OverlayButton mButton;
	private ComponentName mComponent = null;
	private boolean mDragging = false;
	private PointF mDragOffset = new PointF(0, 0);
	private int mKeyCode = Utils.KEYCODE_DEFAULT;
	private boolean mLandscape;
	private LayoutParams mLayoutParams;
	private Notification mNotification;
	private PointF mPosition = new PointF(0, 0);
	private SharedPreferences mPrefs;
	private boolean mPressed = false;
	private boolean mToggle = false;
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
		intent.setComponent(mComponent);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, event);
		// Note that sendOrderedBroadcast is needed since there is only
		// one official receiver of the media button intents at a time
		// (controlled via AudioManager) so the system needs to figure
		// out who will handle it rather than just send it to everyone.
		sendOrderedBroadcast(intent, null);
	}
	
	private void sendToggleEvent()
	{
		long time = SystemClock.uptimeMillis();
		
		// Note that sendOrderedBroadcast is needed since there is only
		// one official receiver of the media button intents at a time
		// (controlled via AudioManager) so the system needs to figure
		// out who will handle it rather than just send it to everyone.
		
		KeyEvent event = new KeyEvent(time, time, KeyEvent.ACTION_DOWN, mKeyCode, 0);
		Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent.setComponent(mComponent);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, event);
		sendOrderedBroadcast(intent, null);
		
		event = new KeyEvent(time, time, KeyEvent.ACTION_UP, mKeyCode, 0);
		intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent.setComponent(mComponent);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, event);
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
		if(pressed != mPressed)
		{
			if(mToggle)
			{
				sendToggleEvent();
				mPressed = pressed;
			}
			else
				sendKeyEvent(pressed);
		}
	}
	
	@SuppressLint("InflateParams")
	@Override
	public void onCreate()
	{
		super.onCreate();
		instance = this;
		if(listener != null)
			listener.onServiceStateChanged(true);
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mPrefs.registerOnSharedPreferenceChangeListener(this);
		
		mLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		readPosition();
		
		mNotification = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(getString(R.string.app_name))
			.setContentText(getString(R.string.notification_text))
			.setPriority(NotificationCompat.PRIORITY_MIN)
			.setContentIntent(PendingIntent.getActivity(this, REQUEST_MAIN, new Intent(this, MainActivity.class), 0))
			.addAction(R.drawable.ic_action_remove, getString(R.string.notification_action_hide),
				PendingIntent.getBroadcast(this, REQUEST_CLOSE, new Intent(ACTION_HIDE), 0))
			.build();
		
		if(mPrefs.getBoolean("pref_foreground", true))
			startForeground(NOTIFICATION_MAIN, mNotification);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
		filter.addAction(ACTION_HIDE);
		registerReceiver(mBroadcastReceiver, filter);
		
		mLayoutParams = new WindowManager.LayoutParams(
			WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
			WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
					| WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
			PixelFormat.TRANSLUCENT);
		
		mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		mLayoutParams.x = (int) mPosition.x;
		mLayoutParams.y = (int) mPosition.y;
		
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		
		mButton = (OverlayButton) inflater.inflate(R.layout.overlay_button, null);
		mButton.setService(this);
		
		for(String key : new String[] {
			"pref_keycode",
			"pref_component",
			"pref_toggle",
			"pref_padding"
		})
			onSharedPreferenceChanged(mPrefs, key);
		
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
			onButtonPressed(false);
		
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
				onButtonPressed(false);
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
					onButtonPressed(false);
				
				mKeyCode = Integer.parseInt(mPrefs.getString(key, Integer.toString(Utils.KEYCODE_DEFAULT)));
				mButton.setImageResource(Utils.KEYCODE_ICONS.get(mKeyCode));
				mButton.setContentDescription(getString(Utils.KEYCODE_LABELS.get(mKeyCode)));
			}
			catch(NumberFormatException e)
			{}
		}
		
		if("pref_component".equals(key))
		{
			if(mPressed)
				onButtonPressed(false);
			
			String component = mPrefs.getString(key, "");
			mComponent = null;
			if(component != null && !"".equals(component))
				mComponent = ComponentName.unflattenFromString(component);
		}
		
		if("pref_padding".equals(key))
		{
			int value = mPrefs.getInt(key, 16);
			mButton.setPadding(value, value, value, value);
		}
		
		if("pref_foreground".equals(key))
		{
			if(mPrefs.getBoolean(key, true))
				startForeground(NOTIFICATION_MAIN, mNotification);
			else
				stopForeground(true);
		}
		
		if("pref_toggle".equals(key))
		{
			if(mPressed)
				onButtonPressed(false);
			mToggle = mPrefs.getBoolean(key, false);
		}
	}
	
	public static interface Listener
	{
		public void onServiceStateChanged(boolean running);
	}
}
