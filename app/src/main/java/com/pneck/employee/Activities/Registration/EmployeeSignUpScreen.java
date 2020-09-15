package com.pneck.employee.Activities.Registration;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.pneck.employee.Activities.EditProfileScreen;
import com.pneck.employee.Const;
import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.MultimediaUpload.AndroidMultiPartEntity;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;
import com.pneck.employee.utills.Tools;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

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
    private CircleImageView UserProfile;
    private RelativeLayout PickImage;
    private ImageView passWardEye,ConfirmpasswordEye;
    private File FileName;

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
        UserProfile=findViewById(R.id.user_image);

        PickImage=findViewById(R.id.pick_image);
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

        PickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker();
            }
        });
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
//                        updateDatabase();
                        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                              if(task.isSuccessful())
                              {
                                  new UploadUserImg(task.getResult().getToken()).execute();
                              }
                            }
                        });


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

    private void ImagePicker() {
        CropImage.activity().setAspectRatio(1, 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(EmployeeSignUpScreen.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri imageUri = result.getUri();

                    Glide.with(EmployeeSignUpScreen.this)
                            .load(result.getUri())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(UserProfile);
                    FileName = new File(result.getUri().getPath());
                    Log.e("editprofile", "before compression FileName " + FileName);
                    Bitmap picBitmap = BitmapFactory.decodeFile(result.getUri().getPath());
                    FileName = saveImage(picBitmap);


                    //UploadImageToFirebase(imageUri);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
                break;
        }
    }
    private File saveImage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("pneck_image", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "compress_img.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);

            // Use the compress method on the BitMap object to write image to the OutputStream
            int quality;
            quality = 20;

            bitmapImage.compress(Bitmap.CompressFormat.JPEG, quality, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath;
    }


   //-----------Uploading data with image
   private class UploadUserImg extends AsyncTask<Void, Integer, String> {

        private String deviceToken;
       public UploadUserImg(String token) {
this.deviceToken=token;
       }

       @Override
       protected void onPreExecute() {
           mProgressBar.setVisibility(View.VISIBLE);
       }

       @Override
       protected void onProgressUpdate(Integer... progress) {

       }

       @Override
       protected String doInBackground(Void... params) {


           return uploadFile(deviceToken);
       }



       @SuppressWarnings("deprecation")
       private String uploadFile(String deviceToken) {


           String responseString = null;


           HttpClient httpclient = new DefaultHttpClient();
           HttpPost httppost = new HttpPost(getResources().getString(R.string.pneck_app_url) + "/EmployeeRegister");

           try {
               AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                       new AndroidMultiPartEntity.ProgressListener() {
                           @Override
                           public void transferred(long num) {

                           }
                       });

               if (FileName != null) {
                   entity.addPart("vehicle_image", new FileBody(FileName));
               }


              entity.addPart("first_name", new StringBody(firstName.getText().toString()));
              entity.addPart("last_name", new StringBody(lastName.getText().toString()));
              entity.addPart("email", new StringBody(email.getText().toString()));
              entity.addPart("type_user", new StringBody(userType));
              entity.addPart("mobile",new StringBody(mobileNo.getText().toString()));
              entity.addPart("password",new StringBody(password.getText().toString()));
              entity.addPart("c_password",new StringBody(confirmPassword.getText().toString()));
              entity.addPart("aadhar_number",new StringBody(adharNumber.getText().toString()));
              entity.addPart("vehicle_number",new StringBody(vehicleNumber.getText().toString()));
              entity.addPart("dl_number",new StringBody(dlNUmber.getText().toString()));
              entity.addPart("device_token", new StringBody(deviceToken));
              entity.addPart("device_id", new StringBody(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)));

               httppost.setEntity(entity);


               // Making server call
               HttpResponse response = httpclient.execute(httppost);
               HttpEntity r_entity = response.getEntity();

               int statusCode = response.getStatusLine().getStatusCode();
               if (statusCode == 200) {
                   // Server response
                   responseString = EntityUtils.toString(r_entity);
               } else {
                   responseString = "Error occurred! Http Status Code: "
                           + statusCode;
               }

           } catch (ClientProtocolException e) {
               responseString = e.toString();
           } catch (IOException e) {
               responseString = e.toString();
           }
           Log.e("response", "responseString " + responseString);
           return responseString;

       }

       @Override
       protected void onPostExecute(String result) {

           //super.onPostExecute(result);

           try {
               JSONObject response=new JSONObject(result);
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

   }
}
