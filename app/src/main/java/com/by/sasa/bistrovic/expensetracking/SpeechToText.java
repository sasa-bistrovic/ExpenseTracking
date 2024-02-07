package com.by.sasa.bistrovic.expensetracking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
//import android.support.v7.app.AppCompatActivity;
import android.speech.SpeechRecognizer;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class SpeechToText extends AppCompatActivity {

    private TextView txvResult;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    private Button cancelamount, enteramount, convertMinusPlusValue;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_to_text);

        // Set the background color of the title bar
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.titleBackground)));

// Set the text color of the title bar
        int titleTextColor = getResources().getColor(R.color.titleText);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='" + titleTextColor + "'>Expense Tracking</font>"));

        txvResult = (TextView) findViewById(R.id.txvResult);

        cancelamount = findViewById(R.id.cancelSpeechResult);
        enteramount = findViewById(R.id.enterSpeechResult);
        convertMinusPlusValue = findViewById(R.id.minusSpeechResult);

        ImageView btnSpeech = findViewById(R.id.btnSpeak);

        // Check for runtime permission if Android version is 6.0 or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            }
        }

        cancelamount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(SpeechToText.this, MainActivity.class));
            }
        });

        convertMinusPlusValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finish();
                //startActivity(new Intent(SpeechToText.this, MainActivity.class));

                if (!TextUtils.isEmpty(txvResult.getText().toString())) {

                    Double amountDouble = Double.valueOf(0);

                    try {
                        amountDouble = Double.parseDouble(txvResult.getText().toString().replaceAll(",", "."));
                    } catch (NumberFormatException e) {
                        //throw new RuntimeException(e);
                    }

                    if (amountDouble != 0) {
                        amountDouble = amountDouble * -1;
                    }

                    txvResult.setText(String.format("%.2f", amountDouble));
                }
            }
        });

        enteramount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //TextRecognitionActivity.mainposition=0;
                //TextRecognitionActivity.lines.clear();
                finish();
                startActivity(new Intent(SpeechToText.this, MainActivity.class));

                Double amountDouble=Double.valueOf(0);

                try {
                    amountDouble = Double.parseDouble(txvResult.getText().toString().replaceAll(",","."));
                } catch (NumberFormatException e) {
                    //throw new RuntimeException(e);
                }

                //amountDouble = amountDouble/100;

                Timer timer = new Timer();

                Double finalAmountDouble = amountDouble;
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        if (finalAmountDouble!=0) {
                            MainActivity.addRow("", Long.valueOf(0), finalAmountDouble);
                        }
                    }
                };

                // Schedule the task to run after a delay of 5 seconds
                timer.schedule(task, 3000);
            }
        });

        // Initialize the SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Set a listener to receive the speech recognition results
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                //txvResult.setText("Error occurred: " + getErrorText(error));
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String recognizedText = matches.get(0);

                    String speechToTextStr="";
                    Double speechToTextDouble=Double.valueOf(0);
                    if (matches.get(0).indexOf("minus")>=0 || matches.get(0).indexOf("-")>=0) {
                        try {
                            speechToTextDouble=Double.parseDouble("0"+matches.get(0).replaceAll("[^0-9]", ""));
                            speechToTextDouble=speechToTextDouble/100*-1;
                        } catch (NumberFormatException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        try {
                            speechToTextDouble=Double.parseDouble("0"+matches.get(0).replaceAll("[^0-9]", ""));
                            speechToTextDouble=speechToTextDouble/100;
                        } catch (NumberFormatException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    //if (speechToTextDouble!=0) {
                    txvResult.setText(String.format( "%.2f", speechToTextDouble));
                    //txvResult.setText("You said: " + recognizedText);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        // Set a click listener for the speech recognition button
        btnSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechRecognizer.startListening(speechRecognizerIntent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    // Helper method to get error messages
    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "RecognitionService busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Error from server";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                return "Unknown error";
        }
    }
/*
    public void getSpeechInput(View view) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String speechToTextStr="";
                    Double speechToTextDouble=Double.valueOf(0);
                    if (result.get(0).indexOf("minus")>=0 || result.get(0).indexOf("-")>=0) {
                        try {
                            speechToTextDouble=Double.parseDouble("0"+result.get(0).replaceAll("[^0-9]", ""));
                            speechToTextDouble=speechToTextDouble/100*-1;
                        } catch (NumberFormatException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        try {
                            speechToTextDouble=Double.parseDouble("0"+result.get(0).replaceAll("[^0-9]", ""));
                            speechToTextDouble=speechToTextDouble/100;
                        } catch (NumberFormatException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    //if (speechToTextDouble!=0) {
                    txvResult.setText(String.format( "%.2f", speechToTextDouble));
                    //}
                }
                break;
        }
    }

 */

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(SpeechToText.this, MainActivity.class));
        //    new AlertDialog.Builder(this)
        //            .setTitle("Really Exit?")
        //            .setMessage("Are you sure you want to exit?")
        //            .setNegativeButton(android.R.string.no, null)
        //            .setPositiveButton(android.R.string.yes, new OnClickListener() {

        //                public void onClick(DialogInterface arg0, int arg1) {
        //                    WelcomeActivity.super.onBackPressed();
        //                }
        //            }).create().show();
    }
}