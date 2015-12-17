package com.parse.starter.letshangout.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.starter.R;
import com.parse.starter.letshangout.dto.User;
import com.parse.starter.letshangout.utils.ContactsCompletionView;
import com.parse.starter.letshangout.utils.GooglePlacesAutocompleteAdapter;
import com.parse.starter.letshangout.utils.PlaceAutocompleteAdapter;
import com.parse.starter.letshangout.utils.WhereCompletionView;
import com.tokenautocomplete.FilteredArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NewInvitationActivity extends AppCompatActivity{

    private static final String TAG = "NewInvitationActivity";

    // widgets
    private EditText editText_what;
    private EditText editText_date;
    private EditText editText_time;
    private ContactsCompletionView contactsCompletionView_who;
    private AutoCompleteTextView autoCompleteTextView_where;
    private AutoCompleteTextView autoCompleteTextView_where2;
    private WhereCompletionView whereCompletionView_where;
    private Button inviteButton;

    // data


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_invitation);

        // set listeners
        setDatePicker();
        setTimePicker();
        setFriendsAutocompleteTextView();
        setWhereAutocompleteTextView3(); // todo cleanup

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

                String myFormat = "EEE M/d/yy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

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

                String myFormat = "h:mm a";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

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


    /**
     * resource:
     *  http://examples.javacodegeeks.com/android/android-google-places-autocomplete-api-example/
     */
    private void setWhereAutocompleteTextView()
    {
        autoCompleteTextView_where = getAutoCompleteTextView_where();

        autoCompleteTextView_where.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_where_item));
        autoCompleteTextView_where.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long id) {
                String str = (String) adapterView.getItemAtPosition(position);
                Toast.makeText(NewInvitationActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setWhereAutocompleteTextView2()
    {
        autoCompleteTextView_where2 = getAutoCompleteTextView_where2();

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */,
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
                new PlaceAutocompleteAdapter(this, mGoogleApiClient, null,
                null);

        autoCompleteTextView_where2.setAdapter(mAdapter);
    }

    private void setWhereAutocompleteTextView3()
    {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
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
                new PlaceAutocompleteAdapter(this, mGoogleApiClient, null,
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

    private AutoCompleteTextView getAutoCompleteTextView_where() {
        return (autoCompleteTextView_where == null)?
                (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView_where):
                autoCompleteTextView_where;
    }

    private AutoCompleteTextView getAutoCompleteTextView_where2() {
        return (autoCompleteTextView_where2 == null)?
                (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView_where2):
                autoCompleteTextView_where2;
    }

    private WhereCompletionView getWhereCompletionView_where() {
        return (whereCompletionView_where == null)?
                (WhereCompletionView)findViewById(R.id.whereCompletionView_where):
                whereCompletionView_where;
    }

    private Button getInviteButton() {
        return (inviteButton == null)?(Button)findViewById(R.id.inviteButton):inviteButton;
    }
}
