package com.coolsix.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolsix.android.gson.Forecast;
import com.coolsix.android.gson.Weather;
import com.coolsix.android.service.AutoUpdateService;
import com.coolsix.android.util.HttpUtil;
import com.coolsix.android.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;
    public DrawerLayout drawerLayout;
    private Button navButton;

private ScrollView weatherLayout;
    private TextView titleCity;
    private  TextView titleUpdateTime;
    private  TextView degreeText;
    private  TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private  TextView pm25Text;
    private  TextView comfortText;
    private  TextView carWashText;
    private  TextView sportText;
private ImageView bingPicImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //Android5.0以上的系统才支持，系统版本号的判断，5.0以上的代码才会执行后面的代码
     if(Build.VERSION.SDK_INT>=21){
         //首先调用getWindow().getDecorView()拿到当前活动的DecorView，。
         View dectorView=getWindow().getDecorView();
         //再调用它的方法改变系统UI的显示,两个参数表示活动的布局会显示到状态栏上.
         dectorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
         //调用这个方法将状态栏设置为透明.
         getWindow().setStatusBarColor(Color.TRANSPARENT);
     }


        //初始化各个控件，1 首先获取控件的实例
        weatherLayout= (ScrollView) findViewById(R.id.weather_layout);
        titleCity= (TextView) findViewById(R.id.title_city);
        titleUpdateTime= (TextView) findViewById(R.id.title_update_time);
        degreeText= (TextView) findViewById(R.id.degree_text);
        weatherInfoText= (TextView) findViewById(R.id.weather_info_text);
        forecastLayout= (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText= (TextView) findViewById(R.id.aqi_text);
        pm25Text= (TextView) findViewById(R.id.pm25_text);
        comfortText= (TextView) findViewById(R.id.comfort_text);
        carWashText= (TextView) findViewById(R.id.car_wash_text);
        sportText= (TextView) findViewById(R.id.sport_text);
        bingPicImg= (ImageView) findViewById(R.id.bing_pic_img);
        swipeRefresh= (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        drawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton= (Button) findViewById(R.id.nav_button);



        //设置下拉刷新的进度条的颜色
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);

        //按钮单击事件
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //单击按钮时打开侧滑菜单
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        //2 然后尝试从本次缓存中读取天气数据。
        if(weatherString!=null){
            //有缓存直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            mWeatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
           //3 第一次缓存中没有,因此就会从Intent中取出天气的id
            //无缓存时去服务器查询天气
            mWeatherId=getIntent().getStringExtra("weather_id");
            //String weatherId=getIntent().getStringExtra("weather_id");
            //5 请求数据的时候先将ScrollView隐藏，不然空数据的界面看上去很奇怪。
            weatherLayout.setVisibility(View.INVISIBLE);
            //4 并调用requestWeather()方法来从服务器请求天气数据
            requestWeather(mWeatherId);
        }

        //下拉刷新的监听器,当下拉操作时,就会调用这个方法.
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //重新请求天气信息。
                requestWeather(mWeatherId);
            }
        });

        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }
    }

    private void loadBingPic() {

       String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }


        });
    }


    //根据天气Id请求城市天气信息
    public void requestWeather(final String weatherId) {

     //6 首先使用参数中传入的天气id和之前申请的APIkey拼接出一个接口地址
      String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
         // String  weatherUrl   =  "http://guolin.tech/api/weather?cityid=weatherId&key=0885425e02e54661bd27eb5c6398a25d";
    //7接着调用HttpUtil.sendOkHttpRequest 方法向改地址发出请求
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            //8 服务器会将相应城市的天气信息以JSON格式返回。
                final String responseText=response.body().string();
                //9 首先调用Utility.handleWeatherResponse方法将返回的JSON数据转换成Weather对象.
                final Weather weather=Utility.handleWeatherResponse(responseText);
                //10 再将当前线程切换到主线程,然后进行判断
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //11 如果服务器返回的状态是ok,就说明请求天气成功了,此时将缓存的数据保存到SharedPreferences中
                        if(weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            //12 并调用如下方法进行显示.
                            showWeatherInfo(weather);
                        }else{
                          Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();

                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

//处理并展示Weather实体中的数据
    private void showWeatherInfo(Weather weather) {
        //13 里面的代码很简单,首先从Weather对象中获取数据,然后显示到相应的控件上.
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split("  ")[1];
        String degree=weather.now.temperature+"°C";
        String weatherInfo=weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        //14 用了一个for循环来处理每天的天气信息,并在循环中动态加载forecast_item布局并设置相应的数据,然后添加到父布局当中.
        for(Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
           TextView dateText= view.findViewById(R.id.date_text);
            TextView infoText= view.findViewById(R.id.info_text);
            TextView maxText= view.findViewById(R.id.max_text);
            TextView minText= view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度："+weather.suggestion.comfort.info;
        String carWash="洗车指数："+weather.suggestion.carWash.info;
        String sport="运动建议："+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        //15 设置完了所有的数据之后,记得要将ScrollView重新变成可见.
        weatherLayout.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }


}
