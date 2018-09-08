package com.example.mohamedali.chatme.constanst;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by Mohamed Ali on 06/09/2018.
 */

public final class Concrete {
    private Concrete() {}

    public static final String  USERS = "users";
    public static final String  FRIENDS = "friends";
    public static final String  CHAT = "chat";
    public static final String  NOTIFICATIONS = "notifications";

    public static final String  PROFILE_PHOTOS = "profile_photos";

    public static String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public static String getRegistrationToken() {
        return FirebaseInstanceId.getInstance().getToken();
    }

}
