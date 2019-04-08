package com.david.speechengine;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity {

    private EditText editTextEvent;
    private EditText editTextDate;
    private EditText editTextTime;

    private Button buttonDone;
    private Button buttonCancel;

    DBAdapter myDb;

    String inputLine = "";
    String [] eventDetails;
    String eventTitle;
    String eventDate;

    String patternDay = "\\b((sat(ur)?)|sun|mon|(tue(s)?)|(wed(nes)?)|(thu(rs)?)|fri)(day)?\\b";
    String patternAt = "\\bat\\b";


    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDb.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        editTextEvent = findViewById(R.id.editTextEvent);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        buttonDone = findViewById(R.id.buttonDone);
        buttonCancel = findViewById(R.id.buttonCancel);

        inputLine = getIntent().getStringExtra("Event");

        myDb = new DBAdapter(this);
        myDb.open();

        eventDetails = inputLine.split(patternDay);
        eventTitle = eventDetails[0];
        eventDate = eventDetails[1];

        Date date = null;

        try{
            date = parseDate(eventDate);

            if( date == null){
                Toast.makeText(this, "Incorrect date format.", Toast.LENGTH_LONG).show();
                finish();
            }

            editTextEvent.setText(eventTitle.trim());
            editTextDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(date));
            editTextTime.setText(new SimpleDateFormat("hh:mm a").format(date));
        }
        catch (Exception e){
            Log.v("LogError", e.getStackTrace().toString());
            Toast.makeText(this, "Exception!", Toast.LENGTH_LONG).show();
        }

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AddEventActivity.this,"Adding event canceled", Toast.LENGTH_LONG);
                finish();
            }
        });

        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEventToDB();
                Toast.makeText(AddEventActivity.this, "Event added", Toast.LENGTH_SHORT).show();
                finish();

            }
        });
    }

    private void addEventToDB() {
        String title = editTextEvent.getText().toString();
        String date = editTextDate.getText().toString();
        String time = editTextTime.getText().toString();

        myDb.insertRow(title, date, time);

    }

    Date parseDate(String str){
        str = str.trim();
        str = str.replaceAll("\\bp.m.","PM");

        String [] possibleFormats = {"MMMMM d yyyy 'at' h:mm a","MMMMM d yyyy 'at' h a","MMMMM d yyyy 'at' H:mm","MMMMM d yyyy 'at' H"};

        for (int i = 0; i < possibleFormats.length; i++) {
            try {
                Date date = new SimpleDateFormat(possibleFormats[i], Locale.getDefault()).parse(str);
                return date;
            } catch (ParseException e) {
                Log.v("LogError", e.getStackTrace().toString());
//                Toast.makeText(this, "Parse date exception!", Toast.LENGTH_LONG).show();
            }
        }
        return null;
    }
}
