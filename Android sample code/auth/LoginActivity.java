package com.wl.sample.ui.auth;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.GetChars;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.wl.sample.R;
import com.wl.sample.common.LifecycleEvent;
import com.wl.sample.databinding.ActivityLoginBinding;
import com.wl.sample.di.MyApp;
import com.wl.sample.domain.user.UserInfo;
import com.wl.sample.ui.UIUtil;
import com.wl.sample.ui.board.BoardActivity;
import com.wl.sample.ui.settings.AppNetworkSettingsDialogFragment;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoginViewOps, View.OnClickListener,
        TextView.OnEditorActionListener, AppNetworkSettingsDialogFragment.NetworkSelectionListener {
    private static final @NonNull String LOG_TAG = LoginActivity.class.getSimpleName();

    public static class LifecycleWrap {
        LifecycleWrap(@NonNull LifecycleEvent e) {
            this.event_ = e;
        }
        @NonNull
        public LifecycleEvent event_;
    }
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    private ActivityLoginBinding binding_;

    @Inject
    LoginPresenter presenter_;

    @Inject
    UserInfo user_;

    private void injectMe() {
        MyApp.getApp(this)
                .getAppComponent()
                .plus(new LoginModule(this))
                .inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectMe();

        binding_ = DataBindingUtil.setContentView(this, R.layout.activity_login);

        binding_.btnLogin.setOnClickListener(this);
        binding_.editLogin.setOnClickListener(this);
        binding_.editPassword.setOnClickListener(this);

        binding_.editPassword.setOnEditorActionListener(this);

        EventBus.getDefault().post(new LifecycleWrap(LifecycleEvent.ON_ATTACH));
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().post(new LifecycleWrap(LifecycleEvent.ON_DETACH));
        EventBus.getDefault().post(new LifecycleWrap(LifecycleEvent.UNSUBSCRIBE));

        binding_.editPassword.setOnEditorActionListener(null);

        binding_.editPassword.setOnClickListener(null);
        binding_.editLogin.setOnClickListener(null);
        binding_.btnLogin.setOnClickListener(null);

        binding_ = null;

        super.onDestroy();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        presenter_.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        presenter_.onSaveInstanceState(outState);
    }

    @NonNull
    public LoginPresenter getPresenter() {
        return presenter_;
    }

    @Override
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.btn_login: {
//                clearError(binding_.tilLogin);
//                clearError(binding_.tilPassword);
                doValidateUserCredentials();

                break;
            }

            case R.id.edit_login: {
                clearError(binding_.tilLogin);

                break;
            }

            case R.id.edit_password: {
                clearError(binding_.tilPassword);

                break;
            }

            default: {
                Log.w(LOG_TAG, "No case for id: " + v.getId());
            }
        }
    }

    /**
     * Called when an action is being performed.
     *
     * @param v        The view that was clicked.
     * @param actionId Identifier of the action.  This will be either the
     *                 identifier you supplied, or {@link EditorInfo#IME_NULL
     *                 EditorInfo.IME_NULL} if being called due to the enter key
     *                 being pressed.
     * @param event    If triggered by an enter key, this is the event;
     *                 otherwise, this is null.
     * @return Return true if you have consumed the action, else false.
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (actionId) {
            case EditorInfo.IME_ACTION_DONE: {
                binding_.btnLogin.performClick();

                return true;
            }
        }

        return false;
    }

    private static @NonNull Pair<String, char[]> extractCredentials(@NonNull CharSequence l, @NonNull GetChars p) {
        final char[] password = new char[p.length()];
        p.getChars(0, p.length(), password, 0);

        return new Pair<>(l.toString(), password);
    }

    private void doValidateUserCredentials() {
        final Pair<String, char[]> credentials = extractCredentials(binding_.editLogin.getText(), binding_.editPassword.getText());
        presenter_.validateCredentials(credentials.first, credentials.second);
    }

    private static void clearError(@NonNull TextInputLayout til) {
        til.setError(null);
    }

    private void updateUIProgressState(boolean isInProgress) {
        binding_.btnLogin.setEnabled(!isInProgress);
        binding_.btnLogin.setVisibility(!isInProgress ? View.VISIBLE : View.GONE);
        binding_.editLogin.setEnabled(!isInProgress);
        binding_.editPassword.setEnabled(!isInProgress);
        binding_.progressIndicator.setVisibility(isInProgress ? View.VISIBLE : View.GONE);
    }

//==================================================================================================
//  LoginViewOps
//--------------------------------------------------------------------------------------------------
    @Override
    public void setProgressState(final boolean isInProgress) {
        Log.d(LOG_TAG, "setProgressState called " + isInProgress);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateUIProgressState(isInProgress);
            }
        });
    }

    @Override
    public void onSuccess() {
        Log.d(LOG_TAG, "Successful login!\n\tUser data " + user_);
        putResultAndFinish(null);
    }

    @Override
    public void onLoginNameError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding_.tilLogin.setError(getString(R.string.error_field_required));
            }
        });
    }

    @Override
    public void onPasswordError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding_.tilPassword.setError(getString(R.string.error_invalid_password));
            }
        });
    }

    @Override
    public void onAuthError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UIUtil.showGravitatedToast(getApplicationContext(), R.string.error_invalid_credentials, Toast.LENGTH_LONG, Gravity.BOTTOM, 0, 240);
            }
        });
    }

    @Override
    public void onNetworkError(@NetworkError final int cause) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleNetworkError(cause);
            }
        });
    }

    @Override
    public void onForbiddenAccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UIUtil.showGravitatedToast(getApplicationContext(), R.string.error_forbidden_access, Toast.LENGTH_LONG, Gravity.BOTTOM, 0, 240);
            }
        });
    }

    private void putResultAndFinish(@Nullable Bundle args) {
        Intent boardIntent = new Intent(this, BoardActivity.class);
        if (args != null) {
            boardIntent.putExtras(args);
        }

        startActivity(boardIntent);
        finish();
    }

    private void handleNetworkError(@NetworkError int cause) {
        switch (cause) {

            case LoginViewOps.ERROR_CONNECTION: {
                UIUtil.showGravitatedToast(getApplicationContext(), getString(R.string.error_server_connect), Toast.LENGTH_SHORT, Gravity.BOTTOM, 0, 240);
                displayNetworkSettingsBar(networkSettingsListener_);
                break;
            }

            case LoginViewOps.ERROR_UNKNOWN: {
                UIUtil.showGravitatedToast(getApplicationContext(), getString(R.string.error_general), Toast.LENGTH_LONG, Gravity.BOTTOM, 0, 240);
                break;
            }

            case LoginViewOps.ERROR_NO:
            default:
        }
    }

    private void displayNetworkSettingsBar(@Nullable View.OnClickListener listener) {
        Snackbar.make(binding_.getRoot(), R.string.user_msg_network_problem, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_settings, listener)
                .show();
    }

    private final View.OnClickListener networkSettingsListener_ = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AppNetworkSettingsDialogFragment.showDialog(getFragmentManager());
        }
    };

//==================================================================================================
//  AppNetworkSettingsDialogFragment.NetworkSelectionListener
//--------------------------------------------------------------------------------------------------
    @Override
    public void onNetworkTypeChanged() {
        getPresenter().onNetworkSystemChanged(MyApp.getApp(this).getNetworkConfig());
    }
}

