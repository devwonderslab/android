package com.wl.sample.ui.auth;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.wl.sample.network.NetworkConfig;

interface LoginPresenter {
    void validateCredentials(String loginName, char[] password);
    void onSaveInstanceState(Bundle outState);
    void onRestoreInstanceState(Bundle savedInstanceState);

    void onNetworkSystemChanged(@NonNull NetworkConfig savedConfig);
}
