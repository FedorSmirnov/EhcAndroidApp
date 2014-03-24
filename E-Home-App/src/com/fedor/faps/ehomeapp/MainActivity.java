package com.fedor.faps.ehomeapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;

import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Apartment apartment;
	private boolean atHome = false;

	// Strings generated from the constants
	private String url_basepath = MyConstants.BASEPATH;

	private String url_local;
	private String url_external;

	// Credentials received from the login activity
	private String email;
	private String password;

	// activity views
	
	
	private ImageButton ib_lampState;
	private TextView tv_temperature;
	private TextView tv_humidity;
	private ImageView iv_home;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// get credentials from the intent
		Intent intent = getIntent();
		email = intent.getStringExtra("email");
		password = intent.getStringExtra("password");

		String locIp = intent.getStringExtra("locIp");
		url_local = "http://" + locIp + ":8080";

		String extIp = intent.getStringExtra("extIp");
		url_external = "http://" + extIp + ":8080";

		// init the views

		
		ib_lampState = (ImageButton) findViewById(R.id.ib_main_lampState);

		tv_humidity = (TextView) findViewById(R.id.tv_main_humidity);
		tv_temperature = (TextView) findViewById(R.id.tv_main_temperature);

		iv_home = (ImageView) findViewById(R.id.iv_main_home);

		// Check whether the PyServer can be accessed through the local Ip
		LocalCheck locCheck = new LocalCheck();
		Void[] locDummy = null;

		try {
			atHome = locCheck.execute(locDummy).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!atHome) {
			iv_home.setAlpha(0);
		}

		// get the ini lamp state
		// HashMap<String, String> inputs = new HashMap<String, String>();
		// inputs.put("src", "mobile");
		// inputs.put("email", email);
		// inputs.put("pw_user", password);
		// inputs.put("func", "getState");
		// String url = url_basepath + "mobileInterface";
		// inputs.put("target", url);
		// DbAccess task = new DbAccess(inputs, MainActivity.this);
		// Void[] dummy = null;
		// task.execute(dummy);

		String url_pyserver = "";
		if (atHome) {
			url_pyserver = "http://" + locIp + ":8080/";
		} else {
			url_pyserver = "http://" + extIp + ":8080/";
		}

		PyServerGet stateCheck = new PyServerGet(url_pyserver);
		Void[] get_dummy = null;
		try {
			apartment = stateCheck.execute(get_dummy).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		updateDisplay();

		// set the button listener

		ib_lampState.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String state = apartment.getDevState("Fedors_Zimmer", "Lampe");
				if (state.equals("on")) {
					apartment.setDevState("Fedors_Zimmer", "Lampe", "off");
				} else {
					apartment.setDevState("Fedors_Zimmer", "Lampe", "on");
				}

				Void[] post_dummy = null;
				String url = "";
				if (atHome) {
					url = url_local;
				} else {
					url = url_external;
				}
				PyServerPost pypost = new PyServerPost(MainActivity.this, url);
				pypost.execute(post_dummy);
				updateDisplay();

			}
		});

		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void updateDisplay() {
		String temperature = apartment.getSensState("Fedors_Zimmer",
				"temperature");
		String humidity = apartment.getSensState("Fedors_Zimmer", "humidity");
		String lamp = apartment.getDevState("Fedors_Zimmer", "Lampe");

		tv_humidity.setText(humidity + "%");
		tv_temperature.setText(temperature + "°C");

		if (lamp.equals("on")) {
			
			ib_lampState.setImageResource(R.drawable.gluehbirne_an);
		} else {
			
			ib_lampState.setImageResource(R.drawable.gluehbirne_aus);
		}

	}

	private class LocalCheck extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Boolean doInBackground(Void... params) {
			String result = httpHelper.getResponseGet(url_local + "/handshake");
			return result.trim().equals("true");
		}

		// Extending classes have to override this method
		@Override
		protected void onPostExecute(Boolean result) {

			return;
		}

	}

	private class PyServerGet extends AsyncTask<Void, Void, Apartment> {

		protected String url;

		public PyServerGet(String url) {
			this.url = url;
		}

		@Override
		protected void onPreExecute() {

			//
		}

		@Override
		protected Apartment doInBackground(Void... params) {
			HttpClient client = new DefaultHttpClient();

			String result_string = "";
			List<NameValuePair> get_params = new LinkedList<NameValuePair>();
			get_params.add(new BasicNameValuePair("Password", password));
			get_params.add(new BasicNameValuePair("User", email));
			String paramString = URLEncodedUtils.format(get_params, "utf-8");
			url = url + "?" + paramString;
			HttpGet get_request = new HttpGet(url);

			try {

				HttpResponse response = client.execute(get_request);
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));

				String line = "";
				while ((line = rd.readLine()) != null) {
					result_string = result_string + line + "\n";

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			Apartment result = new Gson().fromJson(result_string,
					Apartment.class);
			return result;
		}

		@Override
		protected void onPostExecute(Apartment result) {
			return;
		}

	}

	private class PyServerPost extends AsyncTask<Void, Void, String> {
		protected String url;
		protected ProgressDialog progress_post;
		protected Context context;

		public PyServerPost(Context context, String url) {
			this.context = context;
			this.url = url;
		}

		@Override
		protected void onPreExecute() {

			progress_post = ProgressDialog.show(context,
					context.getText(R.string.progress_dialog_title),
					context.getText(R.string.progress_dialog_message), true);
		}

		@Override
		protected String doInBackground(Void... params) {
			HttpClient client = new DefaultHttpClient();
			String json_string = new Gson().toJson(apartment);
			StringEntity se = null;
			try {
				se = new StringEntity(json_string);
				HttpPost post_request = new HttpPost(url);
				post_request.setEntity(se);
				post_request.setHeader("Accept", "application/json");
				post_request.setHeader("Content-type", "application/json");
				String result_string = "";
				HttpResponse response = client.execute(post_request);
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));

				String line = "";
				while ((line = rd.readLine()) != null) {
					result_string = result_string + line + "\n";

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return "success";

		}

		@Override
		protected void onPostExecute(String result) {
			progress_post.dismiss();
			return;
		}
	}

}
