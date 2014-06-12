# Beachfront Media Android SDK usage guide

## Overview
A more developer-friendly way of working with Beachfront Media Android SDK,  Simply add sdk & dependent jars & to your android project, call the appropriate methods, set the listerners and you're done :)

## Features

*  Get video channels
*  Get the videos from a channel supported by pagination
*  Search videos from a channel or from all.
*  Get detail of a specific video
*  Video Player native component
*  Supports video display both portrait and landscape.
*  Monetize video with pre-roll ads using the backend monetization rules
*  Robust Analytics - # installtion & video view is supported out of the box 
*  Revenue tracking

## Requirements

* BeachFront Media app id, app version & secret key
* BeachFront Android SDK (https://github.com/beachfront/beachfront-videosdk-android/tree/master/libs)
* Android 2.2 and above
* GSON jar


## Installation

1. Download the BeachFront Android SDK & GSON jar file, copy into the lib folder of your Android Project

2. Create a bfmconfig.xml file and with following content & put bfmconfig.xml file to res folder of your android project.

```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Put your app secret key here  -->
    <string name="bfm_key">xxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx</string>
    <!-- Put your app id here  -->
    <string name="bfm_id">xxx</string>
    <!-- Put your app version here  -->
    <string name="bfm_version">1.0</string>

</resources>
```

## Example Code

Following methods can be called from VideoSDK object which is a singleton class and instance of VideoSDK can be get as following:

```
/**
   * Get instance of Video SDK
	 * 
	 * @param context- Android application context
	 * @return Instance of Video SDK
	 */
	VideoSDK.getInstance(Context context)
	
```

### Content API:

####  Get Channels - 

Calling class need to implemente OnChannelsFetch interface or create the inner class. i.e 

```
 	ChannelsFetchListener onChannelsFetch = new ChannelsFetchListener() {

			@Override
			public void onChannelFetch(Error error, Set<Channel> channels) {
				// do the stuff

			}
		};
```

To make the request, execute following method of VideoSDK

```
	/**
	 * Return the list of channels 
	 * 
	 * @param onChannelsFetch - Instance of OnChannelsFetch
	 */
	public void getChannels(OnChannelsFetch instance)
``` 

VideoSDK will fetch the channels list in background thread and call the following method once list of channels is availablle

```
public void onChannelFetch(Error error, Set<Channel> channels)

```

Channel - Containing channel meta data i.e id, name, order etc

Error - Error details if something goes wrong 

#### Get Video from Channels

Calling class need to implemente OnVideosFetch interface or create the inner class.

To make the request, execute following method of VideoSDK

``` 
	/**
	 * This method returns videos for a list of channeld's
	 * @param instance - Instance of OnVideosFetch
	 * @param pn - page number
	 * @param rpp - result per page
	 * @param channelIds - Channel id's comma seprataed
	 */
	public void getChannelVideos(OnVideosFetch instance,
			Integer pn, Integer rpp, String channelIds)

``` 

VideoSDK will fetch the videos in background thread and call the following method once list of videos is availablle

```
	/**
	 * Called once videos or error has beeb fetched from backend
	 * 
	 * @param error
	 * @param videoEntite - Containing list of videos having video meta data i.e name,
	 * description, number of like etc
	 */
	public void onVideosFetced(Error error, List<VideoEntity> videoEntites);
```
 
#### Search Video

Calling class need to implemente OnVideoFetch interface or create the inner class.

To make the request, execute following method of VideoSDK

``` 
	/**
	 * Search videos for the given search keywords (string)
	 * @param instance - Instance of OnVideosFetch
	 * @param pn - page number
	 * @param rpp - result per page
	 * @param searchKeywords - Search keywords
	 * @param byRelevance - order by releance or by date
	 */
	public void searchVideos(OnVideosFetch instance, int pn, int rpp,
			String searchKeywords, boolean byRelevance) 

``` 

VideoSDK will fetch the videos in background thread and call the following method once list of videos is availablle

```
	/**
	 * Called by SDK once video list or error has been fetched from backend
	 * 
	 * @param error
	 * @param videoEntite - Containing list of videos having video meta data i.e name,
	 *  description, number of like etc
	 */
	public void onVideosFetced(Error error, List<VideoEntity> videoEntites);
```
 
####  Video Player
 
User BF Video Plaer in layout files


``` 
	<com.bfm.view.VideoViewBFB
        android:id="@+id/video_view"
        android:layout_width="fill_parent"
        android:layout_height="0dip"
        android:layout_weight="50" />
```

Calling activity/fragment should call following methods:

1. Play the video : videoView.start(VideoEntry ve);
2. videoView.onPause() should be called on onPause method
3. videoView.onResume() should be called on onResume method

### Tracking API:

#### Session Start - Called once at the app start point

```
	/**
	 * Start Session 
	 */
	public void sessionStartTracker()
	
```

####  Session End - Call at the app end point. i.e. at onDestroy()

```
	/**
	 * End Session
	 */
	public void sessionEndTracker() {

```

####  Video Share - Call when video is shared to social networks i.e Facebook, Twitter 

```
	/**
	 * Share the video
	 * @param video - Video Object
	 * @param shareTo - Share Source i.e. Facebook, Twitter
	 */
	public void videoSharedTracker(VideoEntity video, SocialType shareTo)
```

For more details, please refer to sample android app demonstrating above api calls.

## Issues and questions
Have a bug? Please [create an issue on GitHub](https://github.com/actolap/android-sdk-sample/issues)!


