package com.pneck.employee.Activities.Registration;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.pneck.employee.Activities.SplashScreen;
import com.pneck.employee.Const;
import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;
import com.pneck.employee.services.LocationService;
import com.pneck.employee.utills.Tools;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class EmployeeSignInScreen extends AppCompatActivity {

    private TextInputEditText mobileNo;
    private TextInputEditText password;
    private ImageView passEye;
    private TextView forgotPassword;
    private TextView signUpBtn;
    private MaterialRippleLayout signInBtn;
    private boolean isEyeOpen=false;
    private ProgressBar progressBar;
    private SessionManager sessionManager;


    private void findViews() {
        mobileNo = (TextInputEditText)findViewById( R.id.mobile_no );
        password = (TextInputEditText)findViewById( R.id.password );
        passEye = (ImageView)findViewById( R.id.pass_eye );
        forgotPassword = (TextView)findViewById( R.id.forgot_password );
        signUpBtn = (TextView)findViewById( R.id.sign_up_btn );
        signInBtn = (MaterialRippleLayout)findViewById( R.id.sign_in_btn );
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_sign_in_screen);
        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);

        sessionManager=new SessionManager(EmployeeSignInScreen.this);
        findViews();

        clickListeners();

    }

    private void clickListeners() {
        passEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEyeOpen){
                    passEye.setImageResource(R.drawable.ic_eye_closed);
                    password.setTransformationMethod(new PasswordTransformationMethod());
                } else {
                    passEye.setImageResource(R.drawable.ic_eye);
                    password.setTransformationMethod(null);
                }
                password.setSelection(password.getText().length());
                isEyeOpen=!isEyeOpen;
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchActivityClass.LaunchForgotPasswordScreen(EmployeeSignInScreen.this);
            }
        });
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchActivityClass.LaunchSignUpScreen(EmployeeSignInScreen.this);
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mobileNo.getText().length()!=10){
                    mobileNo.setError("Please enter correct mobile number");
                    return;
                }
                if (password.getText().toString().length()<4){
                    password.setError("Enter correct password");
                    return;
                }
                LoginUser();
            }
        });
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void LoginUser() {

        progressBar.setVisibility(View.VISIBLE);
        FirebaseInstanceId.getInstance().getInstanceId().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Log.e("skdfhjskfd","this is exception "+e.getMessage());
            }
        }).addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()){
                    String ServerURL = getResources().getString(R.string.pneck_app_url) + "/EmployeeLogin";
                    HashMap<String, String> dataParams = new HashMap<String, String>();

                    dataParams.put("email",mobileNo.getText().toString());
                    dataParams.put("password",password.getText().toString());
                    dataParams.put("device_token", task.getResult().getToken());
                    dataParams.put("device_id", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

                    //Log.d("user_login", "this is url " +ServerURL);

                    //Log.d("user_login", "this is we sending " + dataParams.toString());

                    CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                            ServerURL,
                            dataParams,
                            RegistrationSuccess(),
                            RegistrationError());
                            dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy((int) TimeUnit.SECONDS.
                            toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    Volley.newRequestQueue(EmployeeSignInScreen.this).add(dataParamsJsonReq);
                }
            }
        });
    }


    private Response.Listener<JSONObject> RegistrationSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Log.d("user_login","onResponse() is invoked...");
                try {
                    //Log.d("user_login", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {

                        String msg=innerResponse.getString("message");

                        Toast.makeText(EmployeeSignInScreen.this," "+msg,Toast.LENGTH_SHORT).show();

                        progressBar.setVisibility(View.GONE);

                        if (innerResponse.has("data")){
                            JSONObject object=innerResponse.getJSONObject("data");
                            if (sessionManager.createSession(object.getString("EID"),object.getString("ep_token"),
                                    object.getString("name"),object.getString("last_name")
                                    ,object.getString("mobile"),
                                    object.getString("email"),
                                    object.getString("image"),
                                    object.getString("type_user"))){
                                if (sessionManager.getUserType()== SessionManager.UserType.TYPE_DELIVERY){
                                    LaunchActivityClass.LaunchMainActivity(EmployeeSignInScreen.this);
                                }
                                else LaunchActivityClass.LaunchDriverDashboard(EmployeeSignInScreen.this);
                            }
                        }
                    }else {
                        progressBar.setVisibility(View.GONE);
                        String msg=innerResponse.getString("message");
                        Toast.makeText(EmployeeSignInScreen.this," "+msg,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    //Log.v("user_login", "inside catch block  " + e.getMessage());
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
                Toast.makeText(getApplicationContext(), R.string.SOMETHING_WENT_WRONG, Toast.LENGTH_LONG).show();
                //Log.v("user_registration", "inside error block  " + error.getMessage());
            }
        };
    }

}
