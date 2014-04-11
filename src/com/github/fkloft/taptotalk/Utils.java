package com.github.fkloft.taptotalk;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.KeyEvent;

public final class Utils
{
	/** must be same order as in res/values/strings.xml */
	public static int[] KEYCODES = {
		KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
		KeyEvent.KEYCODE_MEDIA_STOP,
		KeyEvent.KEYCODE_MEDIA_NEXT,
		KeyEvent.KEYCODE_MEDIA_PREVIOUS,
		KeyEvent.KEYCODE_MEDIA_REWIND,
		KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
		KeyEvent.KEYCODE_MEDIA_PLAY,
		KeyEvent.KEYCODE_MEDIA_PAUSE,
		KeyEvent.KEYCODE_MEDIA_CLOSE,
		KeyEvent.KEYCODE_MEDIA_EJECT,
		KeyEvent.KEYCODE_MEDIA_RECORD
	};
	
	public static SparseIntArray KEYCODE_LABELS = new SparseIntArray();
	public static SparseIntArray KEYCODE_ICONS = new SparseIntArray();
	
	static
	{
		KEYCODE_LABELS.append(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, R.string.key_playpause);
		KEYCODE_LABELS.append(KeyEvent.KEYCODE_MEDIA_STOP, R.string.key_stop);
		KEYCODE_LABELS.append(KeyEvent.KEYCODE_MEDIA_NEXT, R.string.key_next);
		KEYCODE_LABELS.append(KeyEvent.KEYCODE_MEDIA_PREVIOUS, R.string.key_previous);
		KEYCODE_LABELS.append(KeyEvent.KEYCODE_MEDIA_REWIND, R.string.key_rewind);
		KEYCODE_LABELS.append(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, R.string.key_ffw);
		KEYCODE_LABELS.append(KeyEvent.KEYCODE_MEDIA_PLAY, R.string.key_play);
		KEYCODE_LABELS.append(KeyEvent.KEYCODE_MEDIA_PAUSE, R.string.key_pause);
		KEYCODE_LABELS.append(KeyEvent.KEYCODE_MEDIA_CLOSE, R.string.key_close);
		KEYCODE_LABELS.append(KeyEvent.KEYCODE_MEDIA_EJECT, R.string.key_eject);
		KEYCODE_LABELS.append(KeyEvent.KEYCODE_MEDIA_RECORD, R.string.key_record);
		
		KEYCODE_ICONS.append(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, R.drawable.ic_action_play);
		KEYCODE_ICONS.append(KeyEvent.KEYCODE_MEDIA_STOP, R.drawable.ic_action_stop);
		KEYCODE_ICONS.append(KeyEvent.KEYCODE_MEDIA_NEXT, R.drawable.ic_action_next);
		KEYCODE_ICONS.append(KeyEvent.KEYCODE_MEDIA_PREVIOUS, R.drawable.ic_action_previous);
		KEYCODE_ICONS.append(KeyEvent.KEYCODE_MEDIA_REWIND, R.drawable.ic_action_rewind);
		KEYCODE_ICONS.append(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, R.drawable.ic_action_fast_forward);
		KEYCODE_ICONS.append(KeyEvent.KEYCODE_MEDIA_PLAY, R.drawable.ic_action_play);
		KEYCODE_ICONS.append(KeyEvent.KEYCODE_MEDIA_PAUSE, R.drawable.ic_action_pause);
		KEYCODE_ICONS.append(KeyEvent.KEYCODE_MEDIA_CLOSE, R.drawable.ic_action_remove);
		KEYCODE_ICONS.append(KeyEvent.KEYCODE_MEDIA_EJECT, R.drawable.ic_action_remove);
		KEYCODE_ICONS.append(KeyEvent.KEYCODE_MEDIA_RECORD, R.drawable.ic_action_mic);
	}
	
	private Utils()
	{}
	
	public static String[] getKeycodeLabels(Context context)
	{
		String[] labels = new String[KEYCODE_LABELS.size()];
		for(int i = 0; i < labels.length; i++)
			labels[i] = context.getString(KEYCODE_LABELS.valueAt(i));
		return labels;
	}
	
	public static String[] getKeycodeValues()
	{
		String[] labels = new String[KEYCODE_LABELS.size()];
		for(int i = 0; i < labels.length; i++)
			labels[i] = Integer.toString(KEYCODE_LABELS.keyAt(i));
		return labels;
	}
}
