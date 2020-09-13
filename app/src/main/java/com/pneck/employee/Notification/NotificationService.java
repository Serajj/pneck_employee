package com.pneck.employee.Notification;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.RemoteMessage;
import com.pneck.employee.Activities.MainActivity;
import com.pneck.employee.Activities.OrderProcess.JobAcceptScreen;
import com.pneck.employee.Activities.OrderProcess.OrderCompleteHappyScreen;
import com.pneck.employee.Const;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class NotificationService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG =  "FirebaseMessagingSlfad";
    public static int StrangersChatNOtificationId=23472326;
    public static int StrangersChatChatNotificationId=234325632;
    public static String NOTIFICATION_SHARED_PREF_NAME="StrangersChat&*^(notification*(";
    private String MyDefaultClickAction="com.app.android.StrangersChat.StrangersChat.StrangersChat_MAIN_ACTIVITY_TARGET";

    Date sysDate, resDate;
    String mBlockedTime, mSystemTime, responseBlockedTime;
    private LocalBroadcastManager broadcaster;
    NotificationCompat.Builder summaryNotificationBuilder;

    @Override
    public void onNewToken(String s) {
        UpdateTokenToServer(s);
        super.onNewToken(s);

        Log.e("dsadasdasdasdas",  "device token   "+s);
    }

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e("sdfsfsjkfsfslsd","firebase service is called ");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();
        }else {
            notificationManager=getManager();
        }



        Log.d(TAG, "From: "+remoteMessage.getFrom());
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        if (remoteMessage.getData().containsKey("image")){
            new createImageNotification(this,remoteMessage,
                    remoteMessage.getData().get("image")).execute();
        }else {
            sendNotification(remoteMessage);
        }

    }

    public class createImageNotification extends AsyncTask<String, Void, Bitmap> {

        private Context mContext;
        private RemoteMessage remoteMessage;
        private String  imageUrl;

        public createImageNotification(Context context, RemoteMessage remoteMessage, String imageUrl) {
            super();
            this.mContext = context;
            this.remoteMessage=remoteMessage;
            this.imageUrl = imageUrl;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            InputStream in;
            try {
                URL url = new URL(this.imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            showImageNotification(result,remoteMessage);

            /*******************Notification for promotional message
             * Intent intent = new Intent(mContext, MyOpenableActivity.class);
            intent.putExtra("key", "value");
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 100, intent, PendingIntent.FLAG_ONE_SHOT);

            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notif = new Notification.Builder(mContext)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(result)
                    .setStyle(new Notification.BigPictureStyle().bigPicture(result))
                    .build();
            notif.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(1, notif);*/
        }
    }

    private void showImageNotification(Bitmap image, RemoteMessage remoteMessage) {


        /*****************sending message notification ***************/
        Intent resultIntent = new Intent();

        SessionManager sessionManager=new SessionManager(NotificationService.this);
        if (!sessionManager.isLoggedIn()){
            return;
        }

        Log.e("kjdhfkdfsfd","this is complete notification "+remoteMessage.getData());
        String notificationGroup="";

        if (remoteMessage.getData().get("notification_type")
                .equalsIgnoreCase("NewBookingRequest")){

            resultIntent=new Intent(NotificationService.this, JobAcceptScreen.class);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            resultIntent.setAction(Long.toString(System.currentTimeMillis()));
            resultIntent.putExtra("booking_order_number",""+remoteMessage.getData().get("booking_order_number"));
            resultIntent.putExtra("booking_order_id",""+remoteMessage.getData().get("booking_order_id"));
            resultIntent.putExtra("distance_km",""+remoteMessage.getData().get("distance_km"));
            resultIntent.putExtra("customer_lat",""+remoteMessage.getData().get("user_lat"));
            resultIntent.putExtra("customer_long",""+remoteMessage.getData().get("user_long"));
        }else if (remoteMessage.getData().get("notification_type").equalsIgnoreCase("BookingCompleted")){

            resultIntent=new Intent(NotificationService.this, OrderCompleteHappyScreen.class);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            resultIntent.setAction(Long.toString(System.currentTimeMillis()));
            //resultIntent.putExtra("booking_order_number",remoteMessage.getData().get("booking_order_number"));
            resultIntent.putExtra("booking_order_id",remoteMessage.getData().get("booking_order_id"));
            /*resultIntent.putExtra("distance_km",remoteMessage.getData().get("distance_km"));
            resultIntent.putExtra("customer_lat",remoteMessage.getData().get("user_lat"));
            resultIntent.putExtra("customer_long",remoteMessage.getData().get("user_long"));*/
        } else {
            resultIntent=new Intent(NotificationService.this, MainActivity.class);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            resultIntent.setAction(Long.toString(System.currentTimeMillis()));
        }

        resultIntent.putExtra("kjdfksffs","data1");


        resultIntent.putExtra("kjdfsdaksffs","data2");

        Log.e("kjdhfkdfsfd","this is current result data "+resultIntent.getStringExtra("booking_order_id"));

        Integer userId=generateRandom();

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        userId,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );



        NotificationCompat.Builder mBuilder;


        Uri alarmSound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Log.e("gfdsgdgdsgsdgds","setting notification sound ");
        mBuilder=new NotificationCompat.Builder(this,
                getResources().getString(R.string.default_notification_channel_id));

        mBuilder.setSmallIcon(R.drawable.pneck_logo)
                .setWhen(0).setAutoCancel(true)
                .setGroup(notificationGroup)
                .setSound(alarmSound)
                .setContentIntent(resultPendingIntent)
                .setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.pneck_logo)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(remoteMessage.getData().get("message")))
                .setContentText(remoteMessage.getData().get("message"))
                .setLargeIcon(image)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            mBuilder.setChannelId(getResources().getString(R.string.default_notification_channel_id));
        }

        notificationManager.notify(userId, mBuilder.build());

    }

    private void sendNotification(RemoteMessage remoteMessage) {

        /*****************sending message notification ***************/
        Intent resultIntent = new Intent();

        SessionManager sessionManager=new SessionManager(NotificationService.this);
        if (!sessionManager.isLoggedIn()){
            return;
        }

        String notificationGroup="";

        if (remoteMessage.getData().get("notification_type")
                .equalsIgnoreCase("OrderReceiving")){


            resultIntent=new Intent(NotificationService.this, JobAcceptScreen.class);
            resultIntent.putExtra("booking_order_number",remoteMessage.getData().get("booking_order_number"));
            resultIntent.putExtra("booking_order_id",remoteMessage.getData().get("booking_order_id"));
            resultIntent.putExtra("distance_km",remoteMessage.getData().get("distance_km"));
            resultIntent.putExtra("customer_lat",remoteMessage.getData().get("user_lat"));
            resultIntent.putExtra("customer_long",remoteMessage.getData().get("user_long"));
        }else {
            resultIntent=new Intent(NotificationService.this, MainActivity.class);
        }


        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.setAction(Long.toString(System.currentTimeMillis()));


        Integer userId=generateRandom();

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        userId,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );



        NotificationCompat.Builder mBuilder;


        Uri alarmSound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Log.e("gfdsgdgdsgsdgds","setting notification sound ");
        mBuilder=new NotificationCompat.Builder(this,
                getResources().getString(R.string.default_notification_channel_id));

        mBuilder.setSmallIcon(R.drawable.pneck_logo)
                .setWhen(0).setAutoCancel(true)
                .setGroup(notificationGroup)
                .setSound(alarmSound)
                .setContentIntent(resultPendingIntent)
                .setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.pneck_logo)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("message"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            mBuilder.setChannelId(getResources().getString(R.string.default_notification_channel_id));
        }

/*****************sending message notification ***************/


        notificationManager.notify(userId, mBuilder.build());

    }


    private void sendNotification(String notiFromUserId, String notificationTitle,
                                  String notificationBody, String click_action) {

    /*****************sending message notification ***************/
        Intent resultIntent = new Intent();

        resultIntent=new Intent(click_action);

        resultIntent=new Intent(NotificationService.this, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.setAction(Long.toString(System.currentTimeMillis()));

        resultIntent.putExtra("clicked_userId",notiFromUserId);
        resultIntent.putExtra("clicked_userName",notificationTitle);
        resultIntent.putExtra("from_notification",true);

        Integer userId=Integer.parseInt(notiFromUserId);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        userId,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );



        NotificationCompat.Builder mBuilder;


        SessionManager sessionManager=new SessionManager(NotificationService.this);

        Uri alarmSound;
        alarmSound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        Log.e("gfdsgdgdsgsdgds","setting notification sound ");
        mBuilder=new NotificationCompat.Builder(this,
                getResources().getString(R.string.default_notification_channel_id));

        int priority;
        priority=NotificationCompat.PRIORITY_LOW;

        summaryNotificationBuilder = new NotificationCompat.Builder(NotificationService.this, "uncover_me_game_noti_channel_id")
                .setGroup(getString(R.string.message_notification_group))
                .setGroupSummary(true)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(resultPendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(0).setAutoCancel(true)
                .setGroup(notiFromUserId)
                .setSound(alarmSound)
                .setContentIntent(resultPendingIntent)
                .setWhen(System.currentTimeMillis()).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setPriority(priority)
                .setGroup(getString(R.string.message_notification_group))
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            mBuilder.setChannelId(getResources().getString(R.string.default_notification_channel_id));
        }

/*****************sending message notification ***************/


        notificationManager.notify(userId, mBuilder.build());
        notificationManager.notify(14620, summaryNotificationBuilder.build());

    }


    private NotificationManager notificationManager;

    private NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    private static String EnableSoundNotification="StrangersChat.enable_sound_notification";
    //private static String DisableSoundNotification="StrangersChat.disable_sound_notification";


    private void createChannels() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Enable sound
            NotificationChannel notificationChannel = new NotificationChannel(getString(R.string.default_notification_channel_id),
                    EnableSoundNotification, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            getManager().createNotificationChannel(notificationChannel);

        }

    }


    public int generateRandom(){
        Random random = new Random();
        return random.nextInt(9999 - 1000) + 1000;
    }

    private void UpdateTokenToServer(String token) {

        SessionManager sessionManager=new SessionManager(NotificationService.this);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "empDevicetoken";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("employee_id", sessionManager.getEmployeeId());
        dataParams.put("device_token",token);

        Log.e("klsddsfdsf", "this is we sending " + dataParams.toString());
        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                RegistrationSuccess(),
                RegistrationError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(NotificationService.this).add(dataParamsJsonReq);

    }


    private Response.Listener<JSONObject> RegistrationSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("klsddsfdsf", "this is complete response " + response);
                    if (response.getBoolean("success")) {
                    }

                } catch (Exception e) {
                    Log.v("klsddsfdsf", "inside catch block  " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }

    private Response.ErrorListener RegistrationError() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
    }


}