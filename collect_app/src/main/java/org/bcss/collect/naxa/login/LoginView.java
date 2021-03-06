package org.bcss.collect.naxa.login;

public interface LoginView {

    void showProgress(boolean show);

    void showPasswordError(int resourceId);

    void showUsernameError(int resourceId);

    void successAction();

    void showError(String errorMessage);
}
