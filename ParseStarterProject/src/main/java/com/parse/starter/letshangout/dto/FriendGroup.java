package com.parse.starter.letshangout.dto;

/**
 * Created by Jason on 12/29/2015.
 */

import java.util.List;
import java.util.Map;

// TODO - could this and InvitationGroup be combined?
public class FriendGroup {

    private String headerTitle; // header
    // List of maps, each map represents a set of details in key/value pairs
    private List<Map<String, Object>> friendDetailsList;


    /**
     *
     * getters/setters
     */
    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    public List<Map<String, Object>> getFriendDetailsList() {
        return friendDetailsList;
    }

    public void setFriendDetailsList(List<Map<String, Object>> friendDetailsList) {
        this.friendDetailsList = friendDetailsList;
    }
}

