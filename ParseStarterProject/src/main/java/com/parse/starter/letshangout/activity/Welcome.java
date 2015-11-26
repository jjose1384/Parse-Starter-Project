package com.parse.starter.letshangout.activity;

import com.parse.ParseUser;
import com.parse.starter.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Welcome extends Activity {

    // Declare Variable
    Button logout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the view from singleitemview.xml
        setContentView(R.layout.welcome);

        // Locate TextView in welcome.xml
        TextView txtuser = (TextView) findViewById(R.id.txtuser);

        // Set the currentUser String into TextView
        Intent intent = getIntent();
        String action = intent.getStringExtra("action");
        if ("signup".equals(action))
        {
            txtuser.setText("An email has been sent to your account. " +
                    "Please follow the instructions in the email to complete " +
                    "the signup process!");
        }
        else
        {
            txtuser.setText("You are logged in as " + ParseUser.getCurrentUser().getUsername().toString());
        }

        // Locate Button in welcome.xml
        logout = (Button) findViewById(R.id.logout);

        // Logout Button Click Listener
        logout.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                // Logout current user
                ParseUser.logOut();
                finish();
            }
        });
    }
}