package com.pneck.employee.Activities.OrderProcess;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
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

public class DeliveryCompleteOTPVerification extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText otpEditText;
    private AppCompatButton verifyBtn;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    private void findViews() {
        otpEditText = (TextInputEditText)findViewById( R.id.otp_edit_text );
        verifyBtn = (AppCompatButton)findViewById( R.id.verify_btn );
        progressBar = (ProgressBar)findViewById( R.id.progress_bar );

        verifyBtn.setOnClickListener( this );
    }

    @Override
    public void onClick(View v) {
        if ( v == verifyBtn ) {

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_complete_otpverification);

        findViews();

        clickListeners();
        sessionManager=new SessionManager(DeliveryCompleteOTPVerification.this);

    }

    private void clickListeners() {
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (otpEditText.getText().length()==6){
                    verifyJobOTP();
                }else {
                    otpEditText.setError(getString(R.string.PLEASE_ENTER_CORRECT_OTP));
                }
            }
        });
    }


    private void verifyJobOTP() {

        progressBar.setVisibility(View.VISIBLE);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/empBookingDeliveryOtpMatch";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ses_booking_id",sessionManager.getCurrentBookingOrderId());
        dataParams.put("otp",otpEditText.getText().toString());

        Log.e("user_otp_verification", "this is url " +ServerURL);

        Log.e("user_otp_verification", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                RegistrationSuccess(),
                RegistrationError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(DeliveryCompleteOTPVerification.this).add(dataParamsJsonReq);
    }


    private Response.Listener<JSONObject> RegistrationSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("user_otp_verification", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {
                        progressBar.setVisibility(View.GONE);
                        LaunchActivityClass.LaunchOrderSubmitRequestScreen(DeliveryCompleteOTPVerification.this);
                        DeliveryCompleteOTPVerification.this.finish();
                        //Toast.makeText(Job_OTP_VerifyScreen.this,"Otp verifies successfully",Toast.LENGTH_SHORT).show();
                    }else {
                        sessionManager.clearOrderSession();
                        Toast.makeText(DeliveryCompleteOTPVerification.this,"Order canceled",Toast.LENGTH_SHORT).show();
                        LaunchActivityClass.LaunchMainActivity(DeliveryCompleteOTPVerification.this);
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
}
