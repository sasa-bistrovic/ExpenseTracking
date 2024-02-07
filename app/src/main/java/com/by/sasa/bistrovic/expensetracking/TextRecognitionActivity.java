package com.by.sasa.bistrovic.expensetracking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

public class TextRecognitionActivity extends AppCompatActivity {

    private SurfaceView surfaceView;
    private TextView amountTextView;
    private CameraSource cameraSource;

    public static String[] lines = new String[6];

    public static Integer mainposition=0;

    boolean isInteger(double number) {
        return number % 1 == 0;// if the modulus(remainder of the division) of the argument(number) with 1 is 0 then return true otherwise false.
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static String cleanPunctuations(String text) {
        return text.replaceAll("\\p{Punct}+", "").replaceAll("\\s+", "+");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the background color of the title bar
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.titleBackground)));

// Set the text color of the title bar
        int titleTextColor = getResources().getColor(R.color.titleText);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='" + titleTextColor + "'>Expense Tracking</font>"));

        mainposition=0;
        setContentView(R.layout.activity_text_recognition);

        surfaceView = findViewById(R.id.surfaceView);
        amountTextView = findViewById(R.id.amountTextView);

        startCamera();
        // Check camera permission
    }

    private void startCamera() {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            // Handle error if text recognition dependencies are not available
            return;
        }

        cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 720)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                StringBuilder stringBuilder = new StringBuilder();

                //String[] linesa = new String[detections.getDetectedItems().size()];

                for (int i = 0; i < detections.getDetectedItems().size(); i++) {
                    TextBlock item = detections.getDetectedItems().valueAt(i);
                    //linesa[i]=String.join("",item.getValue().replaceAll(" ", "").replaceAll(",","."));
                    stringBuilder.append(item.getValue().replaceAll(" ", "").replaceAll(",",".").replaceAll("\\(","").replaceAll("\\)","").replaceAll("kn","").replaceAll("A","").replaceAll("B","").replaceAll("C","").replaceAll("O","0").replaceAll("EUR","").replaceAll("\\*","").replaceAll("€",""));
                    stringBuilder.append("\n");
                }

                // Extract amounts from the text blocks
                String extractedText = stringBuilder.toString();
                String[] words = extractedText.split("\\s+");

                String amount="";

                String textamount="";

                String find="";

                Integer position=0;

                amount="";

                for (int i=words.length-4; i>=0; i--) {
                    float value1 = Float.valueOf(0);
                    float value2 = Float.valueOf(0);
                    float value3 = Float.valueOf(0);
                    float value4 = Float.valueOf(0);
                    try {
                        value1 = Float.valueOf(words[i]);
                        value2 = Float.valueOf(words[i+1]);
                        value3 = Float.valueOf(words[i+2]);
                        value4 = Float.valueOf(words[i+3]);
                        if (value1==round((float) (value2/7.5345),2) || value1==round((float) (value2/7.5345),2)+1 || value1==round((float) (value2/7.5345),2)-1) {
                            amount=String.format("%.2f", value1);
                            break;
                        }
                        if (value2==round((float) (value1/7.5345),2) || value2==round((float) (value1/7.5345),2)+1 || value2==round((float) (value1/7.5345),2)-1) {
                            amount=String.format("%.2f", value2);
                            break;
                        }
                        if (value2==round((float) (value3/7.5345),2) || value2==round((float) (value3/7.5345),2)+1 || value2==round((float) (value3/7.5345),2)-1) {
                            amount=String.format("%.2f", value2);
                            break;
                        }
                        if (value3==round((float) (value2/7.5345),2) || value3==round((float) (value2/7.5345),2)+1 || value3==round((float) (value2/7.5345),2)-1) {
                            amount=String.format("%.2f", value3);
                            break;
                        }
                        if (value3==round((float) (value4/7.5345),2) || value3==round((float) (value4/7.5345),2)+1 || value3==round((float) (value4/7.5345),2)-1) {
                            amount=String.format("%.2f", value3);
                            break;
                        }
                        if (value4==round((float) (value3/7.5345),2) || value4==round((float) (value3/7.5345),2)+1 || value4==round((float) (value3/7.5345),2)-1) {
                            amount=String.format("%.2f", value4);
                            break;
                        }
                        if (value1==value3-value4 || value1==value3+value4) {
                            amount=String.format("%.2f", value1);
                            break;
                        }
                        if (value2==value3-value4 || value1==value3+value4) {
                            amount=String.format("%.2f", value2);
                            break;
                        }
                        if (value1==value2) {
                            amount=String.format("%.2f", value1);
                            break;
                        }
                        if (value1==value3) {
                            amount=String.format("%.2f", value1);
                            break;
                        }
                        if (value1==value4) {
                            amount=String.format("%.2f", value1);
                            break;
                        }
                        if (value2==value3) {
                            amount=String.format("%.2f", value2);
                            break;
                        }
                        if (value3==value4) {
                            amount=String.format("%.2f", value3);
                            break;
                        }
                        if (value2==value4) {
                            amount=String.format("%.2f", value2);
                            break;
                        }
                        if (value1>value2 && value1>value3 && value1>value4) {
                            amount=String.format("%.2f", value1);
                            break;
                        }
                        if (value2>value1 && value2>value3 && value2>value4) {
                            amount=String.format("%.2f", value2);
                            break;
                        }
                        if (value3>value1 && value3>value2 && value3>value4) {
                            amount=String.format("%.2f", value3);
                            break;
                        }
                        if (value4>value1 && value4>value2 && value4>value3) {
                            amount=String.format("%.2f", value4);
                            break;
                        }


                    } catch (Exception e) {
                        //throw new RuntimeException(e);
                        //Toast.makeText(MainActivity.this, e.toString(),
                        //        Toast.LENGTH_SHORT).show();
                    }



                }

                if (amount.equals("")) {

                    for (int i=words.length-3; i>=0; i--) {
                        float value1 = Float.valueOf(0);
                        float value2 = Float.valueOf(0);
                        float value3 = Float.valueOf(0);
                        try {
                            value1 = Float.valueOf(words[i]);
                            value2 = Float.valueOf(words[i+1]);
                            value3 = Float.valueOf(words[i+2]);
                            if (value1==round((float) (value2/7.5345),2) || value1==round((float) (value2/7.5345),2)+1 || value1==round((float) (value2/7.5345),2)-1) {
                                amount=String.format("%.2f", value1);
                                break;
                            }
                            if (value2==round((float) (value1/7.5345),2) || value2==round((float) (value1/7.5345),2)+1 || value2==round((float) (value1/7.5345),2)-1) {
                                amount=String.format("%.2f", value2);
                                break;
                            }
                            if (value2==round((float) (value3/7.5345),2) || value2==round((float) (value3/7.5345),2)+1 || value2==round((float) (value3/7.5345),2)-1) {
                                amount=String.format("%.2f", value2);
                                break;
                            }
                            if (value3==round((float) (value2/7.5345),2) || value3==round((float) (value2/7.5345),2)+1 || value3==round((float) (value2/7.5345),2)-1) {
                                amount=String.format("%.2f", value3);
                                break;
                            }
                            if (value1==value2-value3 || value1==value2+value3) {
                                amount=String.format("%.2f", value1);
                                break;
                            }
                            if (value1==value2) {
                                amount=String.format("%.2f", value1);
                                break;
                            }
                            if (value1==value3) {
                                amount=String.format("%.2f", value1);
                                break;
                            }
                            if (value2==value3) {
                                amount=String.format("%.2f", value2);
                                break;
                            }
                            if (value1>value2 && value1>value3) {
                                amount=String.format("%.2f", value1);
                                break;
                            }
                            if (value2>value1 && value2>value3) {
                                amount=String.format("%.2f", value2);
                                break;
                            }
                            if (value3>value1 && value3>value2) {
                                amount=String.format("%.2f", value3);
                                break;
                            }

                        } catch (Exception e) {
                            //throw new RuntimeException(e);
                            //Toast.makeText(MainActivity.this, e.toString(),
                            //        Toast.LENGTH_SHORT).show();
                        }



                    }

                }

                if (amount.equals("")) {

                    for (int i=words.length-2; i>=0; i--) {
                        float value1 = Float.valueOf(0);
                        float value2 = Float.valueOf(0);
                        try {
                            value1 = Float.valueOf(words[i]);
                            value2 = Float.valueOf(words[i+1]);
                            if (value1==round((float) (value2/7.5345),2) || value1==round((float) (value2/7.5345),2)+1 || value1==round((float) (value2/7.5345),2)-1) {
                                if (isInteger(value1)) {
                                    value1=value1/100;
                                    amount=String.format("%.2f", value1);
                                }
                                break;
                            }
                            if (value2==round((float) (value1/7.5345),2) || value2==round((float) (value1/7.5345),2)+1 || value2==round((float) (value1/7.5345),2)-1) {
                                if (isInteger(value2)) {
                                    value2=value2/100;
                                    amount=String.format("%.2f", value2);
                                }
                                break;
                            }
                            if (value1==value2) {
                                if (isInteger(value1)) {
                                    value1=value1/100;
                                    amount=String.format("%.2f", value1);
                                }
                                break;
                            }
                            if (value1>value2) {
                                if (isInteger(value1)) {
                                    value1=value1/100;
                                    amount=String.format("%.2f", value1);
                                }
                                break;
                            }
                            if (value2>value1) {
                                if (isInteger(value2)) {
                                    value2 = value2 / 100;
                                    amount = String.format("%.2f", value2);
                                }
                                break;
                            }

                        } catch (Exception e) {
                            //throw new RuntimeException(e);
                            //Toast.makeText(MainActivity.this, e.toString(),
                            //        Toast.LENGTH_SHORT).show();
                        }



                    }

                }

                if (amount.equals("")) {

                    for (int i=words.length-1; i>=0; i--) {
                        float value1 = Float.valueOf(0);
                        try {
                            value1 = Float.valueOf(words[i]);
                            amount=String.format("%.2f", value1);
                            if (isInteger(value1)) {
                                value1=value1/100;
                                amount=String.format("%.2f", value1);
                            }
                            break;
                        } catch (Exception e) {
                            //throw new RuntimeException(e);
                            //Toast.makeText(MainActivity.this, e.toString(),
                            //        Toast.LENGTH_SHORT).show();
                        }



                    }

                }


                String finalAmount = amount;
                //runOnUiThread(() -> amountTextView.setText(extractedText));

                if (mainposition<=5 && !finalAmount.equals("")) {
                    lines[mainposition]=finalAmount;
                    mainposition=mainposition+1;
                }
                if (mainposition==6) {
                    mainposition=mainposition+1;
                    finish();
                    Intent intent = new Intent(TextRecognitionActivity.this, TextRecognitionResultActivity.class);
                    startActivity(intent);
                }


            }
        });
    }

    private boolean isAmount(String text) {
        String cleanText = text.replace(",", "");

        // Check if the string matches a valid amount format
        // Example formats: $10.99, 15.50, €25.00, ¥1000
        if (cleanText.matches("^\\$?[0-9]+(\\.[0-9]{2})?$") ||
                cleanText.matches("^[0-9]+(\\.[0-9]{2})?$") ||
                cleanText.matches("^€?[0-9]+(\\.[0-9]{2})?$") ||
                cleanText.matches("^¥?[0-9]+(\\.[0-9]{2})?$")) {
            return true;
        }

        // If none of the formats match, return false
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(TextRecognitionActivity.this, MainActivity.class));
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