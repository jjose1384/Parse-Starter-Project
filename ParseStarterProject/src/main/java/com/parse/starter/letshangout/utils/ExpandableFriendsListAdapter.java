package com.parse.starter.letshangout.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.starter.R;
import com.parse.starter.letshangout.dto.FriendGroup;

import java.util.List;
import java.util.Map;

/**
 * Created by Jason on 12/29/2015.
 */
public class ExpandableFriendsListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<FriendGroup> _friendGroups;

    public ExpandableFriendsListAdapter(Context context, List<FriendGroup> friendGroups) {
        this._context = context;
        this._friendGroups = friendGroups;
    }

    public void updateData(List<FriendGroup> friendGroups)
    {
        _friendGroups = friendGroups;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return (_friendGroups.get(groupPosition)).getFriendDetailsList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return (_friendGroups.get(groupPosition)).getFriendDetailsList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._friendGroups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._friendGroups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_friend_header, null);
        }

        // Friend header title
        String headerTitle = ((FriendGroup) getGroup(groupPosition)).getHeaderTitle();
        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        // added friend count to the header
        TextView lblListCount = (TextView) convertView.findViewById(R.id.lblListCount);
        lblListCount.setTypeface(null, Typeface.BOLD);
        lblListCount.setText(new Integer(getChildrenCount(groupPosition)).toString());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_friend_item, null);
        }

        // Friend title
        Map<String, String> friendDetails = (Map<String, String>) getChild(groupPosition, childPosition);
        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        txtListChild.setText(friendDetails.get("username"));

        TextView textView_userObjectId = (TextView) convertView.findViewById(R.id.textView_userObjectId);
        textView_userObjectId.setText(friendDetails.get("userObjectId"));


        System.out.println("Group Position: " + groupPosition + " username: " + friendDetails.get("username"));


        // for sent requests or friend list group, don't display expand/collapse button
        // only display it for received friend requests
        LinearLayout linearLayout_expandCollapseButtons =
                (LinearLayout) convertView.findViewById(R.id.linearLayout_expandCollapseButtons);
        if (groupPosition == 0)
        {
            linearLayout_expandCollapseButtons.setVisibility(View.VISIBLE);

            System.out.println("Inside if clause: \n Group Position: " + groupPosition + " username: " + friendDetails.get("username"));
        }
        else
        {
            linearLayout_expandCollapseButtons.setVisibility(View.GONE);
        }


        return convertView;
    }
}
