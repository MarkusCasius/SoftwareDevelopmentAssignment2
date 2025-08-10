package com.example.softwaredevelopmentassessment2;

import androidx.test.espresso.IdlingResource;
import androidx.annotation.Nullable;

public class FirebaseAuthIdlingResource implements IdlingResource {

    private ResourceCallback callback;
    private boolean isIdle = false;

    public void setIdle(boolean idle) {
        isIdle = idle;
        if (isIdle && callback != null) {
            callback.onTransitionToIdle();
        }
    }

    @Override
    public String getName() {
        return FirebaseAuthIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.callback = callback;
    }
}