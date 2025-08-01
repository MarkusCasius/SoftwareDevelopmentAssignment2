package com.example.softwaredevelopmentassessment2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.pm.ApplicationInfo;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.BuildConfig;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Authentication;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.FirebaseException;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthMultiFactorException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.MultiFactorResolver;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneMultiFactorAssertion;
import com.google.firebase.auth.PhoneMultiFactorGenerator;
import com.google.firebase.auth.PhoneMultiFactorInfo;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button GotoAuthButton = findViewById(R.id.GotoAuthButton);
        GotoAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VerificationActivity.class);
                startActivity(intent);
            }
        });

        Button SignOutButton = findViewById(R.id.SignOutButton);
        SignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // ...
                            }
                        });
            }
        });

        // 🔐 App Check setup
        Log.d("BuildCheck", "Is Debuggable: " + ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0));
        Log.d("BuildCheck", "BuildConfig.DEBUG: " + BuildConfig.DEBUG);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        //if (BuildConfig.DEBUG) {
        if (true) {
            firebaseAppCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance());
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

// Create a collection "debugTest" and a document "ping"
        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", System.currentTimeMillis());

        db.collection("debugTest").document("ping")
                .set(data)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Document written"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error writing document", e));


        FirebaseAppCheck.getInstance().getToken(false)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("AppCheck", "Token: " + task.getResult().getToken());
                    } else {
                        Log.e("AppCheck", "Token error", task.getException());
                    }
                });


        // 🔗 Facebook SDK setup
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this.getApplication());
        CallbackManager callbackManager = CallbackManager.Factory.create();

        // 🔑 FirebaseUI sign-in
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build()
        );

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }

//    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//    int status = apiAvailability.isGooglePlayServicesAvailable(Authentication);
//    if (status != ConnectionResult.SUCCESS) {
//        // Prompt to install/update Play Services
//    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    // 🔁 Handles result from FirebaseUI sign-in
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        Log.d("Signin Result", "Result Returned.");
        IdpResponse response = result.getIdpResponse();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (result.getResultCode() == RESULT_OK || user != null) {
            Log.d("UserLog", "User is logged in");
            proceedWithUser(user);
        } else {
            Log.d("LoginError", "Initial result not OK or user null — checking again shortly...");

            // 🔁 Retry after short delay in case FirebaseUser is populated late
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                FirebaseUser delayedUser = FirebaseAuth.getInstance().getCurrentUser();
                if (delayedUser != null) {
                    Log.d("UserLog", "User became available after delay");
                    proceedWithUser(delayedUser);
                } else {
                    Log.d("LoginError", "User still null after delay");
                    Exception e = response != null ? response.getError() : null;
                    if (e instanceof FirebaseAuthMultiFactorException) {
                        handleMfaChallenge((FirebaseAuthMultiFactorException) e);
                    } else {
                        // Handle other sign-in errors
                    }
                }
            }, 500); // Delay for half a second
        }
    }

    // MFA Enrollment
    private void enrollSecondFactor(FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Your Phone Number");

        final EditText input = new EditText(this);
        input.setHint("+44...");
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        builder.setPositiveButton("Send Code", (dialog, which) -> {
            String phoneNumber = input.getText().toString().trim();
            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "Phone number cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            PhoneAuthOptions options = PhoneAuthOptions.newBuilder()
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                            PhoneMultiFactorAssertion assertion = PhoneMultiFactorGenerator.getAssertion(credential);
                            user.getMultiFactor().enroll(assertion, "Personal Phone")
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d("MFA", "Enrollment successful");
                                            Toast.makeText(MainActivity.this, "Enrollment successful", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.d("MFA", "Enrollment failed", task.getException());
                                            Toast.makeText(MainActivity.this, "Enrollment failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            Log.e("MFA", "Verification failed: " + e.getMessage());
                            Toast.makeText(MainActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                            showCodeEntryDialogForEnrollment(verificationId, user);
                        }
                    })
                    .build();

            PhoneAuthProvider.verifyPhoneNumber(options);

        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showCodeEntryDialogForEnrollment(String verificationId, FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter SMS Code");

        final EditText input = new EditText(this);
        input.setHint("SMS code");
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (!code.isEmpty()) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                PhoneMultiFactorAssertion assertion = PhoneMultiFactorGenerator.getAssertion(credential);
                user.getMultiFactor().enroll(assertion, "Personal Phone")
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "MFA enrollment successful", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "MFA enrollment failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Code cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // MFA Challenge Handling (Incase there is delay or poor wifi))
    private void handleMfaChallenge(FirebaseAuthMultiFactorException e) {
        MultiFactorResolver resolver = e.getResolver();
        PhoneMultiFactorInfo phoneInfo = (PhoneMultiFactorInfo) resolver.getHints().get(0); // Assuming one factor
        Log.d("OperationBugun", "HandleMFAChallenge.");
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder()
                .setMultiFactorSession(resolver.getSession())
                .setActivity(this)
                .setPhoneNumber("+4407951786254")
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        showCodeEntryDialog(verificationId, resolver); // 🧾 Prompt for code
                    }

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        finishMfaSignIn(resolver, credential); // ✅ Auto-complete
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(MainActivity.this, "MFA failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void showCodeEntryDialog(String verificationId, MultiFactorResolver resolver) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter SMS Code");

        final EditText input = new EditText(this);
        input.setHint("SMS code");
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String code = input.getText().toString().trim();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            finishMfaSignIn(resolver, credential);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void proceedWithUser(FirebaseUser user) {
        if (user.getMultiFactor().getEnrolledFactors().isEmpty()) {
            enrollSecondFactor(user); // MFA enrollment
        } else {
            Log.d("Proceed", "Not enrolling.");
            Intent intent = new Intent(MainActivity.this, VerificationActivity.class);
            startActivity(intent);
            finish();
        }
    }


    // Complete MFA sign-in
    private void finishMfaSignIn(MultiFactorResolver resolver, PhoneAuthCredential credential) {
        PhoneMultiFactorAssertion assertion = PhoneMultiFactorGenerator.getAssertion(credential);
        if (resolver != null) {
            resolver.resolveSignIn(assertion)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            Toast.makeText(this, "Signed in with MFA!", Toast.LENGTH_SHORT).show();
                            // Proceed to main app
                        } else {
                            Toast.makeText(this, "MFA sign-in failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }
}
