package com.example.kenbo736.chatapp2;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChatAppActivity extends AppCompatActivity {

    private EditText messageBox;
    private TextView chatWindow;
    private Button sendButton;
    private Button signoutButton;
    private Button changeUsernameButton;
    private Button sendSpamButton;
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
        messageBox.setHint(R.string.write_something);
        chatWindow = (TextView) findViewById(R.id.chatWindow);
        chatWindow.setMovementMethod(new ScrollingMovementMethod());

        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                chatWindow.setText("");
                for(Iterator<DataSnapshot> i = snapshot.getChildren().iterator(); i.hasNext();){
                    DataSnapshot post = i.next();
                    String user = post.child("user").getValue().toString();
                    String message = post.child("message").getValue().toString();
                    chatWindow.append(user + ": " + message + "\n");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        changeUsernameButton = (Button) findViewById(R.id.changeUsernameButton);
        changeUsernameButton.setText(R.string.change_username);
        changeUsernameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(ChatAppActivity.this, profileActivity.class));
            }
        });

        sendSpamButton = (Button) findViewById(R.id.sendSpamButton);
        sendSpamButton.setText(R.string.send_spam);
        sendSpamButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                        .setMessage(getString(R.string.invitation_message))
                        .setDeepLink(Uri.parse("http://google.com"))
                        .setCustomImage(Uri.parse("https://images-eds-ssl.xboxlive.com/image?url=8Oaj9Ryq1G1_p3lLnXlsaZgGzAie6Mnu24_PawYuDYIoH77pJ.X5Z.MqQPibUVTcAL_uXqNGDhlNIt0wsKBphnYi_G_lNgvWGjm1ZpEzt6T1OHWICnMjSbCLMVj.H5bGPhbofTc8000L_kG4cpIZtcLKYcKE9zco58YzufwifoFFPTqhAOxbFF1Mql6iZU1_d_FjrtR8s22JwBM3LkNpvdVFM_gV8XN1OLcbKQZGBvQ-&w=200&h=300&format=jpg"))
                        .setCallToActionText(getString(R.string.invitation_cta))
                        .build();
                startActivityForResult(intent, 100);
            }
        });

        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setText(R.string.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String key = dataRef.push().getKey();
                String message = messageBox.getText().toString();
                String user = mAuth.getCurrentUser().getDisplayName();
                Map<String, String> map = new HashMap<>();
                map.put("user", user);
                map.put("message", message);

                dataRef.child(key).setValue(map);
                messageBox.setText("");

            }
        });
        signoutButton = (Button) findViewById(R.id.signoutButton);
        signoutButton.setText(R.string.sign_out);
        signoutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(ChatAppActivity.this, MainActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Toast.makeText(ChatAppActivity.this, id, Toast.LENGTH_SHORT).show();
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // ...
            }
        }
    }
}
