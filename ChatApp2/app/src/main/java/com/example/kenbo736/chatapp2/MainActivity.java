package com.example.kenbo736.chatapp2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.Console;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private NotificationManager nManager;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private GeofencingClient mGeofencingClient;
    protected GoogleApiClient mGoogleApiClient;
    private BroadcastReceiver bReceiver;
    private LocalBroadcastManager bManager;
    private FirebaseAnalytics mFirebaseAnalytics;

    public static final String RECEIVE_PLATS = "com.your.package.RECEIVE_PLATS";

    private EditText emailField;
    private EditText passwordField;
    private TextView welcomeMessage;
    private TextView locationBox;

    private Button signInButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mGeofencingClient = LocationServices.getGeofencingClient(this);

        welcomeMessage = (TextView) findViewById(R.id.welcomeMessage);
        locationBox = (TextView) findViewById(R.id.locationBox);
        locationBox.setText(R.string.you_are_not_in);
        locationBox.append(" Linköping");
        emailField = (EditText) findViewById(R.id.emailField);
        emailField.setHint(R.string.mail);
        passwordField = (EditText) findViewById(R.id.passwordField);
        passwordField.setHint(R.string.password);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //toolbar.setNavigationIcon(R.mipmap.rn_launcher);

        nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.CHARACTER, mAuth.getCurrentUser().getEmail());
                    bundle.putString(FirebaseAnalytics.Param.DESTINATION, "Chatten");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
                    startActivity(new Intent(MainActivity.this, ChatAppActivity.class));
                }
            }
        };

        signInButton = (Button) findViewById(R.id.signInButton);
        signInButton.setText(R.string.log_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                startSignIn();

            }
        });

        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setText(R.string.register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startRegister();
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CHARACTER, emailField.getText().toString());
                bundle.putString(FirebaseAnalytics.Param.SIGN_UP_METHOD, "Via knapp");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
            }
        });

        long cacheExpiration = 3600;
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                        }
                        welcomeMessage.setText(mFirebaseRemoteConfig.getString("WELCOME_MESSAGE"));
                    }
                });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(RECEIVE_PLATS)) {
                    String plats = intent.getStringExtra("plats");
                    locationBox.setText(R.string.you_are_in);
                    locationBox.append(" " + plats);
                }
            }
        };

        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_PLATS);
        bManager.registerReceiver(bReceiver, intentFilter);

    }

    private void startRegister() {
        final String email = emailField.getText().toString();
        final String password = passwordField.getText().toString();

        if (email.isEmpty()){
            Toast.makeText(MainActivity.this, "email is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    android.support.v4.app.NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(MainActivity.this)
                                    .setSmallIcon(R.mipmap.rn_launcher)
                                    .setContentTitle("Registration")
                                    .setContentText("You are now registered under the email: " + email);
                    nManager.notify(1, mBuilder.build());
                    startActivity(new Intent(MainActivity.this, profileActivity.class));
                }
                else{

                    Toast.makeText(MainActivity.this, "registration failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void startSignIn() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        if(TextUtils.isEmpty(email)) {
            Toast.makeText(MainActivity.this, "email missing", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)) {
            Toast.makeText(MainActivity.this, "password missing", Toast.LENGTH_SHORT).show();
        }
        else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (!task.isSuccessful()) {

                        Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Geofence geofence = new Geofence.Builder()
                .setRequestId("Linköping") // Geofence ID
                .setCircularRegion( 58.410807, 15.621373, 5000) // defining fence region
                .setExpirationDuration( 10000 ) // expiring date
                // Transition types that it should look for
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();

        GeofencingRequest geoRequest = new GeofencingRequest.Builder()
                // Notification to trigger when the Geofence is created
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence( geofence ) // add a Geofence
                .build();

        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    geoRequest,
                    pintent
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Do something with result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onResult(Status status) {

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        bManager.unregisterReceiver(bReceiver);
    }

}
