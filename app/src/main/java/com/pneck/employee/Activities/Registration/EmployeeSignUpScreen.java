package com.pneck.employee.Activities.Registration;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.pneck.employee.Const;
import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;
import com.pneck.employee.utills.Tools;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class EmployeeSignUpScreen extends AppCompatActivity {

    private LinearLayout signUpLayout;
    private LinearLayout singInLayout;
    private TextInputEditText firstName;
    private TextInputEditText lastName;
    private TextInputEditText mobileNo;
    private TextInputEditText email;
    private TextInputEditText password;
    private TextInputEditText confirmPassword;
    private MaterialRippleLayout signUpBtn;
    private ProgressBar mProgressBar;
    private SessionManager sessionManager;
    private TextInputEditText adharNumber;
    private TextInputEditText vehicleNumber;
    private TextInputEditText dlNUmber;
    private String userType = "";


    private ImageView passWardEye,ConfirmpasswordEye;

    private boolean isEyeOpen=false,ReisEyeOpen=false;

    private void findViews() {
        dlNUmber=findViewById(R.id.dl_number);
        adharNumber = (TextInputEditText)findViewById( R.id.adhar_number );
        vehicleNumber = (TextInputEditText)findViewById( R.id.vehicle_number );
        passWardEye=findViewById(R.id.pass_eye);
        ConfirmpasswordEye=findViewById(R.id.confirm_pass_eye);
        signUpLayout = (LinearLayout)findViewById( R.id.sign_up_layout );
        singInLayout = (LinearLayout)findViewById( R.id.sing_in_layout );
        firstName = (TextInputEditText)findViewById( R.id.first_name );
        lastName = (TextInputEditText)findViewById( R.id.last_name );
        mobileNo = (TextInputEditText)findViewById( R.id.mobile_no );
        email = (TextInputEditText)findViewById( R.id.email );
        password = (TextInputEditText)findViewById( R.id.password );
        confirmPassword = (TextInputEditText)findViewById( R.id.confirm_password );
        signUpBtn = (MaterialRippleLayout)findViewById( R.id.sign_up_btn );
        mProgressBar=findViewById(R.id.progress_bar);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_sign_up_screen);
        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);

        sessionManager=new SessionManager(EmployeeSignUpScreen.this);

        if (sessionManager.isLoggedIn()){
            LaunchActivityClass.LaunchMainActivity(EmployeeSignUpScreen.this);
        }

        showForgotPassScreen();
        findViews();
        clickListeners();
    }

    private void clickListeners() {

        singInLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchActivityClass.LaunchSignInScreen(EmployeeSignUpScreen.this);
            }
        });

        passWardEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEyeOpen){
                    passWardEye.setImageResource(R.drawable.ic_eye_closed);
                    password.setTransformationMethod(new PasswordTransformationMethod());
                }else {
                    passWardEye.setImageResource(R.drawable.ic_eye);
                    password.setTransformationMethod(null);
                }
                password.setSelection(password.getText().length());
                isEyeOpen=!isEyeOpen;
            }
        });

        ConfirmpasswordEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ReisEyeOpen){
                    ConfirmpasswordEye.setImageResource(R.drawable.ic_eye_closed);
                    confirmPassword.setTransformationMethod(new PasswordTransformationMethod());
                }else {
                    ConfirmpasswordEye.setImageResource(R.drawable.ic_eye);
                    confirmPassword.setTransformationMethod(null);
                }
                confirmPassword.setSelection(confirmPassword.getText().length());
                ReisEyeOpen=!ReisEyeOpen;
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firstName.getText().length()<1){
                    firstName.setError("Enter first name");
                    return;
                }
                if(lastName.getText().length()<1){
                    lastName.setError("Enter last name");
                    return;
                }
                if(mobileNo.getText().length()!=10){
                    mobileNo.setError("Enter correct phone number");
                    return;
                }
                if(email.getText().length()<1){
                    email.setError("Enter email name");
                    return;
                }
                if(password.getText().length()<4){
                    password.setError("Enter password");
                    return;
                }
                if(confirmPassword.getText().length()<4){
                    confirmPassword.setError("Confirm password");
                    return;
                }

                if (adharNumber.getText().length()!=12){
                    adharNumber.setError("Please enter correct aadhar number");
                    return;
                }
                if (vehicleNumber.getText().length()<=1){
                    vehicleNumber.setError("Please enter correct vehicle number");
                    return;
                }
                if (dlNUmber.getText().length()<=1){
                    dlNUmber.setError(getString(R.string.ENTER_CORRECT_DRIVING_LICENCE_NUM));
                    return;
                }

                if (password.getText().toString().equalsIgnoreCase(confirmPassword.getText().toString())){
                    if (isValidEmail(email.getText().toString())){
                        updateDatabase();
                    }else {
                        email.setError("Email is not correct");
                    }
                }else {
                    password.setError("Password not matched");
                    confirmPassword.setError("Password not matched");
                }

            }
        });
    }


    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void updateDatabase() {

        mProgressBar.setVisibility(View.VISIBLE);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()){
                            String ServerURL = getResources().getString(R.string.pneck_app_url) + "/EmployeeRegister";
                            HashMap<String, String> dataParams = new HashMap<String, String>();

                            dataParams.put("first_name", firstName.getText().toString());
                            dataParams.put("last_name", lastName.getText().toString());
                            dataParams.put("email", email.getText().toString());
                            dataParams.put("type_user", userType);
                            dataParams.put("mobile",mobileNo.getText().toString());
                            dataParams.put("password",password.getText().toString());
                            dataParams.put("c_password",confirmPassword.getText().toString());
                            dataParams.put("aadhar_number",adharNumber.getText().toString());
                            dataParams.put("vehicle_number",vehicleNumber.getText().toString());
                            dataParams.put("dl_number",dlNUmber.getText().toString());
                            dataParams.put("device_token", task.getResult().getToken());
                            dataParams.put("device_id", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

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
                            Volley.newRequestQueue(EmployeeSignUpScreen.this).add(dataParamsJsonReq);
                        }
                    }
                });

    }


    private Response.Listener<JSONObject> RegistrationSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("user_registration", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {
                        sessionManager.setPhoneAndPass(mobileNo.getText().toString(),password.getText().toString());
                        mProgressBar.setVisibility(View.GONE);
                        Bundle bundle=new Bundle();
                        bundle.putString("mobile_no",mobileNo.getText().toString());
                        LaunchActivityClass.LaunchOTPActivity(EmployeeSignUpScreen.this,bundle);
                    }else {
                        String msg=innerResponse.getString("message");
                        Toast.makeText(EmployeeSignUpScreen.this,msg,Toast.LENGTH_SHORT).show();
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
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), R.string.SOMETHING_WENT_WRONG, Toast.LENGTH_LONG).show();
                Log.v("user_registration", "inside error block  " + error.getMessage());
            }
        };
    }



    private void showForgotPassScreen(){
        final AlertDialog d = new AlertDialog.Builder(this)
                .create();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vw = inflater.inflate(R.layout.layout_user_type_selection,null);
        d.setView(vw);
        d.setCancelable(false);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window win = d.getWindow();
        if (win!=null){
            win.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            //win.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        }

        vw.findViewById(R.id.btn_delivery_boy).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                userType = "1";
                d.dismiss();
            }
        });
        vw.findViewById(R.id.btn_driver).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                userType = "2";
                d.dismiss();
            }
        });


        d.show();
    }


}
