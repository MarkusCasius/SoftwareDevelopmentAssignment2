package com.example.softwaredevelopmentassessment2;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthMultiFactorException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.MultiFactorResolver;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneMultiFactorAssertion;
import com.google.firebase.auth.PhoneMultiFactorGenerator;
import com.google.firebase.auth.PhoneMultiFactorInfo;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private FirebaseAuth mAuth;
    private CallbackManager callbackManager;
    private static final int RC_GOOGLE_SIGN_IN = 1001;
    private GoogleSignInClient googleClient;
    private TextView forgotPasswordText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseFirestore.setLoggingEnabled(true);

        // 🔐 App Check setup
        Log.d("AuthFlow", "Setting up AppCheck");
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        if (true) { // Debug mode for now
            firebaseAppCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance());
            Log.d("AuthFlow", "AppCheck: Debug provider installed");
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());
            Log.d("AuthFlow", "AppCheck: PlayIntegrity provider installed");
        }

        FirebaseAppCheck.getInstance().getToken(false)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("AppCheck", "Token: " + task.getResult().getToken());

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Map<String, Object> data = new HashMap<>();
                        data.put("timestamp", System.currentTimeMillis());
                        db.collection("debugTest").document("ping")
                                .set(data)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Test doc written"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error writing test doc", e));
                    } else {
                        Log.e("AppCheck", "Token error", task.getException());
                    }
                });

        // Facebook SDK setup
        Log.d("AuthFlow", "Initializing Facebook SDK");
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this.getApplication());
        CallbackManager callbackManager = CallbackManager.Factory.create();

        // FirebaseUI sign-in setup
        Log.d("AuthFlow", "Building sign-in intent");
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build()
        );

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // from firebase console
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);

        // Email login
        findViewById(R.id.loginButton).setOnClickListener(v -> loginUser());
        findViewById(R.id.signupButton).setOnClickListener(v -> registerUser());

        // Google
        findViewById(R.id.googleSignInButton).setOnClickListener(v -> startGoogleLogin());

        // Facebook
        findViewById(R.id.facebookSignInButton).setOnClickListener(v -> startFacebookLogin());

        // Twitter
        findViewById(R.id.twitterSignInButton).setOnClickListener(v -> startTwitterLogin());

        // Phone
        findViewById(R.id.phoneSignInButton).setOnClickListener(v -> startPhoneLogin());

        // Facebook callback setup
        callbackManager = CallbackManager.Factory.create();

        // Password reset
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        forgotPasswordText.setOnClickListener(v -> showPasswordResetDialog());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Facebook callback
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        // Google callback
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                signInWithCredential(credential);
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            MfaHelper.proceedWithUser(this, user); // Reuse your MFA check logic
                        }
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthMultiFactorException) {
                            MfaHelper.handleMfaChallenge(this, (FirebaseAuthMultiFactorException) e); // Reuse existing method
                        } else {
                            Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void registerUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                        MfaHelper.proceedWithUser(this, user); // Offer to enroll MFA
                    } else {
                        Toast.makeText(this, "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPasswordResetDialog() {
        EditText resetEmailInput = new EditText(this);
        resetEmailInput.setHint("Enter your email");

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Enter your email to receive password reset instructions.")
                .setView(resetEmailInput)
                .setPositiveButton("Send", (dialog, which) -> {
                    String email = resetEmailInput.getText().toString().trim();
                    if (!email.isEmpty()) {
                        sendPasswordResetEmail(email);
                    } else {
                        Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startGoogleLogin() {
        Intent intent = googleClient.getSignInIntent();
        startActivityForResult(intent, RC_GOOGLE_SIGN_IN);
    }

    private void startFacebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                signInWithCredential(credential);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Facebook login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startTwitterLogin() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");
        FirebaseAuth.getInstance()
                .startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    MfaHelper.proceedWithUser(this, user);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthMultiFactorException) {
                        MfaHelper.handleMfaChallenge(this, (FirebaseAuthMultiFactorException) e);
                    } else {
                        Toast.makeText(this, "Twitter login failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startPhoneLogin() {
        // Prompt for phone number
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Your Phone Number");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        builder.setPositiveButton("Send Code", (dialog, which) -> {
            String phoneNumber = input.getText().toString().trim();

            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential credential) {
                            FirebaseAuth.getInstance().signInWithCredential(credential)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            MfaHelper.proceedWithUser(LoginActivity.this, FirebaseAuth.getInstance().getCurrentUser());
                                        }
                                    });
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            Toast.makeText(LoginActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                            promptForCode(verificationId); // your method to enter SMS code
                        }
                    })
                    .build();

            PhoneAuthProvider.verifyPhoneNumber(options);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void promptForCode(String verificationId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter SMS Code");

        final EditText input = new EditText(this);
        input.setHint("SMS code");
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (!code.isEmpty()) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = task.getResult().getUser();
                                MfaHelper.proceedWithUser(this, user);
                            } else if (task.getException() instanceof FirebaseAuthMultiFactorException) {
                                MfaHelper.handleMfaChallenge(this, (FirebaseAuthMultiFactorException) task.getException());
                            } else {
                                Toast.makeText(this, "Verification failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Code cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithCredential(AuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign-in success! You can access Google user details here if needed
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        Log.d("Login", "Signed in as: " + user.getEmail());

                        // Check if MFA is enrolled
                        if (user.getMultiFactor().getEnrolledFactors().isEmpty()) {
                            Toast.makeText(this, "Signed in with Google: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            // Continue without MFA
                            goToMainScreen(user);
                        } else {
                            // User is MFA-enrolled — continue MFA flow
                            MfaHelper.proceedWithUser(this, user);
                        }
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthMultiFactorException) {
                            MfaHelper.handleMfaChallenge(this, (FirebaseAuthMultiFactorException) e);
                        } else {
                            Toast.makeText(this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void goToMainScreen(FirebaseUser user) {
        // Example: go to MainActivity or home screen
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}


