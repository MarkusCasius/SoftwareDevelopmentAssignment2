package com.example.softwaredevelopmentassessment2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;
import com.google.firebase.auth.MultiFactorResolver;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MfaHelper {

    public static void handleMfaChallenge(Activity activity, FirebaseAuthMultiFactorException e) {
        MultiFactorResolver resolver = e.getResolver();
        MultiFactorSession session = resolver.getSession(); // ✅ Already provided

        // Get the phone factor from the hints
        PhoneMultiFactorInfo phoneInfo = null;
        for (MultiFactorInfo hint : resolver.getHints()) {
            if (hint instanceof PhoneMultiFactorInfo) {
                phoneInfo = (PhoneMultiFactorInfo) hint;
                break;
            }
        }

        if (phoneInfo == null) {
            Toast.makeText(activity, "No phone MFA factor found", Toast.LENGTH_LONG).show();
            return;
        }

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder()
                .setMultiFactorSession(session) // ✅ Use this, not user.getMultiFactor().getSession()
                .setActivity(activity)
                .setPhoneNumber(phoneInfo.getPhoneNumber())
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

    public static void proceedWithUser(Activity activity, FirebaseUser user) {
        if (user.getMultiFactor().getEnrolledFactors().isEmpty()) {
            enrollSecondFactor(activity, user);
        } else {
            Intent intent = new Intent(activity, VerificationActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }
    }
}
