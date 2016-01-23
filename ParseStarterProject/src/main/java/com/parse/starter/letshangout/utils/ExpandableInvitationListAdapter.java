package com.parse.starter.letshangout.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.parse.starter.R;
import com.parse.starter.letshangout.dto.InvitationGroup;

import java.util.List;
import java.util.Map;

/**
 * Created by Jason on 10/12/2015.
 */
public class ExpandableInvitationListAdapter extends BaseExpandableListAdapter
{

    private Context _context;
    private List<InvitationGroup> _invitationGroups;

    public ExpandableInvitationListAdapter(Context context, List<InvitationGroup> invitationGroups)
    {
        this._context = context;
        this._invitationGroups = invitationGroups;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        return (_invitationGroups.get(groupPosition)).getInvitationDetailsList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        return (_invitationGroups.get(groupPosition)).getInvitationDetailsList().size();
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        return this._invitationGroups.get(groupPosition);
    }

    @Override
    public int getGroupCount()
    {
        return this._invitationGroups.size();
    }

    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_invitation_header, null);
        }

        // Invitation header title
        String headerTitle = ((InvitationGroup) getGroup(groupPosition)).getHeaderTitle();
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        // added invitation count to the header
        TextView lblListCount = (TextView) convertView
                .findViewById(R.id.lblListCount);
        lblListCount.setTypeface(null, Typeface.BOLD);
        lblListCount.setText(new Integer(getChildrenCount(groupPosition)).toString());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent)
    {

        if (convertView == null)
        {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_invitation_item, null);
        }

        // Invitation title
        Map<String, String> invitationDetails = (Map<String, String>) getChild(groupPosition, childPosition);
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);
        txtListChild.setText(invitationDetails.get("title"));


        return convertView;
    }
}
