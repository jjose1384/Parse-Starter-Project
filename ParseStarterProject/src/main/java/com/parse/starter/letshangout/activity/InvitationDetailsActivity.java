package com.parse.starter.letshangout.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.starter.R;
import com.parse.starter.letshangout.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvitationDetailsActivity extends AppCompatActivity {

    // widgets
    private Button cancelButton;
    private Button responseButton;
    private Switch goingSwitch;
    private TextView whatValue;
    private TextView whenValue;
    private TextView whoValue;
    private RadioGroup whereRadioGroup;

    // data
    private ParseObject _currentInvitation;
    private ParseObject _currentInviteeLookup;
    private Map<String, List<ParseObject>> _inviteeListMap = new HashMap<>(); // maps accepted, declined and waiting lists
    private Map<String, Integer> _whereCount = new HashMap<>(); // maps where object id to number of votes
    private List<ParseObject> _whereList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_details);

        // populating data
        Intent intent = getIntent();
        String invitationObjectId = intent.getStringExtra("invitationObjectId");
        populateInvitationDetails(invitationObjectId);

        // set listeners on buttons
        goingSwitchChangeListener();
        cancelButtonListener();
        responseButtonListener();

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
     *  saves the going response as well as the where selection if one has been made
     */
    private void responseButtonListener()
    {
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
                                if (selectedRadioButtonIndex != -1) // -1 = no radio button selected
                                {
                                    _currentInviteeLookup.put("whereSelection",
                                            _whereList.get(selectedRadioButtonIndex));
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

    private void goingSwitchChangeListener()
    {
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
    private void populateInvitationDetails(String invitationObjectId)
    {
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
                    whenValue.setText(invitation.getDate("eventTime").toString());

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
            List<ParseObject> inviteeLookupList = inviteeLookupQuery.find();
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
    private void populateLocationDetails(ParseObject invitation)
    {

        // retrieve where locations for the particular invitation
        ParseQuery<ParseObject> whereQuery = ParseQuery.getQuery("Where");
        whereQuery.whereEqualTo("invitation", invitation);

        try
        {
            whereRadioGroup = getWhereRadioGroup();

            _whereList = whereQuery.find();
            for (int i = 0; i < _whereList.size(); i++)
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
}
