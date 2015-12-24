package com.parse.starter.letshangout.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.starter.R;
import com.parse.starter.letshangout.dto.InvitationGroup;
import com.parse.starter.letshangout.utils.ExpandableInvitationListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * resource: http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
 * resource: http://www.tutorialsbuzz.com/2014/07/custom-expandable-listview-image-text.html
 */
public class InvitationListActivity extends AppCompatActivity {

    // widgets
    private Button logout; // TODO - remove
    private FloatingActionButton button_newInvitation;
    private ExpandableListView expListView;

    // data
    private List<InvitationGroup> invitationGroups;
    private static ExpandableInvitationListAdapter listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_list);

        // set listeners on buttons
        expListViewPopulateAndSetClickListener();
        newInvitationButtonListener();
        logoutButtonListener();

    }

    /**
     * expand list view listener
     */
    private void expListViewPopulateAndSetClickListener() {

        // get the listview
        expListView = getExpListView();
        // preparing list data
        prepareListData();
        listAdapter = new ExpandableInvitationListAdapter(this, invitationGroups);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        // expand all groups by default
        for(int i=0; i < listAdapter.getGroupCount(); i++)
            expListView.expandGroup(i);

        // Listview on child click listener
        // navigate to the invitation details screen
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(InvitationListActivity.this, InvitationDetailsActivity.class);
                intent.putExtra("invitationObjectId", (String) invitationGroups.get(groupPosition).getInvitationDetails().get(childPosition).get("invitationObjectId"));
                startActivity(intent);

// details of parameters being passed
//                Toast.makeText(getApplicationContext(), invitationGroups.get(groupPosition).getHeaderTitle() + " : " +
//                        invitationGroups.get(groupPosition).getInvitationDetails().get(childPosition).get("title") + " : " +
//                        invitationGroups.get(groupPosition).getInvitationDetails().get(childPosition).get("eventTime") + " : " +
//                        invitationGroups.get(groupPosition).getInvitationDetails().get(childPosition).get("invitationObjectId"), Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

    /**
     * button_newInvitation listener
     */
    private void newInvitationButtonListener()
    {
        // new invitation button
        button_newInvitation = getButton_newInvitation();
        button_newInvitation.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                Intent intent = new Intent(InvitationListActivity.this, NewInvitationActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * logoutButton listener
     */
    private void logoutButtonListener()
    {
        // Logout Button Click Listener
        logout = getLogoutButton();
        logout.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                // Logout current user
                ParseUser.logOut();
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_invitation_list, menu);
        return true;
    }

    @Override
    /**
     * resource: http://developer.android.com/training/appbar/actions.html#add-actions
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_friendsList)
        {
            // navigate to the new invitation screen
            Intent intent = new Intent(
                    InvitationListActivity.this,
                    NewInvitationActivity.class);

            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    * Preparing the list data
    */
    private void prepareListData() {

        invitationGroups = new ArrayList<InvitationGroup>();

        // Adding groups
        InvitationGroup waitingInvitations = new InvitationGroup();
        waitingInvitations.setHeaderTitle("Waiting Invitations");

        InvitationGroup acceptedInvitations = new InvitationGroup();
        acceptedInvitations.setHeaderTitle("Accepted Invitations");

        InvitationGroup declinedInvitations = new InvitationGroup();
        declinedInvitations.setHeaderTitle("Declined Invitations");


        // Populate list of waiting invitations
        waitingInvitations.setInvitationDetails(loadInvitationList("w"));

        // Populate list of accepted invitations
        acceptedInvitations.setInvitationDetails(loadInvitationList("a"));

        // populate list of declined invitations
        declinedInvitations.setInvitationDetails(loadInvitationList("d"));

        invitationGroups.add(0, waitingInvitations);
        invitationGroups.add(1, acceptedInvitations);
        invitationGroups.add(2, declinedInvitations);
    }

    /**
     * @param invitationType w - waiting invitations
     *                       a - accepted invitations
     *                       d - declined invitations
     * @return List of Maps, each map representing an invitation with key/value pairs
     */
    private List<Map<String, Object>> loadInvitationList(final String invitationType) {
        final List<Map<String, Object>> invitationsList = new ArrayList<Map<String, Object>>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("InviteeLookup");
        query.whereEqualTo("invitee", ParseUser.getCurrentUser());
        query.include("invitation"); // include the invitation object pointer

        if ("w".equals(invitationType))
        {
            // make sure 'accepted' column is null/undefined, not empty string
            query.whereEqualTo("accepted", null);
        }
        else if ("a".equals(invitationType))
        {
            query.whereEqualTo("accepted", "true");
        }
        else if ("d".equals(invitationType))
        {
            query.whereEqualTo("accepted", "false");
        }

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> invitationList, ParseException e) {
                if (e == null) {

                    for (ParseObject inviteeLookup : invitationList) {
                        ParseObject invitation = inviteeLookup.getParseObject("invitation");

                        Map<String, Object> invitationMap = new HashMap<String, Object>();
                        invitationMap.put("title", invitation.getString("title"));
                        invitationMap.put("eventTime", invitation.getDate("eventTime"));
                        invitationMap.put("invitationObjectId", invitation.getObjectId());

                        invitationsList.add(invitationMap);

                        // the following line is critical in making sure
                        // the view gets updated when the data becomes available
                        // in the background
                        listAdapter.notifyDataSetChanged();
                    }
                } else {
                    Log.d("InviteeLookup", "Error: " + e.getMessage());
                }
            }
        });

        return invitationsList;
    }

    /**
     * When invitations have been updated, this will allow the
     * invitations list to be updated
     */
    public static void notifyInvitationsListAdapter()
    {
        listAdapter.notifyDataSetChanged();
    }

    /*
     * private accessors for widgets
     *
     */
    private FloatingActionButton getButton_newInvitation()
    {
        return (button_newInvitation == null)?
                (FloatingActionButton) findViewById(R.id.button_newInvitation):
                button_newInvitation;
    }

    private Button getLogoutButton()
    {
        return (logout == null)?
                (Button) findViewById(R.id.logout):
                logout;
    }

    private ExpandableListView getExpListView() {
        return (expListView == null)?
                (ExpandableListView) findViewById(R.id.lvExp):
                expListView;
    }
}
