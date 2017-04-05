package com.wl.sample.ui.auth;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// This is a bridge to communicate with presenter
interface LoginViewOps {
    int ERROR_NO = 0;
    int ERROR_CONNECTION = 1;
    int ERROR_UNKNOWN = Integer.MAX_VALUE;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ERROR_NO, ERROR_CONNECTION, ERROR_UNKNOWN})
    @interface NetworkError{}

    void setProgressState(boolean isInProgress);

    void onSuccess();
    void onLoginNameError();
    void onPasswordError();
    void onAuthError();
    void onForbiddenAccess();
    void onNetworkError(@NetworkError int cause);
}
