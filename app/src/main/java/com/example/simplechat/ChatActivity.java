package com.example.simplechat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatActivity extends AppCompatActivity {

    public static final String TAG = "ChatActivity";
    public static final int MAX_CHAT_MESSAGES_TO_SHOW = 150;

    EditText etMessage;
    ImageButton btSend;
    RecyclerView rvChat;
    ChatAdapter adapter;
    List<Message> messages;
    // Keep track of initial load to scroll to the bottom of the ListView
    boolean firstLoad;
    // Create a handler which can run code periodically
    static final long POLL_INTERVAL = TimeUnit.SECONDS.toMillis(3);
    Handler handler = new android.os.Handler();
    Runnable refreshMessageRunnable = new Runnable() {
        @Override
        public void run() {
            refreshMessages();
            handler.postDelayed(this, POLL_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // User Login
        if (ParseUser.getCurrentUser() != null) {
            // Toast.makeText(ChatActivity.this, "setup message posting", Toast.LENGTH_LONG).show();
            startWithCurrentUser();
        } else {
            login();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only start checking for new messages when the app becomes active in foreground
        handler.postDelayed(refreshMessageRunnable, POLL_INTERVAL);
    }

    @Override
    protected void onPause() {
        // Stop background task from refreshing messages, to avoid unnecessary traffic & battery drain
        handler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    // Create an anonymous user using ParseAnonymousUtils and set sUserId
    private void login() {
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Anonymous login failed: ", e);
                } else {
                    // Toast.makeText(ChatActivity.this, "setup message posting", Toast.LENGTH_LONG).show();
                    startWithCurrentUser();
                }
            }
        });
    }

    // Get the user id from the cached current User object
    private void startWithCurrentUser() {
        setupMessagePosting();
    }

    private void setupMessagePosting() {
        Toast.makeText(ChatActivity.this, "setup message posting", Toast.LENGTH_LONG).show();
        // Find the text field and button
        etMessage =(EditText) findViewById(R.id.etMessage);
        btSend = (ImageButton) findViewById(R.id.btnSend);
        rvChat = findViewById(R.id.rvChat);
        messages = new ArrayList<>();
        firstLoad = true;
        final String userId = ParseUser.getCurrentUser().getObjectId();
        adapter = new ChatAdapter(ChatActivity.this, userId, messages);
        rvChat.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setReverseLayout(true);
        rvChat.setLayoutManager(linearLayoutManager);

        // When send button is clicked, create message object on Parse
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = etMessage.getText().toString();
                Message message= new Message();
                message.setBody(data);
                message.setUserId(ParseUser.getCurrentUser().getObjectId());
                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Failed to save message: ", e);
                            Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ChatActivity.this, "Successfully created message on Parse", Toast.LENGTH_LONG).show();
                            etMessage.setText(null);
                            refreshMessages();
                        }
                    }
                });
            }
        });
    }

    // Query messages from Parse so we can load them into the chat adapter
    void refreshMessages() {
        // Construct query to execute
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
        // Configure limit and sort order
        query.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);

        // Get the latest 50 message, order will show up newest to oldest
        query.orderByDescending("createdAt");
        // Execute query to fetch all messages from Parse asynchronously
        // equivalent to SELECT query with SQL
        query.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> objects, ParseException e) {
                if (e == null) {
                    messages.clear();
                    messages.addAll(objects);
                    adapter.notifyDataSetChanged();
                    // Scroll to the bottom of the list on initial load
                    if (firstLoad) {
                        rvChat.scrollToPosition(0);
                        firstLoad = false;
                    }
                } else {
                    Log.e("message", "Error Loading Messages" + e);
                }
            }
        });
    }
}