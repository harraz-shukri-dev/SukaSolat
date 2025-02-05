package com.example.sukasolat;

import com.google.gson.annotations.SerializedName;

public class PrayerTimesResponse {
    @SerializedName("data")
    private Data data;

    public Data getData() {
        return data;
    }

    public static class Data {
        @SerializedName("timings")
        private Timings timings;

        @SerializedName("method")
        private Method method;

        public Timings getTimings() {
            return timings;
        }

        public Method getMethod() {
            return method;
        }

        public static class Timings {
            @SerializedName("Imsak")
            private String imsak;
            @SerializedName("Fajr")
            private String fajr;
            @SerializedName("Sunrise")
            private String sunrise;
            @SerializedName("Dhuhr")
            private String dhuhr;
            @SerializedName("Asr")
            private String asr;
            @SerializedName("Maghrib")
            private String maghrib;
            @SerializedName("Isha")
            private String isha;

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

        public static class Method {
            @SerializedName("id")
            private int id;
            @SerializedName("name")
            private String name;

            public int getId() {
                return id;
            }

            public String getName() {
                return name;
            }
        }
    }
}