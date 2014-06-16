package com.bfb.client;

import android.app.Activity;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bfm.model.VideoEntity;
import com.bfm.view.VideoViewBFBP;
import com.google.gson.Gson;

public class VideoPlayActivity extends Activity implements
		OnCompletionListener, MediaPlayer.OnPreparedListener, OnErrorListener {

	private TextView title;

	private TextView desc;

	private VideoViewBFBP videoView = null;

	private DisplayMetrics displaymetrics = new DisplayMetrics();

	VideoEntity ve;

	View video_desc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.video_play);
		title = (TextView) findViewById(R.id.title);
		desc = (TextView) findViewById(R.id.desc);
		video_desc = findViewById(R.id.video_desc);
		videoView = (VideoViewBFBP) findViewById(R.id.video_view);
		if (isLandScape(this)) {
			handleLandscape();
		} else {
			handlePortrait();
		}
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.getString("video") != null) {
			videoView.setOnPreparedListener(this);
			videoView.setOnCompletionListener(this);
			videoView.setOnErrorListener(this);
			ve = new Gson().fromJson(extras.getString("video"),
					VideoEntity.class);
			title.setText(ve.getTitle());
			desc.setText(ve.getDescription());
		} else {
			finish();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			handleLandscape();
		} else {
			handlePortrait();
		}
	}

	private void handleLandscape() {
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		videoView.setLayoutParams(p);
		video_desc.setVisibility(View.GONE);
	}

	private void handlePortrait() {
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int width = displaymetrics.widthPixels;
		int height = Math.round(width / 1.77f);
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, height);
		videoView.setLayoutParams(p);
		RelativeLayout.LayoutParams p2 = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		p2.addRule(RelativeLayout.BELOW, R.id.video_view);
		video_desc.setLayoutParams(p2);
		video_desc.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onPause() {
		videoView.onPause();
		super.onPause();
	}

	@Override
	protected void onStart() {
		videoView.start(ve);
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

	}

	@Override
	protected void onResume() {
		videoView.onResume();
		super.onResume();
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		Toast.makeText(getApplicationContext(),
				"Not supported" + ve.getTitle(), Toast.LENGTH_LONG).show();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {

	}

	@Override
	public void onCompletion(MediaPlayer mp) {

	}

	public static boolean isLandScape(Activity activity) {
		boolean isLandscape = false;
		int displayOrientation = 0;
		displayOrientation = activity.getResources().getConfiguration().orientation;
		if (displayOrientation == 0 || displayOrientation == 2) {
			isLandscape = true;
		}
		return isLandscape;
	}

}
