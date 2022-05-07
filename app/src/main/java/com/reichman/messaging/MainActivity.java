package com.reichman.messaging;

import static com.google.firebase.crashlytics.internal.Logger.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Firebase Authentication Instance
    private FirebaseAuth fireAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Google Sign-in Request Configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Create Google Sign-in Client
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Google Sign-in Button On Click Listener
        SignInButton sign_in = findViewById(R.id.sign_in_button);
        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Sign-in Intent
                mGoogleSignInClient.getSignInIntent();

                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                    // ...
                }
                System.out.println("User Is Trying To Sign In.");
            }

            // Sign In Method
            private void signIn() {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 1);
            }

        });

        // Initialize To Authenticate With Firebase
        fireAuth = FirebaseAuth.getInstance();

        // Firebase Registration / Sign In On Click Listener
        Button firebaseAuthButton = findViewById(R.id.auth_button);
        firebaseAuthButton.setOnClickListener(view -> {
            // Get Email Input
            TextView userEmail = findViewById(R.id.email);

            // Get Password Input
            TextView userPassword = findViewById(R.id.password);

            // Check Values
            System.out.println("Email: " + userEmail.getText().toString());
            System.out.println("Password: " + userPassword.getText().toString());

            // Get String Values Of User's Auth Details
            String userEmailText = userEmail.getText().toString();
            String userPasswordText = userPassword.getText().toString();

            // Check If Any Field Is Empty
            if (userEmailText.equals("") || userPasswordText.equals("")) {
                userEmail.setText("");
                userPassword.setText("");
                Toast.makeText(MainActivity.this, "Authentication Details Incomplete.",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Check If User Already Exists In Firebase
                fireAuth.fetchSignInMethodsForEmail(userEmailText).addOnCompleteListener(task -> {
                    // Get SignIn Methods For Email And See If Any Is Available
                    try {
                        if (task.getResult().getSignInMethods().size() == 0){
                            // Register New User
                            firebaseRegisterSignIn("Register", userEmailText, userPasswordText);
                        }else {
                            // Sign In Existing User
                            firebaseRegisterSignIn("SignIn", userEmailText, userPasswordText);
                        }

                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Authentication Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Authentication Error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());;
            }
        });

    }

    // On Start Method
    protected void onStart() {

        // Override Method
        super.onStart();

        // Set Google Button Dimensions
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        // Check If User Is Already Signed-in With Google
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        // Check If User Is Already Signed-in With Firebase
        FirebaseUser currentUser = fireAuth.getCurrentUser();

        // Check Sign-in Status
        if (account != null) {
            System.out.println("User Is Signed In With Google.");
            updateUI(account);
        } else if (currentUser != null) {
            System.out.println("User Is Signed In With Firebase.");
            updateUIFirebase(currentUser);
        } else {
            System.out.println("User Is Not Signed In.");
            updateUI(null);
        }
    }

    @Override
    public void onClick(View view) {

//        // Firebase Auth Button Click
//        switch (view.getId()) {
//            case R.id.auth_button:
//
//                // Get Email Input
//                TextView userEmail = findViewById(R.id.email);
//
//                // Get Password Input
//                TextView userPassword = findViewById(R.id.password);
//
//                // Check If Any Field Is Empty
//                if (userEmail.equals("") || userPassword.equals("")) {
//                    userEmail.setText("");
//                    userPassword.setText("");
//                    Toast.makeText(MainActivity.this, "Auth Details Incomplete.",
//                            Toast.LENGTH_SHORT).show();
//                } else {
//                    firebaseRegisterSignIn(userEmail.toString(), userPassword.toString());
//                }
//
//                break;
//            // ...
//        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Instantiate Super Class
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 1) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            // Use Sign In Result
            handleSignInResult(task);
        }
    }

    // Handle Sign In & Get Result Object
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // Get Result From Sign In Task
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(null, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    // Handle Registration & Sign In With Firebase
    private void firebaseRegisterSignIn(String registerOrSignIn, String email, String password) {

        // Check If User Should Be Registered Or Signed In
        if (registerOrSignIn.equals("Register")) {

            // Register User With Firebase
            fireAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Successfully Created New User
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = fireAuth.getCurrentUser();
                            updateUIFirebase(user);
                        } else {
                            // New User Creation Failed
                            Log.w("Firebase Register", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed: " + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    });
        } else {

            // Sign In User With Firebase
            fireAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Successfully Signed In Existing User
                            Log.d(null, "signInWithEmail:success");
                            FirebaseUser user = fireAuth.getCurrentUser();
                            updateUIFirebase(user);
                        } else {
                            // Sign In Failed
                            Log.w("Firebase Register", "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed: " + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    });
        }
    }

    // Update App With Google Sign In Result
    private void updateUI(GoogleSignInAccount account) {

        // Check Sign-in Status
        if (account != null) {

            // Get User's Information
            String userName = account.getDisplayName();
            String userGivenName = account.getGivenName();
            String userFamilyName = account.getFamilyName();
            String userEmail = account.getEmail();
            String userId = account.getId();
            Uri userPhoto = account.getPhotoUrl();
            Uri defaultPhoto = Uri.parse("https://icon-library.com/images/generic-profile-icon/generic-profile-icon-23.jpg");

            // Create New Message Object
            Message message = new Message();

            // Check If User Has Complete Data With Picture
            if (userPhoto != null) {
                // Set User Variables With Message Object
                message.setUser(userName);
                message.setPhoto(userPhoto.toString());
            } else {
                // Set User Variables With Message Object But Without Image
                message.setUser(userName);
                message.setPhoto(defaultPhoto.toString());
            }

            // Check
            System.out.println(userName);
            System.out.println(userGivenName);
            System.out.println(userFamilyName);
            System.out.println(userEmail);
            System.out.println(userId);
            System.out.println(userPhoto);

            // Go To Chatting Activity
            Intent chatIntent = new Intent(MainActivity.this, ChattingActivity.class);

            // Put User Data Into Chatting Activity
            chatIntent.putExtra("user_details_message", message);

            // Start Chatting Activity
            startActivity(chatIntent);

        } else {
            System.out.println("User Is Not Signed In.");
        }

    }

    // Update App With Firebase Sign In Result
    private void updateUIFirebase(FirebaseUser account) {

        // Check Sign-in Status
        if (account != null) {

            // Get User's Information
            String userName = account.getDisplayName();
            String userEmail = account.getEmail();
            Uri userPhoto = account.getPhotoUrl();
            Uri defaultPhoto = Uri.parse("https://icon-library.com/images/generic-profile-icon/generic-profile-icon-23.jpg");

            // Create New Message Object
            Message message = new Message();

            // Check If User Has Complete Data With Picture
            if (userPhoto != null) {
                // Set User Variables With Message Object
                message.setUser(userName);
                message.setPhoto(userPhoto.toString());
            } else {
                // Set User Variables With Message Object But Without Image
                // If User Does Not Have A Photo
                // The Chances of Having A Display Name Are Low
                // So We Use The Email As An Identifier Instead
                String userEmailSplit = userEmail.split("@")[0];
                message.setUser(userEmailSplit);
                message.setPhoto(defaultPhoto.toString());
            }

            // Check
            System.out.println(userName);
            System.out.println(userEmail);
            System.out.println(userPhoto);

            // Go To Chatting Activity
            Intent chatIntent = new Intent(MainActivity.this, ChattingActivity.class);

            // Put User Data Into Chatting Activity
            chatIntent.putExtra("user_details_message", message);

            // Start Chatting Activity
            startActivity(chatIntent);

        } else {
            System.out.println("User Is Not Signed In.");
        }

    }
}