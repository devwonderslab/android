package com.wl.sample.ui.auth;

import android.support.annotation.NonNull;

import com.wl.sample.common.DataStorage;
import com.wl.sample.di.ActivityScope;
import com.wl.sample.domain.user.UserInfo;
import com.wl.sample.interactors.LoginInteractor;
import com.wl.sample.interactors.LoginInteractorImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class LoginModule {
    private LoginViewOps viewOps_;

    LoginModule(@NonNull LoginViewOps viewOps) {
        this.viewOps_ = viewOps;
    }

    @Provides @ActivityScope
    LoginViewOps provideView() {
        return viewOps_;
    }

    @Provides @ActivityScope
    LoginPresenter providePresenter(@NonNull LoginViewOps viewOps, @NonNull LoginInteractor interactor, @NonNull UserInfo user, DataStorage<UserInfo> storage) {
        LoginPresenterImpl presenter = new LoginPresenterImpl(viewOps, interactor, user, storage);
        interactor.setListener(presenter);

        return presenter;
    }

    @Provides @ActivityScope
    LoginInteractor provideInteractor(@NonNull LoginInteractorImpl interactorImpl) {
        return interactorImpl;
    }
}
