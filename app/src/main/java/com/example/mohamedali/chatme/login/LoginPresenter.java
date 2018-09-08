package com.example.mohamedali.chatme.login;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.example.mohamedali.chatme.constanst.Concrete;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class LoginPresenter implements LoginMVP.Presenter{
    private FirebaseAuth mAuth;

    private StorageReference mUsersProfilePhotoStorageReference;
    private DatabaseReference mUsersDatabaseReference;
    private LoginMVP.View view;

    @Override
    public void setView(LoginMVP.View view) {
        this.view = view;
    }


    public LoginPresenter() {
        mAuth = FirebaseAuth.getInstance();
        mUsersProfilePhotoStorageReference = FirebaseStorage.getInstance().getReference().child(Concrete.PROFILE_PHOTOS);
        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Concrete.USERS);

    }

    @Override
    public void addNewUser() {
        if (!validateForm()) return;

        Map<String, Object> updates = new HashMap();

        updates.put("/" + Concrete.getUid() + "/" + User.UID + "/", Concrete.getUid());
        updates.put("/" + Concrete.getUid() + "/" + User.REGISTRATION_TOKEN + "/", Concrete.getRegistrationToken());
        updates.put("/" + Concrete.getUid() + "/" + User.USERNAME + "/", view.getUserName());

        mUsersDatabaseReference.updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                view.startMainActivity();
            }
        });

    }

    @Override
    public void pickImageFromGallery() {
        view.startGalleryActivity();
    }


    @Override
    public void storeProfileImage(Uri imageUri) {
        view.showProgressDialog();
        if (mAuth != null) {
            final StorageReference photoRef = mUsersProfilePhotoStorageReference.child(Concrete.getUid());

            UploadTask uploadTask = photoRef.putFile(imageUri);

            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return photoRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        view.setImageProfile(downloadUri);
                        mUsersDatabaseReference.child(Concrete.getUid()).child(User.PHOTO_URL).setValue(downloadUri.toString())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        view.hideProgressDialog();
                                    }
                                });
                    }
                }
            });

        }

    }

    @Override
    public void checkAuth() {
        if (mAuth.getCurrentUser() != null) {
            // already signed in
            view.startMainActivity();

        } else {
            // not signed in
            view.startSignInActivity();
        }
    }


    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(view.getUserName())) {
            view.setUserNameError();
            result = false;
        } else {
            view.setUserNameErrorNull();
        }

        return result;
    }
}
