package com.parse.starter.letshangout.dto;

import java.io.Serializable;

/**
 * Created by Jason on 12/5/2015.
 */
public class Where implements Serializable{

    private String name;
    private String address;

    public Where(String name, String address)
    {
        this.name = name;
        this.address = address;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
