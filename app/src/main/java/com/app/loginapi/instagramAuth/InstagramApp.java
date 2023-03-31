package com.app.loginapi.instagramAuth;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class InstagramApp {

	private InstagramSession mSession;
	private InstagramDialog mDialog;
	private OAuthAuthenticationListener mListener;
	private ProgressDialog mProgress;
	private HashMap<String, String> userInfo = new HashMap<String, String>();
	private String mAuthUrl;
	private String mTokenUrl;
	private String mAccessToken;
	private Context mCtx;

	private String mClientId;
	private String mClientSecret;
	private String profile_picture;

	public static int WHAT_FINALIZE = 0;
	public static int WHAT_ERROR = 1;
	private static int WHAT_FETCH_INFO = 2;



	public static String mCallbackUrl = ApplicationData.CALLBACK_URL;  // for live
//	public static String mCallbackUrl = "http://demo.webmigrates.com/";  // for test
	private static final String AUTH_URL = "https://api.instagram.com/oauth/authorize/";
	private static final String TOKEN_URL = "https://api.instagram.com/oauth/access_token/";
//	private static final String API_URL = "https://api.instagram.com/v1";
	private static final String API_URL = "https://graph.instagram.com/me?";

	private static final String TAG = "InstagramAPI";

	public static final String TAG_DATA = "data";
	public static final String TAG_ID = "id";
	public static final String TAG_PROFILE_PICTURE = "profile_picture";
	public static final String TAG_USERNAME = "username";
	public static final String TAG_BIO = "bio";
	public static final String TAG_WEBSITE = "website";
	public static final String TAG_COUNTS = "counts";
	public static final String TAG_FOLLOWS = "follows";
	public static final String TAG_FOLLOWED_BY = "followed_by";
	public static final String TAG_MEDIA = "media";
	public static final String TAG_FULL_NAME = "full_name";
	public static final String TAG_META = "meta";
	public static final String TAG_CODE = "code";

	public InstagramApp(Context context, String clientId, String clientSecret,
                        String callbackUrl) {

		mClientId = clientId;
		mClientSecret = clientSecret;
		mCtx = context;
		mSession = new InstagramSession(context);
		mAccessToken = mSession.getAccessToken();
		mCallbackUrl = callbackUrl;
		mTokenUrl = TOKEN_URL + "?app_id=" + clientId + "&app_secret="
				+ "&grant_type=authorization_code"+ clientSecret + "&redirect_uri=" + mCallbackUrl;

		mAuthUrl = AUTH_URL
				+ "?app_id="
				+ clientId
				+ "&redirect_uri="
				+ mCallbackUrl
				+ "&scope=user_profile&response_type=code";

		InstagramDialog.OAuthDialogListener listener = new InstagramDialog.OAuthDialogListener() {
			@Override
			public void onComplete(String code) {
				String[] realCode = code.split("#");
				getAccessToken(realCode[0]);
			}

			@Override
			public void onError(String error) {
				mListener.onFail("Authorization failed");
			}
		};

		Log.e(TAG, "Insta login " + mAuthUrl);

		mDialog = new InstagramDialog(context, mAuthUrl, listener);
		mProgress = new ProgressDialog(context);
		mProgress.setCancelable(false);
	}

	private void getAccessToken(final String code) {
		mProgress.setMessage("Getting access token ...");
		mProgress.show();

		new Thread() {
			@Override
			public void run() {
				Log.i(TAG, "Getting access token");
				int what = WHAT_FETCH_INFO;
				try {
					URL url = new URL(TOKEN_URL);
					// URL url = new URL(mTokenUrl + "&code=" + code);
					Log.i(TAG, "Opening Token URL " + url.toString());
					HttpURLConnection urlConnection = (HttpURLConnection) url
							.openConnection();
					urlConnection.setRequestMethod("POST");
					urlConnection.setDoInput(true);
					urlConnection.setDoOutput(true);
					 urlConnection.connect();
					OutputStreamWriter writer = new OutputStreamWriter(
							urlConnection.getOutputStream());
					writer.write("client_id=" + mClientId + "&client_secret="
							+ mClientSecret + "&grant_type=authorization_code"
							+ "&redirect_uri=" + mCallbackUrl + "&code=" + code);
					writer.flush();
					String response = streamToString(urlConnection.getInputStream());
					Log.i(TAG, "response " + response);
					JSONObject jsonObj = new JSONObject(response);


					mAccessToken = jsonObj.getString("access_token");
					Log.i(TAG, "Got access token: " + mAccessToken);

					String id = jsonObj.getString("user_id");
//					String user = jsonObj.getString("username");
//					String name = jsonObj.getJSONObject("user").getString("full_name");
//					profile_picture = jsonObj.getJSONObject("user").getString("profile_picture");
					Log.e(TAG, "run: "+ profile_picture);

					mSession.storeAccessToken(mAccessToken, id, "", "");

					try {
						URL url1 = new URL(API_URL + "fields=id,username&access_token=" + mAccessToken);

						Log.d(TAG, "Opening URL " + url1.toString());
						HttpURLConnection urlConnection1 = (HttpURLConnection) url1.openConnection();
						urlConnection1.setRequestMethod("GET");
						urlConnection1.setDoInput(true);
						urlConnection1.connect();
						String response1 = streamToString(urlConnection1.getInputStream());
						System.out.println(response1);
						JSONObject jsonObj1 = (JSONObject) new JSONObject(response1);

						mSession.storeAccessToken(mAccessToken, jsonObj1.getString(TAG_ID), jsonObj1.getString(TAG_USERNAME), "");
						// String name = jsonObj.getJSONObject("data").getString(
						// "full_name");
						// String bio =
						// jsonObj.getJSONObject("data").getString("bio");
						// Log.i(TAG, "Got name: " + name + ", bio [" + bio + "]");
//					JSONObject data_obj = jsonObj.getJSONObject(TAG_DATA);
						userInfo.put(TAG_ID, jsonObj1.getString(TAG_ID));

//					userInfo.put(TAG_PROFILE_PICTURE, data_obj.getString(TAG_PROFILE_PICTURE));

						userInfo.put(TAG_USERNAME, jsonObj1.getString(TAG_USERNAME));

//					userInfo.put(TAG_BIO, data_obj.getString(TAG_BIO));

//					userInfo.put(TAG_WEBSITE, data_obj.getString(TAG_WEBSITE));

//					JSONObject counts_obj = data_obj.getJSONObject(TAG_COUNTS);

//					userInfo.put(TAG_FOLLOWS, counts_obj.getString(TAG_FOLLOWS));

//					userInfo.put(TAG_FOLLOWED_BY, counts_obj.getString(TAG_FOLLOWED_BY));

//					userInfo.put(TAG_MEDIA, counts_obj.getString(TAG_MEDIA));

//					userInfo.put(TAG_FULL_NAME, data_obj.getString(TAG_FULL_NAME));

//					JSONObject meta_obj = jsonObj.getJSONObject(TAG_META);

//					userInfo.put(TAG_CODE, meta_obj.getString(TAG_CODE));
					} catch (Exception ex) {
						what = WHAT_ERROR;
						ex.printStackTrace();
					}
				} catch (Exception ex) {
					what = WHAT_ERROR;
					ex.printStackTrace();
				}

				mHandler.sendMessage(mHandler.obtainMessage(what, 1, 0));
			}
		}.start();
	}

	public void fetchUserName(final Handler handler) {
		mProgress = new ProgressDialog(mCtx);
		mProgress.setMessage("Loading ...");
		mProgress.show();

		new Thread() {
			@Override
			public void run() {
				Log.i(TAG, "Fetching user info");
				int what = WHAT_FINALIZE;
				try {
					URL url = new URL(API_URL + "/users/" + mSession.getId() + "/?access_token=" + mAccessToken);

					Log.d(TAG, "Opening URL " + url.toString());
					HttpURLConnection urlConnection = (HttpURLConnection) url
							.openConnection();
					urlConnection.setRequestMethod("GET");
					urlConnection.setDoInput(true);
					urlConnection.connect();
					String response = streamToString(urlConnection
							.getInputStream());
					System.out.println(response);
					JSONObject jsonObj = (JSONObject) new JSONTokener(response)
							.nextValue();



					JSONObject data_obj = jsonObj.getJSONObject(TAG_DATA);
					userInfo.put(TAG_ID, data_obj.getString(TAG_ID));

					userInfo.put(TAG_PROFILE_PICTURE,
							data_obj.getString(TAG_PROFILE_PICTURE));

					userInfo.put(TAG_USERNAME, data_obj.getString(TAG_USERNAME));

					userInfo.put(TAG_BIO, data_obj.getString(TAG_BIO));

					userInfo.put(TAG_WEBSITE, data_obj.getString(TAG_WEBSITE));

					JSONObject counts_obj = data_obj.getJSONObject(TAG_COUNTS);

					userInfo.put(TAG_FOLLOWS, counts_obj.getString(TAG_FOLLOWS));

					userInfo.put(TAG_FOLLOWED_BY,
							counts_obj.getString(TAG_FOLLOWED_BY));

					userInfo.put(TAG_MEDIA, counts_obj.getString(TAG_MEDIA));

					userInfo.put(TAG_FULL_NAME,
							data_obj.getString(TAG_FULL_NAME));

					JSONObject meta_obj = jsonObj.getJSONObject(TAG_META);

					userInfo.put(TAG_CODE, meta_obj.getString(TAG_CODE));
				} catch (Exception ex) {
					what = WHAT_ERROR;
					ex.printStackTrace();
				}
				mProgress.dismiss();
				handler.sendMessage(handler.obtainMessage(what, 2, 0));
			}
		}.start();

	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == WHAT_ERROR) {
				mProgress.dismiss();
				if (msg.arg1 == 1) {
					mListener.onFail("Failed to get access token");
				} else if (msg.arg1 == 2) {
					mListener.onFail("Failed to get user information");
				}
			} else if (msg.what == WHAT_FETCH_INFO) {
				// fetchUserName();
				mProgress.dismiss();
				mListener.onSuccess();
			}
		}
	};

	public HashMap<String, String> getUserInfo() {
		return userInfo;
	}

	public boolean hasAccessToken() {
		return (mAccessToken == null) ? false : true;
	}

	public void setListener(OAuthAuthenticationListener listener) {
		mListener = listener;
	}

	public String getUserName() {
		return mSession.getUsername();
	}

	public String getId() {
		return mSession.getId();
	}

	public String getName() {
		return mSession.getName();
	}

	public String getTOken() {
		return mSession.getAccessToken();
	}

	public void authorize() {
		// Intent webAuthIntent = new Intent(Intent.ACTION_VIEW);
		// webAuthIntent.setData(Uri.parse(AUTH_URL));
		// mCtx.startActivity(webAuthIntent);
		mDialog.show();
	}
	public void resetAccessToken() {
		if (mAccessToken != null) {
			mSession.resetAccessToken();
			mAccessToken = null;
		}
	}
	public interface OAuthAuthenticationListener {
		public abstract void onSuccess();
		public abstract void onFail(String error);
	}
	public String getProfile_picture() {
		return profile_picture;
	}

	public static String streamToString(InputStream is) throws IOException {
		String str = "";

		if (is != null) {
			StringBuilder sb = new StringBuilder();
			String line;

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));

				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}

				reader.close();
			} finally {
				is.close();
			}
			str = sb.toString();
		}
		return str;
	}
}