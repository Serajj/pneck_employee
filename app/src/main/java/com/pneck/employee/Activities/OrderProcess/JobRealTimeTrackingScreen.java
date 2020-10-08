package com.pneck.employee.Activities.OrderProcess;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.GeoPoint;
import com.pneck.employee.Const;
import com.pneck.employee.Fragments.TrackingEmployeeFragment;
import com.pneck.employee.InternetConnection;
import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.PublicMethod;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;
import com.pneck.employee.directionhelpers.FetchURL;
import com.pneck.employee.directionhelpers.TaskLoadedCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



import static com.pneck.employee.Const.PERMISSIONS_REQUEST_TOKEN;

public class JobRealTimeTrackingScreen extends AppCompatActivity implements OnMapReadyCallback , TaskLoadedCallback {

    private static final int REQUEST_CHECK_SETTINGS = 2542;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION =1001 ;
    private static final float DEFAULT_ZOOM =13 ;
    private static boolean STATUSOK = false;

    Double userLongitude,userLattitude,destinationLattitude,destinationLongitude;

    String TAG="Serajloc";
    SessionManager sessionManager;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;

    GoogleMap gMap;
    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 2000;
    LocationCallback locationCallback;
    boolean updateLocationOnDb=true;

    FusedLocationProviderClient mFusedLocationProviderClient;
    ScheduledExecutorService executor,customerAcceptEcecutor;
    Runnable customerAcceptRunneble;
    Runnable periodicTask;
    private boolean locationPermissionGranted;

    TextView username,usermobile;
    private LatLng sourceLatLng;
    private LatLng destLatLng;

    private MarkerOptions place1, place2;
    Button pickedUp;
    private Polyline currentPolyline;
    Handler handler;
    String lvalue;
    Dialog dialog;
    ProgressBar progressBar;
    AlertDialog.Builder alertDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_job_real_time_tracking_screen);
        getLocationPermission();

        usermobile=findViewById(R.id.call_user_btn);
        username=findViewById(R.id.user_name);
        pickedUp=findViewById(R.id.picked_btn);
        sessionManager= new SessionManager(JobRealTimeTrackingScreen.this);
        alertDialog=new AlertDialog.Builder(JobRealTimeTrackingScreen.this);

        Log.d("Seraj",sessionManager.getEmployeeId());
        Log.d("Seraj",sessionManager.getEmployeeToken());
        Log.d("Seraj",sessionManager.getCurrentBookingOrderId());
        Log.d("Seraj",sessionManager.getEmployeeCurrentLatitude());
        Log.d("Seraj",sessionManager.getEmployeeCurrentLongitude());
        Log.d("Seraj",sessionManager.getCurrentOrderInfo());
        lvalue=sessionManager.getEmployeeCurrentLatitude();



        // Construct a PlaceDetectionClient.


        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        executor = Executors.newSingleThreadScheduledExecutor();

        Runnable periodicTask = new Runnable() {
            public void run() {
                // Invoke method(s) to do the work
                Log.d("Serajsok","running...");
                if (STATUSOK){
                    // drawRoutes();
                }

            }
        };


        // executor.scheduleAtFixedRate(periodicTask, 0, 3, TimeUnit.SECONDS);

        handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                if (STATUSOK){
                    drawRoutes();
                }
                if (!lvalue.equals(sessionManager.getEmployeeCurrentLatitude())){
                    gMap.clear();
                    lvalue=sessionManager.getEmployeeCurrentLatitude();
                }
                Log.d("Serajsok","running..h.");
                handler.postDelayed(this, 3000);
            }
        };

        handler.postDelayed(r, 3000);

        getUserData();



        if (sessionManager.getOtpVerified()){
            pickedUp.setText("Drop");
            pickedUp.setBackgroundColor(Color.GREEN);
            userLattitude=destinationLattitude;
            userLongitude=destinationLattitude;
        }


        pickedUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!sessionManager.getOtpVerified()) {
                    openOtpDialog();
                }else{
                    dropDialog();
                }
            }
        });



    }

    private void dropDialog() {
        alertDialog.setTitle("Confirm Drop");
        alertDialog.setMessage("Are you sure ?");
        alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dropNow();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        alertDialog.create().show();
    }

    private void dropNow() {
       sendDeliveryOTP();
    }

    private void openOtpDialog() {
        dialog=new Dialog(this);
        dialog.setContentView(R.layout.otpverify);

        final EditText otp=dialog.findViewById(R.id.otp_edit_text);
        Button submitBtn=dialog.findViewById(R.id.verify_btn);
        progressBar=dialog.findViewById(R.id.progress_bar);



        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Serajotp",""+sessionManager.getOtpVerified());

                if (!TextUtils.isEmpty(otp.getText())){
                    verifyOtp(otp.getText().toString());
                }else{
                    Toast.makeText(JobRealTimeTrackingScreen.this, "OTP Required", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void verifyOtp(String otp) {

        progressBar.setVisibility(View.VISIBLE);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/empBookingOtpMatch";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ses_booking_id",sessionManager.getCurrentBookingOrderId());
        dataParams.put("otp",otp);

        Log.d("serajotp", "this is url " +ServerURL);

        Log.d("serajotp", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                RegistrationSuccess(),
                RegistrationError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(JobRealTimeTrackingScreen.this).add(dataParamsJsonReq);

    }

    private Response.Listener<JSONObject> RegistrationSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("user_otp_verification", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {
                        String msg=innerResponse.getString("message");
                        if (innerResponse.has("otpnotmatch")){
                            Toast.makeText(JobRealTimeTrackingScreen.this, "OTP Incorrect", Toast.LENGTH_SHORT).show();

                        }else {
                            Toast.makeText(JobRealTimeTrackingScreen.this, "Verified", Toast.LENGTH_SHORT).show();
                            if (dialog.isShowing()){
                                dialog.dismiss();
                            }
                            pickedUp.setText("Drop");
                            pickedUp.setBackgroundColor(Color.GREEN);
                            sessionManager.setOtpVerified(true);
                            userLattitude=destinationLattitude;
                            userLongitude=destinationLongitude;
                            gMap.clear();
                        }
                        progressBar.setVisibility(View.GONE);

                        //
                    }else {
                        Toast.makeText(JobRealTimeTrackingScreen.this, "Try again..", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    Log.v("user_registration", "inside catch block  " + e.getMessage());
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
                progressBar.setVisibility(View.GONE);
                Log.v("user_otp_verification", "inside error block  " + error.getMessage());
            }
        };
    }






    private void updateDriverOnMap() {
    }

    private void drawRoutes() {

        place1 = new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .position(new LatLng(Double.valueOf(sessionManager.getEmployeeCurrentLatitude()), Double.valueOf(sessionManager.getEmployeeCurrentLongitude())))
                .rotation(0.0f)
                .snippet("" +getResources().getDrawable(R.drawable.car_icon))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon));
        place2 = new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .position(new LatLng(userLattitude, userLongitude))
                .rotation(0.0f)
                .snippet("" +getResources().getDrawable(R.drawable.userloc))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.userloc));
        gMap.addMarker(place1);
        gMap.addMarker(place2);

        LatLng currentLatLng = new LatLng(place1.getPosition().latitude,place1.getPosition().longitude);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng,
                DEFAULT_ZOOM);
        gMap.animateCamera(update);

        new FetchURL(JobRealTimeTrackingScreen.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");

    }



    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + "AIzaSyBTbcRRCLbhqeMVVGD5fnUevHfxj2MdgdI";
        Log.d("Serajurl",url);
        return url;
    }




    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap=googleMap;
        gMap.setMyLocationEnabled(true);


        gMap.setLocationSource(new LocationSource() {
            @Override
            public void activate(OnLocationChangedListener onLocationChangedListener) {
                gMap.clear();
            }

            @Override
            public void deactivate() {

            }
        });


    }




    private void getUserData() {


        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/getUserdata";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("booking_id",sessionManager.getCurrentBookingOrderId());

        Log.d("Serajuserdata", String.valueOf(sessionManager));

        Log.d("Serajuserdata",sessionManager.getEmployeeId());
        Log.d("Serajuserdata",sessionManager.getEmployeeToken());
        Log.d("Serajuserdata",sessionManager.getCurrentBookingOrderId());
        Log.d("Serajuserdata",sessionManager.getEmployeeCurrentLatitude());
        Log.d("Seraj",sessionManager.getEmployeeCurrentLongitude());
        Log.d("Seraj",sessionManager.getCurrentOrderInfo());


        Log.d("Serajuserdata", "this is url " +ServerURL);

        Log.d("Serajuserdata", "this is we sending " + dataParams.toString());

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

                    Log.d("Serajuserdata", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {
                        JSONObject object=innerResponse.getJSONObject("data");
                        String usename= object.getString("username");
                        String mobile=object.getString("mobile");
                        String userLat=object.getString("user_lat");
                        String userLong=object.getString("user_long");
                        String destinationLat=object.getString("destination_latti");
                        String destinationLong=object.getString("destination_longi");

                        username.setText(usename);
                        usermobile.setText("Call "+mobile);

                        userLattitude=Double.valueOf(userLat);
                        userLongitude=Double.valueOf(userLong);
                        destinationLattitude=Double.valueOf(destinationLat);
                        destinationLongitude=Double.valueOf(destinationLong);

                        STATUSOK=true;
                        drawRoutes();

                        Log.d("Serajuserdata",usename+" "+mobile+" "+userLat+" "+userLong+" "+destinationLat+" "+destinationLong);

                    }else {
                        // sessionManager.clearOrderSession();
                        Toast.makeText(JobRealTimeTrackingScreen.this,"Order canceled",Toast.LENGTH_SHORT).show();
                        // LaunchActivityClass.LaunchMainActivity(JobRealTimeTrackingScreen.this);
                    }

                } catch (Exception e) {
                    Log.d("Serajuserdata", "inside catch block  " + e.getMessage());
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
                Log.v("Serajloc", "inside error block  " + error.getMessage());
            }
        };
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
    }



    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)


            currentPolyline.remove();
        currentPolyline = gMap.addPolyline((PolylineOptions) values[0]);
    }



    private void sendDeliveryOTP() {

       // progressBar.setVisibility(View.VISIBLE);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/empBookingDeliveryOtpResend";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());
        dataParams.put("ses_booking_id",sessionManager.getCurrentBookingOrderId());

        Log.d("serajdotp", "this is url " +ServerURL);

        Log.d("serajdotp", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                OTPSuccess(),
                OTPError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(JobRealTimeTrackingScreen.this).add(dataParamsJsonReq);
    }


    private Response.Listener<JSONObject> OTPSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("user_registration", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {
                        //progressBar.setVisibility(View.GONE);
                        LaunchActivityClass.LaunchOrderBookingOTPActivity(JobRealTimeTrackingScreen.this);
                    }else {
                       sessionManager.clearOrderSession();
                        Toast.makeText(JobRealTimeTrackingScreen.this,"Order canceled",Toast.LENGTH_SHORT).show();
                        LaunchActivityClass.LaunchMainActivity(JobRealTimeTrackingScreen.this);
                    }

                } catch (Exception e) {
                    Log.v("user_registration", "inside catch block  " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }

    private Response.ErrorListener OTPError() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progressBar.setVisibility(View.GONE);
                Log.v("user_registration", "inside error block  " + error.getMessage());
            }
        };
    }
}
