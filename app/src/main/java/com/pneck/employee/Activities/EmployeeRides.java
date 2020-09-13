package com.pneck.employee.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.pneck.employee.Adapters.UserOrderAdapters;
import com.pneck.employee.Const;
import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;
import com.pneck.employee.models.UserOrderModel;
import com.pneck.employee.services.LocationService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class EmployeeRides extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView textView;
    private SessionManager sessionManager;

    private RecyclerView mRecyclerView;
    private ArrayList<UserOrderModel> orderList =new ArrayList<>();
    private ArrayList<String> addedOrderList=new ArrayList<>();
    private UserOrderAdapters userOrderAdapters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_rides);

        sessionManager=new SessionManager(EmployeeRides.this);
        textView=findViewById(R.id.resonse);
        progressBar=findViewById(R.id.progress_bar);
        mRecyclerView=findViewById(R.id.recycler_view);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(20);
        userOrderAdapters = new UserOrderAdapters(EmployeeRides.this, orderList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(EmployeeRides.this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(userOrderAdapters);

        findViewById(R.id.go_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        getRidesDetail();

    }

    private void getRidesDetail() {

        progressBar.setVisibility(View.VISIBLE);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/empMyRides";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());

        Log.e("user_rides", "this is url " +ServerURL);

        Log.e("user_rides", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                RegistrationSuccess(),
                RegistrationError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(EmployeeRides.this).add(dataParamsJsonReq);
    }


    private Response.Listener<JSONObject> RegistrationSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("user_rides", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {

                        String msg=innerResponse.getString("message");

                        if (msg.trim().equalsIgnoreCase("Info-No data!")){
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(getString(R.string.ORDER_NOT_FOUND));
                            progressBar.setVisibility(View.GONE);
                        }else {
                            JSONObject data=innerResponse.getJSONObject("data");

                            JSONArray orders=data.getJSONArray("Rides");

                            for (int i=0;i<orders.length();i++){
                                JSONObject object=orders.getJSONObject(i);
                                if (!addedOrderList.contains(object.getString("order_number"))){
                                    orderList.add(new UserOrderModel(object.getString("id"),
                                            object.getString("order_number"),object.getString("order_status"),
                                            object.getString("accept_otp_confirm_at"),object.getString("delivery_confirm_at"),
                                            object.getString("order_subtotal"),object.getString("booking_charge"),
                                            object.getString("grand_total"),object.getString("booking_complete_at")));
                                }
                            }

                            userOrderAdapters.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);

                        }

                    }

                } catch (Exception e) {
                    Log.v("user_rides", "inside catch block  " + e.getMessage());
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
                Log.v("user_rides", "inside error block  " + error.getMessage());
            }
        };
    }
}
