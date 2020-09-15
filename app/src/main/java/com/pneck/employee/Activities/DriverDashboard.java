package com.pneck.employee.Activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.pneck.employee.Const;
import com.pneck.employee.InternetConnection;
import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.PublicMethod;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;
import com.pneck.employee.services.LocationService;
import com.pneck.employee.utills.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.pneck.employee.Const.PERMISSIONS_REQUEST_TOKEN;
import static com.pneck.employee.services.LocationService.completeResponseData;
import static com.pneck.employee.services.LocationService.isOtpcalled;

public class DriverDashboard extends AppCompatActivity {

    private static final int REQUEST_CHECK_SETTINGS = 2542;
    private SwitchCompat startService;
    private LinearLayout account;
    private LinearLayout notification;
    private LinearLayout history;
    private LinearLayout rides;
    private ImageView onlineIcon;
    private TextView liveText;
    private TextView OrderResponse;
    private TextView AboutUs;
    private TextView pneckPartnerName;
    private TextView Feedback;
    private TextView retoggleCase;

    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private LinearLayout Logout;


    private void findViews() {
        startService = (SwitchCompat)findViewById( R.id.start_service );
        account = (LinearLayout)findViewById( R.id.account );
        notification = (LinearLayout)findViewById( R.id.notification );
        history = (LinearLayout)findViewById( R.id.history );
        rides = (LinearLayout)findViewById( R.id.rides );
        onlineIcon = (ImageView)findViewById( R.id.online_icon );
        liveText = (TextView)findViewById( R.id.live_text );
        pneckPartnerName = (TextView)findViewById( R.id.pneck_partner_name );
        OrderResponse=findViewById(R.id.resonse);
        AboutUs=findViewById(R.id.about_us);
        Feedback=findViewById(R.id.feed_back);
        retoggleCase=findViewById(R.id.offline_case_retoggle);
        //onlineProgressBar=findViewById(R.id.online_progress);

        Logout=findViewById(R.id.logout);
        progressBar=findViewById(R.id.progress_bar);

    }

    private Thread.UncaughtExceptionHandler handleAppCrash =
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    Log.e("error", ex.toString());
                    //send email here
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.setSystemBarColor(this, R.color.white);
        Tools.setSystemBarLight(this);
        setContentView(R.layout.activity_driver_dashboard);
        findViews();
        switchChangeListeners();
        clickListeners();

        liveText.setText("OFFLINE");
        liveText.setTextColor(0xFF757575);
        onlineIcon.setImageResource(R.drawable.ic_circle);
        retoggleCase.setVisibility(View.VISIBLE);

        sessionManager=new SessionManager(DriverDashboard.this);

        pneckPartnerName.setText(sessionManager.getUserFirstName()+" "+sessionManager.getUserLastName());

        OrderResponse.setVisibility(View.GONE);

    }


    @Override
    protected void onResume() {
        super.onResume();
        startService.setChecked(false);
        Log.e("kdfhjksf"," on resume calling");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("kdfhjksf","this is postDelayed on resume ");
                if (sessionManager.isServiceStarted()){
                    startService.setChecked(true);
                    if (checkPermission()){
                        Log.e("kdfhjksf","this is calling on resume ");
                        settingsrequest();
                    }else {
                        askForPermission();
                    }
                }else {
                    //startService.setChecked(false);
                }

            }
        },1000);
        updateText();
    }

    private void updateText() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
                                      @Override
                                      public void run() {
                                          Log.e("skdfsdfssfsd","this is response "+completeResponseData);
                                          runOnUiThread(new Runnable() {
                                              @Override
                                              public void run() {
                                                  if (completeResponseData!=null&&
                                                          completeResponseData.length()>0) {
                                                      checkResponse();
                                                  }else {
                                                      liveText.setText("OFFLINE");
                                                      retoggleCase.setVisibility(View.VISIBLE);
                                                      liveText.setTextColor(0xFF757575);
                                                      onlineIcon.setImageResource(R.drawable.ic_circle);
                                                  }
                                                  OrderResponse.setText(completeResponseData);
                                                  //errorResponse.setText(errorData);
                                              }
                                          });
                                      }
                                  },
                0, 3000);


        TextView tvUserType = findViewById(R.id.tv_dashboard_type);
        tvUserType.setVisibility(View.VISIBLE);
        tvUserType.setText(getString(R.string.Driver_Dashboard));
    }

    private void checkResponse() {
        try {
            Log.e("skdfsdfssfsd","this is response "+completeResponseData);
            //bookingId.setText(sessionManager.getCurrentBookingOrderId());


            JSONObject response=new JSONObject(completeResponseData);
            JSONObject innerResponse=response.getJSONObject("response");
            //onlineProgressBar.setVisibility(View.GONE);
            liveText.setText("ONLINE");
            retoggleCase.setVisibility(View.GONE);
            liveText.setTextColor(0xFF00C853);
            onlineIcon.setImageResource(R.drawable.ic_circle_green);
            if (innerResponse.getBoolean("success")) {
                if (innerResponse.has("data")){
                    liveText.setText("ONLINE");
                    retoggleCase.setVisibility(View.GONE);
                    liveText.setTextColor(0xFF00C853);
                    JSONObject object=innerResponse.getJSONObject("data");
                    if (object.getString("is_job_found").equalsIgnoreCase("yes")){
                        Bundle bundle =new Bundle();
                        bundle.putString("booking_order_number",object.getString("booking_order_number"));
                        bundle.putString("booking_order_id",object.getString("booking_order_id"));
                        bundle.putString("distance_km",object.getString("distance_km"));
                        bundle.putString("customer_lat",object.getString("user_lat"));
                        bundle.putString("customer_long",object.getString("user_long"));

                        //completeResponseData="";
                        if (!isOtpcalled&&sessionManager.getCurrentBookingOrderId().length()==0){
                            isOtpcalled=true;
                            LaunchActivityClass.LaunchJOB_AcceptScreen(DriverDashboard.this,bundle);
                        }
                    }else if (object.getString("is_job_found").equalsIgnoreCase("no")){
                        isOtpcalled=false;
                        sessionManager.clearOrderSession();
                    }
                }


            }

        }catch (JSONException e){
            //onlineProgressBar.setVisibility(View.VISIBLE);
            liveText.setText("OFFLINE");
            retoggleCase.setVisibility(View.VISIBLE);
            liveText.setTextColor(0xFF757575);
            onlineIcon.setImageResource(R.drawable.ic_circle);
            Log.e("kdfjkddss","this is error "+e.getMessage());
        }

    }

    private void clickListeners() {

        Feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchActivityClass.LaunchFeedBackScreen(DriverDashboard.this);
            }
        });

        AboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchActivityClass.LaunchAboutUsScreen(DriverDashboard.this);
            }
        });

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchActivityClass.LaunchHelpScreen(DriverDashboard.this);
            }
        });

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchActivityClass.LaunchEditProfileScreen(DriverDashboard.this);
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchActivityClass.LaunchEmployeeRidesScreen(DriverDashboard.this);
            }
        });
        rides.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sessionManager.getCurrentBookingOrderId()!=null&&
                        sessionManager.getCurrentBookingOrderId().length()>0){
                    LaunchActivityClass.LaunchTrackingScreen(DriverDashboard.this);
                }else {
                    Toast.makeText(DriverDashboard.this,getString(R.string.NO_CURRENT_RIDES),Toast.LENGTH_SHORT).show();
                }

                //LaunchActivityClass.LaunchEmployeeRidesScreen(MainActivity.this);
            }
        });
    }

    private void switchChangeListeners(){

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sessionManager.getCurrentBookingOrderId().length()==0){
                    logOutEmployee();
                }else {
                    Toast.makeText(DriverDashboard.this,"Please complete the current order before logout",Toast.LENGTH_SHORT).show();
                }

            }
        });

        startService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e("sjhfsjkfsfsf","location switch service is clicked "+isChecked);
                if (isChecked){

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (startService.isChecked()){
                                if (liveText.getText().toString().equalsIgnoreCase("OFFLINE")){
                                    //NEED TO RETOGGLE THE SERVICE BUTTON
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(DriverDashboard.this,"Please re-toggle your service, to make it online",Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        }
                    },1000*30);

                    if (checkPermission()){
                        settingsrequest();
                    }else {
                        askForPermission();
                    }
                    startService.setText("SERVICE STARTED");
                    employeeServiceToggle(isChecked);
                }else {
                    employeeServiceToggle(isChecked);
                    startService.setText("START SERVICE");
                    sessionManager.setServiceStarted(false);
                    stopLocationService();
                    liveText.setText("OFFLINE");
                    liveText.setTextColor(0xFF757575);
                    onlineIcon.setImageResource(R.drawable.ic_circle);
                    completeResponseData="";
                }
            }
        });
    }

    private void employeeServiceToggle(boolean istrue) {

        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/serviceToggle";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());
        if (istrue){
            dataParams.put("toggle_status",""+1);
        }else {
            dataParams.put("toggle_status",""+0);
        }


        Log.e("serviceToggle", "this is url " +ServerURL);

        Log.e("serviceToggle", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                serviceToggle(),
                LogoutError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(DriverDashboard.this).add(dataParamsJsonReq);
    }


    private Response.Listener<JSONObject> serviceToggle() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("serviceToggle", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {

                        liveText.setText("ONLINE");
                        completeResponseData=response+"";
                        retoggleCase.setVisibility(View.GONE);

                    }

                } catch (Exception e) {
                    Log.v("serviceToggle", "inside catch block  " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }


    private void logOutEmployee() {

        progressBar.setVisibility(View.VISIBLE);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/EmployeeLogout";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());

        Log.e("user_logout", "this is url " +ServerURL);

        Log.e("user_logout", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                LogoutSuccess(),
                LogoutError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(DriverDashboard.this).add(dataParamsJsonReq);
    }


    private Response.Listener<JSONObject> LogoutSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("user_logout", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {

                        if (sessionManager.logout()){
                            completeResponseData="";
                            LaunchActivityClass.LaunchSignInScreen(DriverDashboard.this);
                        }

                    }

                } catch (Exception e) {
                    Log.v("user_logout", "inside catch block  " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }

    private Response.ErrorListener LogoutError() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progressBar.setVisibility(View.GONE);
                //Toast.makeText(getApplicationContext(), R.string.SOMETHING_WENT_WRONG+error.getMessage(), Toast.LENGTH_LONG).show();
                Log.v("user_logout", "inside error block  " + error.getMessage());
            }
        };
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat
                    .checkSelfPermission(DriverDashboard.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)+
                    ContextCompat
                            .checkSelfPermission(DriverDashboard.this,
                                    Manifest.permission.FOREGROUND_SERVICE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                return false;
            }else {
                return true;
            }
        }else {
            return true;
        }
    }

    public void askForPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat
                    .checkSelfPermission(DriverDashboard.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)+
                    ContextCompat
                            .checkSelfPermission(DriverDashboard.this,
                                    Manifest.permission.FOREGROUND_SERVICE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale
                        (DriverDashboard.this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                        ActivityCompat.shouldShowRequestPermissionRationale
                                (DriverDashboard.this, Manifest.permission.FOREGROUND_SERVICE)) {

                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.main_layout), getString(R.string.PERMISSION_SNACK_BAR_MESSAGE), Snackbar.LENGTH_LONG);
                    TextView snack_tv = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(DriverDashboard.this, R.color.primary_800));
                    snack_tv.setGravity(Gravity.CENTER_HORIZONTAL);
                    snack_tv.setTextColor(ContextCompat.getColor(DriverDashboard.this, R.color.white));
                    snackbar.setActionTextColor(getResources().getColor(R.color.white));
                    snackbar.setAction(R.string.ENABLE, new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View view) {
                            requestPermissions(
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.FOREGROUND_SERVICE},
                                    PERMISSIONS_REQUEST_TOKEN);
                        }
                    });
                    snackbar.show();

                }else {
                    requestPermissions(
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.FOREGROUND_SERVICE},
                            PERMISSIONS_REQUEST_TOKEN);
                }

            }
        }else {
            if (!GPSstatusCheck()) {
                settingsrequest();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_TOKEN:
                if (grantResults.length> 0) {


                    boolean locationPemission = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (locationPemission){
                        if (InternetConnection.checkConnection(DriverDashboard.this)){

                            settingsrequest();
                            if (GPSstatusCheck()){
                                startLocationService();
                            }else {

                            }
                        }else {
                            PublicMethod.showSnackBar(DriverDashboard.this,getString(R.string.CHECK_YOUR_INTERNET_CONNECTION));
                            // NetConnectionSnackBar();
                        }
                    }else {
                        Toast.makeText(DriverDashboard.this,"Permission Denied can't post Global post.",Toast.LENGTH_SHORT).show();
                    }

                }
                break;
        }
    }


    public boolean GPSstatusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }else {
            return true;
        }
    }




    private String TAG="deliverymainfksfdsf";

    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;



    public void settingsrequest() {

        mSettingsClient = LocationServices.getSettingsClient(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        Log.e("sdfjdskfs","mSettingsClient");
                        //noinspection MissingPermission
                        startLocationService();

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(DriverDashboard.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(DriverDashboard.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode==REQUEST_CHECK_SETTINGS){
            if (resultCode==RESULT_OK) {
                Log.e("sdfjdskfs","RESULT_OK");
                startLocationService();
            }else if (resultCode==RESULT_CANCELED){
                Log.e(TAG,"location enabl cancled"+data.getExtras());
            }

        }
    }

    private void startLocationService(){

        Log.e("sdfjdskfs","calling location service");
        if(!isLocationServiceRunning()){
            //onlineProgressBar.setVisibility(View.VISIBLE);
            Intent serviceIntent = new Intent(this, LocationService.class);
            serviceIntent.putExtra("is_stop",false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

                DriverDashboard.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
            sessionManager.setServiceStarted(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }





    private void stopLocationService(){
        Intent serviceIntent = new Intent(this, LocationService.class);
        serviceIntent.putExtra("is_stop",true);
        stopService(serviceIntent);
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.retailsly.retailslydelivery.Services.LocationService".equals(service.service.getClassName())) {
                Log.d("kjfsfsfdf", "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d("kjfsfsfdf", "isLocationServiceRunning: location service is not running.");
        return false;
    }

}
