package com.fedor.faps.ehomeapp;

import java.util.HashMap;

import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	// Constants used by the activity
	private String url = MyConstants.BASEPATH + "mobileInterface";

	// View elements used by the activity
	private CheckBox cb_login_remember;
	
	private EditText et_email;
	private EditText et_password;

	private Button b_login;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Init of the view elements
		cb_login_remember = (CheckBox) findViewById(R.id.cb_login_remember);

		et_email = (EditText) findViewById(R.id.et_login_email_enter);
		et_password = (EditText) findViewById(R.id.et_login_pw_enter);

		b_login = (Button) findViewById(R.id.b_login_login);

		// Look into the storage; If the credentials are remembered, write them
		// into the et views

		SharedPreferences sp = LoginActivity.this
				.getPreferences(Activity.MODE_PRIVATE);
		boolean remember = sp.getBoolean("remember", false);

		if (remember) {
			String email = sp.getString("email", "");
			String password = sp.getString("password", "");

			cb_login_remember.setChecked(true);

			et_email.setText(email);
			et_password.setText(password);
		}

		// Set the onClick of the button
		
		

		b_login.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				// Read the input views
				String email = et_email.getText().toString();
				String pw = et_password.getText().toString();
				boolean remember = cb_login_remember.isChecked();

				// Save the state of the checkbox and if it is checked, also
				// save the credentials

				SharedPreferences sp = LoginActivity.this
						.getPreferences(Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();

				editor.putBoolean("remember", remember);

				if (remember) {
					editor.putString("email", email);
					editor.putString("password", pw);
				}

				editor.apply();

				// Create the hashmap that will be given to the asynctask
				HashMap<String, String> inputs = new HashMap<String, String>();

				inputs.put("target", url);
				inputs.put("func", "login");
				inputs.put("src", "mobile");
				inputs.put("email", email);
				inputs.put("pw_user", pw);

				AsyncLogin log = new AsyncLogin(inputs, LoginActivity.this);
				Void[] dummy = null;
				log.execute(dummy);

			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	private class AsyncLogin extends MyAsyncTask {

		public AsyncLogin(HashMap<String, String> inputs, Context context) {
			super(inputs, context);
		}

		@Override
		protected void onPostExecute(String result) {
			JSONObject jo = jsonHelper.makeJSON(result);

			String response = jsonHelper.getString("response", jo);
			String extIp = jsonHelper.getString("extIp", jo);
			String locIp = jsonHelper.getString("locIp", jo);

			if (response.equals("success")) {

				String email = et_email.getText().toString();
				String password = et_password.getText().toString();

				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				intent.putExtra("email", email);
				intent.putExtra("password", password);
				intent.putExtra("locIp", locIp);
				intent.putExtra("extIp", extIp);

				startActivity(intent);
				finish();

			} else {
				String message = context.getText(
						R.string.incorrest_data_message).toString();
				Toast.makeText(super.context, message, Toast.LENGTH_SHORT)
						.show();
			}

			progress.dismiss();
		}
	}

}
