package com.pneck.employee.Activities.Registration;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.balysv.materialripple.MaterialRippleLayout;
import com.pneck.employee.Const;
import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.utills.Tools;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class OtpVerificationScreen extends AppCompatActivity {

    private TextInputEditText enterOtp;
    private TextView resendOtp;
    private MaterialRippleLayout verifyBtn;
    private int SendOTPCounter=0;
    private ProgressBar mProgressBar;

    private String mobileNo="";
    private boolean isFromForgotPassword;

    private void findViews() {

        enterOtp = (TextInputEditText)findViewById( R.id.enter_otp );
        resendOtp = (TextView)findViewById( R.id.resend_otp );
        verifyBtn = (MaterialRippleLayout)findViewById( R.id.verify_btn );
        mProgressBar=findViewById(R.id.progress_bar);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification_screen);
        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);

        findViews();
        clickListeners();
        isFromForgotPassword=getIntent().getBooleanExtra("is_from_forgot_password",false);
        mobileNo=getIntent().getStringExtra("mobile_no");
    }

    private void clickListeners() {
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (enterOtp.getText().length()!=6){
                 enterOtp.setError("Please enter correct OTP");
                }else {
                    verifyOTP();
                }
            }
        });


        resendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendOTPCounter++;
                resendOtp();
            }
        });

        runTimer();

    }

    private void verifyOTP() {
        mProgressBar.setVisibility(View.VISIBLE);
        if (enterOtp.getText().toString().length()==6){
            verifyMobile();
        } else {
         enterOtp.setError("Please enter correct OTP");
        }
    }

    private void runTimer(){

        if (SendOTPCounter==0||SendOTPCounter==1){
            StartCountdownTimer(30000);
        }else if(SendOTPCounter==2||SendOTPCounter==3){
            StartCountdownTimer(60000);
        }else {
            StartCountdownTimer(60000);
        }

        resendOtp.setEnabled(false);
    }

    private void StartCountdownTimer(int timer_millis){
        new CountDownTimer(timer_millis, 1000) {

            public void onTick(long millisUntilFinished) {
                if((millisUntilFinished / 1000)<10){
                    resendOtp.setText("00." + "0"+millisUntilFinished / 1000);

                }else {
                    resendOtp.setText("00." + millisUntilFinished / 1000);

                }

            }

            public void onFinish() {
                resendOtp.setEnabled(true);
                resendOtp.setText("Resend OTP");
            }

        }.start();

    }

    private void verifyMobile() {


        runTimer();

        String ServerURL;
        HashMap<String, String> dataParams = new HashMap<String, String>();
        if (isFromForgotPassword){
            ServerURL= getResources().getString(R.string.pneck_app_url) + "/EmpForgotPassVerifyMobile";
            dataParams.put("mobile_number",mobileNo);
        }else {
            ServerURL= getResources().getString(R.string.pneck_app_url) + "/EmployeeMobileVerify";
            dataParams.put("mobile",mobileNo);
        }

        dataParams.put("otp",enterOtp.getText().toString());
        Log.e("otp_verfication", "this is url " +ServerURL);

        Log.e("otp_verfication", "this is we sending " + dataParams.toString());
        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                VerifySuccess(),
                VerifyError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(OtpVerificationScreen.this).add(dataParamsJsonReq);
    }

    private Response.Listener<JSONObject> VerifySuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("otp_verfication", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    JSONObject object=innerResponse.getJSONObject("data");
                    if (innerResponse.getBoolean("success")) {
                        mProgressBar.setVisibility(View.GONE);
                        if (object.has("is_mobile_verified")&&!isFromForgotPassword){
                            if (object.getString("is_mobile_verified").equalsIgnoreCase("yes")){
                                //processLogin();
                                LaunchActivityClass.LaunchLoginScreen(OtpVerificationScreen.this);
                            }
                        }else {
                            Toast.makeText(OtpVerificationScreen.this,"Password sent to your registered mobile",Toast.LENGTH_SHORT).show();
                            LaunchActivityClass.LaunchLoginScreen(OtpVerificationScreen.this);
                        }
                    }else {
                        String msg=object.getString("message");
                        Toast.makeText(OtpVerificationScreen.this,msg,Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    Log.v("otp_verfication", "inside catch block  " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }

    private Response.ErrorListener VerifyError() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), R.string.SOMETHING_WENT_WRONG, Toast.LENGTH_SHORT).show();
                Log.v("otp_verfication", "inside error block  " + error.getMessage());
            }
        };
    }

    private void processLogin() {

    }



    private void resendOtp() {

        runTimer();

        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/EmployeeOtpResend";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("mobile_no",mobileNo);
        Log.e("otp_verfication", "this is url " +ServerURL);

        Log.e("otp_verfication", "this is we sending " + dataParams.toString());
        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                RegistrationSuccess(),
                RegistrationError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(OtpVerificationScreen.this).add(dataParamsJsonReq);
    }


    private Response.Listener<JSONObject> RegistrationSuccess() {
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

    private Response.ErrorListener RegistrationError() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), R.string.SOMETHING_WENT_WRONG, Toast.LENGTH_SHORT).show();
                Log.v("otp_verfication", "inside error block  " + error.getMessage());
            }
        };
    }

}
