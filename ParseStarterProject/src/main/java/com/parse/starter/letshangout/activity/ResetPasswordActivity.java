package com.parse.starter.letshangout.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.starter.R;
import com.parse.starter.letshangout.utils.Validation;

// used to forward a link to your email to reset your password in case
// of lost password
public class ResetPasswordActivity extends AppCompatActivity {

    // widget Variables
    Button resetPasswordBtn;
    Button cancelBtn;
    EditText emailTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        resetPasswordBtn = (Button) findViewById(R.id.resetPasswordBtn);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        emailTxt = (EditText) findViewById(R.id.emailTxt);


        // Reset Password Button on Click Listener
        // will initiate the process to forward a link to your email
        // to reset password
        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                String emailTextString = emailTxt.getText().toString();
                // email validation failed
                if (emailTextString == null ||
                    emailTextString.equals("") ||
                    Validation.validateEmail(emailTextString) != null)
                {
                    Toast.makeText(getApplicationContext(),
                            "Please enter a valid email address.", Toast.LENGTH_LONG)
                            .show();
                }
                else // valid email was entered
                {
                    // generate password reset link
                    ParseUser.requestPasswordResetInBackground(emailTextString,
                            new RequestPasswordResetCallback() {
                                public void done(ParseException e) {
                                    if (e == null) {
                                        // An email was successfully sent with reset instructions.
                                        // display toast to inform user of reset link
                                        Toast.makeText(getApplicationContext(),
                                                "An email has been sent to your account with password reset link.", Toast.LENGTH_LONG)
                                                .show();

                                        // navigate back to login screen
                                        Intent intent = new Intent(
                                                ResetPasswordActivity.this,
                                                LoginSignupActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // display toast to inform user of reset link
                                        Toast.makeText(getApplicationContext(),
                                                "Please check the email you've entered.", Toast.LENGTH_LONG)
                                                .show();
                                    }
                                }
                            });
                }
            }
        });

        // Cancel Button on Click Listener
        // navigate back to the login screen
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                // navigate back to login screen
                Intent intent = new Intent(
                        ResetPasswordActivity.this,
                        LoginSignupActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reset_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
