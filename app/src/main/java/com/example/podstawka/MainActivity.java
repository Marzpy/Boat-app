package com.example.podstawka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputFilter;
import android.view.View;
import android.os.Handler;
import android.view.MotionEvent;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.telephony.SmsManager;
import android.widget.ToggleButton;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_ITEM = 1;
    int speedV = 5, course = 1;
    int speedV2 = 5, courseV2 = 1;
    private Button btnPlus, btnMin, btnSetcourse, btnspeed, btnhelp;
    private Button speedPlus, speedMin;
    private TextView CourseText, SpeedText, CourseText2, SpeedText2;
    private EditText editcourse, editSpeed;
    private Switch switch1, switch2;
    private TextView AddressText;
    private boolean toggleButtonState = false;
    private Button launchBTN;//przycisk włączania mapy
    private LocationRequest locationRequest;
    static double latitude;
    private static final String TAG = "MyActivity";
    static double longitude;
    static int frame = 0;
    MqttAndroidClient client; //nazwa mqtt
    TextView subText;
    String wys;
    private static ArrayList<String> items = new ArrayList<>();// elemnty przekazywane z listy
    static String zmienna; //dane odbierane od brokera
    private Button editListButton;
    private Button sendSmsButton;
    private ListView listView;
    private Timer timer;
    private TimerTask timerTask;
    private ArrayAdapter<String> adapter;
    private ConstraintLayout containerRL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToggleButton tugle2 = (ToggleButton) findViewById(R.id.toggleButton2);
        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        //symulator
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.mqttdashboard.com:1883",clientId);
        // Raspberry
       // String clientId = "s";
      //  client = new MqttAndroidClient(this.getApplicationContext(), "tcp://192.168.10.1:1883", clientId);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE}, PackageManager.PERMISSION_GRANTED);

        editListButton = findViewById(R.id.editListButton);
        //buttons
        speedMin = findViewById(R.id.minspeed);
        speedPlus = findViewById(R.id.plusspeed);
        btnPlus = findViewById(R.id.dimplus);
        btnMin = findViewById(R.id.dimminus);
        btnspeed = findViewById(R.id.speed);
        btnSetcourse = findViewById(R.id.setcourse);
        btnhelp = findViewById(R.id.stop);
        //textview
        launchBTN = findViewById(R.id.launch);
        CourseText = findViewById(R.id.CourseAngle);
        SpeedText = findViewById(R.id.textSpeed);
        CourseText2 = findViewById(R.id.CourseAngle2);
        SpeedText2 = findViewById(R.id.textSpeed2);
        //edittext
        editcourse = findViewById(R.id.editcourse);
        editSpeed = findViewById(R.id.editvelocity);

        //walidacja
        editcourse.setFilters(new InputFilter[]{new InputFilterMinMax("1", "359")});
        editSpeed.setFilters(new InputFilter[]{new InputFilterMinMax("1", "359")});
        offset(); // wyłącz system
        //tugle1.setEnabled(false);
        tugle2.setEnabled(false);// wyłącz przełącznik
        // po naciśnięciu przycisku włącz system
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    seton();
                    tugle2.setEnabled(true);
                } else {
                    offset();
                    tugle2.setEnabled(false);
                }
            }
        });
        // po naciśnięciu przełącznika połącz się z serwerem
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    conn();
                    client.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                        }
                        String topic2 = "/ship/data/heading";// odbierana kurs
                        int messcourse = courseV2;
                        String topic3 = "/ship/data/speed";// odbierana kurs
                        int messspeed = speedV2;
                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            if (topic.equalsIgnoreCase("/ship/data/speed")) {
                                Log.i(TAG, "topic" + topic);
                                zmienna = new String(message.getPayload());//dziala
                                SpeedText.setText(zmienna);
                            }
                            if (topic.equalsIgnoreCase("/ship/data/heading")) {
                                Log.i(TAG, "topic" + topic);
                                String zmienna2 = new String(message.getPayload());//dziala
                                CourseText.setText(zmienna2);
                            }
                        }
                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "disconnected", Toast.LENGTH_LONG).show();
                }
            }
        });
        if (course == 360)
            course = 0;
// Zwieksz kurs, zapisz ja w tablicy oraz ustaw ją na ekranie oraz wyślij
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (course == 360)
                    course = 0;
                course++;
                String course1 = Integer.toString(course);
                CourseText2.setText(course1 + " °");

                published();
            }
        });
        // Zmniejsz kurs, zapisz ja w tablicy oraz ustaw ją na ekranie oraz wyślij
        btnMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (course == 0)
                    course = 360;
                course--;
                String course1 = Integer.toString(course);
                CourseText2.setText(course1 + " °");

                published();

            }
        });
// Zmniejsz prędkość, zapisz ja w tablicy oraz ustaw ją na ekranie oraz wyślij
        speedMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedV--;
                String speed = Integer.toString(speedV);
                if (speedV < 15) speedPlus.setEnabled(true);
                if (speedV == 0) speedMin.setEnabled(false);
                SpeedText2.setText(speed + " kmh");

                published();

            }
        });
// Zwieksz prędkość, zapisz ja w tablicy oraz ustaw ją na ekranie oraz wyślij
        speedPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedV++;
                String speed = Integer.toString(speedV);
                if (speedV == 15) speedPlus.setEnabled(false);
                if (speedV > 0) speedMin.setEnabled(true);
                SpeedText2.setText(speed + " kmh");

                published();

            }
        });
        // Ustaw kurs z pola edycji, zapisz ja w tablicy oraz ustaw ją na ekranie oraz wyślij
        btnSetcourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String course1 = editcourse.getText().toString();
                course = Integer.valueOf(course1);
                CourseText2.setText(course1 + "°");

                published();
            }

        });

// pole edycji predkosci z pola edycji, zapisz ja w tablicy oraz ustaw ją na ekranie oraz wyślij
        btnspeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String speed1 = editSpeed.getText().toString();
                speedV = Integer.valueOf(speed1);
                SpeedText2.setText(" " + speed1 + "kmh");

                published();
            }
        });

        Handler handler = new Handler();
// po przytrzymaniu przycisku wykonuj odpowiednią czynność
        btnhelp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                getCurrentLocation();
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        handler.postDelayed(run, 2000);
                        break;
                    default:
                        handler.removeCallbacks(run);
                        break;
                }
                return true;
            }
        });
        // Włącz google maps
        launchBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
                if (latitude != 0) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("geo:" + latitude + "," + longitude));
                    Intent chooser = Intent.createChooser(intent, "Launch Maps");
                    startActivity(chooser);
                }
            }
        });
        // Po nacisnięciu przełącznika co 30 sekund zapisuj aktualną lokazlizacje do pliku
        tugle2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleButtonState = isChecked;
                if (toggleButtonState) {
                    setonCourse();
                    timer = new Timer();
                    timerTask = new TimerTask() {
                        public void run() {
                            File file = new File(getFilesDir(), "location.txt");
                            if (!file.exists()) {
                                try {
                                    file.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            FileOutputStream fos = null;
                            try {
                                fos = openFileOutput("location.txt", Context.MODE_APPEND);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            try {
                                getCurrentLocation();
                                Date date = new Date();
                                String currentTime = date.toString();
                                fos.write((Double.toString(latitude) + " " + Double.toString(longitude) + " " + currentTime + "\n").getBytes());

                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    timer.scheduleAtFixedRate(timerTask, 0, 30000);
                } else {
                    setoffCourse();
                    timerTask.cancel();
                }
            }
        });

        AddressText = findViewById(R.id.addressText);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        if (getIntent().hasExtra("items")) {
            items = (ArrayList<String>) getIntent().getSerializableExtra("items");
        } else {
            items = new ArrayList<>();
        }
// Sprawdz pobrane numery z listy
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        if (items != null && !items.isEmpty()) {
            if (items == null || items.isEmpty()) {
                Toast.makeText(this, "Lista numerów jest pusta", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        editListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_ITEM);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    getCurrentLocation();
                } else {
                    turnOnGPS();
                }
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                getCurrentLocation();
            }
        }
        if (requestCode == REQUEST_CODE_ADD_ITEM && resultCode == RESULT_OK) {
            items = data.getStringArrayListExtra("items");
        }
    }
    //Pobierz aktualną lokalizacje
    private void getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                            .removeLocationUpdates(this);
                                    if (locationResult != null && locationResult.getLocations().size() > 0) {
                                        int index = locationResult.getLocations().size() - 1;
                                        latitude = locationResult.getLocations().get(index).getLatitude();
                                        longitude = locationResult.getLocations().get(index).getLongitude();
                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }
    //Wlacz GPS
    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Urzadzenie nie znalezione
                            break;
                    }
                }
            }
        });
    }
    // Sprawdz czy GPS jest dostępny
    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }
    //Wylacz system
    public void offset() {
        editcourse.setEnabled(false);
        editSpeed.setEnabled(false);
        switch2.setEnabled(false);
        launchBTN.setEnabled(false);
        btnPlus.setEnabled(false);
        btnMin.setEnabled(false);
        btnspeed.setEnabled(false);
        btnhelp.setEnabled(false);
        btnSetcourse.setEnabled(false);
        speedMin.setEnabled(true);
        speedMin.setEnabled(true);
        speedMin.setEnabled(false);
        speedPlus.setEnabled(false);
        editListButton.setEnabled(false);
    }
    //Wlacz systen
    public void seton() {
        launchBTN.setEnabled(true);
        switch2.setEnabled(true);
        btnhelp.setEnabled(true);
        editListButton.setEnabled(true);
    }
    // Zablokuj zmiane kursu
    public void setoffCourse() {
        editcourse.setEnabled(false);
        editSpeed.setEnabled(false);
        btnPlus.setEnabled(false);
        btnMin.setEnabled(false);
        btnspeed.setEnabled(false);
        btnSetcourse.setEnabled(false);
        speedMin.setEnabled(false);
        speedPlus.setEnabled(false);
    }
    // Odblokuj ustawianie kursu
    public void setonCourse() {
        editcourse.setEnabled(true);
        editSpeed.setEnabled(true);
        launchBTN.setEnabled(true);
        btnPlus.setEnabled(true);
        btnMin.setEnabled(true);
        btnspeed.setEnabled(true);
        //btnhelp.setEnabled(true);
        btnSetcourse.setEnabled(true);
        speedMin.setEnabled(true);
        speedPlus.setEnabled(true);
    }
    // Opuplikuj wiadomosc
    public void published() {
        String topic4 = "/ship/control/set_headingApp";// wysyłany kurs
        String mes1course = Integer.toString(course);
        String topic5 = "/ship/control/speedApp";// wysyłana prędkosść
        String mes1speed = Integer.toString(speedV);
        try {
            client.publish(topic4, mes1course.getBytes(), 0, false);
            client.publish(topic5, mes1speed.getBytes(), 0, false);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    // Ustaw subskrypcje u brokera
    private void setSubscription() {
        try {
            client.subscribe("/ship/data/heading", 0);
            client.subscribe("/ship/data/speed", 0);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    // funkcja odpowiadaja za wysyłanie  sygnału w momencie połączenia z serwerem
    public void conn() {
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "Połączono!!", Toast.LENGTH_LONG).show();
                    setSubscription();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Nie udało się połączyć!!", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    // funkcja odpowiadaja za wysyłanie sygnału w momencie rozłączenia z serwerem
    public void disconn() {
        try {
            IMqttToken token = client.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "Rozłaczono!!", Toast.LENGTH_LONG).show();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Wystąpił błąd przy rozłączaniu!!", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    // Funkcja wysyjaca sms
    private void sendSms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Użytkownik nie ma uprawnień", Toast.LENGTH_SHORT).show();
        } else {
            if (items != null && !items.isEmpty()) {
                SmsManager smsManager = SmsManager.getDefault();
                for (String number : items) {
                    smsManager.sendTextMessage(number, null, "POTRZEBNA POMOC " + latitude + "," + longitude, null, null);
                }
                Toast.makeText(this, "Wiadomość została wysłana", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lista numerów jest pusta", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // W momencie nacisniecia przycisku
    Runnable run = new Runnable() {
        @Override
        public void run() {
            getCurrentLocation();
            sendSms();
        }
    };
}





