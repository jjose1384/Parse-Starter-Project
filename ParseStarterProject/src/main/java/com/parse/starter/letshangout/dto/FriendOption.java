package com.parse.starter.letshangout.dto;

/**
 * Created by Jason on 11/23/2015.
 */
public class FriendOption
{
    private String objectId;
    private String fullName;
    private String email;

    @Override
    public String toString()
    {
        return email;
    }

    /**
     * getters/setters
     */
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
