package com.bokwas;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.bokwas.datasets.UserDataStore;
import com.bokwas.util.LocalStorage;
import com.bokwas.util.UserDetails;

public class SplashScreen extends Activity {

	private int SPLASH_TIME_OUT = 3000;
	private Context context;
	private String TAG = "SplashScreen";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spash_screen);
		context = this;
		logKeyHash();
		initAppData();
		moveToNextPage();
	}
	
	private void initAppData() {
		if(LocalStorage.getObj(this, UserDataStore.class)!=null) {
			UserDataStore.setInstance(LocalStorage.getObj(this, UserDataStore.class));
			Log.d(TAG , "UserId: "+UserDataStore.getStore().getUserId());
			Log.d(TAG , "BokwasName: "+UserDataStore.getStore().getBokwasName());
			Log.d(TAG , "accessToken: "+UserDataStore.getStore().getUserAccessToken());
		}
	}

	private void logKeyHash() {
		try {
	        PackageInfo info = getPackageManager().getPackageInfo(
	                "com.facebook.samples.loginhowto", 
	                PackageManager.GET_SIGNATURES);
	        for (Signature signature : info.signatures) {
	            MessageDigest md = MessageDigest.getInstance("SHA");
	            md.update(signature.toByteArray());
	            Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
	            }
	    } catch (NameNotFoundException e) {

	    } catch (NoSuchAlgorithmException e) {

	    }
	}

	private void moveToNextPage() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				if (UserDataStore.getStore().getUserId()!=null) {
					Intent i = new Intent(SplashScreen.this,
							ProfileChooserActivity.class);
					startActivity(i);
				} else {
					Intent i = new Intent(SplashScreen.this,
							LoginActivity.class);
					startActivity(i);
					overridePendingTransition(R.anim.activity_slide_in_left,
							R.anim.activity_slide_out_left);
				}
				finish();
			}
		}, SPLASH_TIME_OUT);
	}

}
