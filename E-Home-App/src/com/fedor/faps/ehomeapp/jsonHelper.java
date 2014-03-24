package com.fedor.faps.ehomeapp;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

public class jsonHelper {

	public static JSONObject makeJSON(String string) {

		try {
			return new JSONObject(string);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static int getInt(String key, JSONObject jo) {

		try {
			return jo.getInt(key);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

	}

	public static String getString(String key, JSONObject jo) {

		try {
			return jo.getString(key);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

	}

	public static boolean getBoolean(String key, JSONObject jo) {

		try {
			return jo.getBoolean(key);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * Not used yet, not sure if correct
	 * @param input
	 * @return
	 */
	public static JSONObject twoLevelHashToJo(
			HashMap<String, HashMap<String, String>> input) {

		try {
			JSONObject result = new JSONObject();

			Iterator<String> l1_keys = input.keySet().iterator();

			while (l1_keys.hasNext()) {

				String key1 = l1_keys.next();

				HashMap<String, String> cur_map = input.get(key1);

				Iterator<String> l2_keys = cur_map.keySet().iterator();
				JSONObject entry = new JSONObject();
				while (l2_keys.hasNext()) {

					String key2 = l2_keys.next();
					entry.put(key2, cur_map.get(key2));

				}

				result.put(key1, entry);

			}

			return result;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;

		}

	}

	public static HashMap<String, HashMap<String, String>> joTo2LevelHash(
			JSONObject jo) {
		HashMap<String, HashMap<String, String>> result = new HashMap<String, HashMap<String, String>>();
		Iterator<?> room_keys = jo.keys();
		try {
			int room_num = jo.length();

			for (int ii = 0; ii < room_num; ii++) {

				String room_name = (String) room_keys.next();
				HashMap<String, String> room = new HashMap<String, String>();

				JSONObject json_room = (JSONObject) jo.get(room_name);

				Iterator<?> entry_keys = json_room.keys();

				int entry_num = json_room.length();

				for (int jj = 0; jj < entry_num; jj++) {

					String entry_name = (String) entry_keys.next();

					String entry_status = json_room.getString(entry_name);

					room.put(entry_name, entry_status);

				}
				result.put(room_name, room);

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return result;
	}

}
