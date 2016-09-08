package com.meetingplay.api;

import com.google.gson.JsonObject;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.POST;

public interface APIService {

	/**
	 * USER APIs
	 */

	/**
	 * Send nearest 3 beacons
	 * @header Content-Type
	 * @header X-Authentication-Token
	 * @body json
	 */
//	@POST("/property/3/beacons/startpoint")
//	public void sendNearestBeacons(@Header("Content-Type") String contentType,
//								   @Header("X-Authentication-Token") String authToken,
//								   @Body String beacons,
//								   Callback<JsonObject> sendCallback);
	@POST("/property/3/beacons/startpoint")
	public void sendNearestBeacons(@Header("X-Authentication-Token") String authToken,
								   @Body JsonObject beacons,
								   Callback<JsonObject> sendCallback);

}