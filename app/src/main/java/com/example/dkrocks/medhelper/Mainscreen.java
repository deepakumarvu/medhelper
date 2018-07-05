package com.example.dkrocks.medhelper;

import android.Manifest;
import android.app.Activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import static android.Manifest.permission.SEND_SMS;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.android.gms.location.LocationListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Mainscreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks,
        LocationListener,GoogleApiClient.OnConnectionFailedListener{
    private static final int REQUEST_CHECK_SETTINGS_GPS =0x1 ;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS =0x2 ;
    TextView textView;
    Button scan;
    private String lati,longi;
    View popupView;
    int i;
    private Location mylocation;
    private GoogleApiClient googleApiClient;
    LayoutInflater inflater;
    private TextToSpeech t1 =null;
    private ImageView imageView;
    private static final int REQUEST_SMS = 0;


    private String contact1="",contact2="",name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainscreen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        scan=(Button)findViewById(R.id.scan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator= new IntentIntegrator(Mainscreen.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup, null);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Hospitals near by", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                String uri ="https://www.google.com/maps/search/?api=1&query=Hospitals+near+me";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                //intent.setPackage("com.google.android.apps.maps");
                try
                {
                    startActivity(intent);
                }
                catch(ActivityNotFoundException ex)
                {
                    try
                    {
                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(unrestrictedIntent);
                    }
                    catch(ActivityNotFoundException innerEx)
                    {
                        Toast.makeText(Mainscreen.this, "Please install a maps application", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        setUpGClient();
        t1= new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                }
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        imageView=(ImageView) findViewById(R.id.image);
        basic_qrgen b=new basic_qrgen();
        Bitmap bmImg = BitmapFactory.decodeFile(b.getOutputMediaFile().toString());
        imageView.setImageBitmap(bmImg);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View menu=inflater.inflate(R.layout.nav_header_mainscreen, null);
        textView=(TextView) menu.findViewById(R.id.textView);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        textView.setText(pref.getString("name","Got IT"));
    }
    private synchronized void setUpGClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }
    @Override
    public void onConnected(Bundle bundle) {
        checkPermissions();
    }
    private void checkPermissions(){
        int permissionLocation = ContextCompat.checkSelfPermission(Mainscreen.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        }else{
            getMyLocation();
        }

    }
    @Override
    public void onConnectionSuspended(int i) {
        //Do whatever you need
        //You can display a message here
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //You can display a message here
    }
    private void getMyLocation(){
        if(googleApiClient!=null) {
            if (googleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(Mainscreen.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    mylocation =LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(3000);
                    locationRequest.setFastestInterval(3000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi
                            .requestLocationUpdates(googleApiClient, locationRequest,this);
                    PendingResult<LocationSettingsResult> result =
                            LocationServices.SettingsApi
                                    .checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // All location settings are satisfied.
                                    // You can initialize location requests here.
                                    int permissionLocation = ContextCompat
                                            .checkSelfPermission(Mainscreen.this,
                                                    Manifest.permission.ACCESS_FINE_LOCATION);
                                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                                        mylocation = LocationServices.FusedLocationApi
                                                .getLastLocation(googleApiClient);
                                    }
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    // Location settings are not satisfied.
                                    // But could be fixed by showing the user a dialog.
                                    try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        // Ask to turn on GPS automatically
                                        status.startResolutionForResult(Mainscreen.this,
                                                REQUEST_CHECK_SETTINGS_GPS);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings are not satisfied.
                                    // However, we have no way
                                    // to fix the
                                    // settings so we won't show the dialog.
                                    // finish();
                                    break;
                            }
                        }
                    });
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        int permissionLocation = ContextCompat.checkSelfPermission(Mainscreen.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result= IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!= null)
        {
            switch (requestCode) {
                case REQUEST_CHECK_SETTINGS_GPS:
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            getMyLocation();
                            break;
                        case Activity.RESULT_CANCELED:
                            finish();
                            break;
                    }
                    break;
            }
            if(result.getContents()==null)
            {
                Toast.makeText(this,"Scanning canceled",Toast.LENGTH_LONG).show();
            }
            else
            {
                //Toast.makeText(this, result.getContents(),Toast.LENGTH_LONG).show();
                TextView tv=(TextView)popupView.findViewById(R.id.poptext);
                try {
                    String text = result.getContents();
                    if (text.charAt(0) == 'H') {
                        i = 1;
                        text = text.substring(5);
                    } else {
                        i = 0;
                    }
                    tv.setText(text);
                    if (i == 0) {
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(Mainscreen.this);
                        if (pref.getBoolean("ttos", false)) {
                            texttospeech(result.getContents());
                        }
                        String s = result.getContents(), tp, sd[];
                        int i = s.indexOf("name: ");
                        int j = s.indexOf("Address: ");
                        name = s.substring(i + 6, j - 1);
                        i = s.indexOf("Contact-1:");
                        if (i < s.length()) {
                            tp = s.substring(i);
                            sd = tp.split("\\s");
                            if (sd.length > 1)
                                contact1 = sd[1];
                            else
                                contact1 = null;
                            if (sd.length > 2)
                                contact2 = sd[3];
                            else
                                contact2 = null;
                        }
                    }
                }
                catch (Exception e)
                {
                }

                onButtonShowPopupWindowClick();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    public void onButtonShowPopupWindowClick() {

        // get a reference to the already created main layout
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.activity_screen);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
        Button b=(Button)popupView.findViewById(R.id.done_button) ;
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                t1.stop();
                popupWindow.dismiss();
            }
        });
        Button sendButton=(Button)popupView.findViewById(R.id.send_button);
        if(i==0) {
            sendButton.setEnabled(true);
            sendButton.setVisibility(View.VISIBLE);
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        int hasSMSPermission = checkSelfPermission(SEND_SMS);
                        if (hasSMSPermission != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{SEND_SMS},
                                    REQUEST_SMS);
                            return;
                        }
                        getMyLocation();
                        onLocationChanged(mylocation);
                        if(lati!=null || longi!=null) {
                            String uri1 = "http://maps.google.com/maps?saddr=" + lati + "," + longi + "";
                            sendMySMS(contact1, contact2, name + " has been involved in an Accident at " + uri1);
                        }
                    }
                }
            });
        }
        else
        {
            sendButton.setEnabled(false);
            sendButton.setVisibility(View.INVISIBLE);
        }
    }
    public void sendMySMS(String con1,String con2,String mes) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        if(con2!=null)
            smsIntent.putExtra("address", con1+";"+con2);
        else
            smsIntent.putExtra("address", con1);
        smsIntent.putExtra("sms_body",mes);
        startActivity(smsIntent);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mainscreen, menu);
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
            Intent i=new Intent(this,settings.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void logout()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("con1","");
        editor.putString("con2","");
        editor.putString("name","");
        editor.putString("addr","");
        editor.remove("bg");
        editor.putBoolean("basic",false);
        editor.putBoolean("isAuth",false);
        editor.remove("email");
        editor.remove("pass");
        editor.commit();
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        mAuth.signOut();

        startActivity(new Intent(this,LoginActivity.class));
    }
    void texttospeech(String s)
    {
        String toSpeak = s;
        //Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_myself) {
            Intent i=new Intent(this,basic_qrgen.class);
            startActivity(i);
        } else if (id == R.id.nav_addhis) {
            Intent in= new Intent(this,Prev_history.class);
            startActivity(in);
        } else if (id == R.id.nav_settings) {
            Intent i=new Intent(this,settings.class);
            startActivity(i);
        } else if (id == R.id.nav_about) {
            Intent i=new Intent(this,about.class);
            startActivity(i);
        }else if (id == R.id.nav_history) {
            Intent i=new Intent(this,docinfo.class);
            startActivity(i);
        }
        else if (id == R.id.nav_logout) {
            logout();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(
                "Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false).setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        startActivity(new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onLocationChanged(Location location) {
        mylocation=location;
        // Setting Current Longitude
        longi=Double.toString(location.getLongitude());
        // Setting Current Latitude
        lati=Double.toString(location.getLatitude());
    }
}
