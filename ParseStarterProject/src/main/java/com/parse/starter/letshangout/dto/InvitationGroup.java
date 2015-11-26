package com.parse.starter.letshangout.dto;

import java.util.List;
import java.util.Map;

/**
 * Created by Jason on 10/17/2015.
 */
public class InvitationGroup {

    private String headerTitle; // invitation header
    // List of maps, each map represents a set of invitation details in key/value pairs
    private List<Map<String, Object>> invitationDetailsList;


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

    public List<Map<String, Object>> getInvitationDetails() {
        return invitationDetailsList;
    }

    public void setInvitationDetails(List<Map<String, Object>> invitationDetailsList) {
        this.invitationDetailsList = invitationDetailsList;
    }
}
