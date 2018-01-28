package com.coolsix.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2018/1/28.
 */

public class Now {
    @SerializedName("temp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;

    }
}
