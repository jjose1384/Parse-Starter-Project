package com.parse.starter.letshangout.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jason on 10/8/2015.
 */
public class Validation
{
    /**
     *
     * @param email - email to validate
     * @return - if validation is successful, null will be returned, otherwise the error message will be
     *           returned
     */
    public static String validateEmail(String email)
    {
        String returnVal = null;
        final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches())
        {
            returnVal = "Please enter a valid email address.";
        }

        return returnVal;
    }

    /**
     *
     * @param password - password to validate
     *                 - password must be at least 6 characters long
     * @return - if validation is successful, null will be returned, otherwise the error message will be
     *           returned
     */
    public static String validatePassword(String password)
    {
        // TODO - improve password strength
        String returnVal = null;

        if (password == null || password.length() < 6)
        {
            returnVal = "Password must be at least 6 characters long.";
        }

        return returnVal;
    }
}
