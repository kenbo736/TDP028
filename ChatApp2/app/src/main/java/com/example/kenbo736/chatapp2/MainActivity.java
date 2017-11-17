package com.example.kenbo736.chatapp2;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.Console;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private NotificationManager nManager;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private GeofencingClient mGeofencingClient;

    private EditText emailField;
    private EditText passwordField;
    private TextView welcomeMessage;

    private Button signInButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        Geofence mGeofence;

        welcomeMessage = (TextView) findViewById(R.id.welcomeMessage);
        emailField = (EditText) findViewById(R.id.emailField);
        emailField.setHint(R.string.mail);
        passwordField = (EditText) findViewById(R.id.passwordField);
        passwordField.setHint(R.string.password);

        nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
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

        mGeofence = new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(entry.getKey())

                .setCircularRegion(
                        entry.getValue().latitude,
                        entry.getValue().longitude,
                        GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

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
                                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_normal)
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
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
