package com.david.speechengine;

import android.database.Cursor;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class AllEventsActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> eventsList;
    ArrayList<Integer> eventsIdList;
    ArrayAdapter adapter;

    int clickCounter = 0;
    int eventPosition = -1;
    Handler handler = new Handler();

    DBAdapter myDb;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDb.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_events);

        myDb = new DBAdapter(this);
        myDb.open();

        listView = findViewById(R.id.listView);
        eventsList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, eventsList);
        listView.setAdapter(adapter);

        eventsIdList = new ArrayList<Integer>();

        Cursor cursor = myDb.getAllRows();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(DBAdapter.COL_ROWID);
                String title = cursor.getString(DBAdapter.COL_Title);
                String date = cursor.getString(DBAdapter.COL_Date);
                String time = cursor.getString(DBAdapter.COL_Time);

                eventsList.add(title+" - "+time+" - "+date);
                eventsIdList.add(id);

            } while(cursor.moveToNext());
        }

        cursor.close();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(clickCounter == 0){
                    //record first tap and eventPosition
                    clickCounter++;
                    eventPosition = position;

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //reset clickCounter and eventPosition if double tap timeout
                            clickCounter = 0;
                            eventPosition = -1;
                        }
                    }, ViewConfiguration.getDoubleTapTimeout());

                }
                else if(clickCounter == 1){
                    clickCounter = 0;
                    if(eventPosition == position){
                        String event = (String) listView.getItemAtPosition(position);
                        int eventListId = eventsList.indexOf(event);
                        int eventId = eventsIdList.get(eventListId);

                        eventsIdList.remove(eventListId);
                        eventsList.remove(eventListId);
                        myDb.deleteRow(eventId);

                        adapter.notifyDataSetChanged();

                        Toast.makeText(AllEventsActivity.this, "Event deleted", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }
}
