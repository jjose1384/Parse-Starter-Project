package com.parse.starter.letshangout.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.starter.R;
import com.parse.starter.letshangout.dto.User;
import com.parse.starter.letshangout.utils.ContactsCompletionView;
import com.parse.starter.letshangout.utils.PlaceAutocompleteAdapter;
import com.parse.starter.letshangout.utils.Utils;
import com.parse.starter.letshangout.utils.WhereCompletionView;
import com.tokenautocomplete.FilteredArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewInvitationActivity extends AppCompatActivity{

    private static final String TAG = "NewInvitationAct.";

    // widgets
    private EditText editText_what;
    private EditText editText_date;
    private EditText editText_time;
    private ContactsCompletionView contactsCompletionView_who;
    private WhereCompletionView whereCompletionView_where;
    private Button inviteButton;
    private Button cancelButton;

    // data
    private GoogleApiClient _mGoogleApiClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_invitation);

        // set listeners
        cancelButtonListener();
        inviteButtonListener();

        setDatePicker();
        setTimePicker();
        setFriendsAutocompleteTextView();
        setWhereAutocompleteTextView();

    }

    /**
     * resource: http://stackoverflow.com/questions/14933330/datepicker-how-to-popup-datepicker-when-click-on-edittext
     */
    private void setDatePicker()
    {
        final Calendar myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            // when date is set it datePickerDialog, the value in the date field will be set
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat(Utils.DATE_FORMAT, Locale.US);

                editText_date.setText(sdf.format(myCalendar.getTime()));
            }
        };


        // datePicker edit text field
        editText_date = getEditText_date();
        editText_date.setOnClickListener(new View.OnClickListener() {
            // popup datepicker when field is clicked
            public void onClick(View arg0) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(NewInvitationActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));

                // only display calendar view
                datePickerDialog.getDatePicker().setCalendarViewShown(true);
                datePickerDialog.getDatePicker().setSpinnersShown(false);

                datePickerDialog.show();
            }
        });
    }

    /**
     * resource: http://stackoverflow.com/questions/14933330/datepicker-how-to-popup-datepicker-when-click-on-edittext
     */
    private void setTimePicker()
    {
        final Calendar myCalendar = Calendar.getInstance();

        final TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                myCalendar.set(Calendar.HOUR, hourOfDay);
                myCalendar.set(Calendar.MINUTE, minute);

                SimpleDateFormat sdf = new SimpleDateFormat(Utils.TIME_FORMAT, Locale.US);

                editText_time.setText(sdf.format(myCalendar.getTime()));
            }
        };

        // timePicker edit text field
        editText_time = getEditText_time();
        editText_time.setOnClickListener(new View.OnClickListener() {
            // popup timePicker when field is clicked
            public void onClick(View arg0) {
                new TimePickerDialog(NewInvitationActivity.this, time, myCalendar.get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE), false).show();
            }
        });
    }

    /**
     * resource: https://github.com/splitwise/TokenAutoComplete
     */
    private void setFriendsAutocompleteTextView()
    {
        final ArrayList<User> users = new ArrayList<>();
        final ArrayAdapter<User> userArrayAdapter;

        userArrayAdapter = new FilteredArrayAdapter<User>(this, R.layout.user_layout, users) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {

                    LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = l.inflate(R.layout.user_layout, parent, false);
                }

                User user = getItem(position);
                ((TextView)convertView.findViewById(R.id.name)).setText(user.getName());
                ((TextView)convertView.findViewById(R.id.email)).setText(user.getEmail());

                return convertView;
            }

            @Override
            protected boolean keepObject(User user, String mask) {
                mask = mask.toLowerCase();
                return user.getName().toLowerCase().startsWith(mask)
                        || user.getEmail().toLowerCase().startsWith(mask);
            }
        };

        contactsCompletionView_who = getContactsCompletionView_who();
        contactsCompletionView_who.setAdapter(userArrayAdapter);
        contactsCompletionView_who.setThreshold(1); // number of characters to start showing suggestions
        contactsCompletionView_who.allowDuplicates(false);

        // load friends and set data in adapter
        retrieveFriends(users, userArrayAdapter);
    }

    private void retrieveFriends(final ArrayList<User> users, final ArrayAdapter<User> userArrayAdapter)
    {
        // load fiends
        ParseQuery<ParseObject> query = ParseQuery.getQuery("FriendLookup");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.include("friend"); // include the friend object pointer

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> friendLookupList, ParseException e) {
                if (e == null) {

                    for (ParseObject friendLookup : friendLookupList) {
                        ParseObject parseObjectUser = friendLookup.getParseObject("friend");
                        User user = new User(parseObjectUser.getString("username"), parseObjectUser.getString("email"));
                        users.add(user);
                        userArrayAdapter.notifyDataSetChanged();
                    }
                } else {
                    Log.d("FriendLookup", "Error: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_invitation, menu);
        return true;
    }

    private void setWhereAutocompleteTextView()
    {
        _mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 1 /* TODO - figure out what this is */,
                        new GoogleApiClient.OnConnectionFailedListener()
                        {
                            /**
                             * Called when the Activity could not connect to Google Play services and the auto manager
                             * could resolve the error automatically.
                             * In this case the API is not available and notify the user.
                             *
                             * @param connectionResult can be inspected to determine the cause of the failure
                             */
                            @Override
                            public void onConnectionFailed(ConnectionResult connectionResult) {

                                Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                                        + connectionResult.getErrorCode());

                                // TODO(Developer): Check error code and notify the user of error state and resolution.
                                Toast.makeText(NewInvitationActivity.this,
                                        "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                .addApi(Places.GEO_DATA_API)
                .build();
        PlaceAutocompleteAdapter mAdapter =
                new PlaceAutocompleteAdapter(this, _mGoogleApiClient, null,
                        null);

        whereCompletionView_where = getWhereCompletionView_where();
        whereCompletionView_where.setAdapter(mAdapter);
        whereCompletionView_where.setThreshold(1); // number of characters to start showing suggestions
        whereCompletionView_where.allowDuplicates(false);
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

    private void cancelButtonListener()
    {
        // cancel button - navigate back to the invitation list screen
        cancelButton = getCancelButton();
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                Intent intent = new Intent(NewInvitationActivity.this, InvitationListActivity.class);
                startActivity(intent);

                finish();
            }
        });
    }


    /**
     * TODO:
     *      - Need the invitation and where records to be in one transaction, don't know how to do that
     *      - Some google places records and invitiee records don't get saved sometimes
     *      - look at saveAllinBackground()
     *          - http://stackoverflow.com/questions/26768156/transaction-management-in-parse-com
     */
    private void inviteButtonListener()
    {
        // send invitation button
        inviteButton = getInviteButton();
        inviteButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                try {


                    String what = getEditText_what().getText().toString();
                    String date = getEditText_date().getText().toString();
                    String time = getEditText_time().getText().toString();
                    List<User> submittedUsers = getContactsCompletionView_who().getObjects();
                    List<Object> submittedPlaces = getWhereCompletionView_where().getObjects();


                    // create new Invitation record
                    final ParseObject invitation = new ParseObject("Invitation");
                    invitation.put("title", what);
                    invitation.put("sender", ParseUser.getCurrentUser());
                    Date eventTime = Utils.formatDate(date + " " + time);
                    invitation.put("eventTime", eventTime);
                    invitation.setACL(Utils.getPublicReadPrivateWriteACL(ParseUser.getCurrentUser()));
                    invitation.saveInBackground(); // save the invitation object


                    //
                    // add sender should also get an invitee record
                    //
                    ParseObject inviteeLookupForSender = new ParseObject("InviteeLookup");
                    inviteeLookupForSender.put("invitation", invitation);
                    inviteeLookupForSender.put("invitee", ParseUser.getCurrentUser());

                    // accepted is string not boolean, because boolean doesn't allow
                    // for null values
                    inviteeLookupForSender.put("accepted", "true"); // sender is going by default
                    inviteeLookupForSender.
                            setACL(Utils.getPublicReadPrivateWriteACL(ParseUser.getCurrentUser())); // acl with public read, write only by receiver

                    inviteeLookupForSender.saveInBackground();
                    // create a InviteeLookup record for each of the submitted guests
                    for (User user: submittedUsers)
                    {
                        // retrieve invitee details to add to invitation
                        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery("_User");
                        userQuery.whereEqualTo("email", user.getEmail());
                        List<ParseUser> parseUsers = userQuery.find();
                        // TODO - email addresses are unique, so there should be exactly one entry with a particular email
                        // if email isn't found, there should be a way to send email notification instead of
                        // let's hang out
                        ParseUser parseUser = parseUsers.get(0);

                        //
                        // add invitee record
                        //
                        ParseObject inviteeLookup = new ParseObject("InviteeLookup");
                        inviteeLookup.put("invitation", invitation);
                        inviteeLookup.put("invitee", parseUser);
                        inviteeLookup.setACL(Utils.getPublicReadPrivateWriteACL(parseUser)); // acl with public read, write only by receiver
                        inviteeLookup.saveInBackground();
                    }

                    // create a Where record for each of the submitted places
                    for (Object place: submittedPlaces)
                    {
                        // TODO - all this should only happen for a google place
                        //        for all places only name should be added
                        AutocompletePrediction googlePlace = (AutocompletePrediction) place;
                        Places.GeoDataApi.getPlaceById(_mGoogleApiClient, googlePlace.getPlaceId())
                                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                    @Override
                                    public void onResult(PlaceBuffer places) {
                                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                            final Place myPlace = places.get(0);

                                            //
                                            // add place record
                                            //
                                            ParseObject where = new ParseObject("Where");
                                            where.put("name", myPlace.getName());
                                            where.put("address", myPlace.getAddress());
                                            where.put("googlePlaceId", myPlace.getId());
                                            where.put("invitation", invitation);
                                            where.setACL(Utils.getPublicReadPrivateWriteACL(ParseUser.getCurrentUser()));
                                            where.saveInBackground();

                                            Log.i(TAG, "Place found: " + myPlace.getName());
                                        } else {
                                            Log.e(TAG, "Place not found");
                                        } places.release();
                                    }
                                });
                    }


                    System.out.println("What: " + what);
                    System.out.println("When: " + date + " " + time);
                    System.out.println("Submitted Users: " + submittedUsers);
                    System.out.println("Submitted Places: " + submittedPlaces);

                    // TODO - notify listadapter, so list of invitations will be updated(not working)
                    InvitationListActivity.notifyInvitationsListAdapter();

                    // navigate back to the invitation list screen
                    Intent intent = new Intent(NewInvitationActivity.this, InvitationListActivity.class);
                    startActivity(intent);

                    // TODO - may cause data to not be saved, so not calling finish()
                    // finish();

                }
                catch (ArrayIndexOutOfBoundsException e)
                {
                    // user with email not found
                    // TODO - send an email notification
                }
                catch (ParseException e)
                {
                    Log.e(TAG, "Error: " + e.getMessage());
                }
            }
        });
    }

    /**
     *
     * private accessors for widgets
     */
    private EditText getEditText_what() {
        return (editText_what == null) ? (EditText)findViewById(R.id.editText_what):editText_what;
    }

    private EditText getEditText_date() {
        return (editText_date == null)?(EditText)findViewById(R.id.editText_date):editText_date;
    }

    private EditText getEditText_time() {
        return (editText_time == null)?(EditText)findViewById(R.id.editText_time):editText_time;
    }

    private ContactsCompletionView getContactsCompletionView_who() {
        return (contactsCompletionView_who == null)?
                (ContactsCompletionView)findViewById(R.id.contactsCompletionView_who):
                contactsCompletionView_who;
    }

    private WhereCompletionView getWhereCompletionView_where() {
        return (whereCompletionView_where == null)?
                (WhereCompletionView)findViewById(R.id.whereCompletionView_where):
                whereCompletionView_where;
    }

    private Button getInviteButton() {
        return (inviteButton == null)?(Button)findViewById(R.id.inviteButton):inviteButton;
    }

    private Button getCancelButton() {
        return (cancelButton == null)?(Button)findViewById(R.id.cancelButton):cancelButton;
    }
}
