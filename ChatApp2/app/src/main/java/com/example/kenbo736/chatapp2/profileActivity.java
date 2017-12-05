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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class profileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText usernameBox;
    private Button setUsernameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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
        getSupportActionBar().setTitle("RantNation");
        toolbar.setNavigationIcon(R.mipmap.rn_launcher);

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
