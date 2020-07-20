package com.example.jobscheduler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import androidx.core.content.ContextCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class GetCurrentWeatherJobService extends JobService {

    public static final String TAG = GetCurrentWeatherJobService.class.getSimpleName();
    final String API_KEY = "9deab7e7e85aae3dc3e6855e7bf9896f";
    final String CITY = "Semarang";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "onStartJob() executed");
        getCurrentWeather(jobParameters);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "onStartJob() executed");
        return true;
    }

    private void getCurrentWeather(final JobParameters jobParameters) {
        Log.d(TAG, "Running");
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + CITY + "&appid=" + API_KEY;
        Log.e(TAG, "getCurrentWeather: " + url);

        asyncHttpClient.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.d(TAG, result);

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String currentWeather = jsonObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tempInKelvin = jsonObject.getJSONObject("main").getDouble("temp");

                    double tempInCelcius = tempInKelvin - 273;
                    String temprature = new DecimalFormat("##.##").format(tempInCelcius);
                    String title = "Current Weather";
                    String message = currentWeather + ", " + description + " with " + temprature + " celcius";
                    int notifId = 100;

                    showNotification(getApplicationContext(), title, message, notifId);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        jobFinished(jobParameters, false);
                    }
                } catch (Exception e) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        jobFinished(jobParameters, true);
                    }
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    jobFinished(jobParameters, true);
                }
            }
        });
    }

    private void showNotification(Context context, String title, String message, int notifId) {
        String CHANNEL_ID = "Channel_1";
        String CHANNEL_NAME = "Job scheduler channel";

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_autorenew_black)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, android.R.color.black))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(alarmSound);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{1000, 1000, 1000, 1000, 1000});
            builder.setChannelId(CHANNEL_ID);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        Notification notification = builder.build();

        if (notificationManager != null) {
            notificationManager.notify(notifId, notification);
        }

    }
}
