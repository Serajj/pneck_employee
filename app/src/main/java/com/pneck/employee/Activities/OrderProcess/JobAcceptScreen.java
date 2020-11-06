package com.pneck.employee.Activities.OrderProcess;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationServices;
import com.pneck.employee.Const;
import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;
import com.pneck.employee.services.LocationService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.pneck.employee.services.LocationService.isOtpcalled;

public class JobAcceptScreen extends AppCompatActivity implements View.OnClickListener {

    private String OrderNumber,bookOrderId,orderDistance;

    private TextView bookingOrderNumber;
    private TextView bookingOrderId;
    private TextView bookingOrderDistance;
    private AppCompatButton acceptBtn;
    private AppCompatButton skipOrder;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private TextView userAddress,cashOfferd;
    private String currentLatitude="";
    private String currentLongitude="";
    private String customerLat,customerLong;
    EditText driverCash;
    String cash;
    ScheduledExecutorService executor,customerAcceptEcecutor;
    Runnable customerAcceptRunneble;
    int i=0;
    boolean j=true;


    private void findViews() {
        bookingOrderNumber = (TextView)findViewById( R.id.booking_order_number );
        bookingOrderId = (TextView)findViewById( R.id.booking_order_id );
        bookingOrderDistance = (TextView)findViewById( R.id.booking_order_distance );
        acceptBtn = (AppCompatButton)findViewById( R.id.accept_btn );
        skipOrder = (AppCompatButton)findViewById( R.id.skip_order );
        userAddress=findViewById(R.id.user_address);
        cashOfferd=findViewById(R.id.cash_offered);
        progressBar=findViewById(R.id.progress_bar);
        driverCash=findViewById(R.id.driver_cash_offer);
        acceptBtn.setOnClickListener( this );
        skipOrder.setOnClickListener( this );
    }




    @Override
    public void onClick(View v) {
        if ( v == acceptBtn ) {
            Toast.makeText(this, "Accept order button clicked..", Toast.LENGTH_SHORT).show();
            acceptOrder();

        } else if ( v == skipOrder ) {

            rejectOrder();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_accept_screen);

        findViews();

        isOtpcalled=true;
        sessionManager=new SessionManager(JobAcceptScreen.this);

        orderDistance=getIntent().getStringExtra("distance_km");
        OrderNumber=getIntent().getStringExtra("booking_order_number");
        bookOrderId=getIntent().getStringExtra("booking_order_id");
        customerLat=getIntent().getStringExtra("customer_lat");
        customerLong=getIntent().getStringExtra("customer_long");
        bookingOrderId.setText("Order Id : "+bookOrderId);
        bookingOrderDistance.setText("Distance : "+orderDistance+"km away");
        bookingOrderNumber.setText("Order Number : "+OrderNumber);
        executor = Executors.newSingleThreadScheduledExecutor();

        Runnable periodicTask = new Runnable() {
            public void run() {
                doPeriodicWork();
            }
        };

        customerAcceptEcecutor = Executors.newSingleThreadScheduledExecutor();

        customerAcceptRunneble = new Runnable() {
            public void run() {
                // Invoke method(s) to do the work
                checkforcutomerAgreeorNot();

            }
        };

        executor.scheduleAtFixedRate(periodicTask, 0, 3, TimeUnit.SECONDS);


        currentLatitude=getIntent().getStringExtra("customer_lat");
        currentLongitude=getIntent().getStringExtra("customer_long");

        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            addresses = geocoder.getFromLocation(Double.parseDouble(currentLatitude), Double.parseDouble(currentLongitude), 1);
            Log.e("kjfksfsfsf", "this is error exception addresses "+addresses );
            Log.e("kjfksfsfsf", "currentLatitude  "+currentLatitude+" currentLongitude "+currentLongitude );
            if (addresses.size() > 0) {

                String address = addresses.get(0).getAddressLine(0);
                userAddress.setText(address);
            }

        } catch (Exception e) {
            Log.e("kjfksfsfsf", "this is error exception " + e.getMessage());
        }

    }



    //repetative task
    private void doPeriodicWork()
    {
        Log.d("Serajc","Counter Working");

        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/fetchOfferedCash";
        HashMap<String, String> dataParams = new HashMap<String, String>();
        dataParams.put("booking_id",bookOrderId);
        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                CashFetchSuccess(),
                ErrorListener());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(JobAcceptScreen.this).add(dataParamsJsonReq);
    }

    private Response.Listener<JSONObject> CashFetchSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("Serajc","accept responce recieved");
                    Log.d("Serajc", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {

                        Log.d("Serajcr","cash responce success");
                        JSONArray jsonArray=innerResponse.getJSONArray("data");
                        JSONObject object=jsonArray.getJSONObject(0);
                        Log.d("Serajcr",object.getString("cash_offer"));
                        cash=object.getString("cash_offer");

                        if (!cashOfferd.getText().equals(cash)){
                            cashOfferd.setText(cash);
                        }else{
                            Log.d("Serajcv","cash value : "+cash);
                        }
                        Log.d("Serajcv","cash value : "+cash);
                    }else {
                        Log.d("Seraj","accept responce failed");
                    }

                } catch (Exception e) {
                    Log.d("Seraj", "error cash inside catch block  " + e.getMessage());
                    e.printStackTrace();

                }
            }
        };
    }


    private void acceptOrder() {

        if (driverCash.getText().toString().isEmpty()){
            driverCash.setText(cash);
        }
        Log.d("Seraj","Sending accept request");
        progressBar.setVisibility(View.VISIBLE);
       // String ServerURL = getResources().getString(R.string.pneck_app_url) + "/empBookingAccept";

        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/insertAvailableEmployees";
        HashMap<String, String> dataParams = new HashMap<String, String>();


        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());
        dataParams.put("booking_id",bookOrderId);
        dataParams.put("employee_lat",sessionManager.getEmployeeCurrentLatitude());
        dataParams.put("employee_lng",sessionManager.getEmployeeCurrentLongitude());
        dataParams.put("employee_name",sessionManager.getUserFirstName()+" "+sessionManager.getUserLastName());
        dataParams.put("employee_phone",sessionManager.getUserPhone());
        dataParams.put("employee_time_to_reach",bookOrderId);
        dataParams.put("description","waiting");
        dataParams.put("status","agree");
        dataParams.put("employee_cash_offer",""+driverCash.getText().toString());

        Log.d("Serad", "this is url " +ServerURL);

        Log.e("Seraj", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                AcceptSuccess(),
                ErrorListener());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(JobAcceptScreen.this).add(dataParamsJsonReq);
    }


    private Response.Listener<JSONObject> AcceptSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("Seraj","accept responce recieved");
                    Log.d("Seraj", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {
                        customerAcceptEcecutor.scheduleAtFixedRate(customerAcceptRunneble, 0, 4, TimeUnit.SECONDS);

                        Log.d("Seraj","accept responce success");

                        progressBar.setVisibility(View.GONE);

                        JSONObject object=innerResponse.getJSONObject("data");

                       /** Bundle bundle=new Bundle();
                        bundle.putString("booking_order_number",object.getString("booking_order_number"));
                        bundle.putString("ses_booking_id",object.getString("ses_booking_id"));
                        bundle.putString("customer_mobile",object.getString("customer_mobile"));
                        bundle.putString("customer_name",object.getString("customer_name"));
                        sessionManager.createOrderSession(object.getString("ses_booking_id"),
                                object.getString("customer_name"),object.getString("customer_mobile"),"");
                        Log.d("Seraj","Launcing otp screen");
                        LaunchActivityClass.LaunchJOB_OTPScreen(JobAcceptScreen.this,bundle);**/

                    }else {
                        Log.d("Seraj","accept responce failed");
                        LocationService.isOtpcalled=false;
                        sessionManager.clearOrderSession();
                        Toast.makeText(JobAcceptScreen.this,"Order canceled",Toast.LENGTH_SHORT).show();

                        LaunchActivityClass.LaunchMainActivity(JobAcceptScreen.this);
                    }

                } catch (Exception e) {
                    Log.d("Seraj", "error accept inside catch block  " + e.getMessage());
                    e.printStackTrace();

                }
            }
        };
    }


    @Override
    public void onBackPressed() {
        BackPressAlertDialog(JobAcceptScreen.this);
    }

    public  void BackPressAlertDialog(final Activity activity){

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View mView = activity.getLayoutInflater().inflate(R.layout.backpress_dialog_layout, null);
        TextView message;
        TextView stayOnPage;
        TextView leavePage;
        message = (TextView)mView.findViewById( R.id.message );
        stayOnPage = (TextView)mView.findViewById( R.id.stay_on_page );
        leavePage = (TextView)mView.findViewById( R.id.leave_page );


        builder.setView(mView);
        final AlertDialog NewCategory_dialog = builder.create();



        stayOnPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                NewCategory_dialog.dismiss();
            }
        });
        leavePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewCategory_dialog.dismiss();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LocationService.isOtpcalled=false;
                    }
                },1000*2);
                finish();
            }
        });


        NewCategory_dialog.show();
    }

    private Response.ErrorListener ErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), R.string.SOMETHING_WENT_WRONG+error.getMessage(), Toast.LENGTH_LONG).show();
                Log.v("user_registration", "inside error block  " + error.getMessage());
            }
        };
    }



    //reject order

    private void rejectOrder() {

        progressBar.setVisibility(View.VISIBLE);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/empBookingReject";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());
        dataParams.put("booking_id",bookOrderId);
        dataParams.put("status","0");

        Log.e("user_registration", "this is url " +ServerURL);

        Log.e("user_registration", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                RegistrationSuccess(),
                RegistrationError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(JobAcceptScreen.this).add(dataParamsJsonReq);
    }

    private Response.Listener<JSONObject> RegistrationSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("user_registration", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {

                        Toast.makeText(JobAcceptScreen.this,getResources().getString(R.string.YOU_HAVE_BLOCKED),
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        LaunchActivityClass.LaunchMainActivity(JobAcceptScreen.this);

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
                Log.v("user_registration", "inside error block  " + error.getMessage());
            }
        };
    }


//fire when customer accepts order
    private void CustomerAcceptOrder() {

        progressBar.setVisibility(View.VISIBLE);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/empBookingAccept";
        HashMap<String, String> dataParams = new HashMap<String, String>();
        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());
        dataParams.put("booking_id",bookOrderId);

        Log.d("Seraj", "Customer responce url call " +ServerURL);

        Log.d("Seraj", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                AgreeSuccess(),
                ErrorListener());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(JobAcceptScreen.this).add(dataParamsJsonReq);
    }
    private Response.Listener<JSONObject> AgreeSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("user_registration", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {

                        cancelexecuters();

                        progressBar.setVisibility(View.GONE);

                        JSONObject object=innerResponse.getJSONObject("data");

                        Bundle bundle=new Bundle();
                        bundle.putString("booking_order_number",object.getString("booking_order_number"));
                        bundle.putString("ses_booking_id",object.getString("ses_booking_id"));
                        bundle.putString("customer_mobile",object.getString("customer_mobile"));
                        bundle.putString("customer_name",object.getString("customer_name"));
                        sessionManager.createOrderSession(object.getString("ses_booking_id"),
                                object.getString("customer_name"),object.getString("customer_mobile"),"");
                        sessionManager.saveOrderDetail(bookOrderId,cashOfferd.getText().toString());
                       // LaunchActivityClass.LaunchJOB_OTPScreen(JobAcceptScreen.this,bundle);

                        LaunchActivityClass.LaunchTrackingScreen(JobAcceptScreen.this);

                    }else {
                        LocationService.isOtpcalled=false;
                        sessionManager.clearOrderSession();
                        Toast.makeText(JobAcceptScreen.this,"Order canceled",Toast.LENGTH_SHORT).show();
                        LaunchActivityClass.LaunchMainActivity(JobAcceptScreen.this);
                    }

                } catch (Exception e) {
                    Log.v("user_registration", "inside catch block  " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }

    private void cancelexecuters() {
        customerAcceptEcecutor.shutdown();
    }


    //checking customar accepts or not
    private void checkforcutomerAgreeorNot() {

        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/fetchAvailableEmployeesWithEmpId";
        HashMap<String, String> dataParams = new HashMap<String, String>();
        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("booking_id",bookOrderId);

        Log.d("Seraj", "Customer Acceps or not " +ServerURL);

        Log.d("Seraj", "This is we Sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                CutomerAcceptStatus(),
                ErrorListener());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(JobAcceptScreen.this).add(dataParamsJsonReq);

        //fetchAvailableEmployeesWithEmpId

    }

    private Response.Listener<JSONObject> CutomerAcceptStatus() {

        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("Serajcas", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {

                        Log.d("Serajcas", "success status check" );
                        JSONObject object=innerResponse.getJSONObject("data");
                        String status=object.getString("status");
                        Log.d("Serajcass", "success status : " +status);
                        if (status.equalsIgnoreCase("accept")){
                            CustomerAcceptOrder();
                            i=0;
                        }else{
                            if (j){
                                i=i+4;
                            }
                            if(i==60){
                                j=false;
                                cancelexecuters();
                                Toast.makeText(JobAcceptScreen.this, "Either order cancelled or taken by other..", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                    }else {
                        Toast.makeText(JobAcceptScreen.this, "Please wait!", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    Log.d("Seraj", "inside catch block  " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }







}
