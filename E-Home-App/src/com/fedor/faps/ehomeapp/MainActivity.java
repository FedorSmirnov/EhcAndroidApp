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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Context context = this;

	private Apartment apartment;
	private boolean atHome = false;

	// Variables for the display update
	private Handler updateHandler = new Handler();
	private int updateInterval = 1000;
	private Runnable update;

	// Strings generated from the constants
	private String url_local;
	private String url_external;
	private String url_pyserver;

	// Credentials received from the login activity
	private String email;
	private String password;

	// activity views

	private ImageButton ib_lampState;
	private ImageButton ib_redLamp;
	private ImageButton ib_sound;
	private TextView tv_temperature;
	private TextView tv_humidity;
	private ImageView iv_home;
	private ImageView iv_doorState;
	private ImageView iv_waterState;
	private ImageView iv_window;
	private ImageView iv_flowers;
	private ImageButton ib_rulesDia;
	private ImageButton ib_alarmDia;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_alt);

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
		ib_redLamp = (ImageButton) findViewById(R.id.ib_main_redLamp);
		ib_sound = (ImageButton) findViewById(R.id.ib_main_sound);
		iv_doorState = (ImageView) findViewById(R.id.iv_main_doorState);
		iv_waterState = (ImageView) findViewById(R.id.iv_main_waterState);
		tv_humidity = (TextView) findViewById(R.id.tv_main_humidity);
		tv_temperature = (TextView) findViewById(R.id.tv_main_temperature);

		iv_home = (ImageView) findViewById(R.id.iv_main_home);

		ib_rulesDia = (ImageButton) findViewById(R.id.ib_main_diaRules);

		ib_alarmDia = (ImageButton) findViewById(R.id.ib_main_diaAlarms);

		iv_window = (ImageView) findViewById(R.id.iv_main_window);

		iv_flowers = (ImageView) findViewById(R.id.iv_main_flowers);

		// Check whether the PyServer can be accessed through the local Ip
		LocalCheck locCheck = new LocalCheck();
		Void[] locDummy = null;

		try {
			atHome = locCheck.execute(locDummy).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!atHome) {
			iv_home.setImageResource(R.drawable.not_home_icon);
		}

		url_pyserver = "";
		if (atHome) {
			url_pyserver = "http://" + locIp + ":8080/";
		} else {
			url_pyserver = "http://" + extIp + ":8080/";
		}

		update = new Runnable() {

			@Override
			public void run() {
				PyServerGet stateCheck = new PyServerGet(url_pyserver);
				Void[] get_dummy = null;
				try {
					apartment = stateCheck.execute(get_dummy).get();
				} catch (Exception e) {
					e.printStackTrace();
				}
				updateDisplay();
				updateHandler.postDelayed(update, updateInterval);
			}
		};

		update.run();

		// set the button listeners

		ib_lampState.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String state = apartment.getDevState("Fedors_Zimmer", "Lampe");
				if (state.equals("on")) {
					apartment.setDevState("Fedors_Zimmer", "Lampe", "off");
				} else {
					apartment.setDevState("Fedors_Zimmer", "Lampe", "on");
				}

				sendMessage();

			}
		});

		ib_redLamp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String state = apartment
						.getDevState("Fedors_Zimmer", "Blinken");
				if (state.equals("on")) {
					apartment.setDevState("Fedors_Zimmer", "Blinken", "off");
				} else {
					apartment.setDevState("Fedors_Zimmer", "Blinken", "on");
				}

				sendMessage();

			}
		});

		// Disable the redLamp button
		ib_redLamp.setClickable(false);
		ib_sound.setClickable(false);
		ib_redLamp.setVisibility(View.INVISIBLE);
		ib_sound.setVisibility(View.INVISIBLE);

		ib_sound.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String state = apartment.getDevState("Fedors_Zimmer", "Sound");
				if (state.equals("on")) {
					apartment.setDevState("Fedors_Zimmer", "Sound", "off");
				} else {
					apartment.setDevState("Fedors_Zimmer", "Sound", "on");
				}

				sendMessage();

			}
		});

		// The listener for the rules dialogue

		ib_rulesDia.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Create the dialogue

				final Dialog ruleDia = new Dialog(context);
				ruleDia.setContentView(R.layout.dialogue_rules);

				ruleDia.setTitle(R.string.ruleTitle);

				// Init the dialogue views
				Button b_save = (Button) ruleDia
						.findViewById(R.id.b_diaRule_confirm);
				final RadioButton rb_dia_normal = (RadioButton) ruleDia
						.findViewById(R.id.rb_diaRule_lampBehavNorm);
				final RadioButton rb_dia_move = (RadioButton) ruleDia
						.findViewById(R.id.rb_diaRule_lampBehavMove);

				final Button moveBehaveTitle = (Button) ruleDia
						.findViewById(R.id.b_diaRule_lampBehavTitle);
				final TextView moveBehaveText = (TextView) ruleDia
						.findViewById(R.id.tv_diaRule_lampControl);
				final RadioGroup moveBehaveRadio = (RadioGroup) ruleDia
						.findViewById(R.id.rg_diaRule_lampBehav);
				final TextView lightTimeTitle = (TextView) ruleDia
						.findViewById(R.id.tv_diaRule_lightTimeTitle);
				final LinearLayout ll_lightTime = (LinearLayout) ruleDia
						.findViewById(R.id.ll_diaRule_timeControlLight);

				final TextView tv_diaRule_lightTime_value = (TextView) ruleDia
						.findViewById(R.id.tv_diaRule_timeControlLight_Value);
				final Button b_diaRule_light_minus = (Button) ruleDia
						.findViewById(R.id.b_diaRule_lightTime_minus);
				final Button b_diaRule_light_plus = (Button) ruleDia
						.findViewById(R.id.b_diaRule_lightTime_plus);

				final Button b_flowerRule_expand = (Button) ruleDia
						.findViewById(R.id.b_diaRule_flowerTitle);
				final LinearLayout ll_flowerRule_expand = (LinearLayout) ruleDia
						.findViewById(R.id.ll_diaRule_expand_flower);
				final TextView tv_flowerTime_value = (TextView) ruleDia
						.findViewById(R.id.tv_diaRule_flower_Value);
				final Button b_flower_minus = (Button) ruleDia
						.findViewById(R.id.b_diaRule_flower_minus);
				final Button b_flower_plus = (Button) ruleDia
						.findViewById(R.id.b_diaRule_flower_plus);

				moveBehaveText.setVisibility(View.GONE);
				moveBehaveRadio.setVisibility(View.GONE);
				lightTimeTitle.setVisibility(View.GONE);
				ll_lightTime.setVisibility(View.GONE);
				ll_flowerRule_expand.setVisibility(View.GONE);

				// The expansion of the light rule section
				moveBehaveTitle.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (moveBehaveText.getVisibility() == View.GONE) {
							moveBehaveText.setVisibility(View.VISIBLE);
							moveBehaveRadio.setVisibility(View.VISIBLE);
							lightTimeTitle.setVisibility(View.VISIBLE);
							ll_lightTime.setVisibility(View.VISIBLE);
						} else {
							moveBehaveText.setVisibility(View.GONE);
							moveBehaveRadio.setVisibility(View.GONE);
							lightTimeTitle.setVisibility(View.GONE);
							ll_lightTime.setVisibility(View.GONE);
						}

					}
				});

				// init of the time value
				int light_on_time = apartment.getNo_movement_time();
				String value = String.valueOf(light_on_time);
				tv_diaRule_lightTime_value.setText(value);

				// time_button behavior
				b_diaRule_light_minus
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								int curVal = Integer
										.valueOf(tv_diaRule_lightTime_value
												.getText().toString());
								int newVal = curVal - 30;

								if (newVal >= 30) {
									tv_diaRule_lightTime_value.setText(String
											.valueOf(newVal));
								}

							}
						});
				b_diaRule_light_plus
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								int curVal = Integer
										.valueOf(tv_diaRule_lightTime_value
												.getText().toString());
								int newVal = curVal + 30;

								if (newVal <= 300) {
									tv_diaRule_lightTime_value.setText(String
											.valueOf(newVal));
								}

							}
						});

				// Init the state of the radio buttons
				boolean cur_lampMode = apartment.getLamp_movement();

				if (cur_lampMode) {
					rb_dia_move.setChecked(true);
					rb_dia_normal.setChecked(false);
				} else {
					rb_dia_move.setChecked(false);
					rb_dia_normal.setChecked(true);
				}

				// The flower rule section
				b_flowerRule_expand
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								if (ll_flowerRule_expand.getVisibility() == View.GONE) {
									ll_flowerRule_expand
											.setVisibility(View.VISIBLE);
								} else {
									ll_flowerRule_expand
											.setVisibility(View.GONE);
								}

							}
						});

				// the flower time adjustment

				tv_flowerTime_value.setText(String.valueOf(apartment
						.getNo_water_time() / 60));

				b_flower_minus.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						int curVal = Integer.valueOf(tv_flowerTime_value
								.getText().toString());

						curVal--;
						if (curVal >= 1) {
							tv_flowerTime_value.setText(String.valueOf(curVal));
						}

					}
				});

				b_flower_plus.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						int curVal = Integer.valueOf(tv_flowerTime_value
								.getText().toString());

						curVal++;
						if (curVal <= 14) {
							tv_flowerTime_value.setText(String.valueOf(curVal));
						}

					}
				});

				// The confirm button
				b_save.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						int newLampTime = Integer
								.valueOf(tv_diaRule_lightTime_value.getText()
										.toString());
						int newFlowerTime = Integer.valueOf(tv_flowerTime_value
								.getText().toString()) * 60;
						apartment.setNo_water_time(newFlowerTime);
						apartment.setNo_movement_time(newLampTime);

						apartment.setLamp_movement(rb_dia_move.isChecked());
						sendMessage();

						ruleDia.dismiss();

					}
				});

				ruleDia.show();

			}
		});

		// the listener for the alarm dialogue
		ib_alarmDia.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final Dialog alaDia = new Dialog(context);
				alaDia.setContentView(R.layout.dialogue_alarms);

				TextView urgent = (TextView) alaDia
						.findViewById(R.id.tv_diaAlarm_urgent);

				TextView content = (TextView) alaDia
						.findViewById(R.id.tv_diaAlarm_Content);
				Button confirm = (Button) alaDia
						.findViewById(R.id.b_diaAlarm_ok);

				alaDia.setTitle(R.string.alaDia_Title);

				if (apartment.isAlarm() || apartment.isAlarm_urgent()) {

					String urgentString = "";

					if (apartment.isAlarm_urgent()) {
						urgentString = "DRINGEND:\n\n";

						for (int i = 0; i < apartment.getAlarmUrgentList()
								.size(); i++) {
							urgentString += "-"
									+ apartment.getAlarmUrgentList().get(i)
									+ "\n";
						}

					}

					urgent.setText(urgentString);

					String contentString = "";

					for (int i = 0; i < apartment.getAlarmList().size(); i++) {
						contentString += "\n-"
								+ apartment.getAlarmList().get(i);
					}

					content.setText(contentString);
				} else {
					content.setText(R.string.no_alarm);
				}

				confirm.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						alaDia.dismiss();

					}
				});

				alaDia.show();

			}
		});

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		updateHandler.removeCallbacks(update);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void sendMessage() {
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

	private void updateDisplay() {
		String temperature = apartment.getSensState("Fedors_Zimmer",
				"temperature");
		String humidity = apartment.getSensState("Fedors_Zimmer", "humidity");
		String lamp = apartment.getDevState("Fedors_Zimmer", "Lampe");
		String door = apartment.getSensState("Fedors_Zimmer", "Tuer");
		String water = apartment.getSensState("Fedors_Zimmer", "Wasseralarm");
		String redLamp = apartment.getDevState("Fedors_Zimmer", "Blinken");
		String sound = apartment.getDevState("Fedors_Zimmer", "Sound");
		String window = apartment.getSensState("Fedors_Zimmer", "Fenster");
		String flowers = apartment.getSensState("Fedors_Zimmer", "Wasserstand");

		tv_humidity.setText(humidity + "%");
		tv_temperature.setText(temperature + "°C");

		if (redLamp.equals("on")) {
			ib_redLamp.setImageResource(R.drawable.rot_blinken_icon_an);
		} else {
			ib_redLamp.setImageResource(R.drawable.rot_blinken_icon_aus);
		}

		if (sound.equals("on")) {
			ib_sound.setImageResource(R.drawable.sound_icon_an);
		} else {
			ib_sound.setImageResource(R.drawable.sound_icon_aus);
		}

		if (water.equals("wet")) {
			iv_waterState.setImageResource(R.drawable.nass_icon);
		} else {
			iv_waterState.setImageResource(R.drawable.trocken_icon);
		}

		if (flowers.equals("wet")) {
			iv_flowers.setImageResource(R.drawable.giesskanne_icon_ok);
		} else {
			iv_flowers.setImageResource(R.drawable.giesskanne_icon_not_ok);
		}

		if (door.equals("closed")) {
			iv_doorState.setImageResource(R.drawable.door_closed_icon);
		} else {
			iv_doorState.setImageResource(R.drawable.door_open_icon);
		}

		if (window.equals("open")) {
			iv_window.setImageResource(R.drawable.windows_icon_open);
		} else {
			iv_window.setImageResource(R.drawable.windows_icon_closed);
		}

		if (lamp.equals("on")) {

			ib_lampState.setImageResource(R.drawable.gluehbirne_an);
		} else {
			if (apartment.getLamp_movement()) {
				ib_lampState.setImageResource(R.drawable.gluehbirne_move);
			} else {

				ib_lampState.setImageResource(R.drawable.gluehbirne_aus);
			}
		}

		if (apartment.isAlarm() || apartment.isAlarm_urgent()) {
			ib_alarmDia.setImageResource(R.drawable.alarm_icon);
		} else {
			ib_alarmDia.setImageResource(R.drawable.no_alarm_icon);
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
