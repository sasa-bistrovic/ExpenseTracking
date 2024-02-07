package com.by.sasa.bistrovic.expensetracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import com.by.sasa.bistrovic.expensetracking.R.id;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
//import com.google.firebase.auth.UserRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int MICROPHONE_PERMISSION_REQUEST_CODE = 101;

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private FirestoreAdapter adapter;
    private FirebaseFirestore firestore;
    public static CollectionReference collectionReference;

    private EditText editName, editNumber, editPrice;
    public static String collectionEmail,collectionGroup,collectionAlias,collectiongroupEmail,collectionaccountEmail,collectionaccountPassword, collectionaccountMeasuringUnit, addcompletedStr;

    public static Double collectionLimit=Double.valueOf(0);

    private Button btnAdd;

    public static String id1 = "test_channel_01";
    public static String id2 = "test_channel_02";
    public static String id3 = "test_channel_03";

    public static Timer myTimer;

    public static String extrasTitle, extrasDescription;

    public static List<ArrayList<String>> emailGroups = new ArrayList<ArrayList<String>>();
    NotificationManager nm;
    int NotID = 1;
    ActivityResultLauncher<String[]> rpl;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.POST_NOTIFICATIONS};
    //public static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the background color of the title bar
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.titleBackground)));

// Set the text color of the title bar
        int titleTextColor = getResources().getColor(R.color.titleText);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='" + titleTextColor + "'>Expense Tracking</font>"));

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // for notifications permission now required in api 33
        //this allows us to check with multiple permissions, but in this case (currently) only need 1.
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> isGranted) {
                        boolean granted = true;
                        for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {
                            //logthis(x.getKey() + " is " + x.getValue());
                            if (!x.getValue()) granted = false;
                        }
                        //if (granted)
                            //logthis("Permissions granted for api 33+");
                    }
                }
        );


        FirebaseApp.initializeApp(MainActivity.this);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser usera = firebaseAuth.getCurrentUser();

        if (collectionEmail==null) {
            collectionEmail = "empty";
            collectionaccountEmail = "empty";
            collectionaccountPassword = "empty";
            collectionGroup = "empty";
            collectionAlias = "empty";
            collectiongroupEmail = "empty";
            collectionaccountMeasuringUnit = "empty";
        }

            firestore = FirebaseFirestore.getInstance();
            collectionReference = firestore.collection(collectionEmail + collectionGroup);

            //editName = findViewById(R.id.editName);
            //editNumber = findViewById(R.id.editNumber);
            //editPrice = findViewById(R.id.editPrice);
            btnAdd = findViewById(R.id.btnAdd);

            recyclerView = findViewById(R.id.recyclerView);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            //recyclerView.setLayoutManager(new LinearLayoutManager(this));

            Query query = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

            adapter = new FirestoreAdapter(query);
            recyclerView.setAdapter(adapter);

        if (usera!=null) {

            FirebaseFirestore firestore3 = FirebaseFirestore.getInstance();

            CollectionReference collectionRef = firestore3.collection(usera.getEmail());
            //String fieldName = "name";
            //String desiredValue = name;

            //final String[] documentId2 = {""};
            final String[] Found = {""};
            collectionRef.get()
                    .addOnSuccessListener(querySnapshot -> {
                        String documentId = "";
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            // Access individual documents here
                            if ("current".equals(document.getString("current"))) {
                                if (collectionaccountEmail.equals(document.getString("accountemail")) && collectionaccountPassword.equals(document.getString("accountpassword")) && collectionEmail.equals(document.getString("groupemail")) && collectionGroup.equals(document.getString("name")) && collectionAlias.equals(document.getString("alias")) && collectiongroupEmail.equals(document.getString("groupemail")) && collectionaccountMeasuringUnit.equals(document.getString("measuringunit"))) {
                                    Found[0] ="Found";
                                }
                                collectionaccountEmail = document.getString("accountemail");
                                collectionaccountPassword = document.getString("accountpassword");
                                collectionEmail = document.getString("groupemail");
                                collectionGroup = document.getString("name");
                                collectionLimit = document.getDouble("limit");
                                collectionAlias = document.getString("alias");
                                collectiongroupEmail = document.getString("groupemail");
                                collectionaccountMeasuringUnit = document.getString("measuringunit");
                            }
                            // ... do something with the document data
                        }
                        if (collectionEmail.equals("empty")) {
                            collectionEmail = "empty";
                            collectionaccountEmail = "empty";
                            collectionaccountPassword = "empty";
                            collectionGroup = "empty";
                            collectionAlias = "empty";
                            collectiongroupEmail = "empty";
                            collectionaccountMeasuringUnit="empty";
                            firestore = FirebaseFirestore.getInstance();
                            collectionReference = firestore.collection(collectionEmail);

                            //editName = findViewById(R.id.editName);
                            //editNumber = findViewById(R.id.editNumber);
                            //editPrice = findViewById(R.id.editPrice);
                            btnAdd = findViewById(R.id.btnAdd);

                            recyclerView = findViewById(R.id.recyclerView);
                            RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);
                            recyclerView.setLayoutManager(layoutManager2);
                            //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                            Query query2 = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                            adapter = new FirestoreAdapter(query2);
                            recyclerView.setAdapter(adapter);
                        } else if (!Found[0].equals("Found")){
                            firestore = FirebaseFirestore.getInstance();
                            collectionReference = firestore.collection(collectionEmail+collectionGroup);

                            //editName = findViewById(R.id.editName);
                            //editNumber = findViewById(R.id.editNumber);
                            //editPrice = findViewById(R.id.editPrice);
                            btnAdd = findViewById(R.id.btnAdd);

                            recyclerView = findViewById(R.id.recyclerView);
                            RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);
                            recyclerView.setLayoutManager(layoutManager2);
                            //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                            Query query2 = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                            adapter = new FirestoreAdapter(query2);
                            recyclerView.setAdapter(adapter);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Error occurred while fetching the documents
                    });

        } else {
            collectionEmail = "empty";
            collectionaccountEmail = "empty";
            collectionaccountPassword = "empty";
            collectionGroup = "empty";
            collectionAlias = "empty";
            collectiongroupEmail = "empty";
            collectionaccountMeasuringUnit="empty";
            firestore = FirebaseFirestore.getInstance();
            collectionReference = firestore.collection(collectionEmail);

            //editName = findViewById(R.id.editName);
            //editNumber = findViewById(R.id.editNumber);
            //editPrice = findViewById(R.id.editPrice);
            btnAdd = findViewById(R.id.btnAdd);

            recyclerView = findViewById(R.id.recyclerView);
            RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager2);
            //recyclerView.setLayoutManager(new LinearLayoutManager(this));

            Query query2 = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

            adapter = new FirestoreAdapter(query2);
            recyclerView.setAdapter(adapter);
        }

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddOptionsPopup(v);
            }
        });

        createchannel();
        //for the new api 33+ notifications permissions.
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!allPermissionsGranted()) {
                rpl.launch(REQUIRED_PERMISSIONS);
            }
        }

        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                FirebaseUser usera = firebaseAuth.getCurrentUser();

                if (usera!=null && !collectionEmail.equals("empty")) {

                    FirebaseFirestore firestore3 = FirebaseFirestore.getInstance();

                    CollectionReference collectionRef = firestore3.collection(usera.getEmail());
                    //String fieldName = "name";
                    //String desiredValue = name;

                    //final String[] documentId2 = {""};

                    collectionRef.get()
                            .addOnSuccessListener(querySnapshot -> {
                                        String documentId = "";
                                        for (QueryDocumentSnapshot document : querySnapshot) {
                                            Double currentDouble = Double.parseDouble(String.format("%.2f", document.getDouble("balance")).replaceAll(",", "."));
                                            if (!document.getString("copy").equals("Yes") && currentDouble > 0) {
                                                Double newbalance = Double.parseDouble(String.format("%.2f", document.getDouble("balance")).replaceAll(",", "."));
                                                String balanceexists = "";
                                                Double oldbalance = Double.valueOf(0);
                                                String newbalanceStr = String.format("%.2f", document.getDouble("balance"));
                                                String oldbalanceStr = "";
                                                Integer indexGroup = 0;
                                                for (int i = 0; i < emailGroups.size(); i++) {
                                                    if (emailGroups.get(i).get(0).equals(document.getString("groupemail")) && emailGroups.get(i).get(1).equals(document.getString("name"))) {
                                                        balanceexists = "Exists";
                                                        oldbalance = Double.parseDouble(emailGroups.get(i).get(2).replaceAll(",", "."));
                                                        oldbalanceStr = String.format("%.2f", oldbalance);
                                                        break;
                                                    }
                                                    indexGroup = indexGroup + 1;
                                                }

                                                if (balanceexists.equals("Exists")) {
                                                    if (!oldbalanceStr.equals(newbalanceStr)) {

                                                        emailGroups.get(indexGroup).set(2, String.format("%.2f", document.getDouble("balance")));

                                                        extrasTitle = document.getString("name") + " group";
                                                        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

                                                        // Enable grouping (thousand separators)
                                                        numberFormat.setGroupingUsed(true);

                                                        numberFormat.setMinimumFractionDigits(2);
                                                        numberFormat.setMaximumFractionDigits(2);
                                                        extrasDescription = "expenses exceed limit by " + numberFormat.format(document.getDouble("balance")) + " " + MainActivity.collectionaccountMeasuringUnit;
                                                        extras(3);
                                                    }
                                                } else {

                                                    emailGroups.add(new ArrayList<String>(Arrays.asList(document.getString("groupemail"), document.getString("name"), String.format("%.2f", document.getDouble("balance")))));

                                                    extrasTitle = document.getString("name") + " group";
                                                    NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

                                                    // Enable grouping (thousand separators)
                                                    numberFormat.setGroupingUsed(true);

                                                    numberFormat.setMinimumFractionDigits(2);
                                                    numberFormat.setMaximumFractionDigits(2);
                                                    extrasDescription = "expenses exceed limit by " + numberFormat.format(document.getDouble("balance")) + " " + MainActivity.collectionaccountMeasuringUnit;
                                                    extras(3);
                                                }
                                            }
                                            if (!document.getString("copy").equals("Yes") && !(currentDouble > 0)) {
                                                String balanceexists = "";
                                                Integer indexGroup = 0;
                                                for (int i = 0; i < emailGroups.size(); i++) {
                                                    if (emailGroups.get(i).get(0).equals(document.getString("groupemail")) && emailGroups.get(i).get(1).equals(document.getString("name"))) {
                                                        balanceexists = "Exists";
                                                        //Toast.makeText(MainActivity.this, "Exists 1", Toast.LENGTH_SHORT).show();
                                                        break;
                                                    }
                                                    indexGroup = indexGroup + 1;
                                                }

                                                if (balanceexists.equals("Exists")) {
                                                    //Toast.makeText(MainActivity.this, "Exists 2", Toast.LENGTH_SHORT).show();
                                                    Integer indexGroup2 = 0;
                                                    for (ArrayList<String> check : emailGroups) {
                                                        if (indexGroup == indexGroup2) {
                                                            emailGroups.remove(check);
                                                            break;
                                                        }
                                                        indexGroup2 = indexGroup2 + 1;
                                                    }
                                                }
                                            }
                                        }


                                    // Access individual documents here
                                    //if ("current".equals(document.getString("current"))) {
                                    //}
                                    // ... do something with the document data

                            })
                            .addOnFailureListener(e -> {
                                // Error occurred while fetching the documents
                            });

                }
            }

        }, 0, 1000);

    }

    private void createchannel() {
        NotificationChannel mChannel = null;   //importance level
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(id1,
                    getString(R.string.channel_name),  //name of the channel
                    NotificationManager.IMPORTANCE_DEFAULT);

            //important level: default is is high on the phone.  high is urgent on the phone.  low is medium, so none is low?
            // Configure the notification channel.
            mChannel.setDescription(getString(R.string.channel_description));
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setShowBadge(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            nm.createNotificationChannel(mChannel);

            //a medium level channel
            mChannel = new NotificationChannel(id2,
                    getString(R.string.channel_name2),  //name of the channel
                    NotificationManager.IMPORTANCE_LOW);   //importance level
            // Configure the notification channel.
            mChannel.setDescription(getString(R.string.channel_description2));
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this channel, if the device supports this feature.
            mChannel.setLightColor(Color.BLUE);
            mChannel.enableVibration(true);
            mChannel.setShowBadge(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            nm.createNotificationChannel(mChannel);

            //a urgent level channel
            mChannel = new NotificationChannel(id3,
                    getString(R.string.channel_name2),  //name of the channel
                    NotificationManager.IMPORTANCE_HIGH);   //importance level
            // Configure the notification channel.
            mChannel.setDescription(getString(R.string.channel_description3));
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this channel, if the device supports this feature.
            mChannel.setLightColor(Color.GREEN);
            mChannel.enableVibration(true);
            mChannel.setShowBadge(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            nm.createNotificationChannel(mChannel);
        }
    }


    //ask for permissions when we start.
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void extras(int which) {
        String msg = "";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), id1)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My notification")
                .setWhen(System.currentTimeMillis())  //When the event occurred, now, since noti are stored by time.
                .setAutoCancel(true)   //allow auto cancel when pressed.
                .setContentTitle(MainActivity.extrasTitle)   //Title message top row.
                .setContentText("Hello World!")
                .setChannelId(id1);

        /*
         * Note, since the channel now provides the defaults for sound, vibrate, lights, this section really
         * matter, since it's overridden by the channel settings
         */
        switch (which) {
            case 1:  //sound
                msg = "Sounds only";
                builder.setDefaults(Notification.DEFAULT_SOUND);
                //or harder way
                //builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                //For you own sound in your package
                //builder.setSound(Uri.parse("android.resource://com.my.package/" + R.raw.sound));
                break;
            case 2: //Vibrate
                //NOTE, Need the <uses-permission android:name="android.permission.VIBRATE"></uses-permission> in manifest or force Close
                msg = "Vibrate";
                builder.setDefaults(Notification.DEFAULT_VIBRATE);
                break;
            case 3: //both
                msg = MainActivity.extrasDescription;
                builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);
                break;
            case 4:
                msg = "and Lights";
                builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);
                // Notification.DEFAULT_ALL  does the same thing.
                break;

        }

        //Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        //notificationIntent.putExtra("mytype", msg);
        //PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, NotID, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        //builder.setContentIntent(contentIntent);  //what activity to open.
        builder.setContentText(msg);

        Notification noti = builder.build();
        //This will still work even if the channel is set differently.
        if (which == 4) {  //really annoy the user!
            noti.flags = Notification.FLAG_INSISTENT;
        }


        //Show the notification
        nm.notify(NotID, noti);
        NotID++;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static void addRow(String name, Long number, Double price) {
        Map<String, Object> data = new HashMap<>();
        data.put("datetime", Calendar.getInstance().getTime());
        data.put("name", collectionAlias);
        data.put("number", number);

        double value = price;

        // Create a BigDecimal object from the double value
        BigDecimal decimalValue = BigDecimal.valueOf(value);

        // Round the BigDecimal value to two decimal places
        BigDecimal roundedValue = decimalValue.setScale(2, BigDecimal.ROUND_HALF_UP);

        // Convert the rounded BigDecimal value back to a double
        double priceDouble = roundedValue.doubleValue();

        data.put("price", priceDouble);
        Double saldo=Double.valueOf(0);
        data.put("saldo", saldo);

        collectionReference.add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Document added with ID: " + documentReference.getId());
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                        String collectionPath = collectionEmail + collectionGroup;

                        CollectionReference collectionRef = firestore.collection(collectionPath);

                        collectionRef.orderBy("datetime", Query.Direction.ASCENDING).get()
                                .addOnSuccessListener(querySnapshot -> {
                                    Double saldo = collectionLimit;
                                    Long number =Long.valueOf(0);
                                    for (QueryDocumentSnapshot document : querySnapshot) {
                                        // Access individual documents here
                                        String documentId = document.getId();

                                        FirebaseFirestore firestore3 = FirebaseFirestore.getInstance();

                                        DocumentReference documentRef = firestore3.collection(collectionPath).document(documentId);

                                        Double price = Double.valueOf(0);
                                        try {
                                            price = round(document.getDouble("price"),2);
                                        } catch (Exception e) {
                                            //throw new RuntimeException(e);
                                        }

                                        //DecimalFormat decimalFormat = new DecimalFormat("#.00");
                                        //String formattedNumberString = decimalFormat.format(price);
                                        //price = Double.parseDouble(formattedNumberString);

                                        //BigDecimal bigDecimal = new BigDecimal(price);
                                        //BigDecimal roundedNumber = bigDecimal.setScale(2, RoundingMode.HALF_UP);
                                        //price = roundedNumber.doubleValue();

                                        String formattedNumber = String.format("%.2f", price).replaceAll(",",".");
                                        price = Double.valueOf(formattedNumber);

                                        saldo = saldo + price;

                                        String formattedNumber2 = String.format("%.2f", saldo).replaceAll(",",".");
                                        saldo = Double.valueOf(formattedNumber2);

                                        number = number + 1;

                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("price", price);
                                        updates.put("saldo", saldo);
                                        updates.put("number", number);
                                        //updates.put("field2", "new value 2");

                                        documentRef.update(updates)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Document updated successfully
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Error occurred while updating the document
                                                });
                                    }

                                    FirebaseFirestore firestore2 = FirebaseFirestore.getInstance();

                                    CollectionReference collectionRef4 = firestore2.collection(collectiongroupEmail);

                                    Double finalSaldo = saldo;
                                    collectionRef4.orderBy("datetime", Query.Direction.ASCENDING).get()
                                            .addOnSuccessListener(querySnapshot5 -> {
                                                //List<String> fieldValues = new ArrayList<>();
                                                String DocumentId2 = "";
                                                for (QueryDocumentSnapshot document2 : querySnapshot5) {
                                                    if (collectiongroupEmail.equals(document2.getString("groupemail")) && collectionGroup.equals(document2.getString("name"))) {
                                                        DocumentId2 = document2.getId();

                                                        CollectionReference collectionRef5 = firestore2.collection(document2.getString("accountemail"));

                                                        Double finalSaldo2 = finalSaldo;
                                                        collectionRef5.orderBy("datetime", Query.Direction.ASCENDING).get()
                                                                .addOnSuccessListener(querySnapshot6 -> {
                                                                    //List<String> fieldValues = new ArrayList<>();
                                                                    String DocumentId3 = "";
                                                                    for (QueryDocumentSnapshot document3 : querySnapshot6) {
                                                                        if (collectiongroupEmail.equals(document3.getString("groupemail")) && collectionGroup.equals(document3.getString("name"))) {
                                                                            DocumentId3 = document3.getId();

                                                                            DocumentReference documentRef3 = firestore2.collection(document2.getString("accountemail")).document(DocumentId3);

                                                                            Map<String, Object> updates2 = new HashMap<>();
                                                                            updates2.put("balance", finalSaldo2);
                                                                            //updates.put("field2", "new value 2");

                                                                            documentRef3.update(updates2)
                                                                                    .addOnSuccessListener(aVoid2 -> {
                                                                                        // Document updated successfully
                                                                                    })
                                                                                    .addOnFailureListener(e -> {
                                                                                        // Error occurred while updating the document
                                                                                    });
                                                                            //fieldValues.add(fieldValue);
                                                                        }
                                                                    }

                                                                    if (!DocumentId3.equals("")) {
                                                                    }


                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    // Error occurred while fetching the documents
                                                                });
                                                        //fieldValues.add(fieldValue);
                                                    }
                                                }

                                            })
                                            .addOnFailureListener(e -> {
                                                // Error occurred while fetching the documents
                                            });


                                })
                                .addOnFailureListener(e -> {
                                    // Error occurred while fetching the documents
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error occurred while adding the document
                    }
                });

        //Button btnAdd = findViewById(R.id.btnAdd);

    }

    private void addGroup(String accountemail, String accountpassword, String accountverificationcode, String groupemail, String alias, String task, String name, String measuringunit, Double limit, String current,String copy, Double balance) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser usera = firebaseAuth.getCurrentUser();

        if (usera!=null) {

            String fieldName = "current";
            String fieldValue = "current";

            FirebaseFirestore firestore10 = FirebaseFirestore.getInstance();

            String collectionPath = "your_collection_name";

            CollectionReference collectionRef10 = firestore10.collection(accountemail);

            Query query10 = collectionRef10.whereEqualTo(fieldName, fieldValue);

            //String fieldName = "name";
            //String desiredValue = name;

            //final String[] documentId2 = {""};

            query10.get()
                    .addOnSuccessListener(querySnapshot -> {
                        WriteBatch batch = firestore10.batch();

                        for (DocumentSnapshot document : querySnapshot) {
                            DocumentReference docRef = document.getReference();
                            batch.update(docRef, "current", "");
                        }

                        // Commit the batch update
                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    // Documents updated successfully
                                })
                                .addOnFailureListener(e -> {
                                    // Error occurred while updating the documents
                                });

                        FirebaseFirestore firestore3 = FirebaseFirestore.getInstance();

                        CollectionReference collectionRef = firestore3.collection(usera.getEmail());

                        collectionRef.get()
                                .addOnSuccessListener(querySnapshot2 -> {
                                    String documentId = "";
                                    for (QueryDocumentSnapshot document : querySnapshot2) {
                                        // Access individual documents here
                                        if (name.equals(document.getString("name")) && groupemail.equals(document.getString("groupemail")) && accountemail.equals(document.getString("accountemail"))) {
                                            documentId = document.getId();
                                        }
                                        // ... do something with the document data
                                    }
                                    if (documentId.equals("")) {
                                        FirebaseFirestore firestore2 = FirebaseFirestore.getInstance();
                                        CollectionReference collectionReference2 = firestore2.collection(usera.getEmail());

                                        Map<String, Object> data = new HashMap<>();
                                        data.put("datetime", Calendar.getInstance().getTime());
                                        data.put("accountemail", accountemail);
                                        data.put("accountpassword", accountpassword);
                                        data.put("accountverificationcode", accountverificationcode);
                                        data.put("groupemail", groupemail);
                                        data.put("alias", alias);
                                        data.put("task", task);
                                        data.put("name", name);
                                        data.put("measuringunit", measuringunit);
                                        data.put("limit", limit);
                                        data.put("current", "current");
                                        data.put("copy", "No");
                                        data.put("balance", balance);

                                        collectionReference2.add(data)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Log.d(TAG, "Document added with ID: " + documentReference.getId());
                                                        FirebaseFirestore firestore3 = FirebaseFirestore.getInstance();

                                                        CollectionReference collectionRef = firestore3.collection(usera.getEmail());
                                                        //String fieldName = "name";
                                                        //String desiredValue = name;

                                                        //final String[] documentId2 = {""};

                                                        collectionRef.get()
                                                                .addOnSuccessListener(querySnapshot -> {
                                                                    String documentId = "";
                                                                    for (QueryDocumentSnapshot document : querySnapshot) {
                                                                        // Access individual documents here
                                                                        if ("current".equals(document.getString("current"))) {
                                                                            collectionaccountEmail = document.getString("accountemail");
                                                                            collectionaccountPassword = document.getString("accountpassword");
                                                                            collectionEmail = document.getString("groupemail");
                                                                            collectionGroup = document.getString("name");
                                                                            collectionLimit = document.getDouble("limit");
                                                                            collectionAlias = document.getString("alias");
                                                                            collectionaccountMeasuringUnit = document.getString("measuringunit");
                                                                            collectiongroupEmail = document.getString("groupemail");

                                                                            firestore = FirebaseFirestore.getInstance();
                                                                            collectionReference = firestore.collection(collectionEmail + collectionGroup);

                                                                            FirebaseFirestore firestore5 = FirebaseFirestore.getInstance();

                                                                            String collectionPath2 = collectionEmail + collectionGroup;

                                                                            CollectionReference collectionRef3 = firestore5.collection(collectionPath2);

                                                                            collectionRef3.orderBy("datetime", Query.Direction.ASCENDING).get()
                                                                                    .addOnSuccessListener(querySnapshot4 -> {
                                                                                        Double saldo = collectionLimit;
                                                                                        Long number = Long.valueOf(0);
                                                                                        for (QueryDocumentSnapshot document2 : querySnapshot4) {
                                                                                            // Access individual documents here
                                                                                            String documentId3 = document2.getId();

                                                                                            DocumentReference documentRef4 = firestore5.collection(collectionPath2).document(documentId3);

                                                                                            Double price = Double.valueOf(0);
                                                                                            try {
                                                                                                price = round(document2.getDouble("price"),2);
                                                                                            } catch (
                                                                                                    Exception e) {
                                                                                                //throw new RuntimeException(e);
                                                                                            }

                                                                                            //DecimalFormat decimalFormat = new DecimalFormat("#.00");
                                                                                            //String formattedNumberString = decimalFormat.format(price);
                                                                                            //price = Double.parseDouble(formattedNumberString);

                                                                                            //BigDecimal bigDecimal = new BigDecimal(price);
                                                                                            //BigDecimal roundedNumber = bigDecimal.setScale(2, RoundingMode.HALF_UP);
                                                                                            //price = roundedNumber.doubleValue();

                                                                                            String formattedNumber = String.format("%.2f", price).replaceAll(",",".");
                                                                                            price = Double.valueOf(formattedNumber);

                                                                                            saldo = saldo + price;

                                                                                            String formattedNumber2 = String.format("%.2f", saldo).replaceAll(",",".");
                                                                                            saldo = Double.valueOf(formattedNumber2);

                                                                                            number = number + 1;

                                                                                            Map<String, Object> updates = new HashMap<>();
                                                                                            updates.put("price", price);
                                                                                            updates.put("saldo", saldo);
                                                                                            updates.put("number", number);
                                                                                            //updates.put("field2", "new value 2");

                                                                                            documentRef4.update(updates)
                                                                                                    .addOnSuccessListener(aVoid2 -> {
                                                                                                        // Document updated successfully
                                                                                                    })
                                                                                                    .addOnFailureListener(e -> {
                                                                                                        // Error occurred while updating the document
                                                                                                    });
                                                                                        }

                                                                                        CollectionReference collectionRef4 = firestore.collection(collectiongroupEmail);

                                                                                        Double finalSaldo = saldo;
                                                                                        collectionRef4.orderBy("datetime", Query.Direction.ASCENDING).get()
                                                                                                .addOnSuccessListener(querySnapshot5 -> {
                                                                                                    //List<String> fieldValues = new ArrayList<>();
                                                                                                    String DocumentId2 = "";
                                                                                                    for (QueryDocumentSnapshot document2 : querySnapshot5) {
                                                                                                        if (collectiongroupEmail.equals(document2.getString("groupemail")) && collectionGroup.equals(document2.getString("name"))) {
                                                                                                            DocumentId2 = document2.getId();

                                                                                                            CollectionReference collectionRef5 = firestore.collection(document2.getString("accountemail"));

                                                                                                            Double finalSaldo2 = finalSaldo;
                                                                                                            collectionRef5.orderBy("datetime", Query.Direction.ASCENDING).get()
                                                                                                                    .addOnSuccessListener(querySnapshot6 -> {
                                                                                                                        //List<String> fieldValues = new ArrayList<>();
                                                                                                                        String DocumentId3 = "";
                                                                                                                        for (QueryDocumentSnapshot document3 : querySnapshot6) {
                                                                                                                            if (collectiongroupEmail.equals(document3.getString("groupemail")) && collectionGroup.equals(document3.getString("name"))) {
                                                                                                                                DocumentId3 = document3.getId();
                                                                                                                                //fieldValues.add(fieldValue);

                                                                                                                                DocumentReference documentRef3 = firestore2.collection(document2.getString("accountemail")).document(DocumentId3);

                                                                                                                                Map<String, Object> updates2 = new HashMap<>();
                                                                                                                                updates2.put("task", task);
                                                                                                                                updates2.put("limit", limit);
                                                                                                                                updates2.put("measuringunit", measuringunit);
                                                                                                                                updates2.put("balance", finalSaldo2);
                                                                                                                                //updates.put("field2", "new value 2");

                                                                                                                                documentRef3.update(updates2)
                                                                                                                                        .addOnSuccessListener(aVoid2 -> {
                                                                                                                                            // Document updated successfully
                                                                                                                                        })
                                                                                                                                        .addOnFailureListener(e -> {
                                                                                                                                            // Error occurred while updating the document
                                                                                                                                        });
                                                                                                                            }
                                                                                                                        }

                                                                                                                        if (!DocumentId3.equals("")) {

                                                                                                                        }


                                                                                                                    })
                                                                                                                    .addOnFailureListener(e -> {
                                                                                                                        // Error occurred while fetching the documents
                                                                                                                    });
                                                                                                            //fieldValues.add(fieldValue);
                                                                                                        }
                                                                                                    }
                                                                                                /*
                                                                                                if (!DocumentId2.equals("")) {
                                                                                                    DocumentReference documentRef3 = firestore.collection(collectionEmail).document(DocumentId2);

                                                                                                    Map<String, Object> updates2 = new HashMap<>();
                                                                                                    updates2.put("balance", finalSaldo);
                                                                                                    //updates.put("field2", "new value 2");

                                                                                                    documentRef3.update(updates2)
                                                                                                            .addOnSuccessListener(aVoid2 -> {
                                                                                                                // Document updated successfully
                                                                                                            })
                                                                                                            .addOnFailureListener(e -> {
                                                                                                                // Error occurred while updating the document
                                                                                                            });
                                                                                                }

                                                                                                 */


                                                                                                })
                                                                                                .addOnFailureListener(e -> {
                                                                                                    // Error occurred while fetching the documents
                                                                                                });


                                                                                        //clearFields();

                                                                                        //editName = findViewById(R.id.editName);
                                                                                        //editNumber = findViewById(R.id.editNumber);
                                                                                        //editPrice = findViewById(R.id.editPrice);
                                                                                        //btnAdd = findViewById(R.id.btnAdd);

                                                                                        firestore = FirebaseFirestore.getInstance();
                                                                                        collectionReference = firestore.collection(collectionEmail + collectionGroup);

                                                                                        recyclerView = findViewById(R.id.recyclerView);
                                                                                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                                                                                        recyclerView.setLayoutManager(layoutManager);
                                                                                        //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                                                                                        Query query = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                                                                                        adapter = new FirestoreAdapter(query);
                                                                                        recyclerView.setAdapter(adapter);


                                                                                    })
                                                                                    .addOnFailureListener(e -> {
                                                                                        // Error occurred while fetching the documents
                                                                                    });
                                                                        }
                                                                        // ... do something with the document data
                                                                    }
                                                                    if (collectionEmail.equals("")) {
                                                                        collectionEmail = "empty";
                                                                        collectionaccountEmail = "empty";
                                                                        collectionaccountPassword = "empty";
                                                                        collectionGroup = "empty";
                                                                        collectionAlias = "empty";
                                                                        collectiongroupEmail = "empty";
                                                                        collectionaccountMeasuringUnit="empty";
                                                                        firestore = FirebaseFirestore.getInstance();
                                                                        collectionReference = firestore.collection(collectionEmail);

                                                                        //editName = findViewById(R.id.editName);
                                                                        //editNumber = findViewById(R.id.editNumber);
                                                                        //editPrice = findViewById(R.id.editPrice);
                                                                        btnAdd = findViewById(R.id.btnAdd);

                                                                        recyclerView = findViewById(R.id.recyclerView);
                                                                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                                                                        recyclerView.setLayoutManager(layoutManager);
                                                                        //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                                                                        Query query = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                                                                        adapter = new FirestoreAdapter(query);
                                                                        recyclerView.setAdapter(adapter);
                                                                    } else {
                                                                    }
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    // Error occurred while fetching the documents
                                                                });
                                                        clearFields();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e(TAG, "Error adding document", e);
                                                    }
                                                });
                                    } else {
                                        FirebaseFirestore firestore2 = FirebaseFirestore.getInstance();

                                        DocumentReference documentRef = firestore.collection(usera.getEmail()).document(documentId);

                                        Map<String, Object> data = new HashMap<>();
                                        //data.put("datetime", Calendar.getInstance().getTime());
                                        data.put("accountemail", accountemail);
                                        data.put("accountpassword", accountpassword);
                                        data.put("accountverificationcode", accountverificationcode);
                                        data.put("groupemail", groupemail);
                                        data.put("alias", alias);
                                        data.put("task", task);
                                        data.put("name", name);
                                        data.put("measuringunit", measuringunit);
                                        data.put("limit", limit);
                                        data.put("current", "current");
                                        data.put("copy", "No");
                                        //data.put("balance", balance);
                                        //data.put("balance", balance);

                                        documentRef.update(data)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "Document added with ID: " + documentRef.getId());
                                                    FirebaseFirestore firestore4 = FirebaseFirestore.getInstance();

                                                    CollectionReference collectionRef2 = firestore4.collection(usera.getEmail());
                                                    //String fieldName = "name";
                                                    //String desiredValue = name;

                                                    //final String[] documentId2 = {""};

                                                    collectionRef2.get()
                                                            .addOnSuccessListener(querySnapshot3 -> {
                                                                String documentId2 = "";
                                                                for (QueryDocumentSnapshot document : querySnapshot3) {
                                                                    // Access individual documents here
                                                                    if ("current".equals(document.getString("current"))) {
                                                                        collectionaccountEmail = document.getString("accountemail");
                                                                        collectionaccountPassword = document.getString("accountpassword");
                                                                        collectionEmail = document.getString("groupemail");
                                                                        collectionGroup = document.getString("name");
                                                                        collectionLimit = document.getDouble("limit");
                                                                        collectionAlias = document.getString("alias");
                                                                        collectionaccountMeasuringUnit = document.getString("measuringunit");
                                                                        collectiongroupEmail = document.getString("groupemail");

                                                                        firestore = FirebaseFirestore.getInstance();
                                                                        collectionReference = firestore.collection(collectionEmail + collectionGroup);

                                                                        FirebaseFirestore firestore5 = FirebaseFirestore.getInstance();

                                                                        String collectionPath2 = collectionEmail + collectionGroup;

                                                                        CollectionReference collectionRef3 = firestore5.collection(collectionPath2);

                                                                        collectionRef3.orderBy("datetime", Query.Direction.ASCENDING).get()
                                                                                .addOnSuccessListener(querySnapshot4 -> {
                                                                                    Double saldo = collectionLimit;
                                                                                    Long number = Long.valueOf(0);
                                                                                    for (QueryDocumentSnapshot document2 : querySnapshot4) {
                                                                                        // Access individual documents here
                                                                                        String documentId3 = document2.getId();

                                                                                        DocumentReference documentRef4 = firestore5.collection(collectionPath2).document(documentId3);

                                                                                        Double price = Double.valueOf(0);
                                                                                        try {
                                                                                            price = round(document2.getDouble("price"),2);
                                                                                        } catch (
                                                                                                Exception e) {
                                                                                            //throw new RuntimeException(e);
                                                                                        }

                                                                                        //DecimalFormat decimalFormat = new DecimalFormat("#.00");
                                                                                        //String formattedNumberString = decimalFormat.format(price);
                                                                                        //price = Double.parseDouble(formattedNumberString);

                                                                                        //BigDecimal bigDecimal = new BigDecimal(price);
                                                                                        //BigDecimal roundedNumber = bigDecimal.setScale(2, RoundingMode.HALF_UP);
                                                                                        //price = roundedNumber.doubleValue();

                                                                                        String formattedNumber = String.format("%.2f", price).replaceAll(",",".");
                                                                                        price = Double.valueOf(formattedNumber);

                                                                                        saldo = saldo + price;

                                                                                        String formattedNumber2 = String.format("%.2f", saldo).replaceAll(",",".");
                                                                                        saldo = Double.valueOf(formattedNumber2);

                                                                                        number = number + 1;

                                                                                        Map<String, Object> updates = new HashMap<>();
                                                                                        updates.put("price", price);
                                                                                        updates.put("saldo", saldo);
                                                                                        updates.put("number", number);
                                                                                        //updates.put("field2", "new value 2");

                                                                                        documentRef4.update(updates)
                                                                                                .addOnSuccessListener(aVoid2 -> {
                                                                                                    // Document updated successfully
                                                                                                })
                                                                                                .addOnFailureListener(e -> {
                                                                                                    // Error occurred while updating the document
                                                                                                });
                                                                                    }

                                                                                    CollectionReference collectionRef4 = firestore.collection(collectiongroupEmail);

                                                                                    Double finalSaldo = saldo;
                                                                                    collectionRef4.orderBy("datetime", Query.Direction.ASCENDING).get()
                                                                                            .addOnSuccessListener(querySnapshot5 -> {
                                                                                                //List<String> fieldValues = new ArrayList<>();
                                                                                                String DocumentId2 = "";
                                                                                                for (QueryDocumentSnapshot document2 : querySnapshot5) {
                                                                                                    if (collectiongroupEmail.equals(document2.getString("groupemail")) && collectionGroup.equals(document2.getString("name"))) {
                                                                                                        DocumentId2 = document2.getId();

                                                                                                        CollectionReference collectionRef5 = firestore.collection(document2.getString("accountemail"));

                                                                                                        Double finalSaldo2 = finalSaldo;
                                                                                                        collectionRef5.orderBy("datetime", Query.Direction.ASCENDING).get()
                                                                                                                .addOnSuccessListener(querySnapshot6 -> {
                                                                                                                    //List<String> fieldValues = new ArrayList<>();
                                                                                                                    String DocumentId3 = "";
                                                                                                                    for (QueryDocumentSnapshot document3 : querySnapshot6) {
                                                                                                                        if (collectiongroupEmail.equals(document3.getString("groupemail")) && collectionGroup.equals(document3.getString("name"))) {
                                                                                                                            DocumentId3 = document3.getId();
                                                                                                                            //fieldValues.add(fieldValue);
                                                                                                                            DocumentReference documentRef3 = firestore2.collection(document2.getString("accountemail")).document(DocumentId3);

                                                                                                                            Map<String, Object> updates2 = new HashMap<>();
                                                                                                                            updates2.put("task", task);
                                                                                                                            updates2.put("limit", limit);
                                                                                                                            updates2.put("measuringunit", measuringunit);
                                                                                                                            updates2.put("balance", finalSaldo2);
                                                                                                                            //updates.put("field2", "new value 2");

                                                                                                                            documentRef3.update(updates2)
                                                                                                                                    .addOnSuccessListener(aVoid2 -> {
                                                                                                                                        // Document updated successfully
                                                                                                                                    })
                                                                                                                                    .addOnFailureListener(e -> {
                                                                                                                                        // Error occurred while updating the document
                                                                                                                                    });
                                                                                                                        }
                                                                                                                    }

                                                                                                                    if (!DocumentId3.equals("")) {

                                                                                                                    }


                                                                                                                })
                                                                                                                .addOnFailureListener(e -> {
                                                                                                                    // Error occurred while fetching the documents
                                                                                                                });
                                                                                                        //fieldValues.add(fieldValue);
                                                                                                    }
                                                                                                }
                                                                                                /*
                                                                                                if (!DocumentId2.equals("")) {
                                                                                                    DocumentReference documentRef3 = firestore.collection(collectionEmail).document(DocumentId2);

                                                                                                    Map<String, Object> updates2 = new HashMap<>();
                                                                                                    updates2.put("balance", finalSaldo);
                                                                                                    //updates.put("field2", "new value 2");

                                                                                                    documentRef3.update(updates2)
                                                                                                            .addOnSuccessListener(aVoid2 -> {
                                                                                                                // Document updated successfully
                                                                                                            })
                                                                                                            .addOnFailureListener(e -> {
                                                                                                                // Error occurred while updating the document
                                                                                                            });
                                                                                                }

                                                                                                 */


                                                                                            })
                                                                                            .addOnFailureListener(e -> {
                                                                                                // Error occurred while fetching the documents
                                                                                            });


                                                                                    //clearFields();

                                                                                    //editName = findViewById(R.id.editName);
                                                                                    //editNumber = findViewById(R.id.editNumber);
                                                                                    //editPrice = findViewById(R.id.editPrice);
                                                                                    //btnAdd = findViewById(R.id.btnAdd);

                                                                                    firestore = FirebaseFirestore.getInstance();
                                                                                    collectionReference = firestore.collection(collectionEmail + collectionGroup);

                                                                                    recyclerView = findViewById(R.id.recyclerView);
                                                                                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                                                                                    recyclerView.setLayoutManager(layoutManager);
                                                                                    //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                                                                                    Query query = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                                                                                    adapter = new FirestoreAdapter(query);
                                                                                    recyclerView.setAdapter(adapter);

                                                                                                                                                                    })
                                                                                .addOnFailureListener(e -> {
                                                                                    // Error occurred while fetching the documents
                                                                                });
                                                                    }
                                                                    // ... do something with the document data
                                                                }
                                                                if (collectionEmail.equals("")) {
                                                                    collectionEmail = "empty";
                                                                    collectionaccountEmail = "empty";
                                                                    collectionaccountPassword = "empty";
                                                                    collectionGroup = "empty";
                                                                    collectionAlias = "empty";
                                                                    collectiongroupEmail = "empty";
                                                                    collectionaccountMeasuringUnit="empty";
                                                                    firestore = FirebaseFirestore.getInstance();
                                                                    collectionReference = firestore.collection(collectionEmail);

                                                                    //editName = findViewById(R.id.editName);
                                                                    //editNumber = findViewById(R.id.editNumber);
                                                                    //editPrice = findViewById(R.id.editPrice);
                                                                    //btnAdd = findViewById(R.id.btnAdd);

                                                                    recyclerView = findViewById(R.id.recyclerView);
                                                                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
                                                                    recyclerView.setLayoutManager(layoutManager);
                                                                    //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                                                                    Query query = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                                                                    adapter = new FirestoreAdapter(query);
                                                                    recyclerView.setAdapter(adapter);
                                                                } else {
                                                                }
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                // Error occurred while fetching the documents
                                                            });
                                                    // Document updated successfully
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Error occurred while updating the document
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Error occurred while fetching the documents
                                });

                        if (!accountemail.equals(groupemail)) {

                            firestore3 = FirebaseFirestore.getInstance();

                            collectionRef = firestore3.collection(groupemail);
                            //String fieldName = "name";
                            //String desiredValue = name;

                            //final String[] documentId2 = {""};

                            collectionRef.get()
                                    .addOnSuccessListener(querySnapshot2 -> {
                                        String documentId = "";
                                        for (QueryDocumentSnapshot document : querySnapshot2) {
                                            // Access individual documents here
                                            if (name.equals(document.getString("name")) && groupemail.equals(document.getString("groupemail")) && accountemail.equals(document.getString("accountemail"))) {
                                                documentId = document.getId();
                                            }
                                            // ... do something with the document data
                                        }
                                        if (documentId.equals("")) {
                                            FirebaseFirestore firestore2 = FirebaseFirestore.getInstance();
                                            CollectionReference collectionReference2 = firestore2.collection(groupemail);

                                            Map<String, Object> data = new HashMap<>();
                                            data.put("datetime", Calendar.getInstance().getTime());
                                            data.put("accountemail", accountemail);
                                            data.put("accountpassword", accountpassword);
                                            data.put("accountverificationcode", accountverificationcode);
                                            data.put("groupemail", groupemail);
                                            data.put("alias", alias);
                                            data.put("task", task);
                                            data.put("name", name);
                                            data.put("measuringunit", measuringunit);
                                            data.put("limit", limit);
                                            data.put("current", "");
                                            data.put("copy", "Yes");
                                            data.put("balance", balance);

                                            collectionReference2.add(data)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Log.d(TAG, "Document added with ID: " + documentReference.getId());
                                                            FirebaseFirestore firestore3 = FirebaseFirestore.getInstance();

                                                            CollectionReference collectionRef = firestore3.collection(usera.getEmail());
                                                            //String fieldName = "name";
                                                            //String desiredValue = name;

                                                            //final String[] documentId2 = {""};

                                                            collectionRef.get()
                                                                    .addOnSuccessListener(querySnapshot -> {
                                                                        String documentId = "";
                                                                        for (QueryDocumentSnapshot document : querySnapshot) {
                                                                            // Access individual documents here
                                                                            if ("current".equals(document.getString("current"))) {
                                                                            }
                                                                            // ... do something with the document data
                                                                        }
                                                                        if (collectionEmail.equals("")) {
                                                                        } else {
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        // Error occurred while fetching the documents
                                                                    });
                                                            clearFields();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e(TAG, "Error adding document", e);
                                                        }
                                                    });
                                        } else {
                                            FirebaseFirestore firestore2 = FirebaseFirestore.getInstance();

                                            DocumentReference documentRef = firestore.collection(groupemail).document(documentId);

                                            Map<String, Object> data = new HashMap<>();
                                            //data.put("datetime", Calendar.getInstance().getTime());
                                            data.put("accountemail", accountemail);
                                            data.put("accountpassword", accountpassword);
                                            data.put("accountverificationcode", accountverificationcode);
                                            data.put("groupemail", groupemail);
                                            data.put("alias", alias);
                                            data.put("task", task);
                                            data.put("name", name);
                                            data.put("measuringunit", measuringunit);
                                            data.put("limit", limit);
                                            data.put("current", "");
                                            data.put("copy", "Yes");
                                            data.put("balance", balance);
                                            //data.put("balance", balance);

                                            documentRef.update(data)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d(TAG, "Document added with ID: " + documentRef.getId());
                                                        FirebaseFirestore firestore4 = FirebaseFirestore.getInstance();

                                                        CollectionReference collectionRef2 = firestore4.collection(usera.getEmail());
                                                        //String fieldName = "name";
                                                        //String desiredValue = name;

                                                        //final String[] documentId2 = {""};

                                                        collectionRef2.get()
                                                                .addOnSuccessListener(querySnapshot3 -> {
                                                                    String documentId2 = "";
                                                                    for (QueryDocumentSnapshot document : querySnapshot3) {
                                                                        // Access individual documents here
                                                                        if ("current".equals(document.getString("current"))) {
                                                                        }
                                                                        // ... do something with the document data
                                                                    }
                                                                    if (collectionEmail.equals("")) {
                                                                    } else {
                                                                    }
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    // Error occurred while fetching the documents
                                                                });
                                                        // Document updated successfully
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        // Error occurred while updating the document
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error occurred while fetching the documents
                                    });

                        }
                    })
                    .addOnFailureListener(e -> {
                        // Error occurred while fetching the documents
                    });
        }

        //Button btnAdd = findViewById(R.id.btnAdd);

    }

    private void deleteGroupRow(String groupemail, String name) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser usera = firebaseAuth.getCurrentUser();

        FirebaseFirestore firestore3 = FirebaseFirestore.getInstance();

        CollectionReference collectionRef = firestore3.collection(usera.getEmail());

        String fieldName1 = "groupemail";
        String fieldValue1 = groupemail;
        String fieldName2 = "name";
        String fieldValue2 = name;

        Query query = collectionRef.whereEqualTo(fieldName1, fieldValue1)
                .whereEqualTo(fieldName2, fieldValue2);


        collectionRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> documentsToDelete = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot) {
                        if (document.getString("groupemail").equals(groupemail) && document.getString("name").equals(name)) {
                            //if (usera.getEmail().equals(document.getString("accountemail"))) {
                            documentsToDelete.add(document);
                            //}
                            FirebaseFirestore firestore4 = FirebaseFirestore.getInstance();

                            if (!usera.getEmail().equals(document.getString("groupemail"))) {
                                CollectionReference collectionRef2 = firestore4.collection(groupemail);

                                //Query query2 = collectionRef2.whereEqualTo(fieldName1, fieldValue1)
                                //        .whereEqualTo(fieldName2, fieldValue2);

                                collectionRef2.get()
                                        .addOnSuccessListener(querySnapshot2 -> {
                                            List<DocumentSnapshot> documentsToDelete2 = new ArrayList<>();

                                            for (DocumentSnapshot document2 : querySnapshot2) {
                                                if (document2.getString("accountemail").equals(document.getString("accountemail")) && document2.getString("groupemail").equals(groupemail) && document2.getString("name").equals(name) && "Yes".equals(document2.getString("copy"))) {
                                                    documentsToDelete2.add(document2);
                                                }
                                            }

                                            // Delete the retrieved documents
                                            for (DocumentSnapshot document2 : documentsToDelete2) {
                                                document2.getReference().delete();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            // Error occurred while fetching the documents
                                        });
                            }

                            if (usera.getEmail().equals(document.getString("groupemail")) && !usera.getEmail().equals(document.getString("accountemail"))) {
                                CollectionReference collectionRef2 = firestore4.collection(document.getString("accountemail"));

                                //Query query2 = collectionRef2.whereEqualTo(fieldName1, fieldValue1)
                                //        .whereEqualTo(fieldName2, fieldValue2);

                                collectionRef2.get()
                                        .addOnSuccessListener(querySnapshot2 -> {
                                            List<DocumentSnapshot> documentsToDelete2 = new ArrayList<>();

                                            for (DocumentSnapshot document2 : querySnapshot2) {
                                                if (document2.getString("accountemail").equals(document.getString("accountemail")) && document2.getString("groupemail").equals(groupemail) && document2.getString("name").equals(name)) {
                                                    documentsToDelete2.add(document2);
                                                }
                                            }

                                            // Delete the retrieved documents
                                            for (DocumentSnapshot document2 : documentsToDelete2) {
                                                document2.getReference().delete();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            // Error occurred while fetching the documents
                                        });
                            }
/*
                            if (groupemail.equals(usera.getEmail())) {

                                CollectionReference collectionRef2 = firestore4.collection(document.getString("accountemail"));

                                //Query query2 = collectionRef2.whereEqualTo(fieldName1, fieldValue1)
                                //        .whereEqualTo(fieldName2, fieldValue2);

                                collectionRef2.get()
                                        .addOnSuccessListener(querySnapshot2 -> {
                                            List<DocumentSnapshot> documentsToDelete2 = new ArrayList<>();

                                            for (DocumentSnapshot document2 : querySnapshot2) {
                                                if (document2.getString("groupemail").equals(groupemail) && document2.getString("name").equals(name)) {
                                                    documentsToDelete2.add(document2);
                                                }
                                            }

                                            // Delete the retrieved documents
                                            for (DocumentSnapshot document2 : documentsToDelete2) {
                                                document2.getReference().delete();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            // Error occurred while fetching the documents
                                        });
                            } else {
                                CollectionReference collectionRef2 = firestore4.collection(groupemail);

                                //Query query2 = collectionRef2.whereEqualTo(fieldName1, fieldValue1)
                                //        .whereEqualTo(fieldName2, fieldValue2);

                                collectionRef2.get()
                                        .addOnSuccessListener(querySnapshot2 -> {
                                            List<DocumentSnapshot> documentsToDelete2 = new ArrayList<>();

                                            for (DocumentSnapshot document2 : querySnapshot2) {
                                                if (document2.getString("groupemail").equals(groupemail) && document2.getString("name").equals(name) && "Yes".equals(document2.getString("copy"))) {
                                                    documentsToDelete2.add(document2);
                                                }
                                            }

                                            // Delete the retrieved documents
                                            for (DocumentSnapshot document2 : documentsToDelete2) {
                                                document2.getReference().delete();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            // Error occurred while fetching the documents
                                        });
                            }

 */
                        }
                    }

                    //Delete the retrieved documents
                    for (DocumentSnapshot document : documentsToDelete) {
                        document.getReference().delete();
                    }
                })
                .addOnFailureListener(e -> {
                    // Error occurred while fetching the documents
                });
        if (groupemail.equals(usera.getEmail())) {
            collectionRef = firestore3.collection(groupemail + name);

            CollectionReference finalCollectionRef = collectionRef;
            finalCollectionRef.get()
                    .addOnSuccessListener(querySnapshot -> {
                        // Iterate through the documents and delete them one by one
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            document.getReference().delete();
                        }
                        // Delete the collection itself
                        finalCollectionRef.document().delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Collection deleted successfully
                                })
                                .addOnFailureListener(e -> {
                                    // Error occurred while deleting the collection
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Error occurred while fetching the documents
                    });
        }

    }

    private void deleteGroupAccountRows() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser usera = firebaseAuth.getCurrentUser();

        FirebaseFirestore firestore3 = FirebaseFirestore.getInstance();

        CollectionReference collectionRef = firestore3.collection(usera.getEmail());

        String fieldName1 = "groupemail";
        String fieldValue1 = usera.getEmail();
        //String fieldName2 = "name";
        //String fieldValue2 = name;

        //Query query = collectionRef;
        //        .whereEqualTo(fieldName2, fieldValue2);


        collectionRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> documentsToDelete = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot) {
                        documentsToDelete.add(document);
                        FirebaseFirestore firestore4 = FirebaseFirestore.getInstance();

                        if (usera.getEmail().equals(document.getString("groupemail"))) {

                            FirebaseFirestore firestore5 = FirebaseFirestore.getInstance();

                            CollectionReference collectionRef3 = firestore5.collection(usera.getEmail() + document.getString("name"));

                            collectionRef3.get()
                                    .addOnSuccessListener(querySnapshot2 -> {
                                        // Iterate through the documents and delete them one by one
                                        for (DocumentSnapshot document2 : querySnapshot2.getDocuments()) {
                                            document2.getReference().delete();
                                        }
                                        // Delete the collection itself
                                        collectionRef3.document().delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    // Collection deleted successfully
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Error occurred while deleting the collection
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error occurred while fetching the documents
                                    });
                        }
                    }

                    for (DocumentSnapshot document : querySnapshot) {
                        //documentsToDelete.add(document);
                        FirebaseFirestore firestore4 = FirebaseFirestore.getInstance();

                        if (usera.getEmail().equals(document.getString("accountemail")) && !usera.getEmail().equals(document.getString("groupemail")) && "No".equals(document.getString("copy"))) {

                            CollectionReference collectionRef2 = firestore4.collection(document.getString("groupemail"));

                            //Query query2 = collectionRef2.whereEqualTo(fieldName1, fieldValue1);

                            collectionRef2.get()
                                    .addOnSuccessListener(querySnapshot2 -> {
                                        List<DocumentSnapshot> documentsToDelete2 = new ArrayList<>();

                                        for (DocumentSnapshot document2 : querySnapshot2) {
                                            if (usera.getEmail().equals(document2.getString("accountemail")) && !usera.getEmail().equals(document2.getString("groupemail")) && document.getString("name").equals(document2.getString("name")) && "Yes".equals(document2.getString("copy"))) {
                                                documentsToDelete2.add(document2);
                                            }
                                        }

                                        // Delete the retrieved documents
                                        for (DocumentSnapshot document2 : documentsToDelete2) {
                                            document2.getReference().delete();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error occurred while fetching the documents
                                    });
                        }

                        if (!usera.getEmail().equals(document.getString("accountemail")) && usera.getEmail().equals(document.getString("groupemail")) && "Yes".equals(document.getString("copy"))) {

                            CollectionReference collectionRef2 = firestore4.collection(document.getString("accountemail"));

                            //Query query2 = collectionRef2.whereEqualTo(fieldName1, fieldValue1);

                            collectionRef2.get()
                                    .addOnSuccessListener(querySnapshot2 -> {
                                        List<DocumentSnapshot> documentsToDelete2 = new ArrayList<>();

                                        for (DocumentSnapshot document2 : querySnapshot2) {
                                            if (!usera.getEmail().equals(document2.getString("accountemail")) && usera.getEmail().equals(document2.getString("groupemail")) && document.getString("name").equals(document2.getString("name")) && "No".equals(document2.getString("copy"))) {
                                                documentsToDelete2.add(document2);
                                            }
                                        }

                                        // Delete the retrieved documents
                                        for (DocumentSnapshot document2 : documentsToDelete2) {
                                            document2.getReference().delete();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error occurred while fetching the documents
                                    });


                        }

                    }

                    // Delete the retrieved documents
                    for (DocumentSnapshot document : documentsToDelete) {
                        document.getReference().delete();
                    }

                    collectionRef.document().delete()
                            .addOnSuccessListener(aVoid -> {
                                // Collection deleted successfully
                                AuthCredential credential = EmailAuthProvider.getCredential(collectionaccountEmail, collectionaccountPassword); // Replace "password" with the user's actual password

                                firebaseAuth.signInWithCredential(credential)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                                if (user != null) {
                                                    user.delete()
                                                            .addOnCompleteListener(deleteTask -> {
                                                                if (deleteTask.isSuccessful()) {
                                                                    // User account deleted successfully
                                                                    Toast.makeText(MainActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                                                    collectionEmail = "empty";
                                                                    collectionaccountEmail = "empty";
                                                                    collectionaccountPassword = "empty";
                                                                    collectionGroup = "empty";
                                                                    collectionAlias = "empty";
                                                                    collectiongroupEmail = "empty";
                                                                    collectionaccountMeasuringUnit="empty";
                                                                    firestore = FirebaseFirestore.getInstance();
                                                                    collectionReference = firestore.collection(collectionEmail);

                                                                    //editName = findViewById(R.id.editName);
                                                                    //editNumber = findViewById(R.id.editNumber);
                                                                    //editPrice = findViewById(R.id.editPrice);
                                                                    btnAdd = findViewById(R.id.btnAdd);

                                                                    recyclerView = findViewById(R.id.recyclerView);
                                                                    RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(MainActivity.this);
                                                                    recyclerView.setLayoutManager(layoutManager2);
                                                                    //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                                                                    Query query2 = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                                                                    adapter = new FirestoreAdapter(query2);
                                                                    recyclerView.setAdapter(adapter);
                                                                } else {
                                                                    // Failed to delete user account
                                                                    Toast.makeText(MainActivity.this, "Failed to delete user account", Toast.LENGTH_SHORT).show();
                                                                    collectionEmail = "empty";
                                                                    collectionaccountEmail = "empty";
                                                                    collectionaccountPassword = "empty";
                                                                    collectionGroup = "empty";
                                                                    collectionAlias = "empty";
                                                                    collectiongroupEmail = "empty";
                                                                    collectionaccountMeasuringUnit="empty";
                                                                    firestore = FirebaseFirestore.getInstance();
                                                                    collectionReference = firestore.collection(collectionEmail);

                                                                    //editName = findViewById(R.id.editName);
                                                                    //editNumber = findViewById(R.id.editNumber);
                                                                    //editPrice = findViewById(R.id.editPrice);
                                                                    btnAdd = findViewById(R.id.btnAdd);

                                                                    recyclerView = findViewById(R.id.recyclerView);
                                                                    RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(MainActivity.this);
                                                                    recyclerView.setLayoutManager(layoutManager2);
                                                                    //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                                                                    Query query2 = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                                                                    adapter = new FirestoreAdapter(query2);
                                                                    recyclerView.setAdapter(adapter);
                                                                }
                                                            });
                                                }
                                            } else {
                                                // Failed to authenticate user
                                                Log.w(TAG, "Failed to authenticate user.", task.getException());
                                                Toast.makeText(MainActivity.this, "Failed to authenticate user.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            })
                            .addOnFailureListener(e -> {
                                // Error occurred while deleting the collection
                            });
                })
                .addOnFailureListener(e -> {
                    // Error occurred while fetching the documents
                });
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Item");

        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_add_item, null);
        builder.setView(view);

        //EditText addDialogName = view.findViewById(R.id.addName);
        //EditText addDialogNumber = view.findViewById(R.id.addNumber);
        EditText addDialogPrice = view.findViewById(R.id.addPrice);
        Button cancelAddButton = view.findViewById(R.id.cancelAddButton);
        Button addButton = view.findViewById(R.id.addButton);

        AlertDialog alterdialog = builder.create();

        alterdialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                // Get the buttons from the dialog
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = collectionAlias;
                        Long number= Long.valueOf(0);
                        Double price= Double.valueOf(0);
                        try {
                            //number = Long.parseLong(addDialogNumber.getText().toString().trim());
                            price = round(Double.parseDouble(String.format("%.2f",Double.parseDouble(addDialogPrice.getText().toString().replaceAll(",", ".").trim())).replaceAll(",", ".")),2);
                        } catch (Exception e) {

                        }

                        if (price!=0) {
                            addRow(name, number, price);
                            alterdialog.dismiss();
                        } else {
                            Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                cancelAddButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alterdialog.dismiss();
                    }
                });
            }
        });

        alterdialog.show();
    }

    private void checkVerificationCode(String verificationCode) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), verificationCode);
            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Verification successful
                            Toast.makeText(MainActivity.this, "Verification successful.", Toast.LENGTH_SHORT).show();
                            // Proceed with further actions, e.g., redirect to the main activity
                        } else {
                            // Verification failed
                            Toast.makeText(MainActivity.this, "Verification failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void setSpinnerValue(View view) {
        FirebaseFirestore firestore100 = FirebaseFirestore.getInstance();

        Spinner chooseGroup =view.findViewById(R.id.groupSpinner);
        EditText email = view.findViewById(R.id.emailTextView);
        EditText password = view.findViewById(R.id.passwordTextView);
        EditText verificationCode = view.findViewById(R.id.verificationcodeTextView);
        EditText groupEmail = view.findViewById(R.id.groupEmailTextView);
        EditText alias = view.findViewById(R.id.aliasTextView);
        EditText task = view.findViewById(R.id.taskTextView);
        EditText name = view.findViewById(R.id.nameTextView);
        EditText measuringUnit = view.findViewById(R.id.measuringUnitTextView);
        EditText limit = view.findViewById(R.id.limitTextView);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser usera = firebaseAuth.getCurrentUser();

        CollectionReference collectionRef100;

        if (usera!=null) {
            collectionRef100 = firestore100.collection(usera.getEmail());
        } else {
            collectionRef100 = firestore100.collection(collectionEmail);
        }
        List<String> fieldValues = new ArrayList<>();
        collectionRef100.orderBy("datetime", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(querySnapshot -> {
                    Integer currentIndex = 0;
                    fieldValues.add("NEW");
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        if ("No".equals(document.getString("copy"))) {
                            String fieldValue = document.getString("name");
                            fieldValues.add(fieldValue);
                        }
                    }
                    String findedStr = "";
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        if ("No".equals(document.getString("copy"))) {
                            if ("current".equals(document.getString("current"))) {
                                email.setText(document.getString("accountemail"));
                                password.setText(document.getString("accountpassword"));
                                verificationCode.setText(document.getString("accountverificationcode"));
                                groupEmail.setText(document.getString("groupemail"));
                                alias.setText(document.getString("alias"));
                                task.setText(document.getString("task"));
                                name.setText(document.getString("name"));
                                measuringUnit.setText(document.getString("measuringunit"));
                                limit.setText(String.format("%.2f",document.getDouble("limit")));
                                findedStr = "Yes";
                                currentIndex = currentIndex + 1;
                                break;
                            }
                            currentIndex = currentIndex + 1;
                        }
                    }
                    //Toast.makeText(MainActivity.this, "Populate the spinner with fieldValues", Toast.LENGTH_SHORT).show();
                    // Populate the spinner with fieldValues
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, fieldValues);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    chooseGroup.setAdapter(adapter);
                    //int selectedIndex = 2;  // Index of the item you want to select
                    if (!findedStr.equals("")) {
                        chooseGroup.setSelection(currentIndex);
                    } else {
                        email.setText("");
                        password.setText("");
                        verificationCode.setText("");
                        groupEmail.setText("");
                        alias.setText("");
                        task.setText("");
                        name.setText("");
                        measuringUnit.setText("");
                        limit.setText("");
                        chooseGroup.setSelection(0);
                    }
                })
                .addOnFailureListener(e -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, fieldValues);
                    fieldValues.add("NEW");
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    chooseGroup.setAdapter(adapter);
                    //int selectedIndex = 2;  // Index of the item you want to select
                    chooseGroup.setSelection(0);
                    // Error occurred while fetching the documents
                    //Toast.makeText(MainActivity.this, "Doesn't populate the spinner with fieldValues", Toast.LENGTH_SHORT).show();
                });
    }

    private void showGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Group");

        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_add_group, null);
        builder.setView(view);

        Spinner chooseGroup =view.findViewById(R.id.groupSpinner);
        EditText email = view.findViewById(R.id.emailTextView);
        EditText password = view.findViewById(R.id.passwordTextView);
        EditText verificationCode = view.findViewById(R.id.verificationcodeTextView);
        EditText groupEmail = view.findViewById(R.id.groupEmailTextView);
        EditText alias = view.findViewById(R.id.aliasTextView);
        EditText task = view.findViewById(R.id.taskTextView);
        EditText name = view.findViewById(R.id.nameTextView);
        EditText measuringUnit = view.findViewById(R.id.measuringUnitTextView);
        EditText limit = view.findViewById(R.id.limitTextView);
        Button cancelGroupButton = view.findViewById(R.id.cancelGroupButton);
        Button deleteGroupButton = view.findViewById(R.id.deleteGroupButton);
        Button addButton = view.findViewById(R.id.addGroupButton);
        Button resendCodeButton = view.findViewById(R.id.resendverificationcodeButton);
        Button changePasswordButton = view.findViewById(R.id.changePasswordButton);
        Button deleteAccountButton = view.findViewById(R.id.deleteAccountButton);
        Button logoutAccountButton = view.findViewById(R.id.logoutButton);
        Button loginAccountButton = view.findViewById(R.id.loginButton);
        Button createAccountButton = view.findViewById(R.id.createaccountButton);

        AlertDialog alterdialog = builder.create();

        alterdialog.setOnDismissListener(dialog -> {
            //alterdialog.dismiss();
        });

        alterdialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                setSpinnerValue(view);

                chooseGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Integer selectedItem = chooseGroup.getSelectedItemPosition();

                        if (selectedItem==0) {
                            email.setText("");
                            password.setText("");
                            verificationCode.setText("");
                            groupEmail.setText("");
                            alias.setText("");
                            task.setText("");
                            name.setText("");
                            measuringUnit.setText("");
                            limit.setText("");
                        } else {

                            FirebaseFirestore firestore100 = FirebaseFirestore.getInstance();


                            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                            FirebaseUser usera = firebaseAuth.getCurrentUser();

                            CollectionReference collectionRef100;

                            if (usera!=null) {
                                collectionRef100 = firestore100.collection(usera.getEmail());
                            } else {
                                collectionRef100 = firestore100.collection(collectionEmail);
                            }

                            collectionRef100.orderBy("datetime", Query.Direction.ASCENDING).get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        //List<String> fieldValues = new ArrayList<>();
                                        Integer currentIndex = 0;
                                        //fieldValues.add("NEW");
                                        for (QueryDocumentSnapshot document : querySnapshot) {
                                            if ("No".equals(document.getString("copy"))) {
                                                if (selectedItem-1 == currentIndex) {
                                                    email.setText(document.getString("accountemail"));
                                                    password.setText(document.getString("accountpassword"));
                                                    verificationCode.setText(document.getString("accountverificationcode"));
                                                    groupEmail.setText(document.getString("groupemail"));
                                                    alias.setText(document.getString("alias"));
                                                    task.setText(document.getString("task"));
                                                    name.setText(document.getString("name"));
                                                    measuringUnit.setText(document.getString("measuringunit"));
                                                    limit.setText(String.format("%.2f", document.getDouble("limit")));
                                                }
                                                currentIndex = currentIndex + 1;
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error occurred while fetching the documents
                                    });
                        }
                        // Perform actions based on the selected item
                        // ...

                        // Example: Display a toast message with the selected item
                        //Toast.makeText(getApplicationContext(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Handle the case where nothing is selected (optional)
                    }
                });



                // Get the buttons from the dialog
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view2) {

                        //String username = usernameEditText.getText().toString();

                        String emailStr = email.getText().toString();
                        String passwordStr = password.getText().toString();
                        String verificationCodeStr = verificationCode.getText().toString();
                        String groupEmailStr = groupEmail.getText().toString();
                        String aliasStr = alias.getText().toString();
                        String taskStr = task.getText().toString();
                        String nameStr = name.getText().toString();
                        String measuringUnitStr = measuringUnit.getText().toString();
                        String limitStr = limit.getText().toString();

                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                        //FirebaseAuth.getInstance().signOut();

                        FirebaseUser usera = firebaseAuth.getCurrentUser();

                        addcompletedStr = "";

                        if (!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passwordStr) && !TextUtils.isEmpty(verificationCodeStr) && !TextUtils.isEmpty(groupEmailStr) && !TextUtils.isEmpty(aliasStr) && !TextUtils.isEmpty(nameStr)) {
                        } else {
                            return;
                        }

                        if (usera==null) {
                            return;
                        }

                        if (!emailStr.equals(usera.getEmail())) {
                            return;
                        }

                        if (!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passwordStr)) {

                            firebaseAuth.signInWithEmailAndPassword(emailStr, passwordStr)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(MainActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();

                                            FirebaseUser usera2 = firebaseAuth.getCurrentUser();

                                            if (!collectionaccountEmail.equals(usera2.getEmail())) {
                                                /*
                                                FirebaseFirestore firestore3 = FirebaseFirestore.getInstance();


                                                CollectionReference collectionRef = firestore3.collection(usera2.getEmail());
                                                //String fieldName = "name";
                                                //String desiredValue = name;

                                                //final String[] documentId2 = {""};

                                                collectionEmail = "empty";
                                                collectionaccountEmail = "empty";
                                                collectionaccountPassword = "empty";
                                                collectionGroup = "empty";
                                                collectionAlias = "empty";
                                                collectiongroupEmail = "empty";
                                                collectionaccountMeasuringUnit="empty";

                                                collectionRef.get()
                                                        .addOnSuccessListener(querySnapshot -> {
                                                            String documentId = "";
                                                            for (QueryDocumentSnapshot document : querySnapshot) {
                                                                // Access individual documents here
                                                                if ("current".equals(document.getString("current"))) {
                                                                    collectionaccountEmail = document.getString("accountemail");
                                                                    collectionaccountPassword = document.getString("accountpassword");
                                                                    collectionEmail = document.getString("groupemail");
                                                                    collectionGroup = document.getString("name");
                                                                    collectionLimit = document.getDouble("limit");
                                                                    collectionAlias = document.getString("alias");
                                                                    collectionaccountMeasuringUnit = document.getString("measuringunit");
                                                                    collectiongroupEmail = document.getString("groupemail");
                                                                }
                                                                // ... do something with the document data
                                                            }
                                                            if (collectionEmail.equals("empty")) {
                                                                collectionEmail = "empty";
                                                                collectionaccountEmail = "empty";
                                                                collectionaccountPassword = "empty";
                                                                collectionGroup = "empty";
                                                                collectionAlias = "empty";
                                                                collectiongroupEmail = "empty";
                                                                collectionaccountMeasuringUnit="empty";
                                                                firestore = FirebaseFirestore.getInstance();
                                                                collectionReference = firestore.collection(collectionEmail);

                                                                //editName = findViewById(R.id.editName);
                                                                //editNumber = findViewById(R.id.editNumber);
                                                                //editPrice = findViewById(R.id.editPrice);
                                                                btnAdd = findViewById(R.id.btnAdd);

                                                                recyclerView = findViewById(R.id.recyclerView);
                                                                RecyclerView.LayoutManager layoutManager3 = new LinearLayoutManager(MainActivity.this);
                                                                recyclerView.setLayoutManager(layoutManager3);
                                                                //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                                                                Query query3 = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                                                                adapter = new FirestoreAdapter(query3);
                                                                recyclerView.setAdapter(adapter);
                                                                //setSpinnerValue(view);
                                                            } else {
                                                                firestore = FirebaseFirestore.getInstance();
                                                                collectionReference = firestore.collection(collectionEmail + collectionGroup);

                                                                //editName = findViewById(R.id.editName);
                                                                //editNumber = findViewById(R.id.editNumber);
                                                                //editPrice = findViewById(R.id.editPrice);
                                                                btnAdd = findViewById(R.id.btnAdd);

                                                                recyclerView = findViewById(R.id.recyclerView);
                                                                RecyclerView.LayoutManager layoutManager3 = new LinearLayoutManager(MainActivity.this);
                                                                recyclerView.setLayoutManager(layoutManager3);
                                                                //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                                                                Query query3 = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                                                                adapter = new FirestoreAdapter(query3);
                                                                recyclerView.setAdapter(adapter);
                                                                //setSpinnerValue(view);
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            // Error occurred while fetching the documents
                                                        });

                                                 */
                                            }
                                            FirebaseFirestore firestore200 = FirebaseFirestore.getInstance();
                                            String collectionPath = emailStr;

                                            firestore200.collection(collectionPath).orderBy("datetime", Query.Direction.ASCENDING)
                                                    .get()
                                                    .addOnSuccessListener(querySnapshot -> {

                                                        String notequalsEmail = "";
                                                        String notequalsEmail2 = "";

                                                        //if (!emailStr.equals(groupEmailStr)) {
                                                            notequalsEmail = "NotEquals";

                                                            notequalsEmail2 = "NotEquals";
                                                            Integer selectedItem = chooseGroup.getSelectedItemPosition();
                                                            Integer currentIndex=1;

                                                            for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                                                                if (documentSnapshot.getString("copy").equals("No")) {
                                                                    // Access each document here
                                                                    if (selectedItem==currentIndex && documentSnapshot.getString("groupemail").equals(groupEmailStr) && documentSnapshot.getString("name").equals(nameStr)) {
                                                                        notequalsEmail = "Finded";
                                                                    }

                                                                    if (documentSnapshot.getString("groupemail").equals(groupEmailStr) && documentSnapshot.getString("name").equals(nameStr)) {
                                                                        notequalsEmail2 = "Finded";
                                                                    }
                                                                    currentIndex=currentIndex+1;
                                                                }
                                                                // Process the data as needed
                                                            }
                                                        //}
                                                        int currentPosition = chooseGroup.getSelectedItemPosition();
                                                        String selectedValue = (String) chooseGroup.getItemAtPosition(currentPosition);
                                                        if (!selectedValue.equals("NEW")) {
                                                          if (notequalsEmail.equals("NotEquals")) {
                                                              return;
                                                          }
                                                        }

                                                        if (selectedValue.equals("NEW")) {
                                                            //if (emailStr.equals(groupEmailStr)) {
                                                                if (!notequalsEmail2.equals("NotEquals")) {
                                                                    return;
                                                                }
                                                            //}
                                                        }

                                                        if (nameStr.equals("NEW")) {
                                                            return;
                                                        }
                                                        //if (!emailStr.equals(groupEmailStr) && notequalsEmail.equals("NotEquals")) {
                                                        //    return;
                                                        //}

                                                        Double limitDouble = Double.valueOf(0);
                                                        if (emailStr.equals(groupEmailStr)) {
                                                            if (!TextUtils.isEmpty(limitStr)) {
                                                                try {
                                                                    limitDouble = Double.parseDouble(String.format("%.2f", Double.parseDouble(limitStr.replaceAll(",", "."))).replaceAll(",", "."));
                                                                } catch (NumberFormatException e) {
                                                                    //throw new RuntimeException(e);
                                                                }
                                                            }

                                                            //BigDecimal decimalValue = BigDecimal.valueOf(limitDouble);

                                                            // Round the BigDecimal value to two decimal places
                                                            //BigDecimal roundedValue = decimalValue.setScale(2, BigDecimal.ROUND_HALF_UP);

                                                            //limitDouble = roundedValue.doubleValue();

                                                            if (limitDouble == 0) {
                                                                return;
                                                            }

                                                            if (limitDouble > 0) {
                                                                limitDouble = limitDouble * -1;
                                                            }

                                                        }

                                                        if (!TextUtils.isEmpty(nameStr)) {
                                                            if (chooseGroup.getSelectedItem().toString().equals("NEW")) {
                                                            } else {
                                                                if (!chooseGroup.getSelectedItem().toString().equals(nameStr)) {
                                                                    return;
                                                                }
                                                            }
                                                        }

                                                        FirebaseAuth firebaseAuth2 = FirebaseAuth.getInstance();

                                                        //FirebaseAuth.getInstance().signOut();
                                                        FirebaseUser userb = firebaseAuth2.getCurrentUser();

                                                        if (!groupEmailStr.equals(emailStr)) {
                                                            FirebaseFirestore firestore11 = FirebaseFirestore.getInstance();

                                                            CollectionReference collectionRef11 = firestore11.collection(groupEmailStr);

                                                            Double finalLimitDouble = limitDouble;
                                                            collectionRef11.get()
                                                                    .addOnCompleteListener(task2 -> {
                                                                        if (task2.isSuccessful()) {
                                                                            if (task2.getResult().isEmpty()) {
                                                                                return;
                                                                                // Collection does not exist or is empty
                                                                            } else {
                                                                                FirebaseFirestore firestore12 = FirebaseFirestore.getInstance();
                                                                                String collectionPath2 = groupEmailStr;

                                                                                firestore12.collection(collectionPath2).get()
                                                                                        .addOnSuccessListener(querySnapshot333 -> {
                                                                                            List<Object> fieldValues = new ArrayList<>();

                                                                                            for (QueryDocumentSnapshot documentSnapshot : querySnapshot333) {
                                                                                                if (documentSnapshot.getString("accountemail").equals(groupEmailStr) && documentSnapshot.getString("groupemail").equals(groupEmailStr) && documentSnapshot.getString("name").equals(nameStr) && documentSnapshot.getString("copy").equals("No")) {
                                                                                                    if (userb != null) {
                                                                                                        boolean isEmailVerified = userb.isEmailVerified();
                                                                                                        if (isEmailVerified) {
                                                                                                            Toast.makeText(MainActivity.this, "The email has been verified", Toast.LENGTH_SHORT).show();
                                                                                                            if (!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passwordStr) && !TextUtils.isEmpty(verificationCodeStr) && !TextUtils.isEmpty(groupEmailStr) && !TextUtils.isEmpty(aliasStr) && !TextUtils.isEmpty(nameStr)) {
                                                                                                                if (emailStr.equals(userb.getEmail())) {
                                                                                                                    // The email has been verified
                                                                                                                    addGroup(emailStr, passwordStr, verificationCodeStr, groupEmailStr, aliasStr, documentSnapshot.getString("task"), nameStr, documentSnapshot.getString("measuringunit"), documentSnapshot.getDouble("limit"), "current", "Yes", documentSnapshot.getDouble("balance"));
                                                                                                                    addcompletedStr = "Yes";
                                                                                                                    Timer timer = new Timer();

                                                                                                                    TimerTask task6 = new TimerTask() {
                                                                                                                        @Override
                                                                                                                        public void run() {
                                                                                                                            if (addcompletedStr.equals("Yes")) {
                                                                                                                                setSpinnerValue(view);
                                                                                                                            }
                                                                                                                        }
                                                                                                                    };

                                                                                                                    // Schedule the task to run after a delay of 5 seconds
                                                                                                                    timer.schedule(task6, 2000);
                                                                                                                }
                                                                                                            }

                                                                                                        } else {
                                                                                                            Toast.makeText(MainActivity.this, "The email has not been verified", Toast.LENGTH_SHORT).show();
                                                                                                            if (!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passwordStr) && !TextUtils.isEmpty(verificationCodeStr)) {
                                                                                                                checkVerificationCode(verificationCodeStr);
                                                                                                            }
                                                                                                            //checkVerificationCode(verificationCodeStr);
                                                                                                            // The email has not been verified
                                                                                                        }

                                                                                                    } else {
                                                                                                        // No user is currently signed in
                                                                                                        if (!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passwordStr)) {

                                                                                                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                                                                                                            CollectionReference collectionRef = firestore.collection(emailStr);

                                                                                                            collectionRef.get()
                                                                                                                    .addOnCompleteListener(task10 -> {
                                                                                                                        if (task10.isSuccessful()) {
                                                                                                                            if (task10.getResult().isEmpty()) {
                                                                                                                                firebaseAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
                                                                                                                                        .addOnCompleteListener(task5 -> {
                                                                                                                                            if (task5.isSuccessful()) {
                                                                                                                                                FirebaseUser user2 = firebaseAuth.getCurrentUser();
                                                                                                                                                user2.sendEmailVerification()
                                                                                                                                                        .addOnCompleteListener(task3 -> {
                                                                                                                                                            if (task3.isSuccessful()) {
                                                                                                                                                                // Verification email sent successfully
                                                                                                                                                                Toast.makeText(MainActivity.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                                                                                                                                                            } else {
                                                                                                                                                                // Failed to send verification email
                                                                                                                                                                Toast.makeText(MainActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                                                                                                                            }
                                                                                                                                                        });
                                                                                                                                                Toast.makeText(MainActivity.this, "Registration successfull.", Toast.LENGTH_SHORT).show();
                                                                                                                                            } else {
                                                                                                                                                // Registration failed
                                                                                                                                                Toast.makeText(MainActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                                                                                                                                            }
                                                                                                                                        });
                                                                                                                                // Collection does not exist or is empty
                                                                                                                                Toast.makeText(MainActivity.this, "Collection does not exists", Toast.LENGTH_SHORT).show();
                                                                                                                            } else {
                                                                                                                                firebaseAuth.signInWithEmailAndPassword(emailStr, passwordStr)
                                                                                                                                        .addOnCompleteListener(task5 -> {
                                                                                                                                            if (task5.isSuccessful()) {
                                                                                                                                                FirebaseUser user2 = firebaseAuth.getCurrentUser();
                                                                                                                                                if (user2 != null) {
                                                                                                                                                    boolean isEmailVerified2 = user2.isEmailVerified();
                                                                                                                                                    if (isEmailVerified2) {
                                                                                                                                                        // The email has been verified
                                                                                                                                                        Toast.makeText(MainActivity.this, "The email has been verified", Toast.LENGTH_SHORT).show();
                                                                                                                                                    } else {
                                                                                                                                                        checkVerificationCode(verificationCodeStr);
                                                                                                                                                        // The email has not been verified
                                                                                                                                                        Toast.makeText(MainActivity.this, "The email has not been verified", Toast.LENGTH_SHORT).show();
                                                                                                                                                    }
                                                                                                                                                } else {
                                                                                                                                                    // No user is currently signed in
                                                                                                                                                }

                                                                                                                                                // Login successful, redirect to the main activity
                                                                                                                                                Toast.makeText(MainActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();
                                                                                                                                                // Redirect to the main activity or any desired destination
                                                                                                                                            } else {
                                                                                                                                                // Login failed
                                                                                                                                                Toast.makeText(MainActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                                                                                                                                            }
                                                                                                                                        });
                                                                                                                                // Collection exists
                                                                                                                                Toast.makeText(MainActivity.this, "Collection exists", Toast.LENGTH_SHORT).show();
                                                                                                                            }
                                                                                                                        } else {
                                                                                                                            // Error occurred while fetching the collection
                                                                                                                        }
                                                                                                                    });

                                                                                                        }
                                                                                                    }
                                                                                                    return;
                                                                                                }
                                                                                            }

                                                                                            // Process the field values as needed
                                                                                            //for (Object fieldValue : fieldValues) {
                                                                                            // Perform actions with each field value
                                                                                            //    System.out.println(fieldValue.toString());
                                                                                            //}
                                                                                        })
                                                                                        .addOnFailureListener(e -> {
                                                                                            // Error occurred while retrieving the field values
                                                                                        });

                                                                                // Collection exists
                                                                            }
                                                                        } else {
                                                                            return;
                                                                            // Error occurred while fetching the collection
                                                                        }
                                                                    });
                                                        } else {
                                                            if (userb != null) {
                                                                boolean isEmailVerified = userb.isEmailVerified();
                                                                if (isEmailVerified) {
                                                                    Toast.makeText(MainActivity.this, "The email has been verified", Toast.LENGTH_SHORT).show();
                                                                    if (!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passwordStr) && !TextUtils.isEmpty(verificationCodeStr) && !TextUtils.isEmpty(groupEmailStr) && !TextUtils.isEmpty(aliasStr) && !TextUtils.isEmpty(taskStr) && !TextUtils.isEmpty(nameStr) && !TextUtils.isEmpty(measuringUnitStr) && !TextUtils.isEmpty(limitStr)) {
                                                                        if (emailStr.equals(userb.getEmail())) {
                                                                            // The email has been verified
                                                                            addGroup(emailStr, passwordStr, verificationCodeStr, groupEmailStr, aliasStr, taskStr, nameStr, measuringUnitStr, limitDouble, "current", "Yes", Double.valueOf(0));
                                                                            addcompletedStr = "Yes";
                                                                            Timer timer = new Timer();

                                                                            TimerTask task6 = new TimerTask() {
                                                                                @Override
                                                                                public void run() {
                                                                                    if (addcompletedStr.equals("Yes")) {
                                                                                        setSpinnerValue(view);
                                                                                    }
                                                                                }
                                                                            };

                                                                            // Schedule the task to run after a delay of 5 seconds
                                                                            timer.schedule(task6, 2000);
                                                                        }
                                                                    }

                                                                } else {
                                                                    Toast.makeText(MainActivity.this, "The email has not been verified", Toast.LENGTH_SHORT).show();
                                                                    if (!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passwordStr) && !TextUtils.isEmpty(verificationCodeStr)) {
                                                                        checkVerificationCode(verificationCodeStr);
                                                                    }
                                                                    //checkVerificationCode(verificationCodeStr);
                                                                    // The email has not been verified
                                                                }
                                                            } else {
                                                                // No user is currently signed in
                                                                if (!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passwordStr)) {

                                                                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                                                                    CollectionReference collectionRef = firestore.collection(emailStr);

                                                                    collectionRef.get()
                                                                            .addOnCompleteListener(task3 -> {
                                                                                if (task3.isSuccessful()) {
                                                                                    if (task3.getResult().isEmpty()) {
                                                                                        firebaseAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
                                                                                                .addOnCompleteListener(task2 -> {
                                                                                                    if (task2.isSuccessful()) {
                                                                                                        FirebaseUser user2 = firebaseAuth.getCurrentUser();
                                                                                                        user2.sendEmailVerification()
                                                                                                                .addOnCompleteListener(task4 -> {
                                                                                                                    if (task4.isSuccessful()) {
                                                                                                                        // Verification email sent successfully
                                                                                                                        Toast.makeText(MainActivity.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                                                                                                                    } else {
                                                                                                                        // Failed to send verification email
                                                                                                                        Toast.makeText(MainActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                                                                                    }
                                                                                                                });
                                                                                                        Toast.makeText(MainActivity.this, "Registration successfull.", Toast.LENGTH_SHORT).show();
                                                                                                    } else {
                                                                                                        // Registration failed
                                                                                                        Toast.makeText(MainActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                });
                                                                                        // Collection does not exist or is empty
                                                                                        Toast.makeText(MainActivity.this, "Collection does not exists", Toast.LENGTH_SHORT).show();
                                                                                    } else {
                                                                                        firebaseAuth.signInWithEmailAndPassword(emailStr, passwordStr)
                                                                                                .addOnCompleteListener(task2 -> {
                                                                                                    if (task2.isSuccessful()) {
                                                                                                        FirebaseUser user2 = firebaseAuth.getCurrentUser();
                                                                                                        if (user2 != null) {
                                                                                                            boolean isEmailVerified2 = user2.isEmailVerified();
                                                                                                            if (isEmailVerified2) {
                                                                                                                // The email has been verified
                                                                                                                Toast.makeText(MainActivity.this, "The email has been verified", Toast.LENGTH_SHORT).show();
                                                                                                            } else {
                                                                                                                checkVerificationCode(verificationCodeStr);
                                                                                                                // The email has not been verified
                                                                                                                Toast.makeText(MainActivity.this, "The email has not been verified", Toast.LENGTH_SHORT).show();
                                                                                                            }
                                                                                                        } else {
                                                                                                            // No user is currently signed in
                                                                                                        }

                                                                                                        // Login successful, redirect to the main activity
                                                                                                        Toast.makeText(MainActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();
                                                                                                        // Redirect to the main activity or any desired destination
                                                                                                    } else {
                                                                                                        // Login failed
                                                                                                        Toast.makeText(MainActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                });
                                                                                        // Collection exists
                                                                                        Toast.makeText(MainActivity.this, "Collection exists", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                } else {
                                                                                    // Error occurred while fetching the collection
                                                                                }
                                                                            });

                                                                }
                                                            }
                                                        }


                                                        // Login successful, redirect to the main activity
                                                        //Toast.makeText(MainActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();
                                                        //setSpinnerValue(view);
                                                        // Redirect to the main activity or any desired destination
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        // Error occurred while retrieving documents
                                                    });
                                        } else {
                                            // Login failed
                                            Toast.makeText(MainActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                                        }

                                    });
                        }
                    }
                });

                deleteGroupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view2) {
                        String groupEmailStr = groupEmail.getText().toString();
                        String nameStr = name.getText().toString();
                        String emailStr = email.getText().toString();

                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                        FirebaseUser usera = firebaseAuth.getCurrentUser();

                        if (usera != null) {
                            if (!TextUtils.isEmpty(groupEmailStr) && !TextUtils.isEmpty(nameStr) && !TextUtils.isEmpty(emailStr)) {
                                if (groupEmailStr.equals(collectiongroupEmail) && nameStr.equals(collectionGroup) && emailStr.equals(collectionaccountEmail)) {
                                    //if (groupEmailStr.equals(usera.getEmail())) {
                                    deleteGroupRow(groupEmailStr, nameStr);
                                    collectionEmail = "empty";
                                    collectionaccountEmail = "empty";
                                    collectionaccountPassword = "empty";
                                    collectionGroup = "empty";
                                    collectionAlias = "empty";
                                    collectiongroupEmail = "empty";
                                    collectionaccountMeasuringUnit = "empty";
                                    firestore = FirebaseFirestore.getInstance();
                                    collectionReference = firestore.collection(collectionEmail);

                                    //editName = findViewById(R.id.editName);
                                    //editNumber = findViewById(R.id.editNumber);
                                    //editPrice = findViewById(R.id.editPrice);
                                    btnAdd = findViewById(R.id.btnAdd);

                                    recyclerView = findViewById(R.id.recyclerView);
                                    RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(MainActivity.this);
                                    recyclerView.setLayoutManager(layoutManager2);
                                    //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                                    Query query2 = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                                    adapter = new FirestoreAdapter(query2);
                                    recyclerView.setAdapter(adapter);
                                    //alterdialog.dismiss();
                                    //setSpinnerValue(view);
                                    //}
                                    Timer timer = new Timer();

                                    TimerTask task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            setSpinnerValue(view);
                                        }
                                    };

                                    // Schedule the task to run after a delay of 5 seconds
                                    timer.schedule(task, 2000);
                                } else {
                                    Toast.makeText(MainActivity.this, "Please ADD Group first.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });

                cancelGroupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {alterdialog.dismiss();
                    }
                });

                resendCodeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user!=null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // Verification email sent successfully
                                            Toast.makeText(MainActivity.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                                            //alterdialog.dismiss();
                                        } else {
                                            // Failed to send verification email
                                            Toast.makeText(MainActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });

                logoutAccountButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view2) {
                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        FirebaseAuth.getInstance().signOut();

                        String emailStr = email.getText().toString();
                        String passwordStr = password.getText().toString();

                        collectionEmail = "empty";
                        collectionaccountEmail = "empty";
                        collectionaccountPassword = "empty";
                        collectionGroup = "empty";
                        collectionAlias = "empty";
                        collectiongroupEmail = "empty";
                        collectionaccountMeasuringUnit="empty";
                        firestore = FirebaseFirestore.getInstance();
                        collectionReference = firestore.collection(collectionEmail);

                        //editName = findViewById(R.id.editName);
                        //editNumber = findViewById(R.id.editNumber);
                        //editPrice = findViewById(R.id.editPrice);
                        btnAdd = findViewById(R.id.btnAdd);

                        recyclerView = findViewById(R.id.recyclerView);
                        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(MainActivity.this);
                        recyclerView.setLayoutManager(layoutManager2);
                        //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                        Query query2 = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                        adapter = new FirestoreAdapter(query2);
                        recyclerView.setAdapter(adapter);

                        Timer timer = new Timer();

                        TimerTask task2 = new TimerTask() {
                            @Override
                            public void run() {
                                setSpinnerValue(view);
                            }
                        };

                        // Schedule the task to run after a delay of 5 seconds
                        timer.schedule(task2, 2000);
                    }
                });

                loginAccountButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view2) {
                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                        FirebaseAuth.getInstance().signOut();

                        String emailStr = email.getText().toString();
                        String passwordStr = password.getText().toString();

                        if (!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passwordStr)) {

                            firebaseAuth.signInWithEmailAndPassword(emailStr, passwordStr)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // Login successful, redirect to the main activity
                                            Toast.makeText(MainActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();

                                            FirebaseFirestore firestore3 = FirebaseFirestore.getInstance();

                                            FirebaseUser usera = firebaseAuth.getCurrentUser();

                                            CollectionReference collectionRef = firestore3.collection(usera.getEmail());
                                            //String fieldName = "name";
                                            //String desiredValue = name;

                                            //final String[] documentId2 = {""};

                                            collectionEmail = "empty";
                                            collectionaccountEmail = "empty";
                                            collectionaccountPassword = "empty";
                                            collectionGroup = "empty";
                                            collectionAlias = "empty";
                                            collectiongroupEmail = "empty";
                                            collectionaccountMeasuringUnit="empty";

                                            collectionRef.get()
                                                    .addOnSuccessListener(querySnapshot -> {
                                                        String documentId = "";
                                                        for (QueryDocumentSnapshot document : querySnapshot) {
                                                            // Access individual documents here
                                                            if ("current".equals(document.getString("current"))) {
                                                                collectionaccountEmail = document.getString("accountemail");
                                                                collectionaccountPassword = document.getString("accountpassword");
                                                                collectionEmail = document.getString("groupemail");
                                                                collectionGroup = document.getString("name");
                                                                collectionLimit = document.getDouble("limit");
                                                                collectionAlias = document.getString("alias");
                                                                collectionaccountMeasuringUnit = document.getString("measuringunit");
                                                                collectiongroupEmail = document.getString("groupemail");
                                                            }
                                                            // ... do something with the document data
                                                        }
                                                        if (collectionEmail.equals("empty")) {
                                                            collectionEmail = "empty";
                                                            collectionaccountEmail = "empty";
                                                            collectionaccountPassword = "empty";
                                                            collectionGroup = "empty";
                                                            collectionAlias = "empty";
                                                            collectiongroupEmail = "empty";
                                                            collectionaccountMeasuringUnit="empty";
                                                            firestore = FirebaseFirestore.getInstance();
                                                            collectionReference = firestore.collection(collectionEmail);

                                                            //editName = findViewById(R.id.editName);
                                                            //editNumber = findViewById(R.id.editNumber);
                                                            //editPrice = findViewById(R.id.editPrice);
                                                            btnAdd = findViewById(R.id.btnAdd);

                                                            recyclerView = findViewById(R.id.recyclerView);
                                                            RecyclerView.LayoutManager layoutManager3 = new LinearLayoutManager(MainActivity.this);
                                                            recyclerView.setLayoutManager(layoutManager3);
                                                            //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                                                            Query query3 = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                                                            adapter = new FirestoreAdapter(query3);
                                                            recyclerView.setAdapter(adapter);
                                                            Timer timer = new Timer();

                                                            TimerTask task2 = new TimerTask() {
                                                                @Override
                                                                public void run() {
                                                                    setSpinnerValue(view);
                                                                }
                                                            };

                                                            // Schedule the task to run after a delay of 5 seconds
                                                            timer.schedule(task2, 2000);
                                                        } else {
                                                            firestore = FirebaseFirestore.getInstance();
                                                            collectionReference = firestore.collection(collectionEmail+collectionGroup);

                                                            //editName = findViewById(R.id.editName);
                                                            //editNumber = findViewById(R.id.editNumber);
                                                            //editPrice = findViewById(R.id.editPrice);
                                                            btnAdd = findViewById(R.id.btnAdd);

                                                            recyclerView = findViewById(R.id.recyclerView);
                                                            RecyclerView.LayoutManager layoutManager3 = new LinearLayoutManager(MainActivity.this);
                                                            recyclerView.setLayoutManager(layoutManager3);
                                                            //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                                                            Query query3 = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                                                            adapter = new FirestoreAdapter(query3);
                                                            recyclerView.setAdapter(adapter);
                                                            Timer timer = new Timer();

                                                            TimerTask task2 = new TimerTask() {
                                                                @Override
                                                                public void run() {
                                                                    setSpinnerValue(view);
                                                                }
                                                            };

                                                            // Schedule the task to run after a delay of 5 seconds
                                                            timer.schedule(task2, 2000);
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        // Error occurred while fetching the documents
                                                    });
                                            // Redirect to the main activity or any desired destination
                                        } else {
                                            collectionEmail = "empty";
                                            collectionaccountEmail = "empty";
                                            collectionaccountPassword = "empty";
                                            collectionGroup = "empty";
                                            collectionAlias = "empty";
                                            collectiongroupEmail = "empty";
                                            collectionaccountMeasuringUnit="empty";
                                            firestore = FirebaseFirestore.getInstance();
                                            collectionReference = firestore.collection(collectionEmail);

                                            //editName = findViewById(R.id.editName);
                                            //editNumber = findViewById(R.id.editNumber);
                                            //editPrice = findViewById(R.id.editPrice);
                                            btnAdd = findViewById(R.id.btnAdd);

                                            recyclerView = findViewById(R.id.recyclerView);
                                            RecyclerView.LayoutManager layoutManager3 = new LinearLayoutManager(MainActivity.this);
                                            recyclerView.setLayoutManager(layoutManager3);
                                            //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                                            Query query3 = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                                            adapter = new FirestoreAdapter(query3);
                                            recyclerView.setAdapter(adapter);
                                            Timer timer = new Timer();

                                            TimerTask task2 = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    setSpinnerValue(view);
                                                }
                                            };

                                            // Schedule the task to run after a delay of 5 seconds
                                            timer.schedule(task2, 2000);
                                            // Login failed
                                            Toast.makeText(MainActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                                        }

                                    });
                        }

                        if (TextUtils.isEmpty(emailStr) || TextUtils.isEmpty(passwordStr)) {
                            collectionEmail = "empty";
                            collectionaccountEmail = "empty";
                            collectionaccountPassword = "empty";
                            collectionGroup = "empty";
                            collectionAlias = "empty";
                            collectiongroupEmail = "empty";
                            collectionaccountMeasuringUnit="empty";
                            firestore = FirebaseFirestore.getInstance();
                            collectionReference = firestore.collection(collectionEmail);

                            //editName = findViewById(R.id.editName);
                            //editNumber = findViewById(R.id.editNumber);
                            //editPrice = findViewById(R.id.editPrice);
                            btnAdd = findViewById(R.id.btnAdd);

                            recyclerView = findViewById(R.id.recyclerView);
                            RecyclerView.LayoutManager layoutManager3 = new LinearLayoutManager(MainActivity.this);
                            recyclerView.setLayoutManager(layoutManager3);
                            //recyclerView.setLayoutManager(new LinearLayoutManager(this));

                            Query query3 = collectionReference.orderBy("datetime", Query.Direction.ASCENDING);

                            adapter = new FirestoreAdapter(query3);
                            recyclerView.setAdapter(adapter);
                            Timer timer = new Timer();

                            TimerTask task2 = new TimerTask() {
                                @Override
                                public void run() {
                                    setSpinnerValue(view);
                                }
                            };

                            // Schedule the task to run after a delay of 5 seconds
                            timer.schedule(task2, 2000);
                            // Login failed
                            Toast.makeText(MainActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                changePasswordButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String emailStr = email.getText().toString();
                        String passwordStr = password.getText().toString();

                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                        if (!TextUtils.isEmpty(emailStr)) {
                            //String email = firebaseAuth.getCurrentUser().getEmail();
                            firebaseAuth.sendPasswordResetEmail(emailStr)
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            // Password reset email sent successfully
                                            Toast.makeText(MainActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Failed to send password reset email
                                            Toast.makeText(MainActivity.this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    }
                });

                deleteAccountButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view2) {

                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                        FirebaseUser usera = firebaseAuth.getCurrentUser();

                        String emailStr = email.getText().toString();
                        String passwordStr = password.getText().toString();

                        //FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                        //FirebaseUser user = firebaseAuth.getCurrentUser();

                        //String emailStr = email.getText().toString();
                        //String passwordStr = password.getText().toString();

                        if (!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passwordStr)) {

                            firebaseAuth.signInWithEmailAndPassword(emailStr, passwordStr)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // Login successful, redirect to the main activity
                                            Toast.makeText(MainActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();
                                            collectionaccountEmail=emailStr;
                                            collectionaccountPassword=passwordStr;
                                            deleteGroupAccountRows();
                                            //setSpinnerValue(view);
                                            // Redirect to the main activity or any desired destination
                                        } else {
                                            // Login failed
                                            Toast.makeText(MainActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                        Timer timer = new Timer();

                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                setSpinnerValue(view);
                            }
                        };

                        // Schedule the task to run after a delay of 5 seconds
                        timer.schedule(task, 3000);
                    }
                });

                createAccountButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view2) {

                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                        FirebaseUser usera = firebaseAuth.getCurrentUser();

                        String emailStr = email.getText().toString();
                        String passwordStr = password.getText().toString();

                        //FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                        //FirebaseUser user = firebaseAuth.getCurrentUser();

                        //String emailStr = email.getText().toString();
                        //String passwordStr = password.getText().toString();

                        if (!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passwordStr)) {

                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                            CollectionReference collectionRef = firestore.collection(emailStr);

                            collectionRef.get()
                                    .addOnCompleteListener(task10 -> {
                                        if (task10.isSuccessful()) {
                                            if (task10.getResult().isEmpty()) {
                                                firebaseAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
                                                        .addOnCompleteListener(task5 -> {
                                                            if (task5.isSuccessful()) {
                                                                FirebaseUser user2 = firebaseAuth.getCurrentUser();
                                                                user2.sendEmailVerification()
                                                                        .addOnCompleteListener(task3 -> {
                                                                            if (task3.isSuccessful()) {
                                                                                // Verification email sent successfully
                                                                                Toast.makeText(MainActivity.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                                                                            } else {
                                                                                // Failed to send verification email
                                                                                Toast.makeText(MainActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                                Toast.makeText(MainActivity.this, "Registration successfull.", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                // Registration failed
                                                                Toast.makeText(MainActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                // Collection does not exist or is empty
                                                Toast.makeText(MainActivity.this, "Collection does not exists", Toast.LENGTH_SHORT).show();
                                            } else {
                                                /*
                                                firebaseAuth.signInWithEmailAndPassword(emailStr, passwordStr)
                                                        .addOnCompleteListener(task5 -> {
                                                            if (task5.isSuccessful()) {
                                                                FirebaseUser user2 = firebaseAuth.getCurrentUser();
                                                                if (user2 != null) {
                                                                    boolean isEmailVerified2 = user2.isEmailVerified();
                                                                    if (isEmailVerified2) {
                                                                        // The email has been verified
                                                                        Toast.makeText(MainActivity.this, "The email has been verified", Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        checkVerificationCode(verificationCodeStr);
                                                                        // The email has not been verified
                                                                        Toast.makeText(MainActivity.this, "The email has not been verified", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                } else {
                                                                    // No user is currently signed in
                                                                }

                                                                // Login successful, redirect to the main activity
                                                                Toast.makeText(MainActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();
                                                                // Redirect to the main activity or any desired destination
                                                            } else {
                                                                // Login failed
                                                                Toast.makeText(MainActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });

                                                 */
                                                // Collection exists
                                                Toast.makeText(MainActivity.this, "Collection exists", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            // Error occurred while fetching the collection
                                        }
                                    });

                        }

                        Timer timer = new Timer();

                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                setSpinnerValue(view);
                            }
                        };

                        // Schedule the task to run after a delay of 5 seconds
                        timer.schedule(task, 3000);
                    }
                });
            }
        });

        alterdialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Do something when camera permission is granted
                Intent intent = new Intent(MainActivity.this, TextRecognitionActivity.class);
                startActivity(intent);
            } else {
                // Handle case when camera permission is denied
                //Toast.makeText(MainActivity.this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == MICROPHONE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Do something when microphone permission is granted
                finish();
                Intent intent = new Intent(MainActivity.this, SpeechToText.class);
                startActivity(intent);
            } else {
                // Handle case when microphone permission is denied
                //Toast.makeText(MainActivity.this, "Microphone permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAddOptionsPopup(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.add_options_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.optionCreateGroup:
                        showGroupDialog();
                        break;
                    case R.id.option1:
                        if (!collectionEmail.equals("empty")) {
                            showAddDialog();
                        }
                        // Handle option 1
                        //Toast.makeText(MainActivity.this, "Add by EMTER TEXT", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.option2:
                        if (!collectionEmail.equals("empty")) {
                            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_REQUEST_CODE);
                            } else {
                                finish();
                                Intent intent = new Intent(MainActivity.this, SpeechToText.class);
                                startActivity(intent);
                            }
                        }
                        break;
                    case R.id.option3:
                        if (!collectionEmail.equals("empty")) {
                            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                            } else {
                                // Camera permission already granted, perform desired action
                                // For example, start camera preview
                                finish();
                                Intent intent = new Intent(MainActivity.this, TextRecognitionActivity.class);
                                startActivity(intent);
                            }
                        }
                        break;
                    case R.id.option4:
                        if (!collectionEmail.equals("empty")) {
                            finish();
                            Intent intent = new Intent(MainActivity.this, PdfCreatorExampleActivity.class);
                            startActivity(intent);
                        }
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void clearFields() {
        //editName.setText("");
        //editNumber.setText("");
        //editPrice.setText("");
    }

    private class FirestoreAdapter extends RecyclerView.Adapter<FirestoreAdapter.ViewHolder> {
        private List<DocumentSnapshot> documents;

        FirestoreAdapter(Query query) {
            documents = new ArrayList<>();
            query.addSnapshotListener((snapshot, exception) -> {
                if (exception != null) {
                    Log.e(TAG, "Error retrieving Firestore documents: ", exception);
                    return;
                }

                if (snapshot != null) {
                    for (DocumentChange dc : snapshot.getDocumentChanges()) {
                        DocumentSnapshot document = dc.getDocument();
                        switch (dc.getType()) {
                            case ADDED:
                                documents.add(document);
                                notifyItemInserted(documents.size() - 1);
                                break;
                            case MODIFIED:
                                int index = getDocumentIndex(document);
                                if (index != -1) {
                                    documents.set(index, document);
                                    notifyItemChanged(index);
                                }
                                break;
                            case REMOVED:
                                index = getDocumentIndex(document);
                                if (index != -1) {
                                    documents.remove(index);
                                    notifyItemRemoved(index);
                                }
                                break;
                        }
                    }
                }
            });

        }

        private int getDocumentIndex(DocumentSnapshot document) {
            for (int i = 0; i < documents.size(); i++) {
                if (documents.get(i).getId().equals(document.getId())) {
                    return i;
                }
            }
            return -1;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);

            if (collectionEmail.equals("empty")) {
                view.setVisibility(View.GONE);
            } else {
                view.setVisibility(View.VISIBLE);
            }

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DocumentSnapshot document = documents.get(position);

            Date datetime = document.getDate("datetime");
            String name = document.getString("name");
            Long number = document.getLong("number");
            Double price = document.getDouble("price");
            Double saldo = document.getDouble("saldo");

            //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(datetime);

            holder.textViewNumber.setText(Long.toString(number));
            holder.textViewName.setText("Name : "+name);

            NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

            // Enable grouping (thousand separators)
            numberFormat.setGroupingUsed(true);

            numberFormat.setMinimumFractionDigits(2);
            numberFormat.setMaximumFractionDigits(2);

            holder.textViewPrice.setText("Expense : " + numberFormat.format(price)+ " "+collectionaccountMeasuringUnit);
            if (price<0) {
                holder.textViewPrice.setTextColor(Color.RED);
            } else {
                holder.textViewPrice.setTextColor(Color.rgb(0, 100, 0));
            }
            holder.textViewSaldo.setText("Saldo : "+numberFormat.format(saldo)+" "+collectionaccountMeasuringUnit);

            BigDecimal decimalValue = BigDecimal.valueOf(saldo);

            // Round the BigDecimal value to two decimal places
            BigDecimal roundedValue = decimalValue.setScale(2, BigDecimal.ROUND_HALF_UP);

            // Convert the rounded BigDecimal value back to a double
            saldo = roundedValue.doubleValue();

            if (saldo>0) {
                holder.textViewSaldo.setTextColor(Color.RED);
            } else {
                holder.textViewSaldo.setTextColor(Color.rgb(0, 100, 0));
            }

            holder.btnOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Show popup menu for item options
                    PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                    popupMenu.inflate(R.menu.popup_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.editItem:
                                    // Handle the click event for editing the item
                                    if (collectiongroupEmail.equals(collectionaccountEmail)) {
                                        showEditDialog(document.getId(), name, number, price);
                                    }
                                    return true;
                                case R.id.deleteItem:
                                    // Handle the click event for deleting the item
                                    if (collectiongroupEmail.equals(collectionaccountEmail)) {
                                        deleteRow(document.getId());
                                    }
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        private void showEditDialog(String documentId, String name, Long number, Double price) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Edit Item");

            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_edit_item, null);
            builder.setView(view);

            //EditText editDialogName = view.findViewById(R.id.editName);
            //EditText editDialogNumber = view.findViewById(R.id.editNumber);
            EditText editDialogPrice = view.findViewById(R.id.editPrice);

            //editDialogName.setText(name);
            //editDialogNumber.setText(Long.toString(number));
            editDialogPrice.setText(String.format("%.2f", price));

            Button cancelEditButton = view.findViewById(R.id.cancelEditButton);
            Button editButton = view.findViewById(R.id.editButton);

            AlertDialog alterdialog = builder.create();

            alterdialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    // Get the buttons from the dialog
                    editButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String newName = collectionAlias;
                            Long newNumber= Long.valueOf(0);
                            Double newPrice= Double.valueOf(0);
                            try {
                                //newNumber = Long.parseLong(editDialogNumber.getText().toString().trim());
                                newPrice = round(Double.parseDouble(String.format("%.2f",Double.parseDouble(editDialogPrice.getText().toString().replaceAll(",", ".").trim())).replaceAll(",", ".")),2);
                            } catch (Exception e) {

                            }


                            if (newPrice!=0) {
                                updateRow(documentId, newName, newNumber, newPrice);
                                alterdialog.dismiss();
                            } else {
                                Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    cancelEditButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alterdialog.dismiss();
                        }
                    });
                }
            });

            alterdialog.show();
        }

        private void updateRow(String documentId, String name, Long number, Double price) {
            DocumentReference documentReference = collectionReference.document(documentId);

            double value = price;

            // Create a BigDecimal object from the double value
            BigDecimal decimalValue = BigDecimal.valueOf(value);

            // Round the BigDecimal value to two decimal places
            BigDecimal roundedValue = decimalValue.setScale(2, BigDecimal.ROUND_HALF_UP);

            // Convert the rounded BigDecimal value back to a double
            double priceDouble = roundedValue.doubleValue();

            documentReference.update("price", priceDouble)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Document updated: " + documentId);
                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                            String collectionPath = collectionEmail+collectionGroup;

                            CollectionReference collectionRef = firestore.collection(collectionPath);

                            collectionRef.orderBy("datetime", Query.Direction.ASCENDING).get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        Double saldo = collectionLimit;
                                        Long number =Long.valueOf(0);
                                        for (QueryDocumentSnapshot document : querySnapshot) {
                                            // Access individual documents here
                                            String documentId = document.getId();

                                            FirebaseFirestore firestore3 = FirebaseFirestore.getInstance();

                                            DocumentReference documentRef = firestore3.collection(collectionPath).document(documentId);

                                            Double price = Double.valueOf(0);
                                            try {
                                                price = round(document.getDouble("price"),2);
                                            } catch (Exception e) {
                                                //throw new RuntimeException(e);
                                            }

                                            //DecimalFormat decimalFormat = new DecimalFormat("#.00");
                                            //String formattedNumberString = decimalFormat.format(price);
                                            //price = Double.parseDouble(formattedNumberString);

                                            //BigDecimal bigDecimal = new BigDecimal(price);
                                            //BigDecimal roundedNumber = bigDecimal.setScale(2, RoundingMode.HALF_UP);
                                            //price = roundedNumber.doubleValue();

                                            String formattedNumber = String.format("%.2f", price).replaceAll(",",".");
                                            price = Double.valueOf(formattedNumber);

                                            saldo = saldo + price;

                                            String formattedNumber2 = String.format("%.2f", saldo).replaceAll(",",".");
                                            saldo = Double.valueOf(formattedNumber2);

                                            number = number + 1;

                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("price", price);
                                            updates.put("saldo", saldo);
                                            updates.put("number", number);
                                            //updates.put("field2", "new value 2");

                                            documentRef.update(updates)
                                                    .addOnSuccessListener(aVoid2 -> {
                                                        // Document updated successfully
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        // Error occurred while updating the document
                                                    });
                                        }

                                        FirebaseFirestore firestore2 = FirebaseFirestore.getInstance();

                                        CollectionReference collectionRef4 = firestore2.collection(collectiongroupEmail);

                                        Double finalSaldo = saldo;
                                        collectionRef4.orderBy("datetime", Query.Direction.ASCENDING).get()
                                                .addOnSuccessListener(querySnapshot5 -> {
                                                    //List<String> fieldValues = new ArrayList<>();
                                                    String DocumentId2 = "";
                                                    for (QueryDocumentSnapshot document2 : querySnapshot5) {
                                                        if (collectiongroupEmail.equals(document2.getString("groupemail")) && collectionGroup.equals(document2.getString("name"))) {
                                                            DocumentId2 = document2.getId();

                                                            CollectionReference collectionRef5 = firestore2.collection(document2.getString("accountemail"));

                                                            Double finalSaldo2 = finalSaldo;
                                                            collectionRef5.orderBy("datetime", Query.Direction.ASCENDING).get()
                                                                    .addOnSuccessListener(querySnapshot6 -> {
                                                                        //List<String> fieldValues = new ArrayList<>();
                                                                        String DocumentId3 = "";
                                                                        for (QueryDocumentSnapshot document3 : querySnapshot6) {
                                                                            if (collectiongroupEmail.equals(document3.getString("groupemail")) && collectionGroup.equals(document3.getString("name"))) {
                                                                                DocumentId3 = document3.getId();
                                                                                //fieldValues.add(fieldValue);

                                                                                DocumentReference documentRef3 = firestore2.collection(document2.getString("accountemail")).document(DocumentId3);

                                                                                Map<String, Object> updates2 = new HashMap<>();
                                                                                updates2.put("balance", finalSaldo2);
                                                                                //updates.put("field2", "new value 2");

                                                                                documentRef3.update(updates2)
                                                                                        .addOnSuccessListener(aVoid2 -> {
                                                                                            // Document updated successfully
                                                                                        })
                                                                                        .addOnFailureListener(e -> {
                                                                                            // Error occurred while updating the document
                                                                                        });
                                                                            }
                                                                        }

                                                                        if (!DocumentId3.equals("")) {

                                                                        }


                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        // Error occurred while fetching the documents
                                                                    });
                                                            //fieldValues.add(fieldValue);
                                                        }
                                                    }

                                                })
                                                .addOnFailureListener(e -> {
                                                    // Error occurred while fetching the documents
                                                });


                                        //clearFields();


                                    })
                                    .addOnFailureListener(e -> {
                                        // Error occurred while fetching the documents
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error updating document: " + documentId, e);
                        }
                    });
        }

        private void deleteRow(String documentId) {
            collectionReference.document(documentId)
                    .delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Document deleted: " + documentId);
                                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                                String collectionPath = collectionEmail+collectionGroup;

                                CollectionReference collectionRef = firestore.collection(collectionPath);

                                collectionRef.orderBy("datetime", Query.Direction.ASCENDING).get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            Double saldo = collectionLimit;
                                            Long number =Long.valueOf(0);
                                            for (QueryDocumentSnapshot document : querySnapshot) {
                                                // Access individual documents here
                                                String documentId = document.getId();

                                                FirebaseFirestore firestore3 = FirebaseFirestore.getInstance();

                                                DocumentReference documentRef = firestore3.collection(collectionPath).document(documentId);

                                                Double price = Double.valueOf(0);
                                                try {
                                                    price = round(document.getDouble("price"),2);
                                                } catch (Exception e) {
                                                    //throw new RuntimeException(e);
                                                }

                                                //DecimalFormat decimalFormat = new DecimalFormat("#.00");
                                                //String formattedNumberString = decimalFormat.format(price);
                                                //price = Double.parseDouble(formattedNumberString);

                                                //BigDecimal bigDecimal = new BigDecimal(price);
                                                //BigDecimal roundedNumber = bigDecimal.setScale(2, RoundingMode.HALF_UP);
                                                //price = roundedNumber.doubleValue();

                                                String formattedNumber = String.format("%.2f", price).replaceAll(",",".");
                                                price = Double.valueOf(formattedNumber);

                                                saldo = saldo + price;

                                                String formattedNumber2 = String.format("%.2f", saldo).replaceAll(",",".");
                                                saldo = Double.valueOf(formattedNumber2);

                                                number = number + 1;

                                                Map<String, Object> updates = new HashMap<>();
                                                updates.put("price", price);
                                                updates.put("saldo", saldo);
                                                updates.put("number", number);
                                                //updates.put("field2", "new value 2");

                                                documentRef.update(updates)
                                                        .addOnSuccessListener(aVoid -> {
                                                            // Document updated successfully
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            // Error occurred while updating the document
                                                        });
                                            }

                                            FirebaseFirestore firestore2 = FirebaseFirestore.getInstance();

                                            CollectionReference collectionRef4 = firestore2.collection(collectiongroupEmail);

                                            Double finalSaldo = saldo;
                                            collectionRef4.orderBy("datetime", Query.Direction.ASCENDING).get()
                                                    .addOnSuccessListener(querySnapshot5 -> {
                                                        //List<String> fieldValues = new ArrayList<>();
                                                        String DocumentId2 = "";
                                                        for (QueryDocumentSnapshot document2 : querySnapshot5) {
                                                            if (collectiongroupEmail.equals(document2.getString("groupemail")) && collectionGroup.equals(document2.getString("name"))) {
                                                                DocumentId2 = document2.getId();

                                                                CollectionReference collectionRef5 = firestore2.collection(document2.getString("accountemail"));

                                                                Double finalSaldo2 = finalSaldo;
                                                                collectionRef5.orderBy("datetime", Query.Direction.ASCENDING).get()
                                                                        .addOnSuccessListener(querySnapshot6 -> {
                                                                            //List<String> fieldValues = new ArrayList<>();
                                                                            String DocumentId3 = "";
                                                                            for (QueryDocumentSnapshot document3 : querySnapshot6) {
                                                                                if (collectiongroupEmail.equals(document3.getString("groupemail")) && collectionGroup.equals(document3.getString("name"))) {
                                                                                    DocumentId3 = document3.getId();
                                                                                    //fieldValues.add(fieldValue);
                                                                                    DocumentReference documentRef3 = firestore2.collection(document2.getString("accountemail")).document(DocumentId3);

                                                                                    Map<String, Object> updates2 = new HashMap<>();
                                                                                    updates2.put("balance", finalSaldo2);
                                                                                    //updates.put("field2", "new value 2");

                                                                                    documentRef3.update(updates2)
                                                                                            .addOnSuccessListener(aVoid2 -> {
                                                                                                // Document updated successfully
                                                                                            })
                                                                                            .addOnFailureListener(e -> {
                                                                                                // Error occurred while updating the document
                                                                                            });
                                                                                }
                                                                            }

                                                                            if (!DocumentId3.equals("")) {

                                                                            }


                                                                        })
                                                                        .addOnFailureListener(e -> {
                                                                            // Error occurred while fetching the documents
                                                                        });
                                                                //fieldValues.add(fieldValue);
                                                            }
                                                        }

                                                    })
                                                    .addOnFailureListener(e -> {
                                                        // Error occurred while fetching the documents
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            // Error occurred while fetching the documents
                                        });
                            } else {
                                Log.e(TAG, "Error deleting document: " + documentId, task.getException());
                            }
                        }
                    });
        }

        @Override
        public int getItemCount() {
            return documents.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textViewNumber;
            TextView textViewName;
            TextView textViewPrice;
            TextView textViewSaldo;
            Button btnOption;

            ViewHolder(View itemView) {
                super(itemView);
                textViewNumber = itemView.findViewById(R.id.field1TextView);
                textViewName = itemView.findViewById(R.id.field2TextView);
                textViewPrice = itemView.findViewById(R.id.field3TextView);
                textViewSaldo = itemView.findViewById(R.id.field4TextView);
                btnOption = itemView.findViewById(R.id.actionButton);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Call finish() to exit the app
        super.onBackPressed();
        moveTaskToBack(true);
    }
}
