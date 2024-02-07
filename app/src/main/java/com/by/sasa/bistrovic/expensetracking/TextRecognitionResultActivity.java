package com.by.sasa.bistrovic.expensetracking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class TextRecognitionResultActivity extends AppCompatActivity {

    TextView amount;

    Button back, cancelamount, enteramount, minusplusamount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_recognition_result);

        // Set the background color of the title bar
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.titleBackground)));

// Set the text color of the title bar
        int titleTextColor = getResources().getColor(R.color.titleText);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='" + titleTextColor + "'>Expense Tracking</font>"));

        amount = findViewById(R.id.amount);

        back = findViewById(R.id.back);

        cancelamount = findViewById(R.id.cancelamount);

        enteramount = findViewById(R.id.enteramount);

        minusplusamount = findViewById(R.id.minusTextRecognitionResult);


        String repeatamount1=TextRecognitionActivity.lines[0];

        String repeatamount2=TextRecognitionActivity.lines[1];

        String repeatamount3=TextRecognitionActivity.lines[2];

        String repeatamount4=TextRecognitionActivity.lines[3];

        String repeatamount5=TextRecognitionActivity.lines[4];

        Integer countamount1=0;

        Integer countamount2=0;

        Integer countamount3=0;

        Integer countamount4=0;

        Integer countamount5=0;

        for (int i=0; i<=4;i++) {
            if (repeatamount1.equals(TextRecognitionActivity.lines[i]) && i!=0) {
                countamount1=countamount1+1;
            }
            if (repeatamount2.equals(TextRecognitionActivity.lines[i]) && i!=1) {
                countamount2=countamount2+1;
            }
            if (repeatamount3.equals(TextRecognitionActivity.lines[i]) && i!=2) {
                countamount3=countamount3+1;
            }
            if (repeatamount4.equals(TextRecognitionActivity.lines[i]) && i!=3) {
                countamount4=countamount4+1;
            }
            if (repeatamount5.equals(TextRecognitionActivity.lines[i]) && i!=4) {
                countamount5=countamount5+1;
            }
        }
        if (countamount1>=countamount2 && countamount1>=countamount3 && countamount1>=countamount4 && countamount1>=countamount5) {
            amount.setText(repeatamount1);
        }
        if (countamount2>=countamount1 && countamount2>=countamount3 && countamount2>=countamount4 && countamount2>=countamount5) {
            amount.setText(repeatamount2);
        }
        if (countamount3>=countamount2 && countamount3>=countamount1 && countamount3>=countamount4 && countamount3>=countamount5) {
            amount.setText(repeatamount3);
        }
        if (countamount4>=countamount3 && countamount4>=countamount1 && countamount4>=countamount2 && countamount4>=countamount5) {
            amount.setText(repeatamount4);
        }
        if (countamount5>=countamount4 && countamount5>=countamount1 && countamount5>=countamount2 && countamount5>=countamount3) {
            amount.setText(repeatamount5);
        }



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextRecognitionActivity.mainposition=0;
                //TextRecognitionActivity.lines.clear();
                finish();
                startActivity(new Intent(TextRecognitionResultActivity.this, TextRecognitionActivity.class));
            }
        });

        cancelamount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextRecognitionActivity.mainposition=0;
                //TextRecognitionActivity.lines.clear();
                finish();
                startActivity(new Intent(TextRecognitionResultActivity.this, MainActivity.class));
            }
        });

        enteramount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextRecognitionActivity.mainposition=0;
                //TextRecognitionActivity.lines.clear();
                finish();
                startActivity(new Intent(TextRecognitionResultActivity.this, MainActivity.class));

                Double amountDouble=Double.valueOf(0);

                try {
                    amountDouble = Double.parseDouble(amount.getText().toString().replaceAll(",", "."));
                } catch (NumberFormatException e) {
                    //throw new RuntimeException(e);
                }

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

        minusplusamount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(amount.getText().toString())) {

                    Double amountDouble = Double.valueOf(0);

                    try {
                        amountDouble = Double.parseDouble(amount.getText().toString().replaceAll(",", "."));
                    } catch (NumberFormatException e) {
                        //throw new RuntimeException(e);
                    }

                    if (amountDouble != 0) {
                        amountDouble = amountDouble * -1;
                    }

                    amount.setText(String.format("%.2f", amountDouble));
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(TextRecognitionResultActivity.this, MainActivity.class));
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