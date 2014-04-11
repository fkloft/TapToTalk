package com.github.fkloft.taptotalk;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.WindowManager;

public class OverlayService extends Service implements OverlayButton.OnPressedHandler
{
	private static final int NOTIFICATION_MAIN = 1;
	private static final int REQUEST_MAIN = 1;
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
	
	private OverlayButton mButton;
	private int mKeyCode = KeyEvent.KEYCODE_MEDIA_RECORD;
	private boolean mPressed = false;
	private WindowManager mWindowManager;
	
	public OverlayService()
	{}
	
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
	
	@Override
	public IBinder onBind(Intent intent)
	{
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		instance = this;
		if(listener != null)
			listener.onServiceStateChanged(true);
		
		startForeground(NOTIFICATION_MAIN, new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(getString(R.string.app_name))
			.setContentText("Overlay enabled. Tap to configure") // TODO l10n
			.setPriority(NotificationCompat.PRIORITY_MIN)
			.setContentIntent(PendingIntent.getActivity(this, REQUEST_MAIN, new Intent(this, MainActivity.class), 0))
			.build());
		
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
			WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
			WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
			PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
		
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		
		mButton = (OverlayButton) inflater.inflate(R.layout.overlay_button, null);
		mButton.setOnPressedHandler(this);
		
		// Add layout to window manager
		mWindowManager.addView(mButton, params);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		instance = null;
		if(listener != null)
			listener.onServiceStateChanged(false);
		
		stopForeground(true);
		
		if(mPressed)
			sendKeyEvent(false);
		
		mWindowManager.removeView(mButton);
	}
	
	@Override
	public void onPressed(boolean pressed)
	{
		if(mButton.isPressed() != mPressed)
			sendKeyEvent(mButton.isPressed());
	}
	
	public static interface Listener
	{
		public void onServiceStateChanged(boolean running);
	}
}
