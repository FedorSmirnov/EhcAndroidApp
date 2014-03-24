package com.fedor.faps.ehomeapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class httpHelper {

	public static String getResponseGet(String url) {
		
		HttpParams httpParameters = new BasicHttpParams();
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		HttpClient client = new DefaultHttpClient(httpParameters);
		HttpGet get = new HttpGet(url);
		String result = "";

		try {
			HttpResponse response = client.execute(get);
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			String line = "";
			while ((line = rd.readLine()) != null) {
				result = result + line + "\n";

			}
			return result;
		} catch (Exception e) {
			return "false";
		}

	}

	public static String getResponsePost(String url,
			HashMap<String, String> inputs) {

		HttpClient client = new DefaultHttpClient();

		HttpPost post = new HttpPost(url);
		String result = "";

		try {

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

			for (Entry<String, String> entry : inputs.entrySet()) {

				String key = entry.getKey();
				String value = entry.getValue();

				nameValuePairs.add(new BasicNameValuePair(key, value));

			}

			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			String line = "";
			while ((line = rd.readLine()) != null) {
				result = result + line + "\n";

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

}
