package com.parse.starter.letshangout.utils;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.starter.R;
import com.parse.starter.letshangout.dto.User;
import com.tokenautocomplete.TokenCompleteTextView;

/**
 * Created by Jason on 11/25/2015.
 */
public class ContactsCompletionView extends TokenCompleteTextView<User> {
    public ContactsCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(User user) {
        LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        LinearLayout view = (LinearLayout)l.inflate(R.layout.contact_token, (ViewGroup)ContactsCompletionView.this.getParent(), false);
        ((TextView)view.findViewById(R.id.name)).setText(user.getEmail());

        return view;
    }

    @Override
    protected User defaultObject(String completionText) {
        //Stupid simple example of guessing if we have an email or not
        int index = completionText.indexOf('@');
        if (index == -1) {
            return new User(completionText, completionText.replace(" ", "") + "@example.com");
        } else {
            return new User(completionText.substring(0, index), completionText);
        }
    }
}