package com.wl.sample.interactors;

import android.support.annotation.NonNull;

import com.wl.sample.network.NetworkConfig;
import com.wl.sample.ui.auth.OnLoginListener;

public interface LoginInteractor {
    void login(@NonNull String loginName, char[] password);
    void setListener(@NonNull OnLoginListener listener);

    void updateNetworkSystem(@NonNull NetworkConfig savedConfig);
}
