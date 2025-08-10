package com.example.softwaredevelopmentassessment2;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;
import com.google.firebase.auth.MultiFactorResolver;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.List;
import java.util.concurrent.TimeUnit;

// This class handles methods for resolving Multi Factor Authentication recieved from the LoginActivity
public class MfaHelper {

    private static final String CHANNEL_ID = "welcome_channel_id";
    private static final String CHANNEL_NAME = "Welcome Notifications";

    // When a MFA challenge is sent (error), it will be directed here. Often it is due to an account having MFA enabled, and thus needs
    // to complete the authentication before sign in can be complete.
    public static void handleMfaChallenge(Activity activity, FirebaseAuthMultiFactorException e) {
        MultiFactorResolver resolver = e.getResolver();
        MultiFactorSession session = resolver.getSession();

        PhoneMultiFactorInfo selectedHint = null;
        for (MultiFactorInfo hint : resolver.getHints()) {
            if (hint instanceof PhoneMultiFactorInfo) {
                selectedHint = (PhoneMultiFactorInfo) hint;
                break;
            }
        }

        if (selectedHint == null) {
            Toast.makeText(activity, "No phone MFA factor found", Toast.LENGTH_LONG).show();
            return;
        }

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder()
                .setActivity(activity)
                .setMultiFactorSession(session)
                .setMultiFactorHint(selectedHint)  // Use MultiFactorHint here!
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        showCodeEntryDialog(activity, verificationId, resolver);
                    }

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        finishMfaSignIn(activity, resolver, credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException ex) {
                        Toast.makeText(activity, "MFA failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // Displays an SMS field to enter the code they recieve to complete the MFA
    private static void showCodeEntryDialog(Activity activity, String verificationId, MultiFactorResolver resolver) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Enter SMS Code");

        final EditText input = new EditText(activity);
        input.setHint("SMS code");
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (!code.isEmpty()) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                finishMfaSignIn(activity, resolver, credential);
            } else {
                Toast.makeText(activity, "Code cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Final module to complete the MFA, checking if the MFA was successful, then directing to proceedWithUser
    private static void finishMfaSignIn(Activity activity, MultiFactorResolver resolver, PhoneAuthCredential credential) {
        PhoneMultiFactorAssertion assertion = PhoneMultiFactorGenerator.getAssertion(credential);
        resolver.resolveSignIn(assertion)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        Toast.makeText(activity, "Signed in with MFA!", Toast.LENGTH_SHORT).show();
                        proceedWithUser(activity, user);
                    } else {
                        Toast.makeText(activity, "MFA sign-in failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Module for enrolling an account with MFA. Given Firebase's limitations with a free account, mobile phone is used. Starts by asking the user
    // to enter their phone number, then to input their SMS code. Given the free account, no actual requests can be sent, so a dummy phone and fixed
    // SMS code is used.
    public static void enrollSecondFactor(Activity activity, FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Enter Your Phone Number");

        final EditText input = new EditText(activity);
        input.setHint("+44...");
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        builder.setPositiveButton("Send Code", (dialog, which) -> {
            String phoneNumber = input.getText().toString().trim();
            if (phoneNumber.isEmpty()) {
                Toast.makeText(activity, "Phone number cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            PhoneAuthOptions options = PhoneAuthOptions.newBuilder()
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                            PhoneMultiFactorAssertion assertion = PhoneMultiFactorGenerator.getAssertion(credential);
                            user.getMultiFactor().enroll(assertion, "Personal Phone")
                                    .addOnCompleteListener(task -> {
                                        String message = task.isSuccessful() ? "Enrollment successful" : "Enrollment failed";
                                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                                    });
                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            Toast.makeText(activity, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                            showCodeEntryDialogForEnrollment(activity, verificationId, user);
                        }
                    })
                    .build();

            PhoneAuthProvider.verifyPhoneNumber(options);

        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Similar to showCodeEntryDialog, it works similarly, but for ensuring that the entered phone number is correct before assigning it to the user's account.
    private static void showCodeEntryDialogForEnrollment(Activity activity, String verificationId, FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Enter SMS Code");

        final EditText input = new EditText(activity);
        input.setHint("SMS code");
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (!code.isEmpty()) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                PhoneMultiFactorAssertion assertion = PhoneMultiFactorGenerator.getAssertion(credential);
                user.getMultiFactor().enroll(assertion, "Personal Phone")
                        .addOnCompleteListener(task -> {
                            String message = task.isSuccessful() ? "MFA enrollment successful" : "MFA enrollment failed";
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(activity, "Code cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // A Module to either begin with a user enrolling MFA, or going onto the home page.
    public static void proceedWithUser(Activity activity, FirebaseUser user) {
        if (user.getMultiFactor().getEnrolledFactors().isEmpty()) {
            enrollSecondFactor(activity, user);
        } else {
            showWelcomeNotification(activity);
            Intent intent = new Intent(activity, MainActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }
    }

    // A module for displaying a welcome notification after creating a new account for the application.
    private static void showWelcomeNotification(Activity activity) {
        NotificationManager notificationManager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Oreo+ devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // or your app icon
                .setContentTitle("Welcome to the App!")
                .setContentText("Your account has been created successfully. Enjoy your experience!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}

