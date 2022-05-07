package com.reichman.messaging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChattingActivity extends AppCompatActivity {

    // Declare Global User Id
    public String userId;

    // Declare Notification Channel Value
    public String CHANNEL_ID = "ArielleShemesh!";

    // Declare Firebase Recycler Adapter
    public FirebaseRecyclerAdapter<Message, MessageHolder> adapter;

    // On Create Method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        // Sign-out Button On Click Listener
        Button sign_out = findViewById(R.id.sign_out_button);
        sign_out.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    // ...
                    case R.id.sign_out_button:
                        signOut();
                        break;
                    // ...
                }
            }
        });

        // Get User Information
        Message userDetails = (Message) getIntent().getSerializableExtra("user_details_message");
        String currentUser = userDetails.getUser();
        String currentUserImage = userDetails.getPhoto();

        // Set User Id
        userId = userDetails.getUser();

        // Use User's Information In Activity
        TextView user = findViewById(R.id.currentUser);
        user.setText("Welcome, " + currentUser);
        ImageView userImage = findViewById(R.id.currentUserImage);
        Uri userImageUri = Uri.parse(currentUserImage);

        // Set User Image With Glide
        Glide.with(this)
                .load(userImageUri)
                .override(150, 100)
                .error(R.drawable.avatar)
                .into(userImage);

        // Send Button
        Button sendMessage = findViewById(R.id.send_chat);

        // Send Button OnClick Listener
        sendMessage.setOnClickListener(view -> {

            // Get Message From Edit Text Field
            EditText chatInput = findViewById(R.id.edit_chat);

            // Get Message String
            String chatInputString = chatInput.getText().toString();

            // Check That Text Is Not Empty
            if (chatInputString.length() != 0) {
                // Create New Instance Of The Message Model In Firebase & Save
                FirebaseDatabase.getInstance()
                        .getReference("Messages")
                        .push()
                        .setValue(new Message(chatInputString, currentUser, currentUserImage));
            }

            // Clear Text Box For New Message
            chatInput.setText("");
        });

        // Get Query Object From Firebase Reference
        DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference("Messages");

        // Listen For New Child And Send Notification
        rootReference.limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Message newMessage = dataSnapshot.getValue(Message.class);
                System.out.println("Author: " + newMessage.getUser());
                System.out.println("Message: " + newMessage.getMessage());
                System.out.println("Previous Post ID: " + prevChildKey);

                // Check That Latest Message Is Not From Current User
                if (!newMessage.getUser().equals(userId)) {
                    // Send Notification
                    sendNotification(newMessage);
                }

            }

            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    // On Start Method
    protected void onStart() {

        // Override Method
        super.onStart();

        // Get Recycler View
        RecyclerView chatRecycler = findViewById(R.id.recycler);

        // Set Layout For Recycler View
        chatRecycler.setLayoutManager(new LinearLayoutManager(this));

        // Get Query Object From Firebase Reference
        DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference("Messages");

        // Firebase Recycler Options
        FirebaseRecyclerOptions<Message> recyclerOptions = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(rootReference, Message.class)
                .build();

        // Firebase Recycler Adapter Utilizing Recycler Options
        adapter = new FirebaseRecyclerAdapter<Message, MessageHolder>(recyclerOptions) {
            @Override
            public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Using Custom Chat Layout To Show Messages With View Holder Instance
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat, parent, false);

                return new MessageHolder(view);
            }

            // Connect Data To View
            @Override
            protected void onBindViewHolder(MessageHolder holder, int position, Message message) {
                // Bind the Chat object to the MessageHolder
                // ...
               // Use Conditional To Check Author
                if (!message.getUser().equals(userId)) {
                    // Set User Image With Glide
                    Glide.with(holder.image.getContext())
                            .load(message.getPhoto())
                            .override(150, 100)
                            .error(R.drawable.avatar)
                            .into(holder.image);

                    // Set User Name
                    holder.name.setText(message.getUser());

                    // Set User Message
                    holder.message.setText(message.getMessage());

                    // Format Properly Set Message Time
                    // holder.time.setText((int) message.getTime());
                    holder.time.setText(DateFormat.format("dd-MM-yyyy (HH:mm)",
                            message.getTime()));

                    // Show Necessary Card - Other Users
                    holder.card.setVisibility(View.VISIBLE);

                    // Hide Necessary Card - Current User
                    holder.cu_card.setVisibility(View.GONE);

                } else if (message.getUser().equals(userId)) {
                    // Set User Image With Glide
                    Glide.with(holder.image.getContext())
                            .load(message.getPhoto())
                            .override(150, 100)
                            .error(R.drawable.avatar)
                            .into(holder.cu_image);

                    // Set User Name
                    holder.cu_name.setText(message.getUser());

                    // Set User Message
                    holder.cu_message.setText(message.getMessage());

                    // Format Properly Set Message Time
                    // holder.time.setText((int) message.getTime());
                    holder.cu_time.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                            message.getTime()));

                    // Show Necessary Card - Current User
                    holder.cu_card.setVisibility(View.VISIBLE);

                    // Hide Necessary Card - Other Users
                    holder.card.setVisibility(View.GONE);

                }

            }

            // Perform Action On Data Change
            @Override
            public void onDataChanged() {

                // Instantiate Super Class
                super.onDataChanged();

                // Scroll To Bottom
                chatRecycler.smoothScrollToPosition(adapter.getItemCount() - 1);
            }
        };

        // Add Messages To Recycler With Adapter
        chatRecycler.setAdapter(adapter);

        // Start Listening
        adapter.startListening();

        // Get FCM Token
        // System.out.println("Token Below!");
        // MyFirebaseMessagingService.getToken(getBaseContext());
        // System.out.println(MyFirebaseMessagingService.getToken(getBaseContext()));

    }

    // Create Notification Channel
    private void createNotificationChannel() {
        // Notification Channel Created For Use With Api 26 and Above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Finally Register Channel With System
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Send Notifications
    public void sendNotification(Message message) {

        // Create Notification Channel
        createNotificationChannel();

        // Create Intent For Chat Activity On Notification Click
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Build Notification & Send
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("New message from " + message.getUser())
                .setContentText(message.getMessage())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                // Open Chat Activity On Tap
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Set Notification Manager
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Set Unique Notification Id
        notificationManager.notify((int) message.getTime(), notificationBuilder.build());

    }

    // Sign Out Of Application
    private void signOut() {
        System.out.println("User Is Signing Out.");
        // Google Sign-in Request Configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Sign Out Firebase Authenticated User
        FirebaseAuth.getInstance().signOut();

        // Create Google Sign-in Client
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                        System.out.println("User Is Signed Out.");
                        finish();
                    }
                });
    }

}
