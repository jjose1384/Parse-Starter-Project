package com.parse.starter.letshangout.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.parse.starter.R;
import com.parse.starter.letshangout.dto.User;
import com.parse.starter.letshangout.utils.ContactsCompletionView;
import com.tokenautocomplete.FilteredArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewInvitationActivity extends AppCompatActivity {

    // widgets
    private EditText editText_what;
    private EditText editText_date;
    private EditText editText_time;
    private ContactsCompletionView contactsCompletionView_who;
    private Button inviteButton;

    // data


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_invitation);

        // set listeners
        setDatePicker();
        setTimePicker();
        setFriendsTextView();
    }

    /**
     * resource: http://stackoverflow.com/questions/14933330/datepicker-how-to-popup-datepicker-when-click-on-edittext
     */
    private void setDatePicker()
    {
        final Calendar myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            // when date is set it datePickerDialog, the value in the date field will be set
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "EEE M/d/yy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                editText_date.setText(sdf.format(myCalendar.getTime()));
            }
        };


        // datePicker edit text field
        editText_date = getEditText_date();
        editText_date.setOnClickListener(new View.OnClickListener() {
            // popup datepicker when field is clicked
            public void onClick(View arg0) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(NewInvitationActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));

                // only display calendar view
                datePickerDialog.getDatePicker().setCalendarViewShown(true);
                datePickerDialog.getDatePicker().setSpinnersShown(false);

                datePickerDialog.show();
            }
        });
    }

    /**
     * resource: http://stackoverflow.com/questions/14933330/datepicker-how-to-popup-datepicker-when-click-on-edittext
     */
    private void setTimePicker()
    {
        final Calendar myCalendar = Calendar.getInstance();

        final TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                myCalendar.set(Calendar.HOUR, hourOfDay);
                myCalendar.set(Calendar.MINUTE, minute);

                String myFormat = "h:mm a";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                editText_time.setText(sdf.format(myCalendar.getTime()));;
            }
        };

        // timePicker edit text field
        editText_time = getEditText_time();
        editText_time.setOnClickListener(new View.OnClickListener() {
            // popup timePicker when field is clicked
            public void onClick(View arg0) {
                new TimePickerDialog(NewInvitationActivity.this, time, myCalendar.get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE), false).show();
            }
        });
    }

    private void setFriendsTextView()
    {
        ArrayAdapter<User> adapter;
        User[] users = new User[]{
                new User("Marshall Weir", "marshall@example.com"),
                new User("Margaret Smith", "margaret@example.com"),
                new User("Max Jordan", "max@example.com"),
                new User("Meg Peterson", "meg@example.com"),
                new User("Amanda Johnson", "amanda@example.com"),
                new User("Terry Anderson", "terry@example.com")
        };

        adapter = new FilteredArrayAdapter<User>(this, R.layout.user_layout, users) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {

                    LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = l.inflate(R.layout.user_layout, parent, false);
                }

                User user = getItem(position);
                ((TextView)convertView.findViewById(R.id.name)).setText(user.getName());
                ((TextView)convertView.findViewById(R.id.email)).setText(user.getEmail());

                return convertView;
            }

            @Override
            protected boolean keepObject(User user, String mask) {
                mask = mask.toLowerCase();
                return user.getName().toLowerCase().startsWith(mask) || user.getEmail().toLowerCase().startsWith(mask);
            }
        };

        ContactsCompletionView completionView = getContactsCompletionView_who();
        completionView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_invitation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * private accessors for widgets
     */
    private EditText getEditText_what() {
        return (editText_what == null) ? (EditText)findViewById(R.id.editText_what):editText_what;
    }

    private EditText getEditText_date() {
        return (editText_date == null)?(EditText)findViewById(R.id.editText_date):editText_date;
    }

    private EditText getEditText_time() {
        return (editText_time == null)?(EditText)findViewById(R.id.editText_time):editText_time;
    }

    private ContactsCompletionView getContactsCompletionView_who() {
        return (contactsCompletionView_who == null)?
                (ContactsCompletionView)findViewById(R.id.contactsCompletionView_who):
                contactsCompletionView_who;
    }

    private Button getInviteButton() {
        return (inviteButton == null)?(Button)findViewById(R.id.inviteButton):inviteButton;
    }
}
