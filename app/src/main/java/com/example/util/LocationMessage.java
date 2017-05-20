package com.example.util;

import java.io.Serializable;

public class LocationMessage implements Serializable{
	private static final long serialVersionUID = 1L;
	private double lat;//经度
	private double lng;//纬度

	public void setLng(double lng) {
		this.lng = lng;
	}

	public void setLat(double lat)
	{
		this.lat=lat;
	}

	public double getLat()
	{
		return lat;
	}

	public double getLng()
	{
		return lng;
	}


}
