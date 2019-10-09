package com.foodfriend.selecta;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.foodfriend.selecta.AccountActivity.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ChatActivity extends AppCompatActivity {

    private EditText editMessage;

    //Firebase
    private DatabaseReference mDatabase;
    private DatabaseReference databaseUsers;
    private DatabaseReference databaseReports;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private RecyclerView messageList;

    //private static final String TAG = "ChatActivity";

    public String sentTo;
    public String sentToName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Show action bar back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editMessage = findViewById(R.id.sendMessage);
        messageList = findViewById(R.id.messageRecieved);

        //Check if a user is logged in to avoid any errors
        isUserLoggedIn();

        //Receive info from previous activity (recipient name and ID)
        Bundle extras = getIntent().getExtras();

        if(extras != null)
        {
            sentTo = extras.getString("sentTo");

            sentToName = extras.getString("sentToName");

            //set action bar title to recipient name
            setTitle(sentToName);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserID = user.getUid();

        String chatName = "";

        //Compare the strings alphabetically, this is to make sure they are always in the same order for finding the correct chat room messages in database
        if(currentUserID.compareTo(sentTo) > 0)
        {
            chatName = currentUserID + " " + sentTo;
        }
        else if(sentTo.compareTo(currentUserID) > 0)
        {
            chatName = sentTo + " " + currentUserID;
        }
        //get instance of the messages/chat rooms in the database
        mDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(chatName);
        auth = FirebaseAuth.getInstance();

        messageList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); //start displaying messages from the bottom
        messageList.setLayoutManager(linearLayoutManager); //set the layout manager for the message list

        auth.addAuthStateListener(authStateListener);

        //use firebase's RecyclerAdapter, parameters: Message class, message layout, message view holder and database reference (reference to the correct chat room in database)
        FirebaseRecyclerAdapter <Message,MessageViewHolder> firebaseRec = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(Message.class, R.layout.message, MessageViewHolder.class, mDatabase) {
            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, Message model, int position) {

                //populate the textviews with database data
                viewHolder.setContent(model.getContent());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setTime(model.getTime());
            }
        };

        messageList.setAdapter(firebaseRec);
    }




    //Inflate the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_chat,menu);


        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            this.finish();
        }
        if(id == R.id.reportUser)
        {

            //Dialog builder
            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            View view = getLayoutInflater().inflate(R.layout.dialog_report, null);

            //Get dialog views
            final EditText reason = view.findViewById(R.id.reportReason);
            Button confirmButton = view.findViewById(R.id.confirmButton);

            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //database reference of reports
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("reports");

                    String reportReason = reason.getText().toString();

                    //send name and reason to database
                    mDatabase.child(sentTo).child("name").setValue(sentToName);
                    mDatabase.child(sentTo).child("reason").setValue(reportReason);

                    Toast.makeText(getApplicationContext(), sentToName + " has been reported.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ChatActivity.this, TabbedActivity.class);

                    startActivity(intent);
                }
            });

            //Create dialog
            builder.setView(view);
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }


    //Send button press
    public void buttonClicked(View view) {

        //Close virtual keyboard
        InputMethodManager inputManager = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        currentUser = auth.getCurrentUser();

        databaseUsers = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid()); //get the userid of the current user

        //get text from edit text
        final String message = editMessage.getText().toString().trim();

        //if message text is not empty
        if(!TextUtils.isEmpty(message)) {

            final DatabaseReference ref = mDatabase.push();

            databaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //send message to server
                    ref.child("content").setValue(message);
                    //set message name, get it from account name in users data
                    ref.child("username").setValue(dataSnapshot.child("name").getValue());

                    //get current time
                    DateFormat df = new SimpleDateFormat("kk:mm dd.MM.yyyy ");
                    String currentTime = df.format(Calendar.getInstance().getTime());

                    //send the time of message to server
                    ref.child("time").setValue(currentTime);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            //allows the user to easily scroll by using the messagelist item count
            messageList.scrollToPosition(messageList.getAdapter().getItemCount());

        }

        //clear message text box when message is sent
        editMessage.getText().clear();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        View view;

        public MessageViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setContent(String content) {
            TextView chatMessage = view.findViewById(R.id.messageText); //get reference to users message from message.xml
            chatMessage.setText(content);
        }
        public void setUsername(String username) {
            TextView nameMessage = view.findViewById(R.id.nameText); //get reference to users name text
            nameMessage.setText(username);
        }
        public void setTime(String time) {
            TextView timeMessage = view.findViewById(R.id.timeText);
            timeMessage.setText(time);
        }

    }

    void isUserLoggedIn()
    {
        //check if current user is null
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null)
                {
                    startActivity(new Intent(ChatActivity.this, LoginActivity.class));

                    auth.signOut();

                    finish();
                }
            }
        };
    }
}

