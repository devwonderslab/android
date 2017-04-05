package com.wl.sample.ui.auth;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.wl.sample.common.DataStorage;
import com.wl.sample.domain.auth.AuthModel;
import com.wl.sample.domain.user.UserInfo;
import com.wl.sample.interactors.LoginInteractor;
import com.wl.sample.network.NetworkConfig;

import java.net.ConnectException;


class LoginPresenterImpl implements LoginPresenter, OnLoginListener {
    private final LoginViewOps viewOps_;
    private final LoginInteractor interactor_;

    private final UserInfo user_;
    private DataStorage<UserInfo> userDataStorage_;

    private static final String ARG_OPERATION_IN_PROGRESS = LoginPresenterImpl.class.getSimpleName() + ".ARG_OPERATION_IN_PROGRESS";
    private boolean operationIsInProgress_;

    LoginPresenterImpl(@NonNull LoginViewOps viewOps, @NonNull LoginInteractor interactor, @NonNull UserInfo user, @NonNull DataStorage<UserInfo> storage) {
        this.viewOps_ = viewOps;
        this.interactor_ = interactor;
        this.user_ = user;
        this.userDataStorage_ = storage;
    }

//==================================================================================================
//  LoginPresenter
//--------------------------------------------------------------------------------------------------
    @Override
    public void validateCredentials(String loginName, char[] password) {
        setProgressState(true);
        interactor_.login(loginName, password);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(ARG_OPERATION_IN_PROGRESS, operationIsInProgress_);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        operationIsInProgress_ = savedInstanceState.getBoolean(ARG_OPERATION_IN_PROGRESS, false);
        setProgressState(savedInstanceState.getBoolean(ARG_OPERATION_IN_PROGRESS, false));
    }

//==================================================================================================
//  OnLoginListener
//--------------------------------------------------------------------------------------------------
    @Override
    public void onSuccess(@NonNull AuthModel model) {
        // Save user auth
        user_.setId(model.getUserId());
        user_.setToken(model.getAccessToken());

        userDataStorage_.save(user_);

        setProgressState(false);
        viewOps_.onSuccess();
    }

    @Override
    public void onLoginNameError() {
        setProgressState(false);
        viewOps_.onLoginNameError();
    }

    @Override
    public void onPasswordError() {
        setProgressState(false);
        viewOps_.onPasswordError();
    }

    @Override
    public void onAuthError() {
        setProgressState(false);
        viewOps_.onAuthError();
    }

    @Override
    public void onNetworkError(Throwable t) {
        setProgressState(false);

        if (t instanceof ConnectException) {
            viewOps_.onNetworkError(LoginViewOps.ERROR_CONNECTION);
        }
        else {
            viewOps_.onNetworkError(LoginViewOps.ERROR_UNKNOWN);
        }

    }

    @Override
    public void onForbiddenAccess() {
        setProgressState(false);
        viewOps_.onForbiddenAccess();
    }

    private void setProgressState(boolean isInProgress) {
        operationIsInProgress_ = isInProgress;
        viewOps_.setProgressState(operationIsInProgress_);
    }

    @Override
    public void onNetworkSystemChanged(@NonNull NetworkConfig savedConfig) {
        interactor_.updateNetworkSystem(savedConfig);
    }
}
