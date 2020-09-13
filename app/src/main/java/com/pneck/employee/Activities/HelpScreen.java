package com.pneck.employee.Activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.pneck.employee.R;
import com.pneck.employee.SessionManager;

public class HelpScreen extends AppCompatActivity {

    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_screen);

        findViewById(R.id.call_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callPneckNumber();
            }
        });
        findViewById(R.id.email_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMailToUs();
            }
        });
        findViewById(R.id.go_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        sessionManager=new SessionManager(HelpScreen.this);
        findViewById(R.id.drop_a_mail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMailToUs();
            }
        });
    }

    private void callPneckNumber(){
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", getResources().getString(R.string.PNECK_SUPPORT_NUMBER), null));
        startActivity(intent);
    }

    private void sendMailToUs(){
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse("mailto:" + getResources().getString(R.string.PNECK_SUPPORT_EMAIL)));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Pneck Partner Concern");
            intent.putExtra(Intent.EXTRA_TEXT, "Hi Pneck, \n\n\nThanks and Regards\n"+sessionManager.getUserFirstName()+"\n"+
                    sessionManager.getUserPhone());
            startActivity(intent);
        }catch(ActivityNotFoundException e){
            Toast.makeText(HelpScreen.this,"Please install email capability application.", Toast.LENGTH_SHORT).show();
        }
    }
}
