package com.example.kenbo736.chatapp2;

import android.app.ActionBar;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class profileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference dataRef;

    private ProgressBar expBar;
    private TextView levelText;

    private EditText usernameBox;
    private Button setUsernameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        database = FirebaseDatabase.getInstance();
        dataRef = database.getReference("users");
        expBar = (ProgressBar) findViewById(R.id.expBar);
        levelText = (TextView) findViewById(R.id.levelText);
        usernameBox = (EditText) findViewById(R.id.usernameBox);
        usernameBox.setHint(R.string.username);
        setUsernameButton = (Button) findViewById(R.id.setUsernameButton);
        setUsernameButton.setText(R.string.set_username);
        mAuth = FirebaseAuth.getInstance();

        setUsernameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setUsername();
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //toolbar.setNavigationIcon(R.mipmap.rn_launcher);


        dataRef.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                levelText.setText((Integer.parseInt(snapshot.getValue().toString())/100) + "");
                expBar.setProgress(Integer.parseInt(snapshot.getValue().toString())%100);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }



    private void setUsername(){
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(usernameBox.getText().toString())
                .build();

        mAuth.getCurrentUser().updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(profileActivity.this, ChatAppActivity.class));
                            Toast.makeText(profileActivity.this, mAuth.getCurrentUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(profileActivity.this, "set username failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
