package com.example.mohamedali.chatme.login;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mohamed Ali on 06/09/2018.
 */

public class User {

    public static final String UID = "uid";
    public static final String REGISTRATION_TOKEN = "registrationToken";
    public static final String USERNAME = "username";
    public static final String PHOTO_URL = "photoUrl";


    private String uid;
    private String registrationToken;
    private String username;
    private String photoUrl;

    public User() {
    }

    public User(String uid, String registrationToken, String username, String photoUrl) {
        this.uid = uid;
        this.registrationToken = registrationToken;
        this.username = username;
        this.photoUrl = photoUrl;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUid() {
        return uid;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public String getUsername() {
        return username;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("uid", uid);
        result.put("registrationToken", registrationToken);
        result.put("photoUrl", photoUrl);

        return result;
    }
}
