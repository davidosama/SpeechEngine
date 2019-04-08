package com.david.speechengine;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ImageButton buttonMic;
    Button buttonAllEvents;
    Button buttonExit;
    Button buttonHelp;

    final int VOICE_RECOGNITION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonMic = (ImageButton) findViewById(R.id.buttonMic);
        buttonAllEvents = (Button) findViewById(R.id.buttonEvents);
        buttonExit = (Button) findViewById(R.id.buttonExit);
        buttonHelp = (Button) findViewById(R.id.buttonHelp);

        final Context thisMainActivity = this;

        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });

        buttonHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(thisMainActivity);

                builder.setTitle("Help");
                builder.setMessage("To add a new event properly please use the keyword \"at\" with following format\nexample: \"team lunch Monday November 5 2018 at 7:30 pm\"");

                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });

        buttonAllEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisMainActivity, AllEventsActivity.class);
                startActivity(intent);
            }
        });

        buttonMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechInput();
            }
        });
    }

    private void startSpeechInput(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak!");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
        try{
            startActivityForResult(intent, VOICE_RECOGNITION);
        }
        catch (ActivityNotFoundException e){
            Toast.makeText(this, "Speech Recognition not working...", Toast.LENGTH_LONG).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION) {
            if (resultCode == RESULT_OK) {
                List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                float[] confidence = data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);

                //log speech input
                for (String result: results) {
                    Log.v("speechtext", result);
                }
                ProcessSpeechInput(results.get(0));
                Toast.makeText(this, results.get(0),Toast.LENGTH_LONG).show();
            }
        }
    }

    public void ProcessSpeechInput(String inputline){
        String patternDay = "\\b((sat(ur)?)|sun|mon|(tue(s)?)|(wed(nes)?)|(thu(rs)?)|fri)(day)?\\b";
        String patternAt = "\\bat\\b";

        Log.v("LogInput", inputline);
        inputline = inputline.toLowerCase();

        try{
            if(Pattern.compile(patternDay).matcher(inputline).find() && Pattern.compile(patternAt).matcher(inputline).find()){
                String[] eventDetails = inputline.split(patternDay);
                if(eventDetails.length == 2){
                    inputline = inputline.replaceAll("([0-9]+)(th|nd|rd|st)","$1");
                    Log.v("LogInput New", inputline);
                    Intent intent = new Intent(this, AddEventActivity.class);
                    intent.putExtra("Event", inputline);
                    startActivity(intent);
                }
                else {
                    inputError("eventDetails length");
                }
            }
            else {
                inputError("pattern matching");
            }
        }
        catch (Exception e){
            Log.v("LogError", e.getStackTrace().toString());
            Toast.makeText(this, "Exception!", Toast.LENGTH_LONG).show();
        }

    }

    public void inputError(String msg){
        Toast.makeText(this, "Incorrect input at "+msg+". please check the correct input from the help menu.",Toast.LENGTH_LONG).show();
    }
}
