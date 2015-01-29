package com.bfb.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
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

import com.bfb.utils.URLFileNameGenerator;
import com.bfm.api.Error;
import com.bfm.api.Error.ErrorType;
import com.bfm.common.VideoDateComp;
import com.bfm.common.VideoViewsComp;
import com.bfm.listeners.VideosFetchListener;
import com.bfm.model.VideoEntity;
import com.bfm.sdk.VideoSDK;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.L;

public class VideoListingActivity extends Activity implements
		VideosFetchListener, OnClickListener, OnScrollListener {

	private TextView title;
	public static final String DIR_NAME_IMAGE_CACHE = "image_cache";
	private EditText search;
	private List<VideoEntity> videos = new ArrayList<VideoEntity>();
	private int displayHeight;
	private ChannelAdaptor adaptor;
	private VideoEntity videoEntity;
	private ListView listView;
	private TextView sort_by_date;
	private TextView sort_by_views;
	DisplayImageOptions options;
	ImageLoader mImageLoader;
	private DiscCacheAware mDiscCache;
	private boolean loading = false;
	private boolean complete = false;
	private int currentPage = 1;
	private int rpp = 30;
	View footerView = null;
	private Integer channedlId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_listing);
		options = new DisplayImageOptions.Builder()
				.resetViewBeforeLoading(false).cacheInMemory(true)
				.cacheOnDisc(true).resetViewBeforeLoading(false)
				.displayer(new FadeInBitmapDisplayer(100)) // default
				.build();
		footerView = getLayoutInflater().inflate(R.layout.footer, null, false);
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
		listView.setOnScrollListener(this);
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
		channedlId = getIntent().getExtras().getInt("channel_id");
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
				getImageLoader().displayImage(t.getImageUrl(), holder.image,
						options, null);
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
		try {
			loading = false;
			if (currentPage != 1)
				listView.removeFooterView(footerView);
			if (error == null) {
				title.setText("Videos loaded");
				if (!videoEntites.isEmpty()) {
					if (currentPage == 1)
						videos.clear();
					Collections.sort(videos, new VideoDateComp());
					sort_by_views.setVisibility(View.GONE);
					sort_by_date.setVisibility(View.VISIBLE);
					videos.addAll(videoEntites);
					adaptor.notifyDataSetChanged();
				}
			} else if (error.getErrorType() == ErrorType.NOT_COONECTED) {
				new AlertDialog.Builder(this)
						.setMessage(
								"Please reconnect to the internet to use this application")
						.setTitle("Connection Error")
						.setCancelable(false)
						.setPositiveButton("Retry",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										VideoSDK.getInstance(
												VideoListingActivity.this)
												.getChannelVideos(
														VideoListingActivity.this,
														currentPage, 30,
														channedlId + "");
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								}).show();
			} else if (error.getErrorType() == ErrorType.NETWORK_FAIL) {
				new AlertDialog.Builder(VideoListingActivity.this)
						.setMessage(
								"Oops...There is something wrong at our servers, Please try after some time..")
						.setTitle(getString(R.string.app_name))
						.setCancelable(false)
						.setPositiveButton("Retry",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										VideoSDK.getInstance(
												VideoListingActivity.this)
												.getChannelVideos(
														VideoListingActivity.this,
														currentPage, 30,
														channedlId + "");
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								}).show();

			} else if (error.getErrorType() == ErrorType.INFO_NOT_RECEIVED) {
				new AlertDialog.Builder(VideoListingActivity.this)
						.setMessage(
								"Oops...There is something went wrong, Please try again")
						.setTitle(getString(R.string.app_name))
						.setCancelable(false)
						.setPositiveButton("Retry",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										VideoSDK.getInstance(
												VideoListingActivity.this)
												.getChannelVideos(
														VideoListingActivity.this,
														currentPage, 30,
														channedlId + "");
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();

									}
								}).show();

			}
			adaptor.notifyDataSetChanged();
		} catch (Exception e) {

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

	public ImageLoader getImageLoader() {
		if (mImageLoader != null)
			return mImageLoader;
		final ImageLoader loader = ImageLoader.getInstance();
		final ImageLoaderConfiguration.Builder cb = new ImageLoaderConfiguration.Builder(
				this);
		cb.threadPriority(Thread.NORM_PRIORITY - 2);
		cb.denyCacheImageMultipleSizesInMemory();
		cb.tasksProcessingOrder(QueueProcessingType.LIFO);
		cb.discCache(getDiscCache());
		L.disableLogging();
		loader.init(cb.build());
		return mImageLoader = loader;
	}

	private DiscCacheAware getDiscCache() {
		if (mDiscCache != null)
			return mDiscCache;
		return mDiscCache = getDiscCache(DIR_NAME_IMAGE_CACHE);
	}

	private DiscCacheAware getDiscCache(final String dirName) {
		final File cacheDir = getBestCacheDir(this, dirName);
		return new UnlimitedDiscCache(cacheDir, new URLFileNameGenerator());
	}

	public static File getBestCacheDir(final Context context,
			final String cacheDirName) {
		if (context == null)
			throw new NullPointerException();
		final File extCacheDir;
		try {
			// Workaround for https://github.com/mariotaku/twidere/issues/138
			extCacheDir = context.getExternalCacheDir();
		} catch (final Exception e) {
			return new File(context.getCacheDir(), cacheDirName);
		}
		if (extCacheDir != null && extCacheDir.isDirectory()) {
			final File cacheDir = new File(extCacheDir, cacheDirName);
			if (cacheDir.isDirectory() || cacheDir.mkdirs())
				return cacheDir;
		}
		return new File(context.getCacheDir(), cacheDirName);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		try {
			if (!loading && totalItemCount != 2 && !complete
					&& totalItemCount <= (firstVisibleItem + visibleItemCount)
					&& videos.size() > 10) {
				currentPage++;
				loading = true;
				if (currentPage > 1)
					listView.addFooterView(footerView);
				VideoSDK.getInstance(this).getChannelVideos(this, currentPage,
						rpp, channedlId + "");

			}
		} catch (IndexOutOfBoundsException e) {
			// ignore for first case

		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}
}
