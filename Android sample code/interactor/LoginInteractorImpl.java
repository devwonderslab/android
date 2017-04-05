package com.wl.sample.interactors;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.wl.sample.common.SimpleWrapper;
import com.wl.sample.domain.auth.AuthModel;
import com.wl.sample.domain.auth.LoginTask;
import com.wl.sample.domain.auth.LoginTaskResultListener;
import com.wl.sample.network.HttpCodes;
import com.wl.sample.network.HttpNetClientModule;
import com.wl.sample.network.NetworkConfig;
import com.wl.sample.ui.auth.LoginActivity;
import com.wl.sample.ui.auth.OnLoginListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;


public class LoginInteractorImpl implements LoginInteractor, LoginTaskResultListener {
    private @NonNull static final String LOG_TAG = LoginInteractorImpl.class.getSimpleName();

    @Inject
    LoginTask loginTask_;

    private OnLoginListener listener_;

    private boolean isRegistered_;


    // TODO: Put them to separate helper class
    @Inject
    SimpleWrapper<Retrofit> netSystem_;
    @Inject
    Gson gson_;
    @Inject
    OkHttpClient okHttpClient_;
//    private long viewTagId_ = Long.MIN_VALUE;

    @Inject
    LoginInteractorImpl() {
        Log.d(LOG_TAG, "Intefactor instance " + this + "\n\tState: Login task " + loginTask_);

        subscribeToEvents();
    }

    @Override
    protected void finalize() throws Throwable {
        unsubscribeFromEvents();
        super.finalize();
    }

    @Override
    public void login(@NonNull final String loginName, final char[] password) {
        if (! isLoginNameValid(loginName)) {
            listener_.onLoginNameError();
            return;
        }

        if (! isPasswordValid(password)) {
            listener_.onPasswordError();

            return;
        }

        loginTask_.login(loginName, password);
    }

    @Override
    public void setListener(@NonNull OnLoginListener listener) {
        this.listener_ = listener;
    }

    @Override
    public void onSuccess(@NonNull AuthModel model, int code) {
        if (listener_ != null) {
            listener_.onSuccess(model);
        }
    }

    @Override
    public void onError(@NonNull String body, int code) {
        if (listener_ != null) {
            if (code == HttpCodes.FORBIDDEN) {
                listener_.onForbiddenAccess();
            }
            else {
                listener_.onAuthError();
            }
        }
    }

    @Override
    public void onFailure(@NonNull Throwable t) {
        if (listener_ != null) {
            listener_.onNetworkError(t);
        }
    }

    private static boolean isLoginNameValid(String name) {
        return !TextUtils.isEmpty(name);
    }

    private static boolean isPasswordValid(@NonNull char[] password) {
        return password.length > 4;
    }

    @Override
    public void updateNetworkSystem(@NonNull NetworkConfig savedConfig) {
        Log.d(LOG_TAG, "Going to replace network component with " + NetworkConfig.getServerUrl(savedConfig.getBaseUrlType()));
        netSystem_.set(HttpNetClientModule.buildRetrofit(gson_, okHttpClient_, NetworkConfig.getServerUrl(savedConfig.getBaseUrlType())));
    }

    // Event bus listener
    @SuppressWarnings("unused")
    @Subscribe (threadMode = ThreadMode.BACKGROUND)
    public void onLifecycleChanges(@NonNull LoginActivity.LifecycleWrap wrap) {

        switch (wrap.event_) {
            case ON_ATTACH: {
                loginTask_.onAttach(this);
                break;
            }

            case ON_DETACH: {
                loginTask_.onDetach();
                break;
            }

            case UNSUBSCRIBE: {
                unsubscribeFromEvents();
                break;
            }
        }
    }

    private void subscribeToEvents() {
        if (!isRegistered_) {
            Log.d(LOG_TAG, "subscribeFromEvents() from Activity life cycle " + this);
            EventBus.getDefault().register(this);
            isRegistered_ = true;
        }
    }

    private void unsubscribeFromEvents() {
        if (isRegistered_) {
            Log.d(LOG_TAG, "unsubscribeFromEvents() from Activity life cycle " + this);
            EventBus.getDefault().unregister(this);
            isRegistered_ = false;
        }
    }
}
