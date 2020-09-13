package com.pneck.employee.Fragments;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import com.pneck.employee.Const;
import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.PublicMethod;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;
import com.pneck.employee.models.ClusterMarker;
import com.pneck.employee.models.PolylineData;
import com.pneck.employee.models.User;
import com.pneck.employee.models.UserLocation;
import com.pneck.employee.utills.MyClusterManagerRenderer;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.pneck.employee.Const.MAPVIEW_BUNDLE_KEY;

public class TrackingEmployeeFragment extends Fragment implements
        OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnPolylineClickListener
{

    private static final String TAG = "TrackingEmployee";
    private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
    private static final int MAP_LAYOUT_STATE_EXPANDED = 1;


    //widgets
    //private RecyclerView mUserListRecyclerView;
    private MapView mMapView;
    private RelativeLayout mMapContainer;


    //vars
    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();
    private GoogleMap mGoogleMap;
    private UserLocation mUserPosition;
    private LatLngBounds mMapBoundary;
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private int mMapLayoutState = 0;
    private GeoApiContext mGeoApiContext;
    private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();
    private Marker mSelectedMarker = null;
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();
    private SessionManager sessionManager;
    private int DEFAULT_ZOOM = 1;
    private MaterialRippleLayout sendOtpOnDelivery;
    private MaterialRippleLayout navigateDirection;
    private FloatingActionButton floatingNavigateDir;
    private FloatingActionButton floatingCallUser;
    private ProgressBar progressBar;


    private String customerMobileNo,customerName,currentBookingId,currentOrderNo;
    private String customerAddress="";

    private TextView orderId,orderInfo;
    private LinearLayout orderInfoView;
    private JSONObject initialResponse;
    private String userLatitude,userLongitude;

    public TrackingEmployeeFragment(){

    }
    @SuppressLint("ValidFragment")
    public TrackingEmployeeFragment(JSONObject innerResponse) {
        initialResponse=innerResponse;
    }

    public static TrackingEmployeeFragment newInstance() {
        return new TrackingEmployeeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager=new SessionManager(getActivity());
        if (mUserLocations.size()==0){

            User user=new User("employee",sessionManager.getEmployeeId(),
                    sessionManager.getUserFirstName(),"");

            Log.e("kjdhsfksfsf","this is latitude "+sessionManager.getEmployeeCurrentLatitude());
            GeoPoint geoPoint=new GeoPoint(Double.parseDouble(sessionManager.getEmployeeCurrentLatitude()),
                    Double.parseDouble(sessionManager.getEmployeeCurrentLongitude()));

            mUserLocations.add(new UserLocation(user,geoPoint,""+System.currentTimeMillis()));

            try{
                Log.e("dfksfdsfs","this is complete json "+initialResponse);
                JSONObject object=initialResponse.getJSONObject("data");
                JSONObject custmrObj=object.getJSONObject("customer_loc");

                customerMobileNo=object.getString("customer_mobile");
                customerName=object.getString("customer_name");
                currentBookingId=object.getString("ses_booking_id");
                currentOrderNo=object.getString("booking_order_number");

                user=new User("user",custmrObj.getString("id"),
                        customerName,"");

                addMapMarkers();
                userLatitude=custmrObj.getString("curr_lat");
                userLongitude=custmrObj.getString("curr_long");

                geoPoint=new GeoPoint(Double.parseDouble(custmrObj.getString("curr_lat")),
                        Double.parseDouble(custmrObj.getString("curr_long")));

                mUserLocations.add(new UserLocation(user,geoPoint,""+System.currentTimeMillis()));


                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(getActivity(), Locale.getDefault());
                addresses = geocoder.getFromLocation(geoPoint.getLatitude(), geoPoint.getLongitude(), 1);

                if (addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    customerAddress=address;
            }
            }catch (Exception e){
                Log.e("kjdhfkdssfs","this is error "+e.getMessage());
            }

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        //mUserListRecyclerView = view.findViewById(R.id.user_list_recycler_view);

        mMapView = view.findViewById(R.id.user_list_map);
        view.findViewById(R.id.btn_full_screen_map).setOnClickListener(this);
        view.findViewById(R.id.btn_reset_map).setOnClickListener(this);
        mMapContainer = view.findViewById(R.id.map_container);

        orderId=view.findViewById(R.id.order_id);
        orderInfo=view.findViewById(R.id.order_info);
        orderInfoView=view.findViewById(R.id.info_view);
        floatingCallUser=view.findViewById(R.id.call_user);
        navigateDirection=view.findViewById(R.id.navigate_direction);
        floatingNavigateDir=view.findViewById(R.id.navigate_dir);
        progressBar=view.findViewById(R.id.progressBar);
        sendOtpOnDelivery=view.findViewById(R.id.send_otp);


        sessionManager=new SessionManager(getActivity());

        if (sessionManager.getCurrentOrderId().length()==0){
            orderInfoView.setVisibility(View.GONE);
        }else {
            orderInfoView.setVisibility(View.VISIBLE);
            orderId.setText(sessionManager.getCurrentOrderId());
            orderInfo.setText(sessionManager.getCurrentOrderInfo());
        }


        if (sessionManager.getCurrentBookingOrderId().length()<1){
            getActivity().finish();
        }


        sendOtpOnDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDeliveryOTP();
            }
        });

        navigateDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDirectionMap();
            }
        });

        floatingCallUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", customerMobileNo, null));
                startActivity(intent);
            }
        });
        floatingNavigateDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDirectionMap();
            }
        });
        initUserListRecyclerView();
        initGoogleMap(savedInstanceState);

        setUserPosition();


        return view;
    }

    private void openDirectionMap() {
        Uri navigationIntentUri = Uri.parse("google.navigation:q=" +  userLatitude +"," + userLongitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, navigationIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        try {

            startActivity(mapIntent);
            //startActivity(intent);
        }
        catch(ActivityNotFoundException ex) {
            try
            {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(navigationIntentUri+""));
                startActivity(unrestrictedIntent);
            }
            catch(ActivityNotFoundException innerEx) {
                Toast.makeText(getActivity(), "Please install a maps application", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void sendDeliveryOTP() {

        progressBar.setVisibility(View.VISIBLE);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/empBookingDeliveryOtpResend";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());
        dataParams.put("ses_booking_id",sessionManager.getCurrentBookingOrderId());

        Log.e("user_registration", "this is url " +ServerURL);

        Log.e("user_registration", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                OTPSuccess(),
                OTPError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(getActivity()).add(dataParamsJsonReq);
    }


    private Response.Listener<JSONObject> OTPSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("user_registration", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {
                        progressBar.setVisibility(View.GONE);
                        LaunchActivityClass.LaunchOrderBookingOTPActivity(getActivity());
                    }else {
                        sessionManager.clearOrderSession();
                        Toast.makeText(getActivity(),"Order canceled",Toast.LENGTH_SHORT).show();
                        LaunchActivityClass.LaunchMainActivity(getActivity());
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

    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                currentTrackingBooking();
                //retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }
/*

    private void retrieveUserLocations(){
        Log.d(TAG, "retrieveUserLocations: retrieving location of all users in the chatroom.");

        try{
            for(final ClusterMarker clusterMarker: mClusterMarkers){

                DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                        .collection(getString(R.string.collection_user_locations))
                        .document(clusterMarker.getUser().getUser_id());

                userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){

                            final UserLocation updatedUserLocation = task.getResult().toObject(UserLocation.class);

                            User user=new User("employee",sessionManager.getEmployeeId(),sessionManager.getUserFirstName(),"");

                            GeoPoint geoPoint=new GeoPoint(Double.parseDouble(sessionManager.getEmployeeCurrentLatitude()),
                                    Double.parseDouble(sessionManager.getEmployeeCurrentLongitude()));

                            mUserLocations.add(new UserLocation(user,geoPoint,""+System.currentTimeMillis()));


                            // update the location
                            for (int i = 0; i < mClusterMarkers.size(); i++) {
                                try {
                                    if (mClusterMarkers.get(i).getUser().getUser_id().equals(updatedUserLocation.getUser().getUser_id())) {

                                        LatLng updatedLatLng = new LatLng(
                                                updatedUserLocation.getGeo_point().getLatitude(),
                                                updatedUserLocation.getGeo_point().getLongitude()
                                        );

                                        mClusterMarkers.get(i).setPosition(updatedLatLng);
                                        mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(i));
                                    }


                                } catch (NullPointerException e) {
                                    Log.e(TAG, "retrieveUserLocations: NullPointerException: " + e.getMessage());
                                }
                            }
                        }
                    }
                });
            }
        }catch (IllegalStateException e){
            Log.e(TAG, "retrieveUserLocations: Fragment was destroyed during Firestore query. Ending query." + e.getMessage() );
        }
    }

*/

    private void currentTrackingBooking() {

        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/empCurrBookingTracking";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());
        dataParams.put("ses_booking_id",sessionManager.getCurrentBookingOrderId());
        dataParams.put("curr_lat",sessionManager.getEmployeeCurrentLatitude());
        dataParams.put("curr_long",sessionManager.getEmployeeCurrentLongitude());
        dataParams.put("curr_address","empty");

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
        Volley.newRequestQueue(getActivity()).add(dataParamsJsonReq);
    }


    private Response.Listener<JSONObject> getLocationUpdate() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("user_employee_loc", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {

                        JSONObject object=innerResponse.getJSONObject("data");

                        //JSONObject empObj=object.getJSONObject("employee_loc");

                        // update the location
                        for (int i = 0; i < mClusterMarkers.size(); i++) {
                            try {
                                if (mClusterMarkers.get(i).getUser().getUser_id()
                                        .equals(sessionManager.getEmployeeId())) {
                                    LatLng updatedLatLng = new LatLng(Double.parseDouble(sessionManager.getEmployeeCurrentLatitude()),
                                            Double.parseDouble(sessionManager.getEmployeeCurrentLongitude()));

                                    mClusterMarkers.get(i).setPosition(updatedLatLng);
                                    mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(i));
                                }
                            } catch (NullPointerException e) {
                                Log.e(TAG, "retrieveUserLocations: NullPointerException: " + e.getMessage());
                            }
                        }
                    }else {
                        sessionManager.clearOrderSession();
                        Log.e("user_employee_loc","order is canceled data ");
                        Toast.makeText(getActivity(),"Order canceled",Toast.LENGTH_SHORT).show();
                        LaunchActivityClass.LaunchMainActivity(getActivity());
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
                    mClusterManager = new ClusterManager<ClusterMarker>(getActivity().getApplicationContext(), mGoogleMap);
                }
                if(mClusterManagerRenderer == null){
                    mClusterManagerRenderer = new MyClusterManagerRenderer(
                            getActivity(),
                            mGoogleMap,
                            mClusterManager);


                    mClusterManager.setRenderer(mClusterManagerRenderer);
                }
                mGoogleMap.setOnInfoWindowClickListener(this);

                for(UserLocation userLocation: mUserLocations){

                    Log.d(TAG, "addMapMarkers: location: " + userLocation.getGeo_point().toString());
                    try{
                        String snippet = "";
                        int avatar = R.drawable.bike;
                        if(userLocation.getUser().getUser_id().equals(sessionManager.getEmployeeId())){
                            snippet = "This is your location";
                            avatar = R.drawable.bike;
                        }
                        else{
                            avatar = R.drawable.ic_house;
                            snippet = "Customer Address : " + customerAddress;
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

                    }catch (NullPointerException e){
                        Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage() );
                    }

                }
                mClusterManager.cluster();

                if (mClusterMarkers.size()<1){
                    return;
                }
                ClusterMarker cmarker=mClusterMarkers.get(1);
                Log.e("dsfjsdfsfsgd","this is title "+cmarker.getTitle());
                mClusterManager.removeItem(cmarker);
                Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(cmarker.getPosition())
                        .title(cmarker.getTitle())
                        .snippet(cmarker.getSnippet()));

                resetSelectedMarker();
                mSelectedMarker = marker;
                calculateDirections(marker);

                setCameraView();
            }
        }catch (Exception e){
            Log.e("dsfjsdfsdff","this is error exception "+e.getMessage());
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

    private void setUserPosition() {
        for (UserLocation userLocation : mUserLocations) {
            if (userLocation.getUser().getUser_id().equals(sessionManager.getEmployeeId())) {
                mUserPosition = userLocation;
            }
        }
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_api_key))
                    .build();
        }
    }

    private void initUserListRecyclerView() {
       /* mUserRecyclerAdapter = new UserRecyclerAdapter(mUserList, this);
        mUserListRecyclerView.setAdapter(mUserRecyclerAdapter);
        mUserListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));*/
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        startUserLocationsRunnable(); // update user locations every 'LOCATION_UPDATE_INTERVAL'
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
//        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        map.setMyLocationEnabled(true);
//        mGoogleMap = map;
//        setCameraView();

        Log.e("kjdfhskf","map is ready");

        mGoogleMap = map;
        addMapMarkers();
        mGoogleMap.setOnPolylineClickListener(this);

    }

    @Override
    public void onPause() {
        mMapView.onPause();
        stopLocationUpdates(); // stop updating user locations
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    private void expandMapAnimation(){
        /*ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                50,
                100);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mUserListRecyclerView);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                50,
                0);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();*/
    }

    private void contractMapAnimation(){
        /*ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                100,
                50);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mUserListRecyclerView);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                0,
                50);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_full_screen_map:{

                if(mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED){
                    mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
                    expandMapAnimation();
                }
                else if(mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED){
                    mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
                    contractMapAnimation();
                }
                break;
            }

            case R.id.btn_reset_map:{
                addMapMarkers();
                break;
            }
        }
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        Log.e("dsfsdfsdsdf","this is onInfoWindowClick "+marker);
        Log.e("dsfsdfsdsdf","this is onInfoWindowClick marker title "+marker.getTitle());

        //if (marker.getTitle())

        /*if(marker.getTitle().contains("Trip #")){
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Open Google Maps?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            String latitude = String.valueOf(marker.getPosition().latitude);
                            String longitude = String.valueOf(marker.getPosition().longitude);
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            try{
                                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    startActivity(mapIntent);
                                }
                            }catch (NullPointerException e){
                                Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage() );
                                Toast.makeText(getActivity(), "Couldn't open map", Toast.LENGTH_SHORT).show();
                            }

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
        else{
            if(marker.getSnippet().equals("This is you")){
                marker.hideInfoWindow();
            }
            else{

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(marker.getSnippet())
                        .setCancelable(true)
                        .setMessage("Show poly lines?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                resetSelectedMarker();
                                mSelectedMarker = marker;
                                calculateDirections(marker);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
        }*/

    }

    private void calculateDirections(Marker marker){
        Log.d(TAG, "calculateDirections: calculating directions.");

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
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
//                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
//                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
//                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
//                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                Log.d(TAG, "onResult: successfully retrieved directions.");
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

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
                try {
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
                        polyline.setColor(ContextCompat.getColor(getActivity(), R.color.darkGrey));
                        polyline.setClickable(true);
                        mPolyLinesData.add(new PolylineData(polyline, route.legs[0]));

                        // highlight the fastest route and adjust camera
                        double tempDuration = route.legs[0].duration.inSeconds;
                        if(tempDuration < duration){
                            duration = tempDuration;
                            onPolylineClick(polyline);
                            zoomRoute(polyline.getPoints());
                        }

                        mSelectedMarker.setVisible(false);
                    }
                }catch (Exception e){
                    Log.e("ksdfjkhdsfdfs","this is error "+e.getMessage());
                }

            }
        });
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
    public void onPolylineClick(Polyline polyline) {

        int index = 0;
        for(PolylineData polylineData: mPolyLinesData){
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.blue1));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );

                String Title;
                if (index==1){
                    Title=customerName;
                }else {
                    Title=sessionManager.getUserFirstName();
                }
                Log.e("hjdfsdkssfds","this is current title "+Title);
                Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(endLocation)
                        .title(Title)
                        .icon(PublicMethod.convertToBitmapFromVector(getContext(),
                                R.drawable.ic_placeholder))
                        .snippet("Duration: " + polylineData.getLeg().duration));

                mTripMarkers.add(marker);

                marker.showInfoWindow();
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.darkGrey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }

   /* @Override
    public void onUserClicked(int position) {
        Log.d(TAG, "onUserClicked: selected a user: " + mUserList.get(position).getUser_id());

        String selectedUserId = mUserList.get(position).getUser_id();

        for(ClusterMarker clusterMarker: mClusterMarkers){
            if(selectedUserId.equals(clusterMarker.getUser().getUser_id())){
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(clusterMarker.getPosition().latitude, clusterMarker.getPosition().longitude)),
                        600,
                        null
                );
                break;
            }
        }
    }*/
}



















