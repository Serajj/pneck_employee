package com.pneck.employee.Activities.OrderProcess;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.pneck.employee.Const;
import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class PaymentStatusPendingStage extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout contactLayout;
    private TextView collectAmount;
    private AppCompatButton submitBtn;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    private void findViews() {
        contactLayout = (LinearLayout)findViewById( R.id.contact_layout );
        collectAmount = (TextView)findViewById( R.id.collect_amount );
        submitBtn = (AppCompatButton)findViewById( R.id.submit_btn );
        progressBar = (ProgressBar)findViewById( R.id.progress_bar );

        submitBtn.setOnClickListener( this );
    }

    @Override
    public void onClick(View v) {
        if ( v == submitBtn ) {
            checkBillingAmount();
        }
    }

    private boolean isCalled=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_status_pending_stage);
        findViews();

        sessionManager=new SessionManager(PaymentStatusPendingStage.this);
        String bilingAmount=getIntent().getStringExtra("billing_amount");

        if (bilingAmount.length()>0){
            collectAmount.setText("Collect : "+bilingAmount);
        }else {
            collectAmount.setText("Collect billing amount");
        }

        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
                                      @Override
                                      public void run() {
                                          if (!isCalled){
                                              checkBillingAmount();
                                          }
                                      }
                                  },
                0, 1000);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(PaymentStatusPendingStage.this,getString(R.string.YOU_ARE_IN_MIDDLE_OF_PROCESS),Toast.LENGTH_SHORT).show();
    }

    private void checkBillingAmount() {

        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/empBookingPaymentStatus";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());
        dataParams.put("ses_booking_id",sessionManager.getCurrentBookingOrderId());
        Log.e("payment_status", "this is url " +ServerURL);

        Log.e("payment_status", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                RegistrationSuccess(),
                RegistrationError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(PaymentStatusPendingStage.this).add(dataParamsJsonReq);
    }


    private Response.Listener<JSONObject> RegistrationSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("payment_status", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {

                        JSONObject object =innerResponse.getJSONObject("data");
                        if (object.getString("your_booking_status").equalsIgnoreCase("order_completed")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    isCalled=true;
                                    LaunchActivityClass.LaunchOrderCompleteHappyScreen(PaymentStatusPendingStage.this);
                                }
                            });

                        }

                        //LaunchActivityClass.LaunchTrackingScreen(SubmitRequestOrderAmount.this);

                    }else {
                        sessionManager.clearOrderSession();
                        Toast.makeText(PaymentStatusPendingStage.this,"Order canceled",Toast.LENGTH_SHORT).show();
                        LaunchActivityClass.LaunchMainActivity(PaymentStatusPendingStage.this);
                    }

                } catch (Exception e) {
                    Log.v("payment_status", "inside catch block  " + e.getMessage());
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
                Log.v("payment_status", "inside error block  " + error.getMessage());
            }
        };
    }
}
