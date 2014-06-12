package com.bfb.client;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.bfm.model.VideoEntity;
import com.bfm.view.VideoViewBFB;
import com.google.gson.Gson;

public class VideoPlayActivity extends Activity implements
		OnCompletionListener, MediaPlayer.OnPreparedListener, OnErrorListener {

	TextView title;

	TextView desc;

	VideoViewBFB videoView = null;

	VideoEntity ve;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_play);
		title = (TextView) findViewById(R.id.title);
		desc = (TextView) findViewById(R.id.desc);
		videoView = (VideoViewBFB) findViewById(R.id.video_view);
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

}
