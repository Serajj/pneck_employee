package com.pneck.employee.Activities.OrderProcess;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
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
import com.pneck.employee.Fragments.TrackingEmployeeFragment;
import com.pneck.employee.InternetConnection;
import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.PublicMethod;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.pneck.employee.Const.PERMISSIONS_REQUEST_TOKEN;

public class JobRealTimeTrackingScreen extends AppCompatActivity {

    private static final int REQUEST_CHECK_SETTINGS = 2542;
    String TAG="thisskdfn";
    SessionManager sessionManager;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_real_time_tracking_screen);

        sessionManager= new SessionManager(JobRealTimeTrackingScreen.this);
        if (checkPermission()){
            settingsrequest();
        }else {
            askForPermission();
        }
        
    }
    private void LoadMapFragment(){
        getLocationResponse();
    }


    private void getLocationResponse() {

        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/empCurrBookingTracking";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());
        dataParams.put("ses_booking_id",sessionManager.getCurrentBookingOrderId());
        dataParams.put("curr_lat",sessionManager.getEmployeeCurrentLatitude());
        dataParams.put("curr_long",sessionManager.getEmployeeCurrentLongitude());
        dataParams.put("curr_address","Empty");

        Log.e("user_employee_loc", "this is url " +ServerURL);

        Log.e("user_employee_loc", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                getLocationUpdate(),
                ErrorListeners());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(JobRealTimeTrackingScreen.this).add(dataParamsJsonReq);
    }

    @Override
    public void onBackPressed() {
        LaunchActivityClass.LaunchMainActivity(JobRealTimeTrackingScreen.this);
    }

    private Response.Listener<JSONObject> getLocationUpdate() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("user_employee_loc", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {


                        JSONObject data=innerResponse.getJSONObject("data");

                        if (data.getString("curr_booking_status").equalsIgnoreCase("order_info_provided")){
                            gotoTrackFragment(innerResponse);
                        }else if(data.getString("curr_booking_status").equalsIgnoreCase("accepted")) {
                            //launch otp screen
                            Bundle bundle=new Bundle();
                            bundle.putString("booking_order_number",data.getString("booking_order_number"));
                            bundle.putString("ses_booking_id",data.getString("ses_booking_id"));
                            bundle.putString("customer_mobile",data.getString("customer_mobile"));
                            bundle.putString("customer_name",data.getString("customer_name"));

                            LaunchActivityClass.LaunchJOB_OTPScreen(JobRealTimeTrackingScreen.this,bundle);
                            JobRealTimeTrackingScreen.this.finish();

                        }else if (data.getString("curr_booking_status").equalsIgnoreCase("accepted_otp_confirmed")){
                            //launch order info screen
                            LaunchActivityClass.LaunchJOBDetailsScreen(JobRealTimeTrackingScreen.this);
                            JobRealTimeTrackingScreen.this.finish();
                        }else if (data.getString("curr_booking_status").equalsIgnoreCase("delivery_otp_confirmed")){
                            //launch payment request screen
                            LaunchActivityClass.LaunchOrderSubmitRequestScreen(JobRealTimeTrackingScreen.this);
                            JobRealTimeTrackingScreen.this.finish();

                        }else if (data.getString("curr_booking_status").equalsIgnoreCase("order_request_payment")){
                            //launch launch order complete screen
                            Bundle bundle=new Bundle();
                            bundle.putString("billing_amount","");
                            LaunchActivityClass.LaunchPaymentPendingScreen(JobRealTimeTrackingScreen.this,bundle);
                        }else if (data.getString("curr_booking_status").equalsIgnoreCase("order_completed")){
                            //launch launch order complete screen
                            LaunchActivityClass.LaunchOrderCompleteHappyScreen(JobRealTimeTrackingScreen.this);
                        }
                    }else {
                        sessionManager.clearOrderSession();
                        Toast.makeText(JobRealTimeTrackingScreen.this,"Order canceled",Toast.LENGTH_SHORT).show();
                        LaunchActivityClass.LaunchMainActivity(JobRealTimeTrackingScreen.this);
                    }

                } catch (Exception e) {
                    Log.v("user_employee_loc", "inside catch block  " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }

    private Response.ErrorListener ErrorListeners() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.v("user_employee_loc", "inside error block  " + error.getMessage());
            }
        };
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat
                    .checkSelfPermission(JobRealTimeTrackingScreen.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
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
                    .checkSelfPermission(JobRealTimeTrackingScreen.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale
                        (JobRealTimeTrackingScreen.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.main_layout), getString(R.string.PERMISSION_SNACK_BAR_MESSAGE), Snackbar.LENGTH_LONG);
                    TextView snack_tv = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(JobRealTimeTrackingScreen.this, R.color.primary_800));
                    snack_tv.setGravity(Gravity.CENTER_HORIZONTAL);
                    snack_tv.setTextColor(ContextCompat.getColor(JobRealTimeTrackingScreen.this, R.color.white));
                    snackbar.setActionTextColor(getResources().getColor(R.color.white));
                    snackbar.setAction(R.string.ENABLE, new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View view) {
                            requestPermissions(
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    PERMISSIONS_REQUEST_TOKEN);
                        }
                    });
                    snackbar.show();

                }else {
                    requestPermissions(
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_TOKEN:
                if (grantResults.length> 0) {


                    boolean locationPemission = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (locationPemission){
                        if (InternetConnection.checkConnection(JobRealTimeTrackingScreen.this)){

                            settingsrequest();
                            /*if (GPSstatusCheck()){
                                LoadMapFragment();
                            }else {

                            }*/
                        }else {
                            PublicMethod.showSnackBar(JobRealTimeTrackingScreen.this,getString(R.string.CHECK_YOUR_INTERNET_CONNECTION));
                            // NetConnectionSnackBar();
                        }
                    }else {
                        Toast.makeText(JobRealTimeTrackingScreen.this,"Permission Denied can't post Global post.",Toast.LENGTH_SHORT).show();
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


                        //noinspection MissingPermission
                        LoadMapFragment();

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
                                    rae.startResolutionForResult(JobRealTimeTrackingScreen.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(JobRealTimeTrackingScreen.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode==REQUEST_CHECK_SETTINGS){
            if (resultCode==RESULT_OK) {
                LoadMapFragment();
            }else if (resultCode==RESULT_CANCELED){
                Log.e(TAG,"location enabl cancled"+data.getExtras());
            }

        }
    }


    public void gotoTrackFragment(JSONObject innerResponse) {
        Fragment fragment = new TrackingEmployeeFragment(innerResponse);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }


}
