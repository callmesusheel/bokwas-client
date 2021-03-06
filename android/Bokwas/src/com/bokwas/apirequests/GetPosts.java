package com.bokwas.apirequests;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.bokwas.datasets.UserDataStore;
import com.bokwas.dialogboxes.GenericDialogOk;
import com.bokwas.dialogboxes.GenericDialogOk.DialogType;
import com.bokwas.response.ApiResponse;
import com.bokwas.response.GetPostsResponse;
import com.bokwas.response.Post;
import com.bokwas.util.AppData;
import com.bokwas.util.BokwasHttpClient;
import com.google.gson.Gson;

public class GetPosts extends AsyncTask<String, Void, Boolean> {

	private Context context;
	private String accessToken;
	private String bokwasName;
	private String avatarId;
	private String id;
	private String gcmRegId;
	private boolean isLogin = false;
	private APIListener postsListener;
	private String errorMessage;
	
	public interface APIListener {
		public void onAPIStatus(boolean status);
	}

	public GetPosts(Context context, String accessToken,
			String bokwasName, String avatarId,String gcmRegId, boolean isLogin, APIListener postsListener) {
		this.context = context;
		this.accessToken = accessToken;
		this.bokwasName = bokwasName;
		this.avatarId = avatarId;
		this.isLogin = isLogin;
		this.gcmRegId = gcmRegId;
		this.postsListener = postsListener;
	}
	
	public GetPosts(Context context, String accessToken, String id, APIListener postsListener) {
		this.context = context;
		this.accessToken = accessToken;
		this.id = id;
		this.postsListener = postsListener;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String apiUrl = null;
		List<BasicNameValuePair>  apiParams = new ArrayList<BasicNameValuePair>();
		if(isLogin) {
			apiUrl = AppData.getBaseURL() + "/login";
			apiParams.add(new BasicNameValuePair("access_token", accessToken));
			apiParams.add(new BasicNameValuePair("bokwas_name", bokwasName));
			apiParams.add(new BasicNameValuePair("bokwas_avatar_id", avatarId));
			apiParams.add(new BasicNameValuePair("gcmregid", gcmRegId));
		}else {
			apiUrl = AppData.getBaseURL() + "/getposts";
			apiParams.add(new BasicNameValuePair("access_token", accessToken));
			apiParams.add(new BasicNameValuePair("person_id", id));
		}
		String response = "";
		try {
			response = BokwasHttpClient.postData(apiUrl, apiParams);
			if (response != null) {
				GetPostsResponse getPostsResponse = new Gson().fromJson(
						response, GetPostsResponse.class);
				
				if(getPostsResponse.getStatus().statusCode==403) {
					errorMessage = getPostsResponse.getStatus().message;
					return false;
				}
				
				if(getPostsResponse.getAccess_key()!=null) {
					UserDataStore.getStore().setAccessKey(getPostsResponse.getAccess_key());
					Log.d("GetPosts","Access_Key : "+getPostsResponse.getAccess_key());
				}
//				List<BokwasUser> bokwasUsers = getPostsResponse.getUsers();
//				if(bokwasUsers!=null && bokwasUsers.size() > 0) {
//					for(BokwasUser bokwasUser : bokwasUsers) {
//						UserDataStore.getStore().getFriend(bokwasUser.userId).setBokwasName(bokwasUser.userBokwasName);
//						UserDataStore.getStore().getFriend(bokwasUser.userId).setBokwasAvatarId(bokwasUser.userBokwasAvatarId);
//					}
//				}
				
				List<Post> posts = getPostsResponse.getPosts();
				Log.d("GetPosts","Posts size : "+posts.size());
				for (Post post : posts) {
					UserDataStore.getStore().addNewPost(post);
				}
				long sinceLocal = posts.get(posts.size()-1).getUpdatedTime();
				context.getSharedPreferences("bokwas", Context.MODE_PRIVATE).edit().putLong("since", sinceLocal).commit();
				if(gcmRegId!=null && !gcmRegId.trim().equals("")) {
					UserDataStore.getStore().setGcmUpdated(true);
				}
				UserDataStore.getStore().save(context);
			}
		} catch (Exception e) {
			try {
				ApiResponse errorResponse = new Gson().fromJson(response, ApiResponse.class);
				if (errorResponse.statusCode == 403) {
					// Because there still aren't 5 friends on server
					errorMessage = errorResponse.message;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return false;
			
		}
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if(result) {
			if (postsListener != null) {
				postsListener.onAPIStatus(true);
			}
		}else {
			if (postsListener != null) {
				postsListener.onAPIStatus(false);
			}
			if(isLogin) {
				//Maintain status of login failure to show 
			}
			if(errorMessage!=null) {
				GenericDialogOk dialog = new GenericDialogOk(context, "Bokwas", errorMessage,"Invite",DialogType.DIALOG_INVITE);
				dialog.show();
				return;
			}
			Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
		}
	}

}
