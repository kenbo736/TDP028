package com.example.kenbo736.chatapp2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatAppActivity extends AppCompatActivity {

    private EditText messageBox;
    private TextView chatWindow;
    private Button sendButton;
    private Button signoutButton;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference dataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_app);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dataRef = database.getReference("chatWindow");

        messageBox = (EditText) findViewById(R.id.messageBox);
        chatWindow = (TextView) findViewById(R.id.chatWindow);

        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                chatWindow.setText(snapshot.getValue().toString());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = messageBox.getText().toString();
                chatWindow.append("\n" + mAuth.getCurrentUser().getEmail() + ": " + message);

                dataRef.setValue(chatWindow.getText().toString());

            }
        });
        signoutButton = (Button) findViewById(R.id.signoutButton);
        signoutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(ChatAppActivity.this, MainActivity.class));
            }
        });
    }
}
