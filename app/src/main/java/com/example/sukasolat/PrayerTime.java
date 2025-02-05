package com.example.sukasolat;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "prayer_times")
public class PrayerTime {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String imsak;
    public String fajr;
    public String sunrise;
    public String dhuhr;
    public String asr;
    public String maghrib;
    public String isha;

    public void setImsak(String imsak) {
        this.imsak = imsak;
    }

    public void setFajr(String fajr) {
        this.fajr = fajr;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public void setDhuhr(String dhuhr) {
        this.dhuhr = dhuhr;
    }

    public void setAsr(String asr) {
        this.asr = asr;
    }

    public void setMaghrib(String maghrib) {
        this.maghrib = maghrib;
    }

    public void setIsha(String isha) {
        this.isha = isha;
    }

    public PrayerTime(String imsak, String fajr, String sunrise, String dhuhr, String asr, String maghrib, String isha) {
        this.imsak = imsak;
        this.fajr = fajr;
        this.sunrise = sunrise;
        this.dhuhr = dhuhr;
        this.asr = asr;
        this.maghrib = maghrib;
        this.isha = isha;


    }

    public String getImsak() {
        return imsak;
    }

    public String getFajr() {
        return fajr;
    }

    public String getSunrise() {
        return sunrise;
    }

    public String getDhuhr() {
        return dhuhr;
    }

    public String getAsr() {
        return asr;
    }

    public String getMaghrib() {
        return maghrib;
    }

    public String getIsha() {
        return isha;
    }
}