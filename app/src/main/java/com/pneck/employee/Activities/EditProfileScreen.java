package com.pneck.employee.Activities;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pneck.employee.Const;
import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.MultimediaUpload.AndroidMultiPartEntity;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;
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

public class EditProfileScreen extends AppCompatActivity {

    private LinearLayout toolbar;
    private ImageView goBack;
    private TextInputEditText firstName;
    private TextInputEditText lastName;
    private TextInputEditText mobileNo;
    private TextInputEditText email;
    private TextInputEditText address;
    private ProgressBar progressBar;
    private MaterialRippleLayout updateBtn;
    private SessionManager sessionManager;
    private Spinner genderSelect;
    private RelativeLayout PickImage;
    private CircleImageView UserProfile;
    private File FileName;

    private String[] gender = {"Male", "Female", "Other"};

    private void findViews() {
        toolbar = (LinearLayout)findViewById( R.id.toolbar );
        goBack = (ImageView)findViewById( R.id.go_back );
        firstName = (TextInputEditText)findViewById( R.id.first_name );
        lastName = (TextInputEditText)findViewById( R.id.last_name );
        mobileNo = (TextInputEditText)findViewById( R.id.mobile_no );
        email = (TextInputEditText)findViewById( R.id.email );
        genderSelect = (Spinner) findViewById( R.id.select_gender );
        address = (TextInputEditText)findViewById( R.id.address );
        progressBar = (ProgressBar)findViewById( R.id.progress_bar );
        updateBtn = (MaterialRippleLayout)findViewById( R.id.update_btn );
        PickImage=findViewById(R.id.pick_image);
        UserProfile=findViewById(R.id.user_image);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_screen);
        findViews();

        clickListeners();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(EditProfileScreen.this, R.layout.support_simple_spinner_dropdown_item,
                gender);
        genderSelect.setAdapter(arrayAdapter);

        sessionManager=new SessionManager(EditProfileScreen.this);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserProfileDetail();
            }
        });
        getProfileDetails();


        if (sessionManager.getUserImage().length() >= 3) {
            Glide.with(EditProfileScreen.this)
                    .load(sessionManager.getUserImage())
                    .placeholder(R.drawable.ic_account)
                    .error(R.drawable.ic_account)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .dontAnimate()
                    .into(UserProfile);
        }

        Log.e("kjdhfssfsfs","this is image "+sessionManager.getUserImage());
    }

    @Override
    public void onBackPressed() {
        LaunchActivityClass.LaunchMainActivity(EditProfileScreen.this);
    }

    private void clickListeners() {
        PickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker();
            }
        });

    }
    private void ImagePicker() {
        CropImage.activity().setAspectRatio(1, 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(EditProfileScreen.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri imageUri = result.getUri();

                    Glide.with(EditProfileScreen.this)
                            .load(result.getUri())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(UserProfile);
                    FileName = new File(result.getUri().getPath());
                    Log.e("editprofile", "before compression FileName " + FileName);
                    Bitmap picBitmap = BitmapFactory.decodeFile(result.getUri().getPath());
                    FileName = saveImage(picBitmap);


                    new UploadUserImg().execute();

                    //UploadImageToFirebase(imageUri);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
                break;
        }
    }


    private class UploadUserImg extends AsyncTask<Void, Integer, String> {

        public UploadUserImg() {

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected String doInBackground(Void... params) {


            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(getResources().getString(R.string.pneck_app_url) + "/ImageUpload");

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {
                            @Override
                            public void transferred(long num) {

                            }
                        });

                if (FileName != null) {
                    entity.addPart("image", new FileBody(FileName));
                }

                Log.e("editprofile", "file name is " + FileName);
                entity.addPart("employee_id", new StringBody(sessionManager.getEmployeeId()));
                entity.addPart("ep_token", new StringBody(sessionManager.getEmployeeToken()));
                httppost.setEntity(entity);

                Log.e("kdhjsfksd","employee_id "+sessionManager.getEmployeeId()+" employee token "+sessionManager.getEmployeeToken());

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
            Log.e("editprofile", "responseString " + responseString);
            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {

            Log.e("kdhjsfksd", "image upload result " + result);
            try {

                JSONObject object=new JSONObject(result);
                JSONObject data=object.getJSONObject("data");
                if (data.has("profile_image")){
                    sessionManager.saveUserImage(data.getString("profile_image"));
                    Toast.makeText(EditProfileScreen.this,getString(R.string.IMAGE_UPDATED_SUCCESSFULLY),Toast.LENGTH_SHORT).show();
                }
                Log.e("kndfjdkhsdfs","this is response "+result);

            } catch (Exception e) {
                Log.e("editprofile", "this is exception error " + e.getMessage());
            }

            super.onPostExecute(result);
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


    private void getProfileDetails() {

        progressBar.setVisibility(View.VISIBLE);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/ShowEmployeeProfile";
        HashMap<String, String> dataParams = new HashMap<String, String>();

        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());

        Log.e("user_showProfile", "this is url " +ServerURL);

        Log.e("user_showProfile", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                ShowSuccess(),
                ShowError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(EditProfileScreen.this).add(dataParamsJsonReq);
    }

    private Response.Listener<JSONObject> ShowSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("user_showProfile", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {
                        progressBar.setVisibility(View.GONE);
                        JSONObject data=innerResponse.getJSONObject("data");

                        firstName.setText(data.getString("fname"));
                        lastName.setText(data.getString("last_name"));
                        email.setText(data.getString("email"));
                        mobileNo.setText(data.getString("mobile"));
                        address.setText(data.getString("emp_address"));
                        int i=0;
                        for (i=0;i<gender.length-1;i++){
                            if (data.getString("emp_gender").equalsIgnoreCase(gender[i])){
                                break;
                            }
                        }
                        genderSelect.setSelection(i);
                    }

                } catch (Exception e) {
                    Log.v("user_showProfile", "inside catch block  " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }

    private Response.ErrorListener ShowError() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), R.string.SOMETHING_WENT_WRONG+error.getMessage(), Toast.LENGTH_LONG).show();
                Log.v("user_showProfile", "inside error block  " + error.getMessage());
            }
        };
    }


    private void saveUserProfileDetail() {

        progressBar.setVisibility(View.VISIBLE);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/EditEmployeeProfile";
        HashMap<String, String> dataParams = new HashMap<String, String>();


        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());
        dataParams.put("first_name",firstName.getText().toString());
        dataParams.put("last_name",lastName.getText().toString());
        dataParams.put("emp_gender",gender[genderSelect.getSelectedItemPosition()]);
        dataParams.put("emp_address",address.getText().toString());

        Log.e("user_showProfile", "this is url " +ServerURL);

        Log.e("user_showProfile", "this is we sending " + dataParams.toString());

        CustomRequest dataParamsJsonReq = new CustomRequest(JsonUTF8Request.Method.POST,
                ServerURL,
                dataParams,
                saveSuccess(),
                saveError());
        dataParamsJsonReq.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(Const.VOLLEY_RETRY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(EditProfileScreen.this).add(dataParamsJsonReq);
    }

    private Response.Listener<JSONObject> saveSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("user_showProfile", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {
                        progressBar.setVisibility(View.GONE);
                        JSONObject data=innerResponse.getJSONObject("data");
                        Toast.makeText(EditProfileScreen.this,"Profile updated successfully",Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } catch (Exception e) {
                    Log.v("user_showProfile", "inside catch block  " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }

    private Response.ErrorListener saveError() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), R.string.SOMETHING_WENT_WRONG+error.getMessage(), Toast.LENGTH_LONG).show();
                Log.v("user_showProfile", "inside error block  " + error.getMessage());
            }
        };
    }
}
