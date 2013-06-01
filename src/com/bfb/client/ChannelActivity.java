package com.bfb.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bfm.api.Error;
import com.bfm.listeners.OnChannelsFetch;
import com.bfm.model.Channel;
import com.bfm.sdk.VideoSDK;

public class ChannelActivity extends Activity implements OnChannelsFetch {

	TextView title;

	List<Channel> channels = new ArrayList<Channel>();

	ChannelAdaptor adaptor;

	ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel);
		title = (TextView) findViewById(R.id.screen_title);
		listView = (ListView) findViewById(R.id.channel_list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Channel channel = (Channel) view.getTag(R.id.channel_id);
				Toast.makeText(getApplicationContext(),
						"Clicked on " + channel.getName() + position,
						Toast.LENGTH_LONG).show();
				Intent intent = new Intent(ChannelActivity.this,
						VideoListingActivity.class);
				intent.putExtra("channel_id", channel.getId());
				startActivity(intent);
			}
		});
		adaptor = new ChannelAdaptor();
		listView.setAdapter(adaptor);
		VideoSDK.getInstance(this).sessionStartTracker();
	}

	@Override
	protected void onDestroy() {
		OnChannelsFetch onChannelFetch = new OnChannelsFetch() {

			@Override
			public void onChannelFetch(Error error, Set<Channel> channels) {
				// do the stuff

			}
		};
		VideoSDK.getInstance(this).getChannels(onChannelFetch);
		VideoSDK.getInstance(this).sessionEndTracker();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		VideoSDK.getInstance(this).getChannels(this);
		title.setText("Loading Channels...");
	}

	@Override
	public void onChannelFetch(Error error, Set<Channel> channels) {
		if (error == null) {
			title.setText("Total Channels Found " + channels.size());
			this.channels = new ArrayList<Channel>(channels);
			adaptor.notifyDataSetChanged();
		} else {
			title.setText(error.getErrorType().toString());
		}

	}

	static class ViewHolder {
		public TextView text;
	}

	public class ChannelAdaptor extends BaseAdapter implements ListAdapter {
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			Channel channel = (Channel) getItem(position);
			if (rowView == null) {
				LayoutInflater inflater = getLayoutInflater();
				rowView = inflater.inflate(R.layout.channel_row, null);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.text = (TextView) rowView
						.findViewById(R.id.channel_name);
				rowView.setTag(viewHolder);
			}
			ViewHolder holder = (ViewHolder) rowView.getTag();
			holder.text.setText(channel.getName());
			rowView.setTag(R.id.channel_id, channel);
			return rowView;
		}

		@Override
		public int getCount() {
			return channels.size();
		}

		@Override
		public Object getItem(int position) {
			return channels.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}

}
