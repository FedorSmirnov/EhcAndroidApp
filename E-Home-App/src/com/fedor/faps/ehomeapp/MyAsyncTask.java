package com.fedor.faps.ehomeapp;

import java.util.HashMap;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class MyAsyncTask extends AsyncTask<Void, Void, String> {

	protected HashMap<String, String> inputs;
	protected ProgressDialog progress;
	protected Context context;

	public MyAsyncTask(HashMap<String, String> inputs, Context context) {
		this.inputs = inputs;
		this.context = context;
	}

	@Override
	protected void onPreExecute() {

		progress = ProgressDialog.show(context,
				context.getText(R.string.progress_dialog_title),
				context.getText(R.string.progress_dialog_message), true);
	}
	
	@Override
	protected String doInBackground(Void... params) {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY); // makes the execution faster
		String url = inputs.get("target");
		inputs.remove("target");

		String result = httpHelper.getResponsePost(url, inputs);
		return result;
	}
	
	// Extending classes have to override this method
	@Override
	protected void onPostExecute(String result) {
		return;
	}

}
