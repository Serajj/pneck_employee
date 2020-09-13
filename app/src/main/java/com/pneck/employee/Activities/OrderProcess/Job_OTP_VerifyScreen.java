package com.pneck.employee.Activities.OrderProcess;

import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.design.widget.TextInputEditText;
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
import com.pneck.employee.Activities.Registration.OtpVerificationScreen;
import com.pneck.employee.Const;
import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Job_OTP_VerifyScreen extends AppCompatActivity implements View.OnClickListener {

    private TextView customerNo;
    private TextView customerName;
    private TextInputEditText otpEditText;
    private AppCompatButton verifyBtn;
    private SessionManager sessionManager;
    private int SendOTPCounter=0;
    private ProgressBar progressBar;
    private TextView ResendOtp;

    private void findViews() {
        contactLayout=findViewById(R.id.contact_layout);
        customerNo = (TextView)findViewById( R.id.customer_no );
        customerName = (TextView)findViewById( R.id.customer_name );
        otpEditText = (TextInputEditText)findViewById( R.id.otp_edit_text );
        verifyBtn = (AppCompatButton)findViewById( R.id.verify_btn );
        progressBar=findViewById(R.id.progress_bar);
        ResendOtp=findViewById(R.id.resend_otp);

        verifyBtn.setOnClickListener( this );
    }

    @Override
    public void onClick(View v) {
        if ( v == verifyBtn ) {
        verifyJobOTP();
        }
    }


    private LinearLayout contactLayout;

    private  String bookingOrderNumber,sesBookingId,custmrMobile,custmrName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job__otp__verify_screen);
        findViews();

        clickListeners();
        sessionManager=new SessionManager(Job_OTP_VerifyScreen.this);

        bookingOrderNumber=getIntent().getStringExtra("booking_order_number");
        sesBookingId=getIntent().getStringExtra("ses_booking_id");
        custmrMobile=getIntent().getStringExtra("customer_mobile");
        custmrName=getIntent().getStringExtra("customer_name");

        customerNo.setText(custmrMobile);
        customerName.setText(custmrName);

        runTimer();
    }


    @Override
    public void onBackPressed() {
        Toast.makeText(Job_OTP_VerifyScreen.this,getString(R.string.YOU_ARE_IN_MIDDLE_OF_PROCESS),Toast.LENGTH_SHORT).show();
    }
    private void clickListeners() {
        contactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", custmrMobile, null));
                startActivity(intent);
            }
        });

        ResendOtp.setVisibility(View.GONE);
        ResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendOTPCounter++;
                resendOtp();
            }
        });
    }


    private void runTimer(){

        if (SendOTPCounter==0||SendOTPCounter==1){
            StartCountdownTimer(30000);
        }else if(SendOTPCounter==2||SendOTPCounter==3){
            StartCountdownTimer(60000);
        }else {
            StartCountdownTimer(60000);
        }

        ResendOtp.setEnabled(false);
    }

    private void StartCountdownTimer(int timer_millis){
        new CountDownTimer(timer_millis, 1000) {

            public void onTick(long millisUntilFinished) {
                if((millisUntilFinished / 1000)<10){
                    ResendOtp.setText("00." + "0"+millisUntilFinished / 1000);

                }else {
                    ResendOtp.setText("00." + millisUntilFinished / 1000);

                }

            }

            public void onFinish() {
                ResendOtp.setEnabled(true);
                ResendOtp.setText(getResources().getString(R.string.RESEND_OTP));
            }

        }.start();

    }



    private void resendOtp() {

        runTimer();

        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/EmployeeOtpResend";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        //dataParams.put("mobile_no",mobileNo);
        Log.e("otp_verfication", "this is url " +ServerURL);

        Log.e("otp_verfication", "this is we sending " + dataParams.toString());
        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                OTPSuccess(),
                OTPError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(Job_OTP_VerifyScreen.this).add(dataParamsJsonReq);
    }


    private Response.Listener<JSONObject> OTPSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("otp_verfication", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {


                    }

                } catch (Exception e) {
                    Log.v("otp_verfication", "inside catch block  " + e.getMessage());
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
                Toast.makeText(getApplicationContext(), R.string.SOMETHING_WENT_WRONG, Toast.LENGTH_SHORT).show();
                Log.v("otp_verfication", "inside error block  " + error.getMessage());
            }
        };
    }



    private void verifyJobOTP() {

        progressBar.setVisibility(View.VISIBLE);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/empBookingOtpMatch";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ses_booking_id",sesBookingId);
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
        Volley.newRequestQueue(Job_OTP_VerifyScreen.this).add(dataParamsJsonReq);
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
                            Toast.makeText(Job_OTP_VerifyScreen.this,msg,Toast.LENGTH_SHORT).show();
                        }else {
                            LaunchActivityClass.LaunchJOBDetailsScreen(Job_OTP_VerifyScreen.this);
                            Job_OTP_VerifyScreen.this.finish();
                        }
                        progressBar.setVisibility(View.GONE);

                        //
                    }else {
                        sessionManager.clearOrderSession();
                        Toast.makeText(Job_OTP_VerifyScreen.this,"Order canceled",Toast.LENGTH_SHORT).show();
                        LaunchActivityClass.LaunchMainActivity(Job_OTP_VerifyScreen.this);
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
