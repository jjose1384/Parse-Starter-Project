package com.parse.starter.letshangout.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.parse.starter.R;
import com.parse.starter.letshangout.utils.Validation;

import java.util.List;

/**
 * resource: http://www.androidbegin.com/tutorial/android-parse-com-simple-login-and-signup-tutorial/
 */
public class LoginSignupActivity extends Activity {
    // Declare Variables
    Button loginbutton;
    Button signup;
    EditText password;
    EditText email;
    TextView forgotPasswordLink;

    String emailtxt;
    String passwordtxt;

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the view from main.xml
        setContentView(R.layout.activity_login_signup);

        // Locate EditTexts in main.xml
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        // Locate Buttons and links in main.xml
        loginbutton = (Button) findViewById(R.id.login);
        signup = (Button) findViewById(R.id.signup);
        forgotPasswordLink = (TextView) findViewById(R.id.txtForgotPassword);

        // Login Button Click Listener
        loginbutton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                // Retrieve the text entered from the EditText
                emailtxt = email.getText().toString();
                passwordtxt = password.getText().toString();

                ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
                query.whereEqualTo("username", emailtxt);
                query.whereEqualTo("emailVerified", true);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> userList, ParseException e) {
                        if (e == null) {
                            if (userList.size() == 1) // user has a verified emailAddress
                            {
                                // Send data to Parse.com for verification
                                ParseUser.logInInBackground(emailtxt, passwordtxt,
                                        new LogInCallback() {
                                            public void done(ParseUser user, ParseException e) {
                                                if (user != null) {
                                                    // If user exist and authenticated, send user to Welcome.class
                                                    Intent intent = new Intent(
                                                            LoginSignupActivity.this,
                                                            InvitationListActivity.class);
                                                    startActivity(intent);
                                                    intent.putExtra("action", "login");
                                                    Toast.makeText(getApplicationContext(),
                                                            "Successfully Logged in",
                                                            Toast.LENGTH_LONG).show();

                                                    finish();
                                                } else {
                                                    Toast.makeText(
                                                            getApplicationContext(),
                                                            "No such user exist, please signup",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                            } else // verified email not found
                            {
                                Toast.makeText(
                                        getApplicationContext(),
                                        "No such user exist, please signup",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.d("user", "Error: " + e.getMessage());
                        }
                    }
                });
            }
        });

        // Sign up Button Click Listener
        signup.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                // Retrieve the text entered from the EditText
                emailtxt = email.getText().toString();
                passwordtxt = password.getText().toString();

                // Force user to fill up the form
                if (emailtxt.equals("") || passwordtxt.equals("")) // both are required
                {
                    Toast.makeText(getApplicationContext(),
                            "Please complete the sign up form",
                            Toast.LENGTH_LONG).show();

                } else {
                    String emailValidation = Validation.validateEmail(emailtxt);
                    String passwordValidation = Validation.validatePassword(passwordtxt);
                    if (emailValidation != null) // email validation failed
                    {
                        Toast.makeText(getApplicationContext(),
                                emailValidation, Toast.LENGTH_LONG)
                                .show();
                    } else if (passwordValidation != null) // password validation failed
                    {
                        Toast.makeText(getApplicationContext(),
                                passwordValidation, Toast.LENGTH_LONG)
                                .show();
                    } else {


                        // Save new user data into Parse.com Data Storage
                        ParseUser user = new ParseUser();
                        user.setUsername(emailtxt); // use the submitted email as the username
                        user.setEmail(emailtxt);
                        user.setPassword(passwordtxt);
                        user.signUpInBackground(new SignUpCallback() {
                            public void done(ParseException e) {
                                if (e == null) {
                                    Intent intent = new Intent(
                                            LoginSignupActivity.this,
                                            Welcome.class);
                                    intent.putExtra("action", "signup");
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Sign up Error", Toast.LENGTH_LONG)
                                            .show();
                                }
                            }
                        });
                    }
                }

            }
        });

        // Forgot Password Link Click Listener
        forgotPasswordLink.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                Intent intent = new Intent(
                        LoginSignupActivity.this,
                        ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
}