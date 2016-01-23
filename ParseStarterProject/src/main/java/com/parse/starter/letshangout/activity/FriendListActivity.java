package com.parse.starter.letshangout.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.starter.R;
import com.parse.starter.letshangout.dto.FriendGroup;
import com.parse.starter.letshangout.utils.ExpandableFriendsListAdapter;
import com.parse.starter.letshangout.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendListActivity extends AppCompatActivity {

    private static final String RECEIVED_FRIEND_REQUESTS = "r";
    private static final String SENT_FRIEND_REQUESTS = "s";
    private static final String FRIENDS_LIST = "f";


    // widgets
    private ExpandableListView expandableListView_friendList;

    // data
    private List<FriendGroup> friendGroups;
    private static ExpandableFriendsListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        // set listeners
        expListViewPopulateAndSetClickListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friend_list, menu);
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
        else if (id == R.id.action_addFriend)
        {
            // navigate to the new invitation screen
            Intent intent = new Intent(
                    FriendListActivity.this,
                    FriendListActivity.class); // TODO - dialog?

            startActivity(intent);
            // finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * expand list view listener
     */
    private void expListViewPopulateAndSetClickListener() {

        // get the listview
        expandableListView_friendList = getExpandableListView_friendList();
        // preparing list data
        prepareListData();
        listAdapter = new ExpandableFriendsListAdapter(this, friendGroups);

        // setting list adapter
        expandableListView_friendList.setAdapter(listAdapter);

        // expand all groups by default
        for(int i=0; i < listAdapter.getGroupCount(); i++)
            expandableListView_friendList.expandGroup(i);
    }

    /*
    * Preparing the list data
    */
    private void prepareListData() {

        friendGroups = new ArrayList<FriendGroup>();

        // Adding groups
        FriendGroup receivedFriendRequests = new FriendGroup();
        receivedFriendRequests.setHeaderTitle("Received Friend Requests");

        FriendGroup sentFriendRequests = new FriendGroup();
        sentFriendRequests.setHeaderTitle("Sent Friend Requests");

        FriendGroup friendsList = new FriendGroup();
        friendsList.setHeaderTitle("Friends List");


        // Populate list of received friend requests
        receivedFriendRequests.setFriendDetailsList(loadFriendsList(RECEIVED_FRIEND_REQUESTS));

        // Populate list of sent friend requests
        sentFriendRequests.setFriendDetailsList(loadFriendsList(SENT_FRIEND_REQUESTS));

        // populate list of friends
        friendsList.setFriendDetailsList(loadFriendsList(FRIENDS_LIST));

        friendGroups.add(0, receivedFriendRequests);
        friendGroups.add(1, sentFriendRequests);
        friendGroups.add(2, friendsList);
    }

    /**
     * @param friendType    r - received friend requests
     *                      s - sent friend requests
     *                      f - friends list(mutually accepted)
     * @return List of Maps, each map representing friend details in key/value pairs
     */
    private List<Map<String, Object>> loadFriendsList(final String friendType) {
        final List<Map<String, Object>> friendsList = new ArrayList<Map<String, Object>>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("FriendLookup");
        query.include("friend"); // include the friend object pointer
        query.include("user"); // include the user object pointer

        if (RECEIVED_FRIEND_REQUESTS.equals(friendType))
        {
            query.whereEqualTo("friend", ParseUser.getCurrentUser()); // retrieve records where you are the recipient
            query.whereEqualTo("accepted", false); // friendship has not been accepted
        }
        else if (SENT_FRIEND_REQUESTS.equals(friendType))
        {
            query.whereEqualTo("user", ParseUser.getCurrentUser());
            query.whereEqualTo("accepted", false); // friendship has not been accepted
        }
        else if (FRIENDS_LIST.equals(friendType))
        {
            query.whereEqualTo("user", ParseUser.getCurrentUser());
            query.whereEqualTo("accepted", true); // friendship has been mutually accepted
        }

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> parseFriendsList, ParseException e) {
                if (e == null) {

                    for (ParseObject friendLookup : parseFriendsList) {

                        ParseObject friend;
                        if (RECEIVED_FRIEND_REQUESTS.equals(friendType)) {
                            friend = friendLookup.getParseObject("user"); // you are the recipient
                        } else {
                            friend = friendLookup.getParseObject("friend"); // you are the sender, or mutually accapted
                        }

                        Map<String, Object> friendDetailsMap = new HashMap<String, Object>();
                        friendDetailsMap.put("username", friend.getString("username"));
                        friendDetailsMap.put("email", friend.getDate("email"));
                        friendDetailsMap.put("userObjectId", friend.getObjectId());

                        friendsList.add(friendDetailsMap);

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

        return friendsList;
    }

    /**
     * resource: https://tsicilian.wordpress.com/2013/09/02/android-tips-expandablecollapsible-views/
     */
    public void expandCollapseFriendRequest(View v)
    {
        ImageView expand = (ImageView)v.findViewById(R.id.imageView_expand);
        ImageView collapse = (ImageView)v.findViewById(R.id.imageView_collapse);
        LinearLayout linearLayout_responseButtons =
                (LinearLayout)(((View)v.getParent()).findViewById(R.id.linearLayout_friendRequest)).findViewById(R.id.linearLayout_responseButtons);
        if (expand.isShown())
        {
            expand.setVisibility(View.GONE);
            collapse.setVisibility(View.VISIBLE);
            linearLayout_responseButtons.setVisibility(View.VISIBLE);
        }
        else
        {
            expand.setVisibility(View.VISIBLE);
            collapse.setVisibility(View.GONE);
            linearLayout_responseButtons.setVisibility(View.GONE);
        }
    }

    public void acceptFriendRequest(View v)
    {

        Button button_acceptFriendRequest = (Button)v;

        String userObjectId = (String)
                                ((TextView)
                                        ((LinearLayout)button_acceptFriendRequest
                                        .getParent())
                                        .findViewById(R.id.textView_userObjectId))
                                        .getText();

        final Date requestAcceptedTime = Calendar.getInstance().getTime();

        // retrieve the FriendLookup friend request object based on the userObjecId
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        query.getInBackground(userObjectId, new GetCallback<ParseObject>() {
            public void done(ParseObject user, ParseException e) {
                if (e == null) {

                    ParseQuery<ParseObject> friendLookupQuery = ParseQuery.getQuery("FriendLookup");
                    friendLookupQuery.whereEqualTo("friend", ParseUser.getCurrentUser());
                    friendLookupQuery.whereEqualTo("user", user); // using the retrieved parse user

                    try {
                        List<ParseObject> friendLookupList = friendLookupQuery.find(); // should be exactly one
                        if (friendLookupList.size() != 1) {
                            Log.d("acceptFriendRequest", "Error: " + "Multiple friend requests found");
                        } else {
                            // found the FriendLookup object to update
                            ParseObject friendLookup = friendLookupList.get(0);
                            friendLookup.put("accepted", true);
                            friendLookup.put("acceptedTime", requestAcceptedTime);
                            friendLookup.save(); // update the existing friendLookup object

                            // create new FriendLookup record
                            final ParseObject newFriendLookup = new ParseObject("FriendLookup");
                            newFriendLookup.put("user", ParseUser.getCurrentUser());
                            newFriendLookup.put("friend", user); // using the retrieved parse user
                            newFriendLookup.put("accepted", true);
                            newFriendLookup.put("sentTime", friendLookup.get("sentTime"));

                            newFriendLookup.put("acceptedTime", requestAcceptedTime);
                            newFriendLookup.put("requestSender", false);

                            newFriendLookup.setACL(Utils.getPublicReadWriteACL());
                            newFriendLookup.save(); // save the new friendLookup object

                            // Notify the user of the accepted friend request
                            Toast.makeText(FriendListActivity.this,
                                    "Friend request accepted", Toast.LENGTH_SHORT).show();


                            // load data and update screen
                            prepareListData();
                            // TODO - add this to invitation list, update screen works now
                            listAdapter.updateData(friendGroups);
                            listAdapter.notifyDataSetChanged();
                        }

                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }

                } else {
                    Log.d("acceptFriendRequest", "Error: " + e.getMessage());
                }
            }
        });
    }

    /*
     * private accessors for widgets
     *
     */
    private ExpandableListView getExpandableListView_friendList() {
        return (expandableListView_friendList == null)?
                (ExpandableListView) findViewById(R.id.expandableListView_friendList):
                expandableListView_friendList;
    }
}
