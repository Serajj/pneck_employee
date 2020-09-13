package com.pneck.employee.Activities.OrderProcess;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.pneck.employee.PublicMethod;
import com.pneck.employee.R;
import com.pneck.employee.Requests.CustomRequest;
import com.pneck.employee.Requests.JsonUTF8Request;
import com.pneck.employee.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class JobDetailAddByEmployee extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout top;
    private TextView customerNo;
    private TextView customerName;
    private EditText editJobMessage;
    private AppCompatButton submitJobDetailBtn;
    private SessionManager sessionManager;
    private ProgressBar progressBar;
    private TextView doneText;

    private void findViews() {
        top = (LinearLayout)findViewById( R.id.top );
        customerNo = (TextView)findViewById( R.id.customer_no );
        customerName = (TextView)findViewById( R.id.customer_name );
        editJobMessage = (EditText)findViewById( R.id.edit_job_message );
        submitJobDetailBtn = (AppCompatButton)findViewById( R.id.submit_job_detail_btn );
        progressBar=findViewById(R.id.progress_bar);
        doneText=findViewById(R.id.done_button);

        submitJobDetailBtn.setOnClickListener( this );
    }

    @Override
    public void onClick(View v) {
        if ( v == submitJobDetailBtn ) {
            if (editJobMessage.getText().toString().length()>1){
                subitJobDetails();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail_add_by_employee);

        sessionManager=new SessionManager(JobDetailAddByEmployee.this);
        findViews();
        clickListeners();



        editJobMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editJobMessage.getText().length()>1){
                    doneText.setVisibility(View.VISIBLE);
                }else {
                    doneText.setVisibility(View.GONE);
                }
            }
        });
        customerName.setText(sessionManager.getCurrentOrderUserName());
        customerNo.setText(sessionManager.getCurrentOrderUserPhone());

    }

    private void clickListeners() {

        doneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PublicMethod.hideKeyboard(JobDetailAddByEmployee.this);
            }
        });

        top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", ""+sessionManager.getCurrentOrderUserPhone(), null));
                startActivity(intent);
            }
        });
    }

    private void subitJobDetails() {

        progressBar.setVisibility(View.VISIBLE);
        String ServerURL = getResources().getString(R.string.pneck_app_url) + "/empBookingOrderAdd";
        HashMap<String, String> dataParams = new HashMap<String, String>();


        dataParams.put("employee_id",sessionManager.getEmployeeId());
        dataParams.put("ep_token",sessionManager.getEmployeeToken());
        dataParams.put("ses_booking_id",sessionManager.getCurrentBookingOrderId());
        dataParams.put("order_info",editJobMessage.getText().toString());

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
        Volley.newRequestQueue(JobDetailAddByEmployee.this).add(dataParamsJsonReq);
    }


    private Response.Listener<JSONObject> RegistrationSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("order_info_save", "this is complete response " + response);
                    JSONObject innerResponse=response.getJSONObject("response");
                    if (innerResponse.getBoolean("success")) {

                        JSONObject data=innerResponse.getJSONObject("data");
                        sessionManager.saveOrderDetail(data.getString("booking_order_number"),
                                editJobMessage.getText().toString());
                        progressBar.setVisibility(View.GONE);
                        LaunchActivityClass.LaunchTrackingScreen(JobDetailAddByEmployee.this);

                    }else {
                        sessionManager.clearOrderSession();
                        Toast.makeText(JobDetailAddByEmployee.this,"Order canceled",Toast.LENGTH_SHORT).show();
                        LaunchActivityClass.LaunchMainActivity(JobDetailAddByEmployee.this);
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
    @Override
    public void onBackPressed() {
        Toast.makeText(JobDetailAddByEmployee.this,getString(R.string.YOU_ARE_IN_MIDDLE_OF_PROCESS),Toast.LENGTH_SHORT).show();
    }
}
