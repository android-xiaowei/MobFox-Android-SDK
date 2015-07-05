package com.adsdk.sdk.video;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;

import com.adsdk.sdk.Log;

@SuppressLint("ViewConstructor")
public class MediaController extends FrameLayout {

	private static final int SHOW_PROGRESS = 2;

	private android.widget.MediaController.MediaPlayerControl mPlayer;
	private Context mContext;

	private TextView mLeftTime;
	private VideoData mVideoData;
	StringBuilder mFormatBuilder;
	Formatter mFormatter;
	private boolean mShowing;
	private OnUnpauseListener mOnUnpauseListener;
	private OnPauseListener mOnPauseListener;
	private OnReplayListener mOnReplayListener;

	@SuppressWarnings("deprecation")
	public MediaController(Context context, VideoData videoData) {
		super(context);
		this.setVisibility(View.GONE);
		mShowing = true;
		mContext = context;
		mVideoData = videoData;
		if (mVideoData == null) {
			throw new IllegalArgumentException("Video info cannot be null");
		}
		mFormatBuilder = new StringBuilder();
		mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

		mLeftTime = new AutoResizeTextView(mContext);
		mLeftTime.setGravity(Gravity.CENTER);
		mLeftTime.setTextSize(14);
		mLeftTime.setTextColor(Color.WHITE);

		int paddingHorizontal = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
		int paddingVertical = 0;

		mLeftTime.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);

		GradientDrawable videoEndInfoButtonShape = new GradientDrawable();
		int cornerRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
		videoEndInfoButtonShape.setCornerRadius(cornerRadius);
		videoEndInfoButtonShape.setColor(Color.BLACK);
		this.setBackgroundDrawable(videoEndInfoButtonShape);

		this.addView(mLeftTime, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));

		Log.d("MediaController created");
	}

	public void setMediaPlayer(MediaPlayerControl player) {
		mPlayer = player;
	}

	public void show() {
		if (!mShowing) {
			this.setVisibility(View.VISIBLE);
			mShowing = true;
			Log.d("Change Visibility");
		}
		refreshProgress();
	}

	public boolean isShowing() {
		return mShowing;
	}

	public void hide() {
		Log.d("HIDE");

		if (mShowing) {
			Log.d("Hide change visibility");
			mHandler.removeMessages(SHOW_PROGRESS);
			this.setVisibility(View.GONE);
			mShowing = false;

		}
	}

	public void replay() {
		if (mPlayer != null) {
			mPlayer.seekTo(0);
			mPlayer.start();
		}
		refreshProgress();
		if (mOnReplayListener != null) {
			mOnReplayListener.onVideoReplay();
		}
	}

	private static class ResourceHandler extends Handler {

		private final WeakReference<MediaController> mController;

		public ResourceHandler(MediaController controller) {
			mController = new WeakReference<MediaController>(controller);

		}

		@Override
		public void handleMessage(Message msg) {
			MediaController wController = mController.get();
			if (wController != null) {
				wController.handleMessage(msg);
			}
		}
	};

	private ResourceHandler mHandler = new ResourceHandler(this);

	private void handleMessage(Message msg) {
		switch (msg.what) {
		case SHOW_PROGRESS:
			refreshProgress();
			break;

		}
	}

	private String stringForTime(int timeMs) {
		int totalSeconds = timeMs / 1000;

		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		mFormatBuilder.setLength(0);
		if (hours > 0) {
			return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
		} else if (minutes > 0) {
			return mFormatter.format("%02d:%02d", minutes, seconds).toString();
		} else {
			return mFormatter.format("0:%02d", seconds).toString();
		}
	}

	private int setProgress() {
		if (mPlayer == null) {
			return 0;
		}
		int position = mPlayer.getCurrentPosition();
		int duration = mPlayer.getDuration();

		int timeLeft = duration - position;
		if (mLeftTime != null) {
			mLeftTime.setText("-" + stringForTime(timeLeft));
		}
		return position;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_MENU && keyCode != KeyEvent.KEYCODE_CALL && keyCode != KeyEvent.KEYCODE_ENDCALL) {
			if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
				doPauseResume();
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP && (mPlayer != null) && mPlayer.isPlaying()) {
				mPlayer.pause();
				if (mOnPauseListener != null) {
					mOnPauseListener.onVideoPause();
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void doPauseResume() {
		if (mPlayer == null)
			return;
		if (mPlayer.isPlaying()) {
			mPlayer.pause();
			if (mOnPauseListener != null) {
				mOnPauseListener.onVideoPause();
			}
		} else {
			mPlayer.start();
			if (mOnUnpauseListener != null) {
				mOnUnpauseListener.onVideoUnpause();
			}
		}
	}

	public void onStart() {
		refreshProgress();
	}

	private void refreshProgress() {
		if (mShowing) {
			int pos = setProgress();
			if ((mPlayer != null) && (mPlayer.isPlaying())) {
				mHandler.removeMessages(SHOW_PROGRESS);
				Message msg = mHandler.obtainMessage(SHOW_PROGRESS);
				mHandler.sendMessageDelayed(msg, 1000 - (pos % 1000));
			}
		}
	}

	public void onPause() {
		show();
	}

	public void setOnPauseListener(OnPauseListener l) {
		mOnPauseListener = l;
	}

	public void setOnUnpauseListener(OnUnpauseListener l) {
		mOnUnpauseListener = l;
	}

	public void setOnReplayListener(OnReplayListener l) {
		mOnReplayListener = l;
	}

	public interface OnPauseListener {
		public void onVideoPause();
	}

	public interface OnUnpauseListener {
		public void onVideoUnpause();
	}

	public interface OnReplayListener {
		public void onVideoReplay();
	}

}