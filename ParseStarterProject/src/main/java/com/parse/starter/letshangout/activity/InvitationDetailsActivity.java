package com.parse.starter.letshangout.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.starter.R;
import com.parse.starter.letshangout.utils.PlaceAutocompleteAdapter;
import com.parse.starter.letshangout.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvitationDetailsActivity extends AppCompatActivity {

    private static final String TAG = "InvitationDetailsAct.";

    // widgets
    private Button cancelButton;
    private Button responseButton;
    private Switch goingSwitch;
    private TextView whatValue;
    private TextView whenValue;
    private TextView whoValue;
    private RadioGroup whereRadioGroup;
    private AutoCompleteTextView autocompleteTextView_whereOther;

    // data
    private ParseObject _currentInvitation;
    private ParseObject _currentInviteeLookup;
    private Map<String, List<ParseObject>> _inviteeListMap = new HashMap<>(); // maps accepted, declined and waiting lists
    private Map<String, Integer> _whereCount = new HashMap<>(); // maps where object id to number of votes
    private List<ParseObject> _whereList = new ArrayList<>();

    // google autocomplete
    private GoogleApiClient _mGoogleApiClient;
    private PlaceAutocompleteAdapter _mAdapter;
    private String _suggestedPlaceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_details);

        // populating data
        Intent intent = getIntent();
        String invitationObjectId = intent.getStringExtra("invitationObjectId");
        populateInvitationDetails(invitationObjectId);

        // set listeners on buttons
        radioButtonChangeListener();
        goingSwitchChangeListener();
        cancelButtonListener();
        responseButtonListener();
        setWhereAutocompleteTextView();

    }


    private void cancelButtonListener()
    {
        // cancel button
        cancelButton = getCancelButton();
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                Intent intent = new Intent(InvitationDetailsActivity.this, InvitationListActivity.class);
                startActivity(intent);

                finish();
            }
        });
    }

    /**
     * enables or disables other editText based on whether the 'Other' radio option
     * is selected
     */
    private void radioButtonChangeListener()
    {
        // radio button group
        whereRadioGroup = getWhereRadioGroup();
        whereRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                autocompleteTextView_whereOther = getAutocompleteTextView_whereOther();

                // checkedId is the RadioButton selected
                if (checkedId == (group.getChildCount() - 1)) // 'Other' option is the last one in the group
                {
                    // 'other' is selected
                    enableDisableOtherEditText(true);
                } else {
                    // 'other' is not selected
                    enableDisableOtherEditText(false);
                }

            }
        });
    }

    /**
     *  saves the going response as well as the where selection if one has been made
     */
    private void responseButtonListener() {
        // response button
        responseButton = getResponseButton();
        responseButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                // refresh your InviteeLookup object and update it
                _currentInviteeLookup.fetchInBackground(new GetCallback<ParseObject>() {
                    public void done(ParseObject object, ParseException e) {
                        if (e == null) {
                            // update where selection if one has been selected, only if the goingSwitch is set to yes
                            goingSwitch = getGoingSwitch();
                            if (goingSwitch.isChecked()) {
                                _currentInviteeLookup.put("accepted", "true"); // set the going value

                                whereRadioGroup = getWhereRadioGroup();
                                int selectedRadioButtonIndex = whereRadioGroup.getCheckedRadioButtonId();

                                if (selectedRadioButtonIndex == (whereRadioGroup.getChildCount()-1))
                                // last radio button is selected, meaning the other option is selected
                                {
                                    /*
                                    Issue a request to the Places Geo Data API to retrieve a Place object with additional
                                    details about the place.
                                    TODO - This request for details may not be necessary. Could reduce total requests to google api
                                    */
                                    Places.GeoDataApi.getPlaceById(_mGoogleApiClient, _suggestedPlaceId)
                                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                        @Override
                                        public void onResult(PlaceBuffer places) {
                                            if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                                // get place details and save in variable to be used
                                                // when response is sent
                                                final Place suggestedPlace = places.get(0);

                                                // add the new place suggestion
                                                ParseObject where = new ParseObject("Where");
                                                where.put("name", suggestedPlace.getName());
                                                where.put("address", suggestedPlace.getAddress());
                                                where.put("googlePlaceId", suggestedPlace.getId());
                                                where.put("invitation", _currentInvitation);
                                                where.setACL(Utils.getPublicReadPrivateWriteACL(ParseUser.getCurrentUser()));
                                                where.saveInBackground();

                                                _currentInviteeLookup.put("whereSelection", where); // add the new where record as user's selection

                                                Log.i(TAG, "Place found: " + suggestedPlace.getName());
                                            } else {
                                                Log.e(TAG, "Place not found");
                                            }
                                            places.release();
                                        }
                                    });
                                }
                                else if (selectedRadioButtonIndex != -1) // -1 = no radio button selected
                                {
                                    _currentInviteeLookup.put("whereSelection", _whereList.get(selectedRadioButtonIndex));
                                }

                                else
                                {
                                    _currentInviteeLookup.remove("whereSelection"); // remove selection
                                }
                            }
                            else
                            {
                                // not going, therefore update the accepted value and remove the where selection
                                // if there was one
                                _currentInviteeLookup.put("accepted", "false");
                                _currentInviteeLookup.remove("whereSelection");
                            }

                            _currentInviteeLookup.saveInBackground();

                            // navigate back to the invitations list screen
                            Intent intent = new Intent(InvitationDetailsActivity.this, InvitationListActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            // Failure!
                        }

                    }
                });

            }
        });
    }

    private void goingSwitchChangeListener() {
        // going switch
        goingSwitch = getGoingSwitch();
        goingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // enable or disable where options depending on whether the going
                // switch is checked or not
                enableDisableWhereOptions(isChecked);
            }
        });
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
                                Toast.makeText(InvitationDetailsActivity.this, "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                            }
                        })
                .addApi(Places.GEO_DATA_API)
                .build();
        _mAdapter =
                new PlaceAutocompleteAdapter(this, _mGoogleApiClient, null,
                        null);


        autocompleteTextView_whereOther = getAutocompleteTextView_whereOther();
        // Register a listener that receives callbacks when a suggestion has been selected
        autocompleteTextView_whereOther.setOnItemClickListener(mAutocompleteClickListener);
        autocompleteTextView_whereOther.setAdapter(_mAdapter);
        autocompleteTextView_whereOther.setThreshold(1); // number of characters to start showing suggestions
    }

    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = _mAdapter.getItem(position);
            _suggestedPlaceId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            // set the primary text as the where text
            getAutocompleteTextView_whereOther().setText(primaryText);

            Log.i(TAG, "Autocomplete item selected: " + primaryText);


            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Called getPlaceById to get Place details for " + _suggestedPlaceId);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_invitation_details, menu);
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

    /*
     * Populates the invitation details based on object id of invitation
     */
    private void populateInvitationDetails(String invitationObjectId) {
        ParseQuery<ParseObject> invitationQuery = ParseQuery.getQuery("Invitation");

        invitationQuery.getInBackground(invitationObjectId, new GetCallback<ParseObject>() {
            public void done(ParseObject invitation, ParseException e) {
                if (e == null) {
                    _currentInvitation = invitation;

                    // add title
                    whatValue = getWhatValue();
                    whatValue.setText(invitation.getString("title"));

                    // add event time
                    whenValue = getWhenValue();
                    whenValue.setText(Utils.formatDate(invitation.getDate("eventTime")));

                    // populate invitee details
                    populateInviteeDetails(invitation);

                    // populate location details
                    // must be called after populateInviteeDetails() since it expects that
                    // the whereCount is already populated
                    populateLocationDetails(invitation);

                    // enable or disable the where options based on
                    // whether the going switch is checked
                    goingSwitch = getGoingSwitch();
                    enableDisableWhereOptions(goingSwitch.isChecked());

                } else {
                    Log.d("invitation", "Error: " + e.getMessage());
                }
            }
        });
    }

    /*
     * Populates invitee data based on the invitation
     */
    private void populateInviteeDetails(ParseObject invitation)
    {
        List<ParseObject> acceptedInvitees = new ArrayList<>();
        List<ParseObject> declinedInvitees = new ArrayList<>();
        List<ParseObject> waitingInvitees = new ArrayList<>();

        // retrieve invitees for the particular invitation
        ParseQuery<ParseObject> inviteeLookupQuery = ParseQuery.getQuery("InviteeLookup");
        inviteeLookupQuery.whereEqualTo("invitation", invitation);
        inviteeLookupQuery.orderByDescending("accepted"); // accepted first, then declined, then waiting
        inviteeLookupQuery.include("invitee");
        inviteeLookupQuery.include("whereSelection");

        try
        {
            List<ParseObject> inviteeLookupList = inviteeLookupQuery.find(); // TODO - does this need to be in background
            for (ParseObject inviteeLookup: inviteeLookupList)
            {
                String accepted = inviteeLookup.getString("accepted");
                ParseObject invitee = inviteeLookup.getParseObject("invitee");
                if (ParseUser.getCurrentUser().getObjectId().equals(invitee.getObjectId()))
                {
                    _currentInviteeLookup = inviteeLookup; // populate the current user's inviteeLookup object
                    // populate going switch
                    goingSwitch = getGoingSwitch();
                    goingSwitch.setChecked("true".equalsIgnoreCase(_currentInviteeLookup.getString("accepted")));
                }

                if ("true".equals(accepted)) // accepted invitees
                {
                    acceptedInvitees.add(invitee);

                    // count votes for each location
                    // increment whereCount, if there is one
                    ParseObject where = inviteeLookup.getParseObject("whereSelection");
                    if (where != null) // whereSelection has been made
                    {
                        Integer whereCount = _whereCount.get(where.getObjectId());
                        if (whereCount != null) // there's a count in there for location
                        {
                            _whereCount.put(where.getObjectId(), ++whereCount); // inrement whereCount
                        }
                        else // first entry in map
                        {
                            _whereCount.put(where.getObjectId(), 1); // first entry in count map
                        }
                    }
                }
                else if ("false".equals(accepted)) // declined invitees
                {
                    declinedInvitees.add(invitee);
                }
                else // waiting invitees
                {
                    waitingInvitees.add(invitee);
                }
            }

            _inviteeListMap.put("accepted", acceptedInvitees);
            _inviteeListMap.put("declined", declinedInvitees);
            _inviteeListMap.put("waiting", waitingInvitees);

            String formattedInviteeDetails = formatInviteeData("a", acceptedInvitees);
            formattedInviteeDetails += formatInviteeData("d", declinedInvitees);
            formattedInviteeDetails += formatInviteeData("w", waitingInvitees);

            // add invitee counts and list
            whoValue = getWhoValue();
            whoValue.setText(formattedInviteeDetails);

        }
        catch (ParseException e)
        {
            Log.d("invitee", "Error: " + e.getMessage());
        }
    }

    /**
     * @param inviteeType  w - waiting invitations
     *                     a - accepted invitations
     *                     d - declined invitations
     */
    private String formatInviteeData(String inviteeType, List<ParseObject> inviteeList)
    {
        String formattedInviteeDetails;
        String inviteeTypeFullString = "";
        if ("w".equals(inviteeType))
        {
            inviteeTypeFullString = "Waiting";
        }
        else if ("a".equals(inviteeType))
        {
            inviteeTypeFullString = "Accepted";
        }
        else if ("d".equals(inviteeType))
        {
            inviteeTypeFullString = "Declined";
        }

        // loop through list and populate the data
        formattedInviteeDetails = inviteeTypeFullString + ": " + inviteeList.size() + "\n";
        for (ParseObject invitee: inviteeList)
        {
            formattedInviteeDetails += " -> " + invitee.getString("username") + "\n";
        }
        formattedInviteeDetails += "\n";

        return formattedInviteeDetails;
    }

    /*
    * Populates location data based on the invitation
    * resource:
    *   - dynamically loading radio buttons: http://stackoverflow.com/questions/6646442/creating-radiobuttons-programmatically
    */
    private void  populateLocationDetails(ParseObject invitation)
    {

        // retrieve where locations for the particular invitation
        ParseQuery<ParseObject> whereQuery = ParseQuery.getQuery("Where");
        whereQuery.whereEqualTo("invitation", invitation);

        try
        {
            whereRadioGroup = getWhereRadioGroup();

            _whereList = whereQuery.find();
            int i = 0;
            for (; i < _whereList.size(); i++)
            {
                RadioButton radioButton = new RadioButton(this);
                int whereCount = Utils.nullToZero(_whereCount.get(_whereList.get(i).getObjectId()));
                radioButton.setText(_whereList.get(i).getString("name") +
                                    " (" + whereCount + " vote" + ((whereCount!=1)?"s":"") + ")");
                radioButton.setId(i);

                whereRadioGroup.addView(radioButton);

                // if radio button is user's selection, check the radio button
                if (_whereList.get(i).equals(
                        _currentInviteeLookup.getParseObject("whereSelection")))
                {
                    // populate where selection
                    radioButton.setChecked(true);
                }
            }

            // adding the 'other' radio button option
            // TODO - this should only happen if the inviter allows? or maybe the options
            //        are a core part of this app
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(i); // other will be the last option
            radioButton.setText("Other");
            whereRadioGroup.addView(radioButton);


            // disable autocomplete edit text by default
            // needs to be put here after all the radio options have been created
            enableDisableOtherEditText(false);
        }
        catch (ParseException e)
        {
            Log.d("where", "Error: " + e.getMessage());
        }
    }

    /**
     * This method is used to enable or disable and clear the
     * where radio buttons
     *
     * @param enable
     */
    private void enableDisableWhereOptions(boolean enable)
    {
        whereRadioGroup = getWhereRadioGroup();
        if (enable)
        {
            for (int i = 0; i < whereRadioGroup.getChildCount(); i++) {
                whereRadioGroup.getChildAt(i).setEnabled(true);
            }
        }
        else
        {
            whereRadioGroup.clearCheck();
            for (int i = 0; i < whereRadioGroup.getChildCount(); i++) {
                whereRadioGroup.getChildAt(i).setEnabled(false);
            }
        }
    }

    /**
     * This method is used to enable or disable and clear the
     * where other text field
     *
     * @param enable
     */
    private void enableDisableOtherEditText(boolean enable)
    {
        autocompleteTextView_whereOther = getAutocompleteTextView_whereOther();
        if(enable)
        {
            autocompleteTextView_whereOther.setHint(R.string.where_prompt_new_suggestion);
            autocompleteTextView_whereOther.setEnabled(true);
        }
        else
        {
            autocompleteTextView_whereOther.setText("");
            autocompleteTextView_whereOther.setHint(null);
            autocompleteTextView_whereOther.setEnabled(false);
        }

    }

    /*
     * private accessors for widgets
     *
     */
    private Button getCancelButton() {
        return (cancelButton == null)?(Button) findViewById(R.id.cancelButton):cancelButton;
    }

    private Button getResponseButton() {
        return (responseButton == null)?(Button) findViewById(R.id.responseButton):responseButton;
    }

    private Switch getGoingSwitch() {
        return (goingSwitch == null)?(Switch) findViewById(R.id.goingSwitch):goingSwitch;
    }

    private TextView getWhatValue() {
        return (whatValue == null)?(TextView) findViewById(R.id.whatValue):whatValue;
    }

    private TextView getWhenValue() {
        return (whenValue == null)?(TextView) findViewById(R.id.whenValue):whenValue;
    }

    private TextView getWhoValue() {
        return (whoValue == null)?(TextView) findViewById(R.id.whoValue):whoValue;
    }

    private RadioGroup getWhereRadioGroup() {
        return (whereRadioGroup == null)?(RadioGroup) findViewById(R.id.whereRadioGroup):whereRadioGroup;
    }

    private AutoCompleteTextView getAutocompleteTextView_whereOther() {
        return (autocompleteTextView_whereOther == null)?
                (AutoCompleteTextView) findViewById(R.id.autocompleteTextView_whereOther):
                autocompleteTextView_whereOther;
    }
}
