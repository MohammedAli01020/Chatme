package com.example.mohamedali.chatme.login;

import android.net.Uri;

/**
 * Created by Mohamed Ali on 08/09/2018.
 */

public interface LoginMVP {
    interface View {

        String getUserName();
        void setUserNameError();
        void setUserNameErrorNull();
        void startMainActivity();
        void startGalleryActivity();
        void showProgressDialog();
        void hideProgressDialog();
        void startSignInActivity();
        void setImageProfile(Uri downloadUri);
    }

    interface Presenter {
        void setView(LoginMVP.View view);
        void addNewUser();
        void pickImageFromGallery();
        void storeProfileImage(Uri imageUri);
        void checkAuth();
    }
}
