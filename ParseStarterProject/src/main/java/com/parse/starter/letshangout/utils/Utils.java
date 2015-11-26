package com.parse.starter.letshangout.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jason on 10/22/2015.
 * TODO
 */
public class Utils
{
    private final static String appDateFormat = "h:mm a";

    public static String formatDate(String date)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(appDateFormat);
        return null;
    }

    public static String formatDate(Date date)
    {
        return null;
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
