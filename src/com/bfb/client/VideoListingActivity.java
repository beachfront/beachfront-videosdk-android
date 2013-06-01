package com.bfb.client;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.bfio.ad.BFIOErrorCode;
import com.bfio.ad.BFIOInterstitial;
import com.bfio.ad.BFIOInterstitial.InterstitialListener;
import com.bfio.ad.model.BFIOInterstitalAd;
import com.bfm.api.Error;
import com.bfm.listeners.OnVideosFetch;
import com.bfm.model.VideoEntity;
import com.bfm.sdk.VideoSDK;
import com.google.gson.Gson;

public class VideoListingActivity extends Activity implements OnVideosFetch,
		InterstitialListener {

	TextView title;
	EditText search;

	List<VideoEntity> videos = new ArrayList<VideoEntity>();

	int displayHeight;

	BFIOInterstitial interstitial;

	ChannelAdaptor adaptor;

	VideoEntity videoEntity;

	ListView listView;

	String videoStartId = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video);
		title = (TextView) findViewById(R.id.channel_name);
		search = (EditText) findViewById(R.id.search_edit_box);
		listView = (ListView) findViewById(R.id.video_list);
		interstitial = new BFIOInterstitial(VideoListingActivity.this, this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				videoEntity = (VideoEntity) view.getTag(R.id.video_id);
				interstitial.requestInterstitial(
						"xxxxx-xxxxx-xxxx-xxxxx-xxxxx-xxx", // appID
						"xxxxx-xxxxx-xxxx-xxxxx-xxxxx-xxx"); // adUnitId
				Toast.makeText(VideoListingActivity.this,
						"Interstitial request sent", Toast.LENGTH_SHORT).show();

			}
		});
		adaptor = new ChannelAdaptor();
		listView.setAdapter(adaptor);
		loadDisplay();
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

			if (t.getImage() != null && !t.getImage().isRecycled()) {
				holder.image.setImageBitmap(t.getImage());
			} else {
				DrawableManager.getInstance().fetchDrawableOnThread(
						t.getImageUrl(), holder.image);
			}
			cellLayout.setTag(R.id.video_id, t);
			holder.duration.setText(t.getLengthonImage());
			holder.updated.setText(t.getPubDate());
			holder.title.setText(Html.fromHtml(t.getTitle()));
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

	public void onClick(View view) {
		videos.clear();
		adaptor.notifyDataSetChanged();
		VideoSDK.getInstance(this).searchVideos(this, 1, 30,
				search.getText().toString(), true);
		title.setText("Searching  Videos...");

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
			adaptor.notifyDataSetChanged();
		} else {
			title.setText(error.getErrorType().toString());
		}

	}

	@Override
	public void onInterstitialFailed(BFIOErrorCode errorCode) {
		Toast.makeText(VideoListingActivity.this, "Interstitial not received",
				Toast.LENGTH_SHORT).show();
		playVideo();

	}

	@Override
	public void onInterstitialClicked() {
		Toast.makeText(VideoListingActivity.this, "Interstitial Clicked",
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onInterstitialDismissed() {
		Toast.makeText(VideoListingActivity.this, "Interstitial dismissed",
				Toast.LENGTH_SHORT).show();
		playVideo();
	}

	@Override
	public void onReceiveInterstitial(BFIOInterstitalAd ad) {
		Toast.makeText(VideoListingActivity.this, "Received interstitial",
				Toast.LENGTH_SHORT).show();
		interstitial.showInterstitial(ad);		

	}

	@Override
	public void onInterstitialCompleted() {
		Toast.makeText(VideoListingActivity.this,
				"Interstitial play completed", Toast.LENGTH_SHORT).show();
		playVideo();

	}

	@Override
	public void onInterstitialStarted() {
		Toast.makeText(VideoListingActivity.this, "Interstitial started",
				Toast.LENGTH_SHORT).show();
	}

}
