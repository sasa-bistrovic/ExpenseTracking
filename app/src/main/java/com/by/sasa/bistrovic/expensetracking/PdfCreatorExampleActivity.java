package com.by.sasa.bistrovic.expensetracking;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tejpratapsingh.pdfcreator.activity.PDFCreatorActivity;
import com.tejpratapsingh.pdfcreator.utils.PDFUtil;
import com.tejpratapsingh.pdfcreator.views.PDFBody;
import com.tejpratapsingh.pdfcreator.views.PDFFooterView;
import com.tejpratapsingh.pdfcreator.views.PDFHeaderView;
import com.tejpratapsingh.pdfcreator.views.PDFTableView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFHorizontalView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFImageView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFLineSeparatorView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFPageBreakView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFTextView;

import java.io.File;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfCreatorExampleActivity extends PDFCreatorActivity {

    private List<ArrayList<String>> namesAndNumbers = new ArrayList<ArrayList<String>>();
    private Integer numberofItems=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.titleBackground)));

        // Set the text color of the title bar
        int titleTextColor = getResources().getColor(R.color.titleText);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='" + titleTextColor + "'>Expense Tracking</font>"));




        FirebaseApp.initializeApp(PdfCreatorExampleActivity.this);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser usera = firebaseAuth.getCurrentUser();


        FirebaseFirestore firestore3 = FirebaseFirestore.getInstance();

        CollectionReference collectionRef = firestore3.collection(MainActivity.collectionEmail+MainActivity.collectionGroup);

        collectionRef.orderBy("datetime", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(querySnapshot -> {
                    String documentId = "";
                    namesAndNumbers.clear();
                    numberofItems=0;
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String timeStamp = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss").format(document.getDate("datetime"));
                        namesAndNumbers.add(new ArrayList<String>(Arrays.asList(document.getString("name"), timeStamp,String.format("%.2f", document.getDouble("price")))));
                        numberofItems=numberofItems+1;
                    }
                    Collections.sort(namesAndNumbers, new Comparator<ArrayList<String>>() {
                        @Override
                        public int compare(ArrayList<String> o1, ArrayList<String> o2) {
                            return o1.get(0).compareTo(o2.get(0));
                        }
                    });




                    createPDF("test", new PDFUtil.PDFUtilListener() {
                        @Override
                        public void pdfGenerationSuccess(File savedPDFFile) {
                        }

                        @Override
                        public void pdfGenerationFailure(Exception exception) {
                        }
                    });



                })
                .addOnFailureListener(e -> {
                });


    }

    @Override
    protected PDFHeaderView getHeaderView(int pageIndex) {

        PDFHeaderView headerView = new PDFHeaderView(getApplicationContext());

        return headerView;
    }

    @Override
    protected PDFBody getBodyViews() {

        PDFBody pdfBody = new PDFBody();

        if (numberofItems!=0) {

            PDFLineSeparatorView lineSeparatorView3 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);

            int[] widthPercent = {6, 25, 25, 22, 22}; // Sum should be equal to 100%
            String[] textInTable = {"1", "2", "3", "4", "5"};
            PDFTextView pdfTableTitleView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
            pdfTableTitleView.setText("Table Example");

            final PDFPageBreakView pdfPageBreakView = new PDFPageBreakView(getApplicationContext());

            PDFTableView.PDFTableRowView tableHeader = new PDFTableView.PDFTableRowView(getApplicationContext());
            for (String s : textInTable) {
                PDFTextView pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
            }

            PDFTableView.PDFTableRowView tableRowView1 = new PDFTableView.PDFTableRowView(getApplicationContext());
            for (String s : textInTable) {
                PDFTextView pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
            }

            PDFTableView tableView = new PDFTableView(getApplicationContext(), tableHeader, tableRowView1);


            Integer numberofItems1 = 0;
            Double subtotal = Double.valueOf(0);
            Double total = Double.valueOf(0);

            String alias = "";
            for (int i = 0; i < numberofItems; i++) {
                Double price = Double.valueOf(0);
                try {
                    price = Double.parseDouble(namesAndNumbers.get(i).get(2).replaceAll(",", "."));
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
                subtotal = subtotal + price;
                total = total + price;
                numberofItems1 = numberofItems1 + 1;
                if (alias.equals(namesAndNumbers.get(i).get(0))) {
                    PDFTableView.PDFTableRowView tableRowView2 = new PDFTableView.PDFTableRowView(getApplicationContext());

                    PDFTextView pdfTextView1 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                    pdfTextView1.setText(Integer.toString(numberofItems1));
                    pdfTextView1.getView().setGravity(Gravity.CENTER);
                    pdfTextView1.setBackgroundColor(Color.WHITE);
                    tableRowView2.addToRow(pdfTextView1);
                    PDFTextView pdfTextView2 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                    DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");

                    Date date = null;

                    try {
                        date = dateFormat.parse(namesAndNumbers.get(i).get(1));
                        System.out.println(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    String timeStamp1 = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss").format(date);

                    DateFormat dateFormat2 = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault());

                    String timeStamp = dateFormat2.format(date);

                    pdfTextView2.setText(timeStamp);
                    pdfTextView2.getView().setGravity(Gravity.LEFT);
                    pdfTextView2.setBackgroundColor(Color.WHITE);
                    tableRowView2.addToRow(pdfTextView2);
                    PDFTextView pdfTextView3 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                    pdfTextView3.setText(namesAndNumbers.get(i).get(0));
                    pdfTextView3.getView().setGravity(Gravity.LEFT);
                    pdfTextView3.setBackgroundColor(Color.WHITE);
                    tableRowView2.addToRow(pdfTextView3);
                    PDFTextView pdfTextView4 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

                    NumberFormat numberFormat3 = NumberFormat.getInstance(Locale.getDefault());

                    // Enable grouping (thousand separators)
                    numberFormat3.setGroupingUsed(true);

                    numberFormat3.setMinimumFractionDigits(2);
                    numberFormat3.setMaximumFractionDigits(2);

                    pdfTextView4.setText(numberFormat3.format(price) + " " + MainActivity.collectionaccountMeasuringUnit);
                    if (price < 0) {
                        pdfTextView4.setTextColor(Color.RED);
                    } else {
                        pdfTextView4.setTextColor(Color.rgb(0, 100, 0));
                    }
                    pdfTextView4.getView().setGravity(Gravity.RIGHT);
                    pdfTextView4.setBackgroundColor(Color.WHITE);
                    tableRowView2.addToRow(pdfTextView4);
                    PDFTextView pdfTextView5 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                    NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

                    // Enable grouping (thousand separators)
                    numberFormat.setGroupingUsed(true);

                    numberFormat.setMinimumFractionDigits(2);
                    numberFormat.setMaximumFractionDigits(2);

                    pdfTextView5.setText(numberFormat.format(subtotal) + " " + MainActivity.collectionaccountMeasuringUnit);
                    if (subtotal < 0) {
                        pdfTextView5.setTextColor(Color.RED);
                    } else {
                        pdfTextView5.setTextColor(Color.rgb(0, 100, 0));
                    }
                    pdfTextView5.getView().setGravity(Gravity.RIGHT);
                    pdfTextView5.setBackgroundColor(Color.WHITE);
                    tableRowView2.addToRow(pdfTextView5);
                    tableView.addRow(tableRowView2);
                    if (numberofItems == i + 1) {
                        PDFLineSeparatorView lineSeparatorView4 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
                        tableView.addSeparatorRow(lineSeparatorView4);

                        PDFTableView.PDFTableRowView tableRowView3c = new PDFTableView.PDFTableRowView(getApplicationContext());
                        PDFTextView pdfTextView1c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView1c.setText("");
                        pdfTextView1c.getView().setGravity(Gravity.CENTER);
                        pdfTextView1c.setBackgroundColor(Color.WHITE);
                        tableRowView3c.addToRow(pdfTextView1c);
                        PDFTextView pdfTextView2c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView2c.setText("");
                        pdfTextView2c.getView().setGravity(Gravity.LEFT);
                        pdfTextView2c.setBackgroundColor(Color.WHITE);
                        tableRowView3c.addToRow(pdfTextView2c);
                        PDFTextView pdfTextView3c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView3c.setText("");
                        pdfTextView3c.getView().setGravity(Gravity.LEFT);
                        pdfTextView3c.setBackgroundColor(Color.WHITE);
                        tableRowView3c.addToRow(pdfTextView3c);
                        PDFTextView pdfTextView4c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView4c.setText("SUBTOTAL");
                        pdfTextView4c.getView().setGravity(Gravity.RIGHT);
                        pdfTextView4c.setBackgroundColor(Color.LTGRAY);
                        tableRowView3c.addToRow(pdfTextView4c);
                        PDFTextView pdfTextView5c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

                        NumberFormat numberFormat2 = NumberFormat.getInstance(Locale.getDefault());

                        // Enable grouping (thousand separators)
                        numberFormat2.setGroupingUsed(true);

                        numberFormat2.setMinimumFractionDigits(2);
                        numberFormat2.setMaximumFractionDigits(2);

                        pdfTextView5c.setText(numberFormat2.format(subtotal) + " " + MainActivity.collectionaccountMeasuringUnit);
                        if (subtotal < 0) {
                            pdfTextView5c.setTextColor(Color.RED);
                        } else {
                            pdfTextView5c.setTextColor(Color.rgb(0, 100, 0));
                        }
                        pdfTextView5c.getView().setGravity(Gravity.RIGHT);
                        pdfTextView5c.setBackgroundColor(Color.LTGRAY);
                        tableRowView3c.addToRow(pdfTextView5c);

                        tableView.addRow(tableRowView3c);

                        PDFTableView.PDFTableRowView tableRowViewa = new PDFTableView.PDFTableRowView(getApplicationContext());

                        PDFTextView pdfTextView1a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView1a.setText("");
                        pdfTextView1a.getView().setGravity(Gravity.CENTER);
                        pdfTextView1a.setBackgroundColor(Color.WHITE);
                        tableRowViewa.addToRow(pdfTextView1a);
                        PDFTextView pdfTextView2a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView2a.setText("");
                        pdfTextView2a.getView().setGravity(Gravity.LEFT);
                        pdfTextView2a.setBackgroundColor(Color.WHITE);
                        tableRowViewa.addToRow(pdfTextView2a);
                        PDFTextView pdfTextView3a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView3a.setText("");
                        pdfTextView3a.getView().setGravity(Gravity.LEFT);
                        pdfTextView3a.setBackgroundColor(Color.WHITE);
                        tableRowViewa.addToRow(pdfTextView3a);
                        PDFTextView pdfTextView4a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView4a.setText("TOTAL");
                        pdfTextView4a.getView().setGravity(Gravity.RIGHT);
                        pdfTextView4a.setBackgroundColor(Color.LTGRAY);
                        tableRowViewa.addToRow(pdfTextView4a);
                        PDFTextView pdfTextView5a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

                        NumberFormat numberFormat5 = NumberFormat.getInstance(Locale.getDefault());

                        numberFormat5.setGroupingUsed(true);

                        numberFormat5.setMinimumFractionDigits(2);
                        numberFormat5.setMaximumFractionDigits(2);

                        pdfTextView5a.setText(numberFormat5.format(total) + " " + MainActivity.collectionaccountMeasuringUnit);
                        if (total < 0) {
                            pdfTextView5a.setTextColor(Color.RED);
                        } else {
                            pdfTextView5a.setTextColor(Color.rgb(0, 100, 0));
                        }
                        pdfTextView5a.getView().setGravity(Gravity.RIGHT);
                        pdfTextView5a.setBackgroundColor(Color.LTGRAY);
                        tableRowViewa.addToRow(pdfTextView5a);

                        tableView.addRow(tableRowViewa);

                        PDFLineSeparatorView lineSeparatorView4d = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
                        //pdfBody.addView(lineSeparatorView4);
                        tableView.addSeparatorRow(lineSeparatorView4d);

                        tableView.setColumnWidth(widthPercent);
                        pdfBody.addView(tableView);

                        return pdfBody;
                    } else if (!alias.equals(namesAndNumbers.get(i + 1).get(0))) {
                        PDFLineSeparatorView lineSeparatorView10 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
                        tableView.addSeparatorRow(lineSeparatorView10);

                        PDFTableView.PDFTableRowView tableRowView3c = new PDFTableView.PDFTableRowView(getApplicationContext());
                        PDFTextView pdfTextView1c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView1c.setText("");
                        pdfTextView1c.getView().setGravity(Gravity.CENTER);
                        pdfTextView1c.setBackgroundColor(Color.WHITE);
                        tableRowView3c.addToRow(pdfTextView1c);
                        PDFTextView pdfTextView2c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView2c.setText("");
                        pdfTextView2c.getView().setGravity(Gravity.LEFT);
                        pdfTextView2c.setBackgroundColor(Color.WHITE);
                        tableRowView3c.addToRow(pdfTextView2c);
                        PDFTextView pdfTextView3c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView3c.setText("");
                        pdfTextView3c.getView().setGravity(Gravity.LEFT);
                        pdfTextView3c.setBackgroundColor(Color.WHITE);
                        tableRowView3c.addToRow(pdfTextView3c);
                        PDFTextView pdfTextView4c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView4c.setText("SUBTOTAL");
                        pdfTextView4c.getView().setGravity(Gravity.RIGHT);
                        pdfTextView4c.setBackgroundColor(Color.LTGRAY);
                        tableRowView3c.addToRow(pdfTextView4c);
                        PDFTextView pdfTextView5c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

                        NumberFormat numberFormat2 = NumberFormat.getInstance(Locale.getDefault());

                        numberFormat2.setGroupingUsed(true);

                        numberFormat2.setMinimumFractionDigits(2);
                        numberFormat2.setMaximumFractionDigits(2);

                        pdfTextView5c.setText(numberFormat2.format(subtotal) + " " + MainActivity.collectionaccountMeasuringUnit);
                        if (subtotal < 0) {
                            pdfTextView5c.setTextColor(Color.RED);
                        } else {
                            pdfTextView5c.setTextColor(Color.rgb(0, 100, 0));
                        }
                        pdfTextView5c.getView().setGravity(Gravity.RIGHT);
                        pdfTextView5c.setBackgroundColor(Color.LTGRAY);
                        tableRowView3c.addToRow(pdfTextView5c);

                        tableView.addRow(tableRowView3c);
                        subtotal = Double.valueOf(0);
                        numberofItems1 = 0;
                    }
                } else {
                    if (alias.equals("")) {

                        PDFTableView.PDFTableRowView tableRowView5 = new PDFTableView.PDFTableRowView(getApplicationContext());

                        PDFTextView pdfTextView1f = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView1f.setText("#");
                        pdfTextView1f.getView().setGravity(Gravity.CENTER);
                        pdfTextView1f.setBackgroundColor(Color.LTGRAY);
                        tableRowView5.addToRow(pdfTextView1f);
                        PDFTextView pdfTextView2f = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView2f.setText("Date Time");
                        pdfTextView2f.getView().setGravity(Gravity.LEFT);
                        pdfTextView2f.setBackgroundColor(Color.LTGRAY);
                        tableRowView5.addToRow(pdfTextView2f);
                        PDFTextView pdfTextView3f = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView3f.setText("Alias");
                        pdfTextView3f.getView().setGravity(Gravity.LEFT);
                        pdfTextView3f.setBackgroundColor(Color.LTGRAY);
                        tableRowView5.addToRow(pdfTextView3f);
                        PDFTextView pdfTextView4f = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView4f.setText("Expense");
                        pdfTextView4f.getView().setGravity(Gravity.RIGHT);
                        pdfTextView4f.setBackgroundColor(Color.LTGRAY);
                        tableRowView5.addToRow(pdfTextView4f);
                        PDFTextView pdfTextView5f = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView5f.setText("Saldo");
                        pdfTextView5f.getView().setGravity(Gravity.RIGHT);
                        pdfTextView5f.setBackgroundColor(Color.LTGRAY);
                        tableRowView5.addToRow(pdfTextView5f);
                        tableView.addRow(tableRowView5);

                        PDFLineSeparatorView lineSeparatorView5 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
                        tableView.addSeparatorRow(lineSeparatorView5);

                        PDFTableView.PDFTableRowView tableRowView3 = new PDFTableView.PDFTableRowView(getApplicationContext());

                        PDFTextView pdfTextView1a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView1a.setText(Integer.toString(numberofItems1));
                        pdfTextView1a.getView().setGravity(Gravity.CENTER);
                        pdfTextView1a.setBackgroundColor(Color.WHITE);
                        tableRowView3.addToRow(pdfTextView1a);
                        PDFTextView pdfTextView2a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");

                        Date date = null;

                        try {
                            date = dateFormat.parse(namesAndNumbers.get(i).get(1));
                            System.out.println(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        String timeStamp1 = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss").format(date);

                        DateFormat dateFormat2 = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault());

                        String timeStamp = dateFormat2.format(date);

                        pdfTextView2a.setText(timeStamp);
                        pdfTextView2a.getView().setGravity(Gravity.LEFT);
                        pdfTextView2a.setBackgroundColor(Color.WHITE);
                        tableRowView3.addToRow(pdfTextView2a);
                        PDFTextView pdfTextView3a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView3a.setText(namesAndNumbers.get(i).get(0));
                        pdfTextView3a.getView().setGravity(Gravity.LEFT);
                        pdfTextView3a.setBackgroundColor(Color.WHITE);
                        tableRowView3.addToRow(pdfTextView3a);
                        PDFTextView pdfTextView4a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

                        NumberFormat numberFormat3 = NumberFormat.getInstance(Locale.getDefault());

                        numberFormat3.setGroupingUsed(true);

                        numberFormat3.setMinimumFractionDigits(2);
                        numberFormat3.setMaximumFractionDigits(2);

                        pdfTextView4a.setText(numberFormat3.format(price) + " " + MainActivity.collectionaccountMeasuringUnit);
                        if (price < 0) {
                            pdfTextView4a.setTextColor(Color.RED);
                        } else {
                            pdfTextView4a.setTextColor(Color.rgb(0, 100, 0));
                        }
                        pdfTextView4a.getView().setGravity(Gravity.RIGHT);
                        pdfTextView4a.setBackgroundColor(Color.WHITE);
                        tableRowView3.addToRow(pdfTextView4a);
                        PDFTextView pdfTextView5a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

                        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

                        numberFormat.setGroupingUsed(true);

                        numberFormat.setMinimumFractionDigits(2);
                        numberFormat.setMaximumFractionDigits(2);

                        pdfTextView5a.setText(numberFormat.format(subtotal) + " " + MainActivity.collectionaccountMeasuringUnit);
                        if (subtotal < 0) {
                            pdfTextView5a.setTextColor(Color.RED);
                        } else {
                            pdfTextView5a.setTextColor(Color.rgb(0, 100, 0));
                        }
                        pdfTextView5a.getView().setGravity(Gravity.RIGHT);
                        pdfTextView5a.setBackgroundColor(Color.WHITE);
                        tableRowView3.addToRow(pdfTextView5a);
                        tableView.addRow(tableRowView3);
                        alias = namesAndNumbers.get(i).get(0);
                        if (numberofItems == i + 1) {
                            PDFLineSeparatorView lineSeparatorView10 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
                            tableView.addSeparatorRow(lineSeparatorView10);

                            PDFTableView.PDFTableRowView tableRowView3c = new PDFTableView.PDFTableRowView(getApplicationContext());
                            PDFTextView pdfTextView1c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView1c.setText("");
                            pdfTextView1c.getView().setGravity(Gravity.CENTER);
                            pdfTextView1c.setBackgroundColor(Color.WHITE);
                            tableRowView3c.addToRow(pdfTextView1c);
                            PDFTextView pdfTextView2c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView2c.setText("");
                            pdfTextView2c.getView().setGravity(Gravity.LEFT);
                            pdfTextView2c.setBackgroundColor(Color.WHITE);
                            tableRowView3c.addToRow(pdfTextView2c);
                            PDFTextView pdfTextView3c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView3c.setText("");
                            pdfTextView3c.getView().setGravity(Gravity.LEFT);
                            pdfTextView3c.setBackgroundColor(Color.WHITE);
                            tableRowView3c.addToRow(pdfTextView3c);
                            PDFTextView pdfTextView4c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView4c.setText("SUBTOTAL");
                            pdfTextView4c.getView().setGravity(Gravity.RIGHT);
                            pdfTextView4c.setBackgroundColor(Color.LTGRAY);
                            tableRowView3c.addToRow(pdfTextView4c);
                            PDFTextView pdfTextView5c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

                            NumberFormat numberFormat2 = NumberFormat.getInstance(Locale.getDefault());

                            numberFormat2.setGroupingUsed(true);

                            numberFormat2.setMinimumFractionDigits(2);
                            numberFormat2.setMaximumFractionDigits(2);

                            pdfTextView5c.setText(numberFormat2.format(subtotal) + " " + MainActivity.collectionaccountMeasuringUnit);
                            if (subtotal < 0) {
                                pdfTextView5c.setTextColor(Color.RED);
                            } else {
                                pdfTextView5c.setTextColor(Color.rgb(0, 100, 0));
                            }
                            pdfTextView5c.getView().setGravity(Gravity.RIGHT);
                            pdfTextView5c.setBackgroundColor(Color.LTGRAY);
                            tableRowView3c.addToRow(pdfTextView5c);

                            tableView.addRow(tableRowView3c);

                            PDFTableView.PDFTableRowView tableRowViewa = new PDFTableView.PDFTableRowView(getApplicationContext());

                            PDFTextView pdfTextView1d = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView1d.setText("");
                            pdfTextView1d.getView().setGravity(Gravity.CENTER);
                            pdfTextView1d.setBackgroundColor(Color.WHITE);
                            tableRowViewa.addToRow(pdfTextView1d);
                            PDFTextView pdfTextView2d = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView2d.setText("");
                            pdfTextView2d.getView().setGravity(Gravity.LEFT);
                            pdfTextView2d.setBackgroundColor(Color.WHITE);
                            tableRowViewa.addToRow(pdfTextView2d);
                            PDFTextView pdfTextView3d = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView3d.setText("");
                            pdfTextView3d.getView().setGravity(Gravity.LEFT);
                            pdfTextView3d.setBackgroundColor(Color.WHITE);
                            tableRowViewa.addToRow(pdfTextView3d);
                            PDFTextView pdfTextView4d = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView4d.setText("TOTAL");
                            pdfTextView4d.getView().setGravity(Gravity.RIGHT);
                            pdfTextView4d.setBackgroundColor(Color.LTGRAY);
                            tableRowViewa.addToRow(pdfTextView4d);
                            PDFTextView pdfTextView5d = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

                            NumberFormat numberFormat5 = NumberFormat.getInstance(Locale.getDefault());

                            numberFormat5.setGroupingUsed(true);

                            numberFormat5.setMinimumFractionDigits(2);
                            numberFormat5.setMaximumFractionDigits(2);

                            pdfTextView5d.setText(numberFormat5.format(total) + " " + MainActivity.collectionaccountMeasuringUnit);
                            if (total < 0) {
                                pdfTextView5d.setTextColor(Color.RED);
                            } else {
                                pdfTextView5d.setTextColor(Color.rgb(0, 100, 0));
                            }
                            pdfTextView5d.getView().setGravity(Gravity.RIGHT);
                            pdfTextView5d.setBackgroundColor(Color.LTGRAY);
                            tableRowViewa.addToRow(pdfTextView5d);

                            tableView.addRow(tableRowViewa);

                            PDFLineSeparatorView lineSeparatorView4d = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
                            tableView.addSeparatorRow(lineSeparatorView4d);

                            tableView.setColumnWidth(widthPercent);
                            pdfBody.addView(tableView);

                            return pdfBody;
                        } else if (!alias.equals(namesAndNumbers.get(i + 1).get(0))) {
                            PDFLineSeparatorView lineSeparatorView10 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
                            tableView.addSeparatorRow(lineSeparatorView10);

                            PDFTableView.PDFTableRowView tableRowView3c = new PDFTableView.PDFTableRowView(getApplicationContext());
                            PDFTextView pdfTextView1c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView1c.setText("");
                            pdfTextView1c.getView().setGravity(Gravity.CENTER);
                            pdfTextView1c.setBackgroundColor(Color.WHITE);
                            tableRowView3c.addToRow(pdfTextView1c);
                            PDFTextView pdfTextView2c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView2c.setText("");
                            pdfTextView2c.getView().setGravity(Gravity.LEFT);
                            pdfTextView2c.setBackgroundColor(Color.WHITE);
                            tableRowView3c.addToRow(pdfTextView2c);
                            PDFTextView pdfTextView3c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView3c.setText("");
                            pdfTextView3c.getView().setGravity(Gravity.LEFT);
                            pdfTextView3c.setBackgroundColor(Color.WHITE);
                            tableRowView3c.addToRow(pdfTextView3c);
                            PDFTextView pdfTextView4c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView4c.setText("SUBTOTAL");
                            pdfTextView4c.getView().setGravity(Gravity.RIGHT);
                            pdfTextView4c.setBackgroundColor(Color.LTGRAY);
                            tableRowView3c.addToRow(pdfTextView4c);
                            PDFTextView pdfTextView5c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

                            NumberFormat numberFormat2 = NumberFormat.getInstance(Locale.getDefault());

                            // Enable grouping (thousand separators)
                            numberFormat2.setGroupingUsed(true);

                            numberFormat2.setMinimumFractionDigits(2);
                            numberFormat2.setMaximumFractionDigits(2);

                            pdfTextView5c.setText(numberFormat2.format(subtotal) + " " + MainActivity.collectionaccountMeasuringUnit);
                            if (subtotal < 0) {
                                pdfTextView5c.setTextColor(Color.RED);
                            } else {
                                pdfTextView5c.setTextColor(Color.rgb(0, 100, 0));
                            }
                            pdfTextView5c.getView().setGravity(Gravity.RIGHT);
                            pdfTextView5c.setBackgroundColor(Color.LTGRAY);
                            tableRowView3c.addToRow(pdfTextView5c);

                            tableView.addRow(tableRowView3c);
                            subtotal = Double.valueOf(0);
                            numberofItems1 = 0;
                        }
                    } else {
                        PDFLineSeparatorView lineSeparatorView4a = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
                        tableView.addSeparatorRow(lineSeparatorView4a);

                        PDFTableView.PDFTableRowView tableRowView3a = new PDFTableView.PDFTableRowView(getApplicationContext());

                        PDFTextView pdfTextView1a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView1a.setText("#");
                        pdfTextView1a.getView().setGravity(Gravity.CENTER);
                        pdfTextView1a.setBackgroundColor(Color.LTGRAY);
                        tableRowView3a.addToRow(pdfTextView1a);
                        PDFTextView pdfTextView2a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView2a.setText("Date Time");
                        pdfTextView2a.getView().setGravity(Gravity.LEFT);
                        pdfTextView2a.setBackgroundColor(Color.LTGRAY);
                        tableRowView3a.addToRow(pdfTextView2a);
                        PDFTextView pdfTextView3a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView3a.setText("Alias");
                        pdfTextView3a.getView().setGravity(Gravity.LEFT);
                        pdfTextView3a.setBackgroundColor(Color.LTGRAY);
                        tableRowView3a.addToRow(pdfTextView3a);
                        PDFTextView pdfTextView4a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView4a.setText("Expense");
                        pdfTextView4a.getView().setGravity(Gravity.RIGHT);
                        pdfTextView4a.setBackgroundColor(Color.LTGRAY);
                        tableRowView3a.addToRow(pdfTextView4a);
                        PDFTextView pdfTextView5a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView5a.setText("Saldo");
                        pdfTextView5a.getView().setGravity(Gravity.RIGHT);
                        pdfTextView5a.setBackgroundColor(Color.LTGRAY);
                        tableRowView3a.addToRow(pdfTextView5a);
                        tableView.addRow(tableRowView3a);
                        PDFLineSeparatorView lineSeparatorView5 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
                        tableView.addSeparatorRow(lineSeparatorView5);
                        PDFTableView.PDFTableRowView tableRowView5 = new PDFTableView.PDFTableRowView(getApplicationContext());
                        PDFTextView pdfTextView1b = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView1b.setText(Integer.toString(numberofItems1));
                        pdfTextView1b.getView().setGravity(Gravity.CENTER);
                        pdfTextView1b.setBackgroundColor(Color.WHITE);
                        tableRowView5.addToRow(pdfTextView1b);
                        PDFTextView pdfTextView2b = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");

                        Date date = null;

                        try {
                            date = dateFormat.parse(namesAndNumbers.get(i).get(1));
                            System.out.println(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        String timeStamp1 = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss").format(date);

                        DateFormat dateFormat2 = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault());

                        String timeStamp = dateFormat2.format(date);

                        pdfTextView2b.setText(timeStamp);
                        pdfTextView2b.getView().setGravity(Gravity.LEFT);
                        pdfTextView2b.setBackgroundColor(Color.WHITE);
                        tableRowView5.addToRow(pdfTextView2b);
                        PDFTextView pdfTextView3b = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                        pdfTextView3b.setText(namesAndNumbers.get(i).get(0));
                        pdfTextView3b.getView().setGravity(Gravity.LEFT);
                        pdfTextView3b.setBackgroundColor(Color.WHITE);
                        tableRowView5.addToRow(pdfTextView3b);
                        PDFTextView pdfTextView4b = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

                        NumberFormat numberFormat3 = NumberFormat.getInstance(Locale.getDefault());

                        numberFormat3.setGroupingUsed(true);

                        numberFormat3.setMinimumFractionDigits(2);
                        numberFormat3.setMaximumFractionDigits(2);

                        pdfTextView4b.setText(numberFormat3.format(price) + " " + MainActivity.collectionaccountMeasuringUnit);
                        pdfTextView4b.getView().setGravity(Gravity.RIGHT);
                        if (price < 0) {
                            pdfTextView4b.setTextColor(Color.RED);
                        } else {
                            pdfTextView4b.setTextColor(Color.rgb(0, 100, 0));
                        }
                        pdfTextView4b.setBackgroundColor(Color.WHITE);
                        tableRowView5.addToRow(pdfTextView4b);
                        PDFTextView pdfTextView5b = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

                        NumberFormat numberFormat2 = NumberFormat.getInstance(Locale.getDefault());

                        numberFormat2.setGroupingUsed(true);

                        numberFormat2.setMinimumFractionDigits(2);
                        numberFormat2.setMaximumFractionDigits(2);

                        pdfTextView5b.setText(numberFormat2.format(subtotal) + " " + MainActivity.collectionaccountMeasuringUnit);
                        if (subtotal < 0) {
                            pdfTextView5b.setTextColor(Color.RED);
                        } else {
                            pdfTextView5b.setTextColor(Color.rgb(0, 100, 0));
                        }
                        pdfTextView5b.getView().setGravity(Gravity.RIGHT);
                        pdfTextView5b.setBackgroundColor(Color.WHITE);
                        tableRowView5.addToRow(pdfTextView5b);
                        tableView.addRow(tableRowView5);
                        alias = namesAndNumbers.get(i).get(0);
                        if (numberofItems == i + 1) {
                            PDFLineSeparatorView lineSeparatorView10 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
                            tableView.addSeparatorRow(lineSeparatorView10);

                            PDFTableView.PDFTableRowView tableRowView3c = new PDFTableView.PDFTableRowView(getApplicationContext());
                            PDFTextView pdfTextView1c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView1c.setText("");
                            pdfTextView1c.getView().setGravity(Gravity.CENTER);
                            pdfTextView1c.setBackgroundColor(Color.WHITE);
                            tableRowView3c.addToRow(pdfTextView1c);
                            PDFTextView pdfTextView2c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView2c.setText("");
                            pdfTextView2c.getView().setGravity(Gravity.LEFT);
                            pdfTextView2c.setBackgroundColor(Color.WHITE);
                            tableRowView3c.addToRow(pdfTextView2c);
                            PDFTextView pdfTextView3c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView3c.setText("");
                            pdfTextView3c.getView().setGravity(Gravity.LEFT);
                            pdfTextView3c.setBackgroundColor(Color.WHITE);
                            tableRowView3c.addToRow(pdfTextView3c);
                            PDFTextView pdfTextView4c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView4c.setText("SUBTOTAL");
                            pdfTextView4c.getView().setGravity(Gravity.RIGHT);
                            pdfTextView4c.setBackgroundColor(Color.LTGRAY);
                            tableRowView3c.addToRow(pdfTextView4c);
                            PDFTextView pdfTextView5c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

                            NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

                            numberFormat.setGroupingUsed(true);

                            numberFormat.setMinimumFractionDigits(2);
                            numberFormat.setMaximumFractionDigits(2);

                            pdfTextView5c.setText(numberFormat.format(subtotal) + " " + MainActivity.collectionaccountMeasuringUnit);
                            if (subtotal < 0) {
                                pdfTextView5c.setTextColor(Color.RED);
                            } else {
                                pdfTextView5c.setTextColor(Color.rgb(0, 100, 0));
                            }
                            pdfTextView5c.getView().setGravity(Gravity.RIGHT);
                            pdfTextView5c.setBackgroundColor(Color.LTGRAY);
                            tableRowView3c.addToRow(pdfTextView5c);

                            tableView.addRow(tableRowView3c);

                            PDFTableView.PDFTableRowView tableRowViewa = new PDFTableView.PDFTableRowView(getApplicationContext());

                            PDFTextView pdfTextView1d = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView1d.setText("");
                            pdfTextView1d.getView().setGravity(Gravity.CENTER);
                            pdfTextView1d.setBackgroundColor(Color.WHITE);
                            tableRowViewa.addToRow(pdfTextView1d);
                            PDFTextView pdfTextView2d = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView2d.setText("");
                            pdfTextView2d.getView().setGravity(Gravity.LEFT);
                            pdfTextView2d.setBackgroundColor(Color.WHITE);
                            tableRowViewa.addToRow(pdfTextView2d);
                            PDFTextView pdfTextView3d = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView3d.setText("");
                            pdfTextView3d.getView().setGravity(Gravity.LEFT);
                            pdfTextView3d.setBackgroundColor(Color.WHITE);
                            tableRowViewa.addToRow(pdfTextView3d);
                            PDFTextView pdfTextView4d = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView4d.setText("TOTAL");
                            pdfTextView4d.getView().setGravity(Gravity.RIGHT);
                            pdfTextView4d.setBackgroundColor(Color.LTGRAY);
                            tableRowViewa.addToRow(pdfTextView4d);
                            PDFTextView pdfTextView5d = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

                            NumberFormat numberFormat5 = NumberFormat.getInstance(Locale.getDefault());

                            numberFormat5.setGroupingUsed(true);

                            numberFormat5.setMinimumFractionDigits(2);
                            numberFormat5.setMaximumFractionDigits(2);

                            pdfTextView5d.setText(numberFormat5.format(total) + " " + MainActivity.collectionaccountMeasuringUnit);
                            if (total < 0) {
                                pdfTextView5d.setTextColor(Color.RED);
                            } else {
                                pdfTextView5d.setTextColor(Color.rgb(0, 100, 0));
                            }
                            pdfTextView5d.getView().setGravity(Gravity.RIGHT);
                            pdfTextView5d.setBackgroundColor(Color.LTGRAY);
                            tableRowViewa.addToRow(pdfTextView5d);

                            tableView.addRow(tableRowViewa);

                            PDFLineSeparatorView lineSeparatorView4d = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
                            tableView.addSeparatorRow(lineSeparatorView4d);
                            tableView.setColumnWidth(widthPercent);
                            pdfBody.addView(tableView);

                            return pdfBody;
                        } else if (!alias.equals(namesAndNumbers.get(i + 1).get(0))) {
                            PDFLineSeparatorView lineSeparatorView10 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
                            tableView.addSeparatorRow(lineSeparatorView10);

                            PDFTableView.PDFTableRowView tableRowView3c = new PDFTableView.PDFTableRowView(getApplicationContext());
                            PDFTextView pdfTextView1c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView1c.setText("");
                            pdfTextView1c.getView().setGravity(Gravity.CENTER);
                            pdfTextView1c.setBackgroundColor(Color.WHITE);
                            tableRowView3c.addToRow(pdfTextView1c);
                            PDFTextView pdfTextView2c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView2c.setText("");
                            pdfTextView2c.getView().setGravity(Gravity.LEFT);
                            pdfTextView2c.setBackgroundColor(Color.WHITE);
                            tableRowView3c.addToRow(pdfTextView2c);
                            PDFTextView pdfTextView3c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView3c.setText("");
                            pdfTextView3c.getView().setGravity(Gravity.LEFT);
                            pdfTextView3c.setBackgroundColor(Color.WHITE);
                            tableRowView3c.addToRow(pdfTextView3c);
                            PDFTextView pdfTextView4c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                            pdfTextView4c.setText("SUBTOTAL");
                            pdfTextView4c.getView().setGravity(Gravity.RIGHT);
                            pdfTextView4c.setBackgroundColor(Color.LTGRAY);
                            tableRowView3c.addToRow(pdfTextView4c);
                            PDFTextView pdfTextView5c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

                            NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

                            // Enable grouping (thousand separators)
                            numberFormat.setGroupingUsed(true);

                            numberFormat.setMinimumFractionDigits(2);
                            numberFormat.setMaximumFractionDigits(2);

                            pdfTextView5c.setText(numberFormat.format(subtotal) + " " + MainActivity.collectionaccountMeasuringUnit);
                            if (subtotal < 0) {
                                pdfTextView5c.setTextColor(Color.RED);
                            } else {
                                pdfTextView5c.setTextColor(Color.rgb(0, 100, 0));
                            }
                            pdfTextView5c.getView().setGravity(Gravity.RIGHT);
                            pdfTextView5c.setBackgroundColor(Color.LTGRAY);
                            tableRowView3c.addToRow(pdfTextView5c);

                            tableView.addRow(tableRowView3c);
                            subtotal = Double.valueOf(0);
                            numberofItems1 = 0;
                        }
                    }
                }
            }
            pdfBody.addView(tableView);
        }

        return pdfBody;
    }

    @Override
    protected PDFFooterView getFooterView(int pageIndex) {
        PDFFooterView footerView = new PDFFooterView(getApplicationContext());

        return footerView;
    }

    @Nullable
    @Override
    protected PDFImageView getWatermarkView(int forPage) {
        PDFImageView pdfImageView = new PDFImageView(getApplicationContext());
        return pdfImageView;
    }

    @Override
    protected void onNextClicked(final File savedPDFFile) {
        Uri pdfUri = Uri.fromFile(savedPDFFile);

        Intent intentPdfViewer = new Intent(PdfCreatorExampleActivity.this, PdfViewerExampleActivity.class);
        intentPdfViewer.putExtra(PdfViewerExampleActivity.PDF_FILE_URI, pdfUri);

        startActivity(intentPdfViewer);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }
}