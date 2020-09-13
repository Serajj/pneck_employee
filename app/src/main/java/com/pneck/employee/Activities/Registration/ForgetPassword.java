package com.pneck.employee.Activities.Registration;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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

public class ForgetPassword extends AppCompatActivity {

    private TextInputEditText enterMobileNo;
    private ProgressBar progressBar;
    private MaterialRippleLayout sendOtp;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2019-11-05 14:18:20 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        enterMobileNo = (TextInputEditText)findViewById( R.id.enter_mobile_no );
        progressBar = (ProgressBar)findViewById( R.id.progress_bar );
        sendOtp = (MaterialRippleLayout)findViewById( R.id.send_otp );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);

        findViews();

        sendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (enterMobileNo.getText().toString().length()!=10){
                    enterMobileNo.setError("Enter correct mobile number");
                    return;
                }
                checkINDatabase();
            }
        });
    }

    private void checkINDatabase() {

        progressBar.setVisibility(View.VISIBLE);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/EmployeeForgotPassWithMobile";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("mobile",enterMobileNo.getText().toString());

        Log.e("user_registration", "this is url " +ServerURL);

        Log.e("user_registration", "this is we sending " + dataParams.toString());

        Toast.makeText(ForgetPassword.this," this is we are sending "+enterMobileNo.getText().toString(),Toast.LENGTH_LONG).show();
        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                RegistrationSuccess(),
                RegistrationError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(ForgetPassword.this).add(dataParamsJsonReq);
    }


    private Response.Listener<JSONObject> RegistrationSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("user_registration", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    Toast.makeText(ForgetPassword.this," this is recieved response "+response,Toast.LENGTH_LONG).show();

                    if (innerResponse.getBoolean("success")) {

                        progressBar.setVisibility(View.GONE);
                        Bundle bundle=new Bundle();
                        bundle.putString("mobile_no",enterMobileNo.getText().toString());
                        bundle.putBoolean("is_from_forgot_password",true);
                        LaunchActivityClass.LaunchOTPActivity(ForgetPassword.this,bundle);

                    }

                } catch (Exception e) {
                    Toast.makeText(ForgetPassword.this," this is error block "+e.getMessage(),Toast.LENGTH_LONG).show();
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
