package com.fedor.faps.ehomeapp;

import com.google.gson.Gson;

public class MyGSON {

	
	
	String jsonInput = "{\"Fedors_Zimmer\":{\"sensors\":{\"temperature\":\"18.0\",\"humidity\":\"58\"},\"devices\":{\"Lampe\":\"off\"}}}";
	Apartment apartment = new Gson().fromJson(jsonInput, Apartment.class);
	
	String bla = "bla";
	
	
}
