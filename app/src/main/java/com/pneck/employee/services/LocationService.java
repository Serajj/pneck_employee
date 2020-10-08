package com.pneck.employee.services;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.pneck.employee.Activities.MainActivity;
import com.pneck.employee.Const;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static android.app.AlarmManager.ELAPSED_REALTIME;
import static android.os.SystemClock.elapsedRealtime;


public class LocationService extends Service {

    public static String errorData = "No Error";
    public static String completeResponseData = "";
    private static final String TAG = "Seraj";
    private LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 8 * 1000;  /* 4 secs */
    private final static long FASTEST_INTERVAL = 1000 * 5; /* 2 sec */
    public static double LastKnownLatitude = 0, LastKnownLongitude = 0;

    private Looper myLooper;
    private LocationCallback mLocationCallback = new LocationCallback() {


        @Override
        public void onLocationResult(LocationResult locationResult) {

            Log.d(TAG, "onLocationResult: got location result.");

            final Location location = locationResult.getLastLocation();

            Log.d("Seraj", "the location service call back IsStopped = " + IsStopped);

            Log.d("Seraj", "location is not stopped ");
            if (!IsStopped) {
                Log.d("Seraj", "location is not stopped ");
                if (location != null) {
                    Log.d("Seraj", "location is not null " + LastKnownLatitude);
                    LastKnownLatitude = location.getLatitude();
                    LastKnownLongitude = location.getLongitude();
                    saveUserLocation(location.getLatitude(), location.getLongitude());
                    Log.d("Seraj", "location LastKnownLatitude " + LastKnownLatitude);
                } else {
                    startOtherLocation();
                    Log.d("Seraj", "location is null ");
                }
            } else {
                Log.d("Seraj", "stopping fused location api");
                if (mFusedLocationClient != null) {
                    mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                }
                stopSelf();
                return;
            }
        }
    };

    private void startOtherLocation() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        LastKnownLatitude = location.getLatitude();
                        LastKnownLongitude = location.getLongitude();
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(LocationService.this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        @SuppressLint("WrongConstant") PendingIntent pendingIntent=PendingIntent.getActivity(this, 0,
                notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "employee_location_023";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Employee Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("First")
                    .setContentText("")
                    .setContentIntent(pendingIntent).build();

            startForeground(1, notification);
        }
    }
    private boolean IsStopped=false;
    private boolean IsArreadyRunnig=false;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Seraj", "onStartCommand: called.");
        if (!IsArreadyRunnig){
            if (intent.getBooleanExtra("is_stop",true)){
                this.stopSelf();
                IsStopped=true;
                IsArreadyRunnig=false;
            }else {
                String CHANNEL_ID = "employee_location_023";
                String CHANNEL_NAME = "Employee Channel";
                NotificationChannel channel = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    channel = new NotificationChannel(CHANNEL_ID,
                            CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                }

                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (manager != null) {
                        manager.createNotificationChannel(channel);
                    }
                }


                Notification notification = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    notification = new Notification.Builder(this, CHANNEL_ID)
                            .setContentTitle("")
                            .setContentText("")
                            .build();
                    this.startForeground(1,notification);
                }
                IsArreadyRunnig=true;
                IsStopped=false;
                getLocation();
                startLookingForJobs();
                isOtpcalled=false;
            }
        }
        Log.d("Seraj", "onStartCommand: called. "+IsStopped);
        return START_NOT_STICKY;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    @Override
    public void onTaskRemoved(Intent rootIntent){
        Log.d("data_response","onTaskRemoved is called ");

        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmService.set(ELAPSED_REALTIME, elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }


    private void startLookingForJobs() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
                                      @Override
                                      public void run() {
                                          if (!IsStopped){
                                              checkForUserLocation();
                                          }
                                      }
                                  },
                0, 10000);
    }


    private void checkForUserLocation(){


        SessionManager sessionManager=new SessionManager(LocationService.this);
        if (!sessionManager.isLoggedIn()&&sessionManager.getEmployeeId()==null){
            stopSelf();
            return;
        }
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/empNearByABooking";
        HashMap<String, String> dataParams = new HashMap<String, String>();
        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());
        dataParams.put("curr_lat",""+LastKnownLatitude);
        dataParams.put("curr_long",""+LastKnownLongitude);
        /*dataParams.put("curr_lat","28.6411469");
        dataParams.put("curr_long","77.359123");*/

        Log.d("Seraj", "this is url " +ServerURL);

        Log.d("Seraj", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                SuccessListener(),
                ErrorListener());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(LocationService.this).add(dataParamsJsonReq);
    }

    public static boolean isOtpcalled=false;

    private Response.Listener<JSONObject> SuccessListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("Seraj", "this is complete response " + response);
                    completeResponseData=""+response;
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {
                        SessionManager sessionManager=new SessionManager(LocationService.this);
                        if (innerResponse.has("bookingId")&&innerResponse.getString("bookingId").length()>1){
                            sessionManager.saveCurrentOrderBookingId(innerResponse.getString("bookingId"));
                        }else {
                            JSONObject object=innerResponse.getJSONObject("data");
                            if (object.getString("is_job_found").equalsIgnoreCase("yes")){
                                Bundle bundle =new Bundle();
                                bundle.putString("booking_order_number",object.getString("booking_order_number"));
                                bundle.putString("booking_order_id",object.getString("booking_order_id"));
                                bundle.putString("distance_km",object.getString("distance_km"));

                            }else if (object.getString("is_job_found").equalsIgnoreCase("no")){
                                sessionManager.clearOrderSession();
                            }
                        }
                    }

                } catch (Exception e) {
                    errorData="Error data "+e.getMessage();
                    Log.d("Seraj", "inside catch block  " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }

    private Response.ErrorListener ErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                errorData="Error data "+error.getMessage();
                Log.d("Seraj", "inside error block  " + error.getMessage());
            }
        };
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        IsStopped=true;
        Log.d("Seraj","service stopped");
    }


    private void getLocation() {

        final LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);
        myLooper = Looper.myLooper();



        if (ActivityCompat.checkSelfPermission(LocationService.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Seraj", "getLocation: stopping the location service. return");
            stopSelf();
            return;
        }
        Log.d("Seraj", "getLocation: getting location information. fetching");
        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, mLocationCallback, myLooper);

        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location!=null){
                    LastKnownLatitude = location.getLatitude();
                    LastKnownLongitude = location.getLongitude();
                    saveUserLocation(location.getLatitude(), location.getLongitude());
                    Log.d("Seraj","location fetch success locatin is "+location.toString());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Seraj","location fetch error "+e.getMessage());
            }
        });

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)

    }


    private void saveUserLocation(double latitude, double longitude){

        SessionManager sessionManager=new SessionManager(LocationService.this);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/sendOnlineEmployeesCurrLocation";

        if (!sessionManager.isLoggedIn()&&sessionManager.getEmployeeId()!=null){
            stopSelf();
            return;
        }

        sessionManager.setEmployeeLocation(""+latitude,""+longitude);
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());
        dataParams.put("emp_lat",""+latitude);
        dataParams.put("emp_long",""+longitude);

        /*dataParams.put("emp_lat","28.6411469");
        dataParams.put("emp_long","77.359123");*/
        dataParams.put("emp_currentAddress","");

        Log.d("employee_current_locat", "this is url " +ServerURL);

        Log.d("employee_current_locat", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                RegistrationSuccess(),
                RegistrationError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(LocationService.this).add(dataParamsJsonReq);
    }


    private Response.Listener<JSONObject> RegistrationSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("employee_current_locat", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {

                    }

                } catch (Exception e) {
                    Log.d("employee_current_locat", "inside catch block  " + e.getMessage());
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
                Log.d("employee_current_locat", "inside error block  " + error.getMessage());
            }
        };
    }
}