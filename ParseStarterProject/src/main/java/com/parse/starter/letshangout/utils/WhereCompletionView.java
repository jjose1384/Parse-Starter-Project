package com.parse.starter.letshangout.utils;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.parse.starter.R;
import com.parse.starter.letshangout.dto.Where;
import com.tokenautocomplete.TokenCompleteTextView;

/**
 * Created by Jason on 11/25/2015.
 */
public class WhereCompletionView extends TokenCompleteTextView<Object> {
    public WhereCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(Object where) {
        LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        LinearLayout view = (LinearLayout)l.inflate(R.layout.contact_token, (ViewGroup)WhereCompletionView.this.getParent(), false);
        ((TextView)view.findViewById(R.id.name)).setText(((AutocompletePrediction)where).getPrimaryText(null));

        return view;
    }

    @Override
    protected Where defaultObject(String completionText) {
        return new Where(completionText, completionText);
    }
}