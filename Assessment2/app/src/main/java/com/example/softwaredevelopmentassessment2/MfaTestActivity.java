package com.example.softwaredevelopmentassessment2;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;
import com.google.firebase.auth.MultiFactorInfo;
import com.google.firebase.auth.MultiFactorResolver;
import com.google.firebase.auth.MultiFactorSession;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MfaTestActivity extends Activity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // 🔹 Replace with an MFA-enabled account's email + password
        String email = "caseymark94@gmail.com";
        String password = "AbsoluteCinema69@";

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Signed in without MFA", Toast.LENGTH_SHORT).show();
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthMultiFactorException) {
                            handleMfaChallenge((FirebaseAuthMultiFactorException) e);
                        } else {
                            Toast.makeText(this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void handleMfaChallenge(FirebaseAuthMultiFactorException e) {
        MultiFactorResolver resolver = e.getResolver();
        MultiFactorSession session = resolver.getSession();

        PhoneMultiFactorInfo phoneInfo = null;
        for (MultiFactorInfo hint : resolver.getHints()) {
            if (hint instanceof PhoneMultiFactorInfo) {
                phoneInfo = (PhoneMultiFactorInfo) hint;
                break;
            }
        }

        if (phoneInfo == null) {
            Toast.makeText(this, "No phone MFA factor found", Toast.LENGTH_LONG).show();
            return;
        }

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance()) // ✅ Required
                .setMultiFactorSession(session)
                .setActivity(this)
                .setPhoneNumber(phoneInfo.getPhoneNumber())
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        Toast.makeText(MfaTestActivity.this, "Code sent! Enter it in logs", Toast.LENGTH_SHORT).show();
                        // Here you can prompt user to enter code
                        // For testing, we won't auto-submit it
                    }

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        finishMfaSignIn(resolver, credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException ex) {
                        Toast.makeText(MfaTestActivity.this, "MFA failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void finishMfaSignIn(MultiFactorResolver resolver, PhoneAuthCredential credential) {
        PhoneMultiFactorAssertion assertion = PhoneMultiFactorGenerator.getAssertion(credential);
        resolver.resolveSignIn(assertion)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "MFA sign-in complete!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Failed to complete MFA: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
