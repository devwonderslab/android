package com.wl.sample.ui.auth;

import com.wl.sample.di.ActivityScope;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(
        modules = { LoginModule.class }
)
public interface LoginComponent {
    void inject(LoginActivity activity);
}
