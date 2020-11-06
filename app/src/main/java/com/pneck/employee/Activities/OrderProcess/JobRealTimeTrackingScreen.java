package com.pneck.employee.Activities.OrderProcess;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.WindowManager;

import com.google.android.gms.location.LocationListener;
import com.pneck.employee.Const;
import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.PublicMethod;
import com.pneck.employee.R;



import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;
import com.pneck.employee.models.ClusterMarker;
import com.pneck.employee.models.PolylineData;
import com.pneck.employee.models.User;
import com.pneck.employee.models.UserLocation;
import com.pneck.employee.utills.MyClusterManagerRenderer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JobRealTimeTrackingScreen extends AppCompatActivity implements OnMapReadyCallback,View.OnClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnPolylineClickListener,
        LocationListener
{
    private static final String TAG ="SerajTrack" ;
    GoogleMap mGoogleMap;
    SessionManager sessionManager;
    private LatLngBounds mMapBoundary;
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private int mMapLayoutState = 0;
    private GeoApiContext mGeoApiContext;
    private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();
    private Marker mSelectedMarker = null;
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();
    private UserLocation mUserPosition;
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();
    String otp,vehicleNumber,avatar,mobile,name,vehicleImage;
    Double empLattitude,empLongitude;
    TextView userOtp,mVehicleNumber,mDriverName,mcarName;
    ImageView driverAvatar,carAvatar;
    Button callBtn;

    Dialog dialog;
    ProgressBar progressBar;
    AlertDialog.Builder alertDialog;

    Double userLongitude,userLattitude,destinationLattitude,destinationLongitude;

    private int DEFAULT_ZOOM = 1;
    private ScheduledExecutorService executor;
    private Runnable periodicTask;
    TextView username,usermobile;
    String empcl;

    Button pickedUp;
    private boolean essentialLoaded=true;
    private boolean startLoad=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_job_real_time_tracking_screen);



        usermobile=findViewById(R.id.call_user_btn);
        username=findViewById(R.id.user_name);
        pickedUp=findViewById(R.id.picked_btn);

        alertDialog=new AlertDialog.Builder(JobRealTimeTrackingScreen.this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        sessionManager = new SessionManager(JobRealTimeTrackingScreen.this);
        empcl=sessionManager.getEmployeeCurrentLatitude();



        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey("AIzaSyBTbcRRCLbhqeMVVGD5fnUevHfxj2MdgdI")
                    .build();
        }

        executor = Executors.newSingleThreadScheduledExecutor();

        periodicTask = new Runnable() {
            public void run() {
                // Invoke method(s) to do the work
                if (startLoad) {
                   if (!empcl.equals(sessionManager.getEmployeeCurrentLatitude())){
                       getUserData();
                       empcl=sessionManager.getEmployeeCurrentLatitude();
                   }
                }

            }
        };

        executor.scheduleAtFixedRate(periodicTask, 0, 1, TimeUnit.SECONDS);






        if (sessionManager.getOtpVerified()){
            pickedUp.setText("Drop");
            pickedUp.setBackgroundColor(Color.GREEN);
            //userLatLong=new LatLng(Double.valueOf(sessionManager.getDestinationLatitude()),);
            userLattitude=Double.valueOf(sessionManager.getDestinationLatitude());
            userLongitude=Double.valueOf(sessionManager.getDestinationLongitude());

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


        getUserData();

        ///////////////////////////

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
                            startLoad=true;
                            mGoogleMap.clear();
                            getUserData();
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
                        final String mobile=object.getString("mobile");
                        String userLat=object.getString("user_lat");
                        String userLong=object.getString("user_long");
                        String destinationLat=object.getString("destination_latti");
                        String destinationLong=object.getString("destination_longi");


                        Log.d("serajlocv",userLat+" "+userLong+" "+destinationLat+" "+destinationLong);

                        if (essentialLoaded){
                            if (usename.length()>8){
                                username.setTextSize(14);
                            }
                            username.setText(usename);
                            usermobile.setText("Call "+mobile);
                            sessionManager.setDeinationLat(destinationLat,destinationLong);

                            usermobile.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String phone = "+91"+mobile;
                                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                                    startActivity(intent);
                                }
                            });

                            userLattitude=Double.valueOf(userLat);
                            userLongitude=Double.valueOf(userLong);
                            destinationLattitude=Double.valueOf(destinationLat);
                            destinationLongitude=Double.valueOf(destinationLong);
                            essentialLoaded=false;

                        }

                        addGeoPoints();
                        setUserPosition();
                        addMapMarkers();


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


    private void setUserPosition() {
        for (UserLocation userLocation : mUserLocations) {
            if (!userLocation.getUser().getUser_id().equals("me_employee_4")) {
                mUserPosition = userLocation;
            }
        }
    }










    private void addGeoPoints() {
        mUserLocations.clear();

            try{

                User user=new User("employee","user",name,"");

                GeoPoint geoPoint;
                Log.e("serajgeopint","this is latitude "+empLattitude);
                    geoPoint=new GeoPoint(Double.parseDouble(sessionManager.getEmployeeCurrentLatitude()),
                        Double.parseDouble(sessionManager.getEmployeeCurrentLongitude()));





                mUserLocations.add(new UserLocation(user,geoPoint,""+System.currentTimeMillis()));



                user=new User("user","customer",
                        sessionManager.getCurrentBookingOrderId(),"");


                if (sessionManager.getOtpVerified()){
                    geoPoint=new GeoPoint(Double.parseDouble(sessionManager.getDestinationLatitude()),
                            Double.parseDouble(sessionManager.getDestinationLongitude()));
                }else{
                    geoPoint=new GeoPoint(userLattitude,userLongitude);
                }


                mUserLocations.add(new UserLocation(user,geoPoint,""+System.currentTimeMillis()));


                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(JobRealTimeTrackingScreen.this, Locale.getDefault());
                addresses = geocoder.getFromLocation(geoPoint.getLatitude(), geoPoint.getLongitude(), 1);

                if (addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    // customerAddress=address;
                }
            }catch (Exception e){
                Log.e("kjdhfkdssfs","this is error "+e.getMessage());
            }



    }
    @Override
    public void onClick(View view) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        int index = 0;
        for(PolylineData polylineData: mPolyLinesData){
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(JobRealTimeTrackingScreen.this, R.color.blue1));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );

                String Title;
                Title=sessionManager.getUserFirstName();
                Marker marker;

                if (sessionManager.getOtpVerified()){
                    marker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(
                                    Double.parseDouble(sessionManager.getDestinationLatitude()),
                                    Double.parseDouble(sessionManager.getDestinationLongitude())))
                            .title(Title)
                            .icon(PublicMethod.convertToBitmapFromVector(this,
                                    R.drawable.ic_placeholder))
                            .snippet("Duration: " + polylineData.getLeg().duration));
                }else{
                    marker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(
                                    userLattitude,
                                    userLongitude))
                            .title(Title)
                            .icon(PublicMethod.convertToBitmapFromVector(this,
                                    R.drawable.ic_placeholder))
                            .snippet("Duration: " + polylineData.getLeg().duration));
                }

                mTripMarkers.add(marker);

                marker.showInfoWindow();
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(JobRealTimeTrackingScreen.this, R.color.darkGrey));
                polylineData.getPolyline().setZIndex(0);
            }
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap=googleMap;


        mGoogleMap.setMyLocationEnabled(true);
        //addMapMarkers();
        getUserData();

    }




    private void resetMap(){
        if(mGoogleMap != null) {
            mGoogleMap.clear();

            if(mClusterManager != null){
                mClusterManager.clearItems();
            }

            if (mClusterMarkers.size() > 0) {
                mClusterMarkers.clear();
                mClusterMarkers = new ArrayList<>();
            }

            if(mPolyLinesData.size() > 0){
                mPolyLinesData.clear();
                mPolyLinesData = new ArrayList<>();
            }
        }
    }


    private void addMapMarkers(){

        try {
            if(mGoogleMap != null){

                resetMap();

                if(mClusterManager == null){
                    mClusterManager = new ClusterManager<ClusterMarker>(JobRealTimeTrackingScreen.this, mGoogleMap);
                }
                if(mClusterManagerRenderer == null){
                    mClusterManagerRenderer = new MyClusterManagerRenderer(
                            this,
                            mGoogleMap,
                            mClusterManager);


                    mClusterManager.setRenderer(mClusterManagerRenderer);
                    Log.d("Serajmarker", "All set");

                }
                //mGoogleMap.setOnInfoWindowClickListener(RealTimeActivity.this);

                for(UserLocation userLocation: mUserLocations){

                    Log.d("Serajmarker", "All set for");
                    try{
                        String snippet = "";
                        int avatar = R.drawable.car_icon;
                        if(userLocation.getUser().getUser_id().equals("user")){
                            snippet = "This current Pneck Boy location";
                            avatar = R.drawable.car_icon;
                        }
                        else{
                            avatar = R.drawable.ic_house;
                            snippet = "Customer Address : My Location" ;
                        }

                        // set the default avatar
                        try{
                            avatar = Integer.parseInt(userLocation.getUser().getAvatar());
                        }catch (NumberFormatException e){
                            Log.d(TAG, "addMapMarkers: no avatar for " + userLocation.getUser().getUsername() + ", setting default.");
                        }
                        ClusterMarker newClusterMarker = new ClusterMarker(
                                new LatLng(userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude()),
                                userLocation.getUser().getUsername(),
                                snippet,
                                avatar,
                                userLocation.getUser()
                        );
                        mClusterManager.addItem(newClusterMarker);
                        mClusterMarkers.add(newClusterMarker);

                        Log.e("Serajmarker", "All done" );


                    }catch (NullPointerException e){
                        Log.e("Serajmarker", "addMapMarkers: NullPointerException: " + e.getMessage() );
                    }

                }
                mClusterManager.cluster();



                if (mClusterMarkers.size()<1){
                    Log.e("Serajmarker", "Returned" );
                    return;
                }
                ClusterMarker cmarker=mClusterMarkers.get(1);
                mClusterManager.removeItem(cmarker);
                cmarker=mClusterMarkers.get(0);
                Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(cmarker.getPosition())
                        .title(cmarker.getTitle())
                        .snippet(cmarker.getSnippet()));

                resetSelectedMarker();
                mSelectedMarker = marker;
                calculateDirections(marker);

                //setCameraView();
            }
        }catch (Exception e){
            Log.e("kjdhfsdfss","this is exception error "+e.getMessage());
        }

    }

    /**
     * Determines the view boundary then sets the camera
     * Sets the view
     */
    private void setCameraView() {

        // Set a boundary to start
        double bottomBoundary = mUserPosition.getGeo_point().getLatitude() - .0058;
        double leftBoundary = mUserPosition.getGeo_point().getLongitude() - .0058;
        double topBoundary = mUserPosition.getGeo_point().getLatitude() + .0058;
        double rightBoundary = mUserPosition.getGeo_point().getLongitude() +.0058;

        mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, DEFAULT_ZOOM));
            }
        });

        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition arg0) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, DEFAULT_ZOOM));
            }
        });
        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 30));
    }

    private void calculateDirections(Marker marker){
        Log.d(TAG, "calculateDirections: calculating directions.");
        Log.d("Serajdirection", "Direction called");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mUserPosition.getGeo_point().getLatitude(),
                        mUserPosition.getGeo_point().getLongitude()
                )
        );
        Log.d("Serajdirection", "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
//                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
//                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
//                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
//                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                Log.d(TAG, "onResult: successfully retrieved directions.");
                Log.d("Serajdirection", "Direction result"+result.toString());
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("Serajdirection", "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });
    }


    private void resetSelectedMarker(){
        if(mSelectedMarker != null){
            mSelectedMarker.setVisible(true);
            mSelectedMarker = null;
            removeTripMarkers();
        }
    }

    private void removeTripMarkers(){
        for(Marker marker: mTripMarkers){
            marker.remove();
        }
    }

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                if(mPolyLinesData.size() > 0){
                    for(PolylineData polylineData: mPolyLinesData){
                        polylineData.getPolyline().remove();
                    }
                    mPolyLinesData.clear();
                    mPolyLinesData = new ArrayList<>();
                }

                double duration = 999999999;
                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(JobRealTimeTrackingScreen.this, R.color.darkGrey));
                    polyline.setClickable(true);
                    mPolyLinesData.add(new PolylineData(polyline, route.legs[0]));

                    // highlight the fastest route and adjust camera
                    double tempDuration = route.legs[0].duration.inSeconds;
                    if(tempDuration < duration){
                        duration = tempDuration;
                        onPolylineClick(polyline);
                        zoomRoute(polyline.getPoints());
                    }

                    // mSelectedMarker.setVisible(false);
                }
                //progressBar.setVisibility(View.GONE);
            }
        });

        startLoad=true;
    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (mGoogleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 50;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mGoogleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    @Override
    public void onLocationChanged(Location location) {
        userLattitude=location.getLatitude();
        userLongitude=location.getLongitude();
        addMapMarkers();
    }
}




