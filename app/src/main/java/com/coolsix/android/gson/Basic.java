package com.coolsix.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2018/1/28.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    @SerializedName("id")
    public Update update;


    public class Update {

        @SerializedName("loc")
        public String updateTime;
    }
}
