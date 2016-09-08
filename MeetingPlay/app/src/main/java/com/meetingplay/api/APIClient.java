package com.meetingplay.api;

import android.content.Context;
import android.util.Log;

import retrofit.RestAdapter;

public class APIClient {
	private static final String TAG = "APIClient";

	private static APIClient mInstance = null;

	private APIService mAPIService;

	private APIRequestInterceptor mRequestInterceptor;

	/**
	 * Default constructor to create Instance
	 * 
	 * @param context
	 */
	private APIClient() {
	}

	/**
	 * Sets the Application {@link Context} and {@link APIRequestInterceptor}
	 * 
	 * @param context
	 */
	private void init(Context context) {
		Log.d(TAG, "init()....");

		mRequestInterceptor = new APIRequestInterceptor();

		mAPIService = new RestAdapter.Builder()
				.setRequestInterceptor(mRequestInterceptor)
				.setEndpoint(APIConstants.REST_HOST).build()
				.create(APIService.class);
	}

	public static APIClient getInstance(Context context) {
		Log.d(TAG, "getInstance()....");
		if (mInstance == null) {
			Log.d(TAG, "getInstance() - mInstance == null");
			mInstance = new APIClient();
			mInstance.init(context);
		}
		return mInstance;
	}

	public APIService getApiService() {
		Log.d(TAG, "getApiService()....");
		return mAPIService;
	}

}