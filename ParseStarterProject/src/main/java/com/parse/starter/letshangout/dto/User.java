package com.parse.starter.letshangout.dto;

import java.io.Serializable;

/**
 * Created by Jason on 11/25/2015.
 */
public class User implements Serializable {
    private String name;
    private String email;

    public User(String n, String e) { name = n; email = e; }

    public String getName() { return name; }
    public String getEmail() { return email; }

    @Override
    public String toString() { return name; }
}