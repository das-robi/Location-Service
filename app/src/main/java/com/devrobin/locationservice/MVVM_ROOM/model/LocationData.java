package com.devrobin.locationservice.MVVM_ROOM.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "location_table")
public class LocationData {

    @PrimaryKey(autoGenerate = true)
    private int locId;
//
//    @SerializedName("countryName")
//    private String countryName;
//
//    @SerializedName("areaName")
//    private String areaName;

    @ColumnInfo(name = "lat")
    private double lat;

    @ColumnInfo(name = "lng")
    private double lng;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    public LocationData(double lat, double lng, long timestamp) {
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
    }

    //Getters and Setters
    public int getLocId() {
        return locId;
    }

    public void setLocId(int locId) {
        this.locId = locId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
