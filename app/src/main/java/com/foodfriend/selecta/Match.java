package com.foodfriend.selecta;

/**
 * Created by Dan on 03/04/2018.
 */

public class Match {
    private String image;
    private String name;
    private String poi;
    private String time;
    private String date;

    public Match(String image, String name, String poi, String time, String date) {
        this.image = image;
        this.name = name;
        this.poi = poi;
        this.time = time;
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPoi() {
        return poi;
    }

    public void setPoi(String poi) {
        this.poi = poi;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
