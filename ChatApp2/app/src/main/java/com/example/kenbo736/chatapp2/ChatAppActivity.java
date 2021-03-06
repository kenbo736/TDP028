package com.example.kenbo736.chatapp2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.view.Menu;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChatAppActivity extends AppCompatActivity {

    private EditText messageBox;
    private EditText timeStamp;
    private ListView chatWindow;
    private Button sendButton;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference dataRef;
    private DatabaseReference userRef;
    private FirebaseAnalytics mFirebaseAnalytics;

    private static final int REQUEST_CODE=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_app);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dataRef = database.getReference("chatWindow");
        userRef = database.getReference("users");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        messageBox = (EditText) findViewById(R.id.messageBox);
        messageBox.setHint(R.string.write_something);
        chatWindow = (ListView) findViewById(R.id.chatWindow);

        // Styr actionbaren
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<String> messages = new ArrayList<String>();
                //chatWindow.setText("");
                for(Iterator<DataSnapshot> i = snapshot.getChildren().iterator(); i.hasNext();){
                    DataSnapshot post = i.next();
                    String user = post.child("user").getValue().toString();
                    String message = post.child("message").getValue().toString();

                    messages.add(user + ":\n" + message);
                    //chatWindow.append(user + ": " + message + "\n");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ChatAppActivity.this, R.layout.chatbubble_layout, messages);
                chatWindow.setAdapter(adapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setText(R.string.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String key = dataRef.push().getKey();
                final String message = messageBox.getText().toString();
                String user = mAuth.getCurrentUser().getDisplayName();
                Map<String, String> map = new HashMap<>();
                map.put("user", user);
                map.put("message", message);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CHARACTER, user);
                bundle.putString(FirebaseAnalytics.Param.CONTENT, message);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.EARN_VIRTUAL_CURRENCY, bundle);

                dataRef.child(key).setValue(map);
                messageBox.setText("");

                userRef.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Long value = mutableData.getValue(Long.class);
                        mutableData.setValue(value + message.length());

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b,
                                           DataSnapshot dataSnapshot) {
                    }
                });

                View view = ChatAppActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout_menu:
                mAuth.signOut();
                startActivity(new Intent(ChatAppActivity.this, MainActivity.class));

        }
        switch(item.getItemId()) {
            case R.id.invite_menu:
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CHARACTER, mAuth.getCurrentUser().getDisplayName());
                bundle.putString(FirebaseAnalytics.Param.VALUE, "a lot");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);

                Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                        .setMessage(getString(R.string.invitation_message))
                        .setCustomImage(Uri.parse("https://raw.githubusercontent.com/kenbo736/TDP028/master/Images/rn_icon.png"))
                        .build();
                startActivityForResult(intent, REQUEST_CODE);

        }
        switch(item.getItemId()) {
            case R.id.change_username_menu:
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CHARACTER, mAuth.getCurrentUser().getDisplayName());
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "username_changed");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);

                startActivity(new Intent(ChatAppActivity.this, profileActivity.class));

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {

                }
            }
            else {
                // Sending failed or it was canceled, show failure message to the user
            }
        }
    }

    @Override
    public void onStart(){
        super.onStart();
    }
}
