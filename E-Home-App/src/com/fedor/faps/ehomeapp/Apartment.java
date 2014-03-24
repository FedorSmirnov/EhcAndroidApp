package com.fedor.faps.ehomeapp;

import java.util.ArrayList;

public class Apartment {

	private ArrayList<Room> rooms;
	private String User;
	private String Password;

	public static class Room {

		private String name;
		private ArrayList<Device> devices;
		private ArrayList<Sensor> sensors;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ArrayList<Device> getDevices() {
			return devices;
		}

		public void setDevices(ArrayList<Device> devices) {
			this.devices = devices;
		}

		public ArrayList<Sensor> getSensors() {
			return sensors;
		}

		public void setSensors(ArrayList<Sensor> sensors) {
			this.sensors = sensors;
		}

	}

	public static class Sensor {
		private String name;
		private String state;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}
	}

	public static class Device {
		private String name;
		private String state;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

	}

	public String getDevState(String room_name, String dev_name) {

		for (Room room : this.rooms) {
			if (room.getName().equals(room_name)) {
				for (Device device : room.getDevices()) {
					if (device.getName().equals(dev_name)) {
						return device.getState();
					}
				}
			}
		}

		return "Failure";

	}
	
	public void setDevState(String room_name, String dev_name, String dev_state){
		for (Room room:this.rooms){
			if (room.getName().equals(room_name)){
				for (Device device:room.getDevices()){
					if (device.getName().equals(dev_name)){
						device.setState(dev_state);
						return;
					}
				}
			}
		}
		return;
	}

	public String getSensState(String room_name, String sens_name) {
		for (Room room : this.rooms) {
			if (room.getName().equals(room_name)) {
				for (Sensor sens : room.getSensors()) {
					if (sens.getName().equals(sens_name)) {
						return sens.getState();
					}
				}
			}
		}
		return "Failure";

	}

	public String getUser() {
		return User;
	}

	public void setUser(String user) {
		this.User = user;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		this.Password = password;
	}

	public ArrayList<Room> getRooms() {
		return rooms;
	}

	public void setRooms(ArrayList<Room> rooms) {
		this.rooms = rooms;
	}

}
