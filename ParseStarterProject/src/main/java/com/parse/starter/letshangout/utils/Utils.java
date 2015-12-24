package com.parse.starter.letshangout.utils;

import com.parse.ParseACL;
import com.parse.ParseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jason on 10/22/2015.
 * TODO
 */
public class Utils
{
    public static final String DATE_FORMAT = "EEE M/d/yy";
    public static final String TIME_FORMAT = "h:mm a";

    /**
     *
     * @return public read and write ACL
     */
    public static ParseACL getPublicReadWriteACL()
    {
        ParseACL publicReadWriteACL = new ParseACL();
        publicReadWriteACL.setPublicReadAccess(true);
        publicReadWriteACL.setPublicWriteAccess(true);

        return publicReadWriteACL;
    }

    /**
     *
     * @return public read and owner write ACL
     */
    public static ParseACL getPublicReadPrivateWriteACL(ParseUser...parseUsers)
    {
        ParseACL publicReadPrivateWriteACL = new ParseACL();
        publicReadPrivateWriteACL.setPublicReadAccess(true);

        for (ParseUser parseUser: parseUsers)
        {
            publicReadPrivateWriteACL.setWriteAccess(parseUser, true);
        }

        return publicReadPrivateWriteACL;
    }

    public static String formatDate(Date date)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT + " " + TIME_FORMAT);
        return simpleDateFormat.format(date);
    }

    public static Date formatDate(String date)
    {
        Date returnValue = null;
        try
        {
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat(DATE_FORMAT + " " + TIME_FORMAT);

            returnValue =  simpleDateFormat.parse(date);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        return returnValue;
    }

    /**
     *
     * @param integer
     * @return zero if the value is null
     */
    public static Integer nullToZero(Integer integer)
    {
        return (integer == null)?0:integer;
    }
}
