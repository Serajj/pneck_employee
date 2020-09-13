package com.pneck.employee.Activities.OrderProcess;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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
import java.util.concurrent.TimeUnit;

public class SubmitRequestOrderAmount extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText enterBilingAmount;
    private AppCompatButton submitBtn;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    private void findViews() {
        enterBilingAmount = (TextInputEditText)findViewById( R.id.enter_biling_amount );
        submitBtn = (AppCompatButton)findViewById( R.id.submit_btn );
        progressBar = (ProgressBar)findViewById( R.id.progress_bar );

        submitBtn.setOnClickListener( this );
    }

    @Override
    public void onClick(View v) {
        if ( v == submitBtn ) {
            submitBillingAmount();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_request_order_amount);
        findViews();
        sessionManager=new SessionManager(SubmitRequestOrderAmount.this);

        enterBilingAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String text=s.toString();

                Log.e("ksfkgerfgg","this is string "+enterBilingAmount.getText().toString().length());

                if(!s.toString().startsWith("₹")){
                    if (!text.trim().equalsIgnoreCase("₹")){
                        enterBilingAmount.setText("₹"+text);
                        Selection.setSelection(enterBilingAmount.getText(), enterBilingAmount.getText().length());

                    }

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(SubmitRequestOrderAmount.this,getString(R.string.YOU_ARE_IN_MIDDLE_OF_PROCESS),Toast.LENGTH_SHORT).show();
    }

    private void submitBillingAmount() {

        progressBar.setVisibility(View.VISIBLE);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/empBookingMarkCompleted";
        HashMap<String, String> dataParams = new HashMap<String, String>();


        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());
        dataParams.put("ses_booking_id",sessionManager.getCurrentBookingOrderId());
        dataParams.put("booking_amount",enterBilingAmount.getText().toString().substring(1));

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
        Volley.newRequestQueue(SubmitRequestOrderAmount.this).add(dataParamsJsonReq);
    }


    private Response.Listener<JSONObject> RegistrationSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("user_registration", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {

                        progressBar.setVisibility(View.GONE);
                        Bundle bundle=new Bundle();
                        bundle.putString("billing_amount",enterBilingAmount.getText().toString());
                        LaunchActivityClass.LaunchPaymentPendingScreen(SubmitRequestOrderAmount.this,bundle);
                        //LaunchActivityClass.LaunchTrackingScreen(SubmitRequestOrderAmount.this);

                    }else {
                        sessionManager.clearOrderSession();
                        Toast.makeText(SubmitRequestOrderAmount.this,"Order canceled",Toast.LENGTH_SHORT).show();
                        LaunchActivityClass.LaunchMainActivity(SubmitRequestOrderAmount.this);
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
                Toast.makeText(getApplicationContext(), R.string.SOMETHING_WENT_WRONG+error.getMessage(), Toast.LENGTH_LONG).show();
                Log.v("user_registration", "inside error block  " + error.getMessage());
            }
        };
    }
}
