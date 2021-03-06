package com.bokwas.apirequests;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.AsyncTask;

import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.ApiResponse;
import com.bokwas.response.Comment;
import com.bokwas.response.Post;
import com.bokwas.util.AppData;
import com.bokwas.util.BokwasHttpClient;
import com.google.gson.Gson;

public class DeleteApi extends AsyncTask<String, Void, Boolean>{
	
	private String accessKey;
	private String postId;
	private String personId;
	private String commentId;
	private Activity activity;
	private APIListener listener;

	public DeleteApi(String accessKey, String postId, String personId,
			String commentId, Activity activity, APIListener listener) {
		super();
		this.accessKey = accessKey;
		this.postId = postId;
		this.personId = personId;
		this.commentId = commentId;
		this.activity = activity;
		this.listener = listener;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String apiUrl = null;
		List<BasicNameValuePair>  apiParams = new ArrayList<BasicNameValuePair>();
		apiUrl = AppData.getBaseURL() + "/deletepost";
		apiParams.add(new BasicNameValuePair("access_key", accessKey));
		apiParams.add(new BasicNameValuePair("post_id", postId));
		apiParams.add(new BasicNameValuePair("person_id", personId));
		apiParams.add(new BasicNameValuePair("comment_id", commentId));
		
		try {
			String response = BokwasHttpClient.postData(apiUrl, apiParams);
			ApiResponse apiResponse = new Gson().fromJson(
					response, ApiResponse.class);
			if(apiResponse.statusCode == 200) {
				if (commentId == null) {
					Post post = UserDataStore.getStore().getPost(postId);
					UserDataStore.getStore().deletePost(post);
					UserDataStore.getStore().removeOldNotifications();
					UserDataStore.getStore().save(activity);
				}else {
					Comment comment = UserDataStore.getStore().getPost(postId).getComment(commentId);
					UserDataStore.getStore().getPost(postId).getComments().remove(comment);
					UserDataStore.getStore().save(activity);
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if(listener!=null) {
			listener.onAPIStatus(result);
		}
	}

}
