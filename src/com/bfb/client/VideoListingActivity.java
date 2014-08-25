package com.bfb.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.bfb.utils.DrawableManager;
import com.bfm.api.Error;
import com.bfm.common.VideoDateComp;
import com.bfm.common.VideoViewsComp;
import com.bfm.listeners.VideosFetchListener;
import com.bfm.model.VideoEntity;
import com.bfm.sdk.VideoSDK;
import com.google.gson.Gson;

public class VideoListingActivity extends Activity implements
		VideosFetchListener, OnClickListener {

	private TextView title;
	private EditText search;
	private List<VideoEntity> videos = new ArrayList<VideoEntity>();
	private int displayHeight;
	private ChannelAdaptor adaptor;
	private VideoEntity videoEntity;
	private ListView listView;
	private TextView sort_by_date;
	private TextView sort_by_views;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_listing);
		title = (TextView) findViewById(R.id.channel_name);
		search = (EditText) findViewById(R.id.search_edit_box);
		listView = (ListView) findViewById(R.id.video_list);
		sort_by_date = (TextView) findViewById(R.id.sort_by_date);
		sort_by_views = (TextView) findViewById(R.id.sort_by_views);
		sort_by_views.setOnClickListener(this);
		sort_by_date.setOnClickListener(this);
		sort_by_views.setVisibility(View.GONE);
		sort_by_date.setVisibility(View.VISIBLE);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				videoEntity = (VideoEntity) view.getTag(R.id.video_id);
				playVideo();
			}
		});
		adaptor = new ChannelAdaptor();
		listView.setAdapter(adaptor);
		loadDisplay();
		search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.hideSoftInputFromWindow(
							search.getApplicationWindowToken(),
							InputMethodManager.SHOW_FORCED);
					search();
					return true;
				}
				return false;
			}

		});
	}

	private void playVideo() {
		Intent intent = new Intent(VideoListingActivity.this,
				VideoPlayActivity.class);
		Bundle b = new Bundle();
		b.putString("video", new Gson().toJson(videoEntity));
		intent.putExtras(b);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		videos.clear();
		adaptor.notifyDataSetChanged();
		Integer channedlId = getIntent().getExtras().getInt("channel_id");
		VideoSDK.getInstance(this).getChannelVideos(this, 1, 30,
				channedlId + "");
		title.setText("Loading Videos...");
	}

	public class ViewHolder {
		public TextView title;
		public ImageView image;
		public TextView duration;
		public TextView updated;
		public TextView views;

	}

	public class ChannelAdaptor extends BaseAdapter implements ListAdapter {
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			VideoEntity t = (VideoEntity) getItem(position);
			View cellLayout = convertView;
			final ViewHolder holder;
			cellLayout = getLayoutInflater().inflate(
					R.layout.list_item_videolisting, null);
			RelativeLayout imageBox = (RelativeLayout) cellLayout
					.findViewById(R.id.video_listing_main_box);
			@SuppressWarnings("deprecation")
			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
					LayoutParams.FILL_PARENT, displayHeight / 4);
			imageBox.setLayoutParams(p);
			holder = new ViewHolder();
			holder.title = (TextView) cellLayout
					.findViewById(R.id.video_listing_item_title);
			holder.duration = (TextView) cellLayout
					.findViewById(R.id.video_listing_item_duration);
			holder.updated = (TextView) cellLayout
					.findViewById(R.id.video_listing_item_updated);
			holder.image = (ImageView) cellLayout
					.findViewById(R.id.video_listing_item_image);
			holder.views = (TextView) cellLayout
					.findViewById(R.id.video_listing_item_views);
			if (t.getImage() != null && !t.getImage().isRecycled()) {
				holder.image.setImageBitmap(t.getImage());
			} else {
				DrawableManager.getInstance().fetchDrawableOnThread(
						t.getImageUrl(), holder.image);
			}
			cellLayout.setTag(R.id.video_id, t);
			holder.title.setText(Html.fromHtml(t.getTitle()));
			holder.duration.setText(t.getLengthonImage());
			holder.views.setText(t.getViews() + " Views");
			holder.updated.setText(t.getFriendlyTime());
			return cellLayout;
		}

		@Override
		public int getCount() {
			return videos.size();
		}

		@Override
		public Object getItem(int position) {
			return videos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}

	public void search() {
		videos.clear();
		adaptor.notifyDataSetChanged();
		if (!search.getText().toString().equals("")) {
			VideoSDK.getInstance(this).searchVideos(this, 1, 30,
					search.getText().toString(), true);
			title.setText("Searching  Videos...");
		} else {
			Integer channedlId = getIntent().getExtras().getInt("channel_id");
			VideoSDK.getInstance(this).getChannelVideos(this, 1, 30,
					channedlId + "");
		}
	}

	@SuppressWarnings("deprecation")
	protected void loadDisplay() {
		Display display = getWindowManager().getDefaultDisplay();
		displayHeight = display.getHeight();
	}

	@Override
	public void onVideosFetch(Error error, List<VideoEntity> videoEntites) {
		if (error == null) {
			title.setText("Videos loaded");
			this.videos = videoEntites;
			Collections.sort(videos, new VideoDateComp());
			adaptor.notifyDataSetChanged();
			sort_by_views.setVisibility(View.GONE);
			sort_by_date.setVisibility(View.VISIBLE);
		} else {
			title.setText(error.getErrorType().toString());
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sort_by_views:
			Collections.sort(videos, new VideoDateComp());
			adaptor.notifyDataSetChanged();
			sort_by_views.setVisibility(View.GONE);
			sort_by_date.setVisibility(View.VISIBLE);
			break;
		case R.id.sort_by_date:
			Collections.sort(videos, new VideoViewsComp());
			adaptor.notifyDataSetChanged();
			sort_by_views.setVisibility(View.VISIBLE);
			sort_by_date.setVisibility(View.GONE);
			break;
		default:
			break;
		}

	}
}
